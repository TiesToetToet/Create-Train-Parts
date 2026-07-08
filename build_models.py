import json
import shutil
from pathlib import Path


# Base folder paths
ROOT = Path(__file__).resolve().parent

# Base folder paths
step_models_folder = ROOT / "src" / "main" / "resources" / "assets" / \
                     "create_train_parts" / "models" / "block" / "step_models"

step_models_train_folder = ROOT / "src" / "main" / "resources" / "assets" / \
                           "create_train_parts" / "models" / "block" / "step_models_train"

slide_models_input_folder = ROOT / "src" / "main" / "resources" / "assets" / \
                            "create_train_parts" / "models" / "block" / "slide_models"

slide_models_train_input_folder = ROOT / "src" / "main" / "resources" / "assets" / \
                                  "create_train_parts" / "models" / "block" / "slide_models_train"

base_folder_path = ROOT / "src" / "generated" / "resources" / "assets" / \
                   "create_train_parts" / "models" / "block"

# Block types and their respective textures
block_types = {
    "train_step_andesite": "create:block/andesite_casing",
    "train_step_brass": "create:block/brass_casing",
    "train_step_copper": "create:block/copper_casing",
    "train_step_train": "create:block/railway_casing"  # No texture replacement for train_step_train
}

# Directions to process
directions = ["north", "east", "south", "west", "up", "down"]

# Input for generating or removing models
actions = ["r", "g"]

# if user

