"""
Generates the train step / train slide block models.

Per block this writes:
  - the static models (north/east/south/west/steps/slide) used by blockstates and items
  - one merged model per animated part in `partial/<part>.json`

Each merged model contains every facing/connected-state/CT-face variant of that part.
Every face carries a `tintindex` encoding which variant it belongs to:

    tintindex = FACING * 100 + CONNECTED_STATE * 10 + CT_FACE

FACING and CT_FACE use Direction.get3DDataValue(), CONNECTED_STATE the ConnectedState ordinal.
CTPartialModels (Java) splits the baked quads back up by this code at runtime.
"""
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "src" / "main" / "resources" / "assets" / "create_train_parts" / "models" / "block"
OUTPUT = ROOT / "src" / "generated" / "resources" / "assets" / "create_train_parts" / "models" / "block"

BASE_TEXTURE = "create:block/andesite_casing"

MATERIALS = {
    "andesite": "create:block/andesite_casing",
    "brass": "create:block/brass_casing",
    "copper": "create:block/copper_casing",
    "train": "create:block/railway_casing",
}

# Must match Direction.get3DDataValue()
DIRECTIONS = {"down": 0, "up": 1, "north": 2, "south": 3, "west": 4, "east": 5}
# Must match TrainStepBlock/TrainSlideBlock.ConnectedState ordinals
CONNECTED_STATES = {"none": 0, "right": 1, "left": 2, "both": 3}


def variant_code(facing, state, ct_face):
    return DIRECTIONS[facing] * 100 + CONNECTED_STATES[state] * 10 + DIRECTIONS[ct_face]


def pick(faces, *names):
    return {name: faces[name] for name in names if name in faces}


def step_bucket(part, is_train, element, bucket, is_bottom2):
    """Faces of `element` that use the connected texture of `bucket` (model-local face)."""
    faces = element.get("faces", {})

    if part == "steps":
        if is_bottom2:
            if bucket == "north":
                return pick(faces, "north", "up")
            if bucket == "up":
                return {}
        elif element.get("name") == "Flat":
            if bucket == "down":
                return pick(faces, "north", "down")
            if bucket == "north":
                return {}
        return pick(faces, bucket)

    if bucket not in faces:
        return {}

    if part == "slide":
        if bucket == "north":
            if is_train:
                return pick(faces, "north", "up", "south", "east", "west")
            return pick(faces, "north", "up", "south")
        if bucket in ("up", "south") or (is_train and bucket in ("east", "west")):
            return {}
    elif part == "pivot":
        if bucket == "up":
            return pick(faces, "up", "south")
        if bucket == "south":
            return {}
    elif part == "flap":
        if bucket == "north":
            return pick(faces, "north", "up")
        if bucket == "up":
            return {}

    return pick(faces, bucket)


def slide_bucket(part, is_train, element, bucket, is_bottom2):
    faces = element.get("faces", {})
    if part in ("bottom", "centre", "top"):
        if bucket == "north":
            return pick(faces, "north", "south", "up")
        if bucket in ("south", "up"):
            return {}
    return pick(faces, bucket)


def bottom2_indices(model):
    indices = set()
    for group in model.get("groups", []):
        if not (isinstance(group, dict) and group.get("name") == "stairs"):
            continue
        for child in group.get("children", []):
            if not (isinstance(child, dict) and child.get("name") == "Bottom"):
                continue
            for child2 in child.get("children", []):
                if isinstance(child2, dict) and child2.get("name") == "Bottom2":
                    indices.update(i for i in child2.get("children", []) if isinstance(i, int))
    return indices


def retexture(textures, material_texture):
    return {key: (material_texture if value == BASE_TEXTURE else value) for key, value in textures.items()}


def write_json(path, data):
    with open(path, "w") as file:
        json.dump(data, file, separators=(",", ":"))


def clean_element(element, faces):
    cleaned = {"from": element["from"], "to": element["to"], "faces": faces}
    if "rotation" in element:
        cleaned["rotation"] = element["rotation"]
    if element.get("shade") is False:
        cleaned["shade"] = False
    return cleaned


class MergedModel:
    def __init__(self, particle):
        self.particle = particle
        self.texture_keys = {}
        self.elements = []

    def texture_key(self, texture_path):
        if texture_path not in self.texture_keys:
            self.texture_keys[texture_path] = str(len(self.texture_keys))
        return "#" + self.texture_keys[texture_path]

    def add(self, model, textures, facing, state, bucket_fn, part, is_train):
        bottom2 = bottom2_indices(model) if part == "steps" else set()
        for index, element in enumerate(model.get("elements", [])):
            tagged = []
            for bucket in DIRECTIONS:
                faces = bucket_fn(part, is_train, element, bucket, index in bottom2)
                for face_name, face in faces.items():
                    face = dict(face)
                    texture_path = textures.get(face.get("texture", "").lstrip("#"))
                    # Unresolved refs (e.g. Blockbench "#missing") are kept so they still resolve to missingno.
                    if texture_path is not None:
                        face["texture"] = self.texture_key(texture_path)
                    face["tintindex"] = variant_code(facing, state, bucket)
                    tagged.append((face_name, face))

            # A face direction can only occur once per element, so overflow goes into a copy.
            copies = []
            for face_name, face in tagged:
                target = next((c for c in copies if face_name not in c), None)
                if target is None:
                    target = {}
                    copies.append(target)
                target[face_name] = face
            self.elements.extend(clean_element(element, faces) for faces in copies)

    def to_json(self):
        textures = {key: path for path, key in self.texture_keys.items()}
        textures["particle"] = self.particle
        return {"render_type": "minecraft:cutout_mipped", "textures": textures, "elements": self.elements}


def build(block_name, source_folder, material_texture, bucket_fn, is_train):
    target = OUTPUT / block_name
    if target.exists():
        shutil.rmtree(target)
    (target / "partial").mkdir(parents=True)

    merged = {}
    count = 0
    for source_file in sorted(source_folder.glob("*.json")):
        with open(source_file) as file:
            model = json.load(file)
        textures = retexture(model.get("textures", {}), material_texture)

        name_parts = source_file.stem.split("_")
        if len(name_parts) == 1:
            model["textures"] = textures
            model.pop("credit", None)
            model.pop("groups", None)
            write_json(target / source_file.name, model)
            count += 1
            continue

        part, facing, state = name_parts
        if part not in merged:
            merged[part] = MergedModel(material_texture)
        merged[part].add(model, textures, facing, state, bucket_fn, part, is_train)

    for part, model in merged.items():
        write_json(target / "partial" / f"{part}.json", model.to_json())
        count += 1

    print(f"Generated {block_name}: {count} files")


for material, texture in MATERIALS.items():
    is_train = material == "train"
    build(f"train_step_{material}", SOURCE / ("step_models_train" if is_train else "step_models"),
          texture, step_bucket, is_train)
    build(f"train_slide_{material}", SOURCE / ("slide_models_train" if is_train else "slide_models"),
          texture, slide_bucket, is_train)
