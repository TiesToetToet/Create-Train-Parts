import json
import shutil
from pathlib import Path


# Base folder paths
ROOT = Path(__file__).resolve().parent

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
    "train_step_brass":    "create:block/brass_casing",
    "train_step_copper":   "create:block/copper_casing",
    "train_step_train":    "create:block/railway_casing",
}

actions = ["r", "g"]

# ── Train Step ─────────────────────────────────────────────────────────────────
# Model key format: {blockType}/{facing}_{connected}
# We simply copy the source models (named {variant}_{facing}_{connected}.json)
# into the generated folder, replacing the andesite_casing texture where needed.
# The old per-direction splitting is gone — AllPartialModels no longer uses a face key.

for user_input in actions:
    for block_type, texture_replacement in block_types.items():
        generated_count = 0
        removed_count = 0
        target_folder = base_folder_path / block_type
        target_folder.mkdir(parents=True, exist_ok=True)

        source_folder = step_models_train_folder if block_type == "train_step_train" else step_models_folder

        if user_input == "r":
            for file_path in target_folder.iterdir():
                if file_path.is_file():
                    file_path.unlink()
                    removed_count += 1

        elif user_input == "g":
            for source_file in source_folder.iterdir():
                if not source_file.is_file() or source_file.suffix != ".json":
                    continue

                target_file = target_folder / source_file.name

                with open(source_file, "r") as f:
                    model_data = json.load(f)

                # Replace andesite_casing texture with the block type's texture
                if "textures" in model_data:
                    model_data["textures"] = {
                        key: (texture_replacement if value == "create:block/andesite_casing" else value)
                        for key, value in model_data["textures"].items()
                    }

                with open(target_file, "w") as f:
                    json.dump(model_data, f, indent=4)

                generated_count += 1

        friendly_name = block_type.replace("train_step_", "")
        if user_input == "g":
            print(f"Generated {friendly_name} steps: {generated_count} files")
        else:
            print(f"Removed {friendly_name} steps: {removed_count} files")


# ── Train Slide ────────────────────────────────────────────────────────────────
# Same simplification: copy source models directly, no per-direction splitting.
# Model key format: {blockType}/{facing}_{connected}

for user_input in actions:
    for block_type, texture_replacement in block_types.items():
        generated_count = 0
        removed_count = 0

        output_folder = base_folder_path / f'train_slide_{block_type.split("_")[-1]}'
        output_folder.mkdir(parents=True, exist_ok=True)

        source_folder = (
            slide_models_train_input_folder if block_type == "train_step_train"
            else slide_models_input_folder
        )

        if not source_folder.exists():
            continue

        if user_input == "r":
            for file_path in output_folder.iterdir():
                if file_path.is_file():
                    file_path.unlink()
                    removed_count += 1

        elif user_input == "g":
            for source_file in source_folder.iterdir():
                if not source_file.is_file() or source_file.suffix != ".json":
                    continue

                output_file = output_folder / source_file.name

                with open(source_file, "r") as f:
                    model_data = json.load(f)

                if "textures" in model_data:
                    model_data["textures"] = {
                        key: (texture_replacement if value == "create:block/andesite_casing" else value)
                        for key, value in model_data["textures"].items()
                    }

                with open(output_file, "w") as f:
                    json.dump(model_data, f, indent=4)

                generated_count += 1

        friendly_name = block_type.replace("train_step_", "")
        if user_input == "g":
            print(f"Generated {friendly_name} slides: {generated_count} files")
        else:
            print(f"Removed {friendly_name} slides: {removed_count} files")
