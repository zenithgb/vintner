#!/usr/bin/env python3
"""Build an editable, textured Blockbench review assembly from runtime models."""

import argparse
import base64
import copy
import json
import uuid
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/vintner/models/block"
OUTPUT = ROOT / "blockbench/workstations/connected_estate_workstations.bbmodel"


def stable_id(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "vintner:workstation-preview/" + name))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("minecraft_jar", type=Path, help="Local Minecraft client JAR containing vanilla textures")
    args = parser.parse_args()
    elements, groups, textures = [], [], []
    texture_ids = {}
    with zipfile.ZipFile(args.minecraft_jar) as jar:
        for index, family in enumerate(("vintage_archive", "estate_management_desk", "surveyors_map_table")):
            children = []
            models = [family]
            if index > 0:
                models.append(family + "_connection_left")
            if index < 2:
                models.append(family + "_connection_right")
            for model_name in models:
                model = json.loads((MODELS / (model_name + ".json")).read_text())
                for position, source in enumerate(model["elements"]):
                    element = copy.deepcopy(source)
                    element_id = stable_id(f"{model_name}/{position}")
                    element.update({"uuid": element_id, "type": "cube", "box_uv": False,
                                    "autouv": 0, "name": source.get("name", f"{family}_{position}")})
                    x0, y0, z0 = source["from"]
                    x1, y1, z1 = source["to"]
                    uv = {
                        "north": [16-x1, 16-y1, 16-x0, 16-y0],
                        "south": [x0, 16-y1, x1, 16-y0],
                        "west": [z0, 16-y1, z1, 16-y0],
                        "east": [16-z1, 16-y1, 16-z0, 16-y0],
                        "up": [x0, z0, x1, z1],
                        "down": [x0, 16-z1, x1, 16-z0],
                    }
                    for side, face in element["faces"].items():
                        texture = model["textures"][face["texture"].removeprefix("#")]
                        if texture not in texture_ids:
                            texture_id = len(textures)
                            texture_ids[texture] = texture_id
                            namespace, resource = texture.split(":")
                            png = jar.read(f"assets/{namespace}/textures/{resource}.png")
                            textures.append({
                                "uuid": stable_id(texture), "id": str(texture_id),
                                "name": resource.rsplit("/", 1)[-1] + ".png",
                                "namespace": namespace, "folder": "block", "mode": "bitmap",
                                "source": "data:image/png;base64," + base64.b64encode(png).decode(),
                            })
                        face.update({"texture": texture_ids[texture], "uv": uv[side]})
                    offset = (index - 1) * 16
                    element["from"][0] += offset
                    element["to"][0] += offset
                    rotation = element.pop("rotation", None)
                    if rotation:
                        element["origin"] = rotation["origin"]
                        element["origin"][0] += offset
                        element["rotation"] = [rotation["angle"] if axis == rotation["axis"] else 0 for axis in "xyz"]
                    elements.append(element)
                    children.append(element_id)
            groups.append({"name": family, "uuid": stable_id(family), "origin": [0, 0, 0], "children": children})
    project = {
        "meta": {"format_version": "5.0", "model_format": "free", "box_uv": False},
        "name": "Connected estate workstations", "resolution": {"width": 16, "height": 16},
        "elements": elements, "outliner": groups, "textures": textures,
    }
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(project, indent=2) + "\n")
    print(f"Generated {OUTPUT.relative_to(ROOT)} ({len(elements)} cuboids)")


if __name__ == "__main__":
    main()
