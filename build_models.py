import os
import json

# Folder containing the model files
input_folder_path = r'C:\Users\tiesh\Documents\Create-Train-Parts\src\main\resources\assets\create_train_parts\models\block\slide_models'
output_folder_path = r'C:\Users\tiesh\Documents\Create-Train-Parts\src\generated\resources\assets\create_train_parts\models\block\train_slide_andesite'

# Ensure the output folder exists
os.makedirs(output_folder_path, exist_ok=True)

# Directions to process
directions = ["north", "east", "south", "west", "up", "down"]

input_action = input("Generate (g) or Remove (r) models? ")

# Process each JSON file in the folder
if os.path.exists(input_folder_path):
    for filename in os.listdir(input_folder_path):
        if filename.endswith('.json'):
            if input_action == "g":
                if len(filename.split("_")) > 1:
                    with open(os.path.join(input_folder_path, filename), 'r') as file:
                        model_data = json.load(file)

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
                            if "faces" in element:
                                if filename.startswith("bottom") or filename.startswith("centre") or filename.startswith("top"):
                                    if direction == "north":
                                        # Combine bottom, south, and top under north
                                        if "north" in element["faces"]:
                                            new_faces["north"] = element["faces"]["north"]
                                        if "down" in element["faces"]:
                                            new_faces["down"] = element["faces"]["down"]
                                        if "south" in element["faces"]:
                                            new_faces["south"] = element["faces"]["south"]
                                        if "up" in element["faces"]:
                                            new_faces["up"] = element["faces"]["up"]
                                    elif direction == "down":
                                        continue
                                    elif direction == "south":
                                        continue
                                    elif direction == "up":
                                        continue
                                    else: 
                                        # For other directions, keep the faces as is
                                        if direction in element["faces"]:
                                            new_faces[direction] = element["faces"][direction]
                                else:
                                    # For other models, keep the faces as is
                                    if direction in element["faces"]:
                                        new_faces[direction] = element["faces"][direction]

                            if new_faces:
                                new_element = element.copy()
                                new_element["faces"] = new_faces
                                new_model["elements"].append(new_element)

                        # If no elements are left, create an empty model
                        if not new_model["elements"]:
                            new_model["elements"] = []

                        # Save the new model to a file
                        output_file = os.path.join(output_folder_path, f"{os.path.splitext(filename)[0]}_{direction}.json")
                        with open(output_file, 'w') as out_file:
                            json.dump(new_model, out_file, indent=4)
                        print(f"Saved {output_file}")
                else:
                    # just copy the exact files with names to the folder
                    output_file = os.path.join(output_folder_path, filename)
                    input_file = os.path.join(input_folder_path, filename)
                    with open(input_file, 'r') as file:
                        model_data = json.load(file)
                    with open(output_file, 'w') as out_file:
                        json.dump(model_data, out_file, indent=4)

            elif input_action == "r":
                # Remove the generated models
                for direction in directions:
                    output_file = os.path.join(output_folder_path, f"{os.path.splitext(filename)[0]}_{direction}.json")
                    if os.path.exists(output_file):
                        os.remove(output_file)
                        print(f"Removed {output_file}")