# Copy files from step_models to each block type folder
for user_input in actions:
    for block_type, texture_replacement in block_types.items():
        generated_count = 0
        removed_count = 0
        target_folder = base_folder_path / block_type
        target_folder.mkdir(parents=True, exist_ok=True)

        if user_input == "g":

            # Copy all files from step_models to the target folder
            source_folder = step_models_train_folder if block_type == "train_step_train" else step_models_folder

            for source_file in source_folder.iterdir():
                target_file = target_folder / source_file.name

                if source_file.is_file():
                    shutil.copy(source_file, target_file)

                    # Modify the textures and particles in the copied file
                    if source_file.suffix == '.json':
                        with open(target_file, 'r') as file:
                            model_data = json.load(file)

                        # Update the textures and particles
                        if "textures" in model_data:
                            model_data["textures"] = {
                                key: (texture_replacement if value == "create:block/andesite_casing" else value)
                                for key, value in model_data["textures"].items()
                            }

                        # Save the updated file
                        with open(target_file, 'w') as file:
                            json.dump(model_data, file, indent=4)

                        generated_count += 1

        if user_input == "r":
            # Remove all files once
            for file_path in target_folder.iterdir():
                if file_path.is_file():
                    file_path.unlink()
                    removed_count += 1
        # Process each JSON file in the target folder
        else:
            for filename in target_folder.iterdir():
                if filename.suffix == '.json' and len(filename.stem.split("_")) > 1:
                    file_path = filename

                        # Read the original model file
                    with open(file_path, 'r') as file:
                        model_data = json.load(file)

                    # Update the textures for the current block type
                    if "textures" in model_data:
                        model_data["textures"] = {
                            key: (texture_replacement if value == "create:block/andesite_casing" else value)
                            for key, value in model_data["textures"].items()
                        }

                    # Process each direction
                    for direction in directions:
                        # Create a copy of the model data
                        new_model = {
                            "credit": model_data.get("credit", ""),
                            "textures": model_data.get("textures", {}),
                            "elements": [],
                            "groups": model_data.get("groups", [])
                        }

                        # Filter elements to include only the specified direction
                        for element in model_data.get("elements", []):
                            new_faces = {}
                            if "faces" in element and direction in element["faces"]:
                                # Handle exceptions for slide_east/slide_west
                                if filename.stem.startswith("slide"):
                                    if block_type == "train_step_train":
                                        if direction == "north":
                                            # Include both north and up sides in the same file
                                            if "north" in element["faces"]:
                                                new_faces["north"] = element["faces"]["north"]
                                            if "up" in element["faces"]:
                                                new_faces["up"] = element["faces"]["up"]
                                            if "south" in element["faces"]:
                                                new_faces["south"] = element["faces"]["south"]
                                            # if "down" in element["faces"]:
                                            #     new_faces["down"] = element["faces"]["down"]
                                            if "east" in element["faces"]:
                                                new_faces["east"] = element["faces"]["east"]
                                            if "west" in element["faces"]:
                                                new_faces["west"] = element["faces"]["west"]
                                        elif direction == "up":
                                            # Make the up side empty
                                            continue
                                        elif direction == "south":
                                            # Make the south side empty
                                            continue
                                        # elif direction == "down":
                                        #     # Make the down side empty
                                        #     continue
                                        elif direction == "east":
                                            continue
                                        elif direction == "west":
                                            continue
                                        else:
                                            # Default behavior for other directions
                                            new_faces[direction] = element["faces"][direction]

                                    else:
                                        if direction == "north":
                                            # Include both north and up sides in the same file
                                            if "north" in element["faces"]:
                                                new_faces["north"] = element["faces"]["north"]
                                            if "up" in element["faces"]:
                                                new_faces["up"] = element["faces"]["up"]
                                            if "south" in element["faces"]:
                                                new_faces["south"] = element["faces"]["south"]
                                            # if "down" in element["faces"]:
                                            #     new_faces["down"] = element["faces"]["down"]
                                        elif direction == "up":
                                            # Make the up side empty
                                            continue
                                        elif direction == "south":
                                            # Make the south side empty
                                            continue
                                        # elif direction == "down":
                                        #     # Make the down side empty
                                        #     continue
                                        else:
                                            # Default behavior for other directions
                                            new_faces[direction] = element["faces"][direction]
                                # Handle exceptions for pivot_east/pivot_west
                                elif filename.stem.startswith("pivot"):
                                    if direction == "up":
                                        # Include both up and south sides in the same file
                                        if "up" in element["faces"]:
                                            new_faces["up"] = element["faces"]["up"]
                                        if "south" in element["faces"]:
                                            new_faces["south"] = element["faces"]["south"]
                                    elif direction == "south":
                                        # Make the south side empty
                                        continue
                                    else:
                                        # Default behavior for other directions
                                        new_faces[direction] = element["faces"][direction]

                                elif filename.stem.startswith("flap"):
                                    if direction == "north":
                                        # Include both north and up sides in the same file
                                        if "north" in element["faces"]:
                                            new_faces["north"] = element["faces"]["north"]
                                        if "up" in element["faces"]:
                                            new_faces["up"] = element["faces"]["up"]
                                    elif direction == "up":
                                        # Make the up side empty
                                        continue
                                    else:
                                        # Default behavior for other directions
                                        new_faces[direction] = element["faces"][direction]

                                elif filename.stem.startswith("steps"):
                                    # Check if the element belongs to the Bottom2 group
                                    bottom2_elements = []
                                    if "groups" in model_data:
                                        for group in model_data["groups"]:
                                            if isinstance(group, dict) and group.get("name") == "stairs":
                                                for child in group.get("children", []):
                                                    if isinstance(child, dict) and child.get("name") == "Bottom":
                                                        for child2 in child.get("children", []):
                                                            if isinstance(child2, dict) and child2.get("name") == "Bottom2":
                                                                # Collect elements in the Bottom2 group
                                                                for child_index in child2.get("children", []):
                                                                    if isinstance(child_index, int) and child_index < len(model_data["elements"]):
                                                                        bottom2_elements.append(model_data["elements"][child_index])

                                    # Process all elements, including Bottom2 and Flat
                                    for element in model_data.get("elements", []):
                                        new_faces = {}
                                        if element in bottom2_elements:
                                            # Special handling for Bottom2 elements
                                            if "faces" in element:
                                                if direction == "north":
                                                    # Include both north and up sides in the same file
                                                    if "north" in element["faces"]:
                                                        new_faces["north"] = element["faces"]["north"]
                                                    if "up" in element["faces"]:
                                                        new_faces["up"] = element["faces"]["up"]
                                                elif direction == "up":
                                                    # Make the up side empty
                                                    continue
                                                else:
                                                    # Default behavior for other directions
                                                    if direction in element["faces"]:
                                                        new_faces[direction] = element["faces"][direction]
                                        elif element.get("name") == "Flat":
                                            # Special handling for Flat elements
                                            if "faces" in element:
                                                if direction == "down":
                                                    if "north" in element["faces"]:
                                                        new_faces["north"] = element["faces"]["north"]
                                                    if "down" in element["faces"]:
                                                        new_faces["down"] = element["faces"]["down"]
                                                elif direction == "north":
                                                    continue
                                                else:
                                                    # Default behavior for other directions
                                                    if direction in element["faces"]:
                                                        new_faces[direction] = element["faces"][direction]


                                        else:
                                            # Default behavior for other elements
                                            if "faces" in element and direction in element["faces"]:
                                                new_faces[direction] = element["faces"][direction]

                                        if new_faces:
                                            new_element = element.copy()
                                            new_element["faces"] = new_faces
                                            new_model["elements"].append(new_element)


                                # Default behavior for other cases
                                else:
                                    new_faces[direction] = element["faces"][direction]

                            if new_faces:
                                new_element = element.copy()
                                new_element["faces"] = new_faces
                                new_model["elements"].append(new_element)

                        # If no elements are left, create an empty model
                        if not new_model["elements"]:
                            new_model["elements"] = []

                        # Save the new model to a file
                        output_file = target_folder / f"{filename.stem}_{direction}.json"
                        with open(output_file, 'w') as out_file:
                            json.dump(new_model, out_file, indent=4)

                        generated_count += 1

        friendly_name = block_type.replace("train_step_", "")

        if user_input == "g":
            print(f"Generated {friendly_name} steps: {generated_count} files")
        else:
            print(f"Removed {friendly_name} steps: {removed_count} files")
    #

    for block_type, texture_replacement in block_types.items():
        generated_count = 0
        removed_count = 0
        # if block_type == "train_step_train":
        #     continue  # Skip train casing for now

        output_folder_path = (
                base_folder_path /
                f'train_slide_{block_type.split("_")[-1]}'
        )

        output_folder_path.mkdir(parents=True, exist_ok=True)

        source_folder = slide_models_train_input_folder if block_type == "train_step_train" else slide_models_input_folder

        if source_folder.exists():
            for filename in source_folder.iterdir():
                if filename.suffix == '.json':
                    input_file = filename
                    if user_input == "g":
                        if len(filename.stem.split("_")) > 1:
                            with open(input_file, 'r') as file:
                                model_data = json.load(file)

                            if "textures" in model_data:
                                model_data["textures"] = {
                                    key: (texture_replacement if value == "create:block/andesite_casing" else value)
                                    for key, value in model_data["textures"].items()
                                }

                            for direction in directions:
                                new_model = {
                                    "credit": model_data.get("credit", ""),
                                    "textures": model_data.get("textures", {}),
                                    "elements": [],
                                    "groups": model_data.get("groups", [])
                                }

                                for element in model_data.get("elements", []):
                                    new_faces = {}
                                    if "faces" in element:
                                        #for train variant top needs own up
                                        # if block_type == "train_step_train":

                                        # else:
                                        if filename.stem.startswith("bottom") or filename.stem.startswith("centre") or filename.stem.startswith("top"):
                                            if direction == "north":
                                                if "north" in element["faces"]:
                                                    new_faces["north"] = element["faces"]["north"]
                                                if "south" in element["faces"]:
                                                    new_faces["south"] = element["faces"]["south"]
                                                if "up" in element["faces"]:
                                                    new_faces["up"] = element["faces"]["up"]
                                            elif direction == "south" or direction == "up":
                                                continue
                                            else:
                                                if direction in element["faces"]:
                                                    new_faces[direction] = element["faces"][direction]
                                        else:
                                            if direction in element["faces"]:
                                                new_faces[direction] = element["faces"][direction]

                                    if new_faces:
                                        new_element = element.copy()
                                        new_element["faces"] = new_faces
                                        new_model["elements"].append(new_element)

                                if not new_model["elements"]:
                                    new_model["elements"] = []

                                output_file = output_folder_path / f"{filename.stem}_{direction}.json"
                                with open(output_file, 'w') as out_file:
                                    json.dump(new_model, out_file, indent=4)
                                generated_count += 1
                        else:
                            # just copy the exact files with names to the folder
                            output_file = output_folder_path / filename.name
                            with open(input_file, 'r') as file:
                                model_data = json.load(file)
                            if "textures" in model_data:
                                model_data["textures"] = {
                                    key: (texture_replacement if value == "create:block/andesite_casing" else value)
                                    for key, value in model_data["textures"].items()
                                }

                            with open(output_file, 'w') as out_file:
                                json.dump(model_data, out_file, indent=4)
                            generated_count += 1

                    elif user_input == "r":
                        # Remove the generated models
                        for direction in directions:
                            output_file = output_folder_path / f"{filename.stem}_{direction}.json"
                            if output_file.exists():
                                output_file.unlink()
                                removed_count += 1
                        # Also remove the direct copy if it exists
                        output_file = output_folder_path / filename.name
                        if output_file.exists():
                            output_file.unlink()
                            removed_count += 1

        friendly_name = block_type.replace("train_step_", "")

        if user_input == "g":
            print(f"Generated {friendly_name} slides: {generated_count} files")
        else:
            print(f"Removed {friendly_name} slides: {removed_count} files")