#!/usr/bin/env python3
import json
import math
import struct
import sys
from pathlib import Path

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/generated/battleV2ThreeDAssets")

CUBE_VERTS = [
    (-1, -1, -1), (1, -1, -1), (1, 1, -1), (-1, 1, -1),
    (-1, -1, 1), (1, -1, 1), (1, 1, 1), (-1, 1, 1),
]

CUBE_INDICES = [
    0, 1, 2, 0, 2, 3, 4, 6, 5, 4, 7, 6,
    0, 4, 5, 0, 5, 1, 3, 2, 6, 3, 6, 7,
    1, 5, 6, 1, 6, 2, 0, 3, 7, 0, 7, 4,
]


def qx(degrees):
    r = math.radians(degrees) / 2
    return (math.sin(r), 0.0, 0.0, math.cos(r))


def qy(degrees):
    r = math.radians(degrees) / 2
    return (0.0, math.sin(r), 0.0, math.cos(r))


def qz(degrees):
    r = math.radians(degrees) / 2
    return (0.0, 0.0, math.sin(r), math.cos(r))


def build_scene(path):
    blob = bytearray()
    views = []
    accessors = []
    nodes = []
    meshes = []
    materials = []
    animations = []

    def align4():
        while len(blob) % 4:
            blob.append(0)

    def add_data(data, target=None):
        align4()
        offset = len(blob)
        blob.extend(data)
        item = {
            "buffer": 0,
            "byteOffset": offset,
            "byteLength": len(data),
        }
        if target:
            item["target"] = target
        views.append(item)
        return len(views) - 1

    def add_accessor(values, kind, component=5126, target=None, bounds=False):
        if component == 5123:
            raw = struct.pack("<" + "H" * len(values), *values)
            count = len(values)
            components = 1
        else:
            if values and isinstance(values[0], (tuple, list)):
                components = len(values[0])
                flat = [item for row in values for item in row]
                count = len(values)
            else:
                components = 1
                flat = list(values)
                count = len(flat)
            raw = struct.pack("<" + "f" * len(flat), *flat)

        accessor = {
            "bufferView": add_data(raw, target),
            "componentType": component,
            "count": count,
            "type": kind,
        }

        if bounds:
            if components == 1:
                accessor["min"] = [min(values)]
                accessor["max"] = [max(values)]
            else:
                accessor["min"] = [
                    min(value[i] for value in values)
                    for i in range(components)
                ]
                accessor["max"] = [
                    max(value[i] for value in values)
                    for i in range(components)
                ]

        accessors.append(accessor)
        return len(accessors) - 1

    position_accessor = add_accessor(
        CUBE_VERTS, "VEC3", target=34962, bounds=True
    )
    index_accessor = add_accessor(
        CUBE_INDICES,
        "SCALAR",
        component=5123,
        target=34963,
        bounds=True,
    )

    def add_material_set(prefix, goblin):
        colors = (
            [
                (0.22, 0.48, 0.18, 1.0),
                (0.44, 0.72, 0.30, 1.0),
                (0.10, 0.07, 0.06, 1.0),
                (0.55, 0.50, 0.45, 1.0),
                (0.62, 0.20, 0.10, 1.0),
            ]
            if goblin
            else [
                (0.10, 0.24, 0.58, 1.0),
                (0.92, 0.69, 0.51, 1.0),
                (0.05, 0.06, 0.10, 1.0),
                (0.74, 0.80, 0.90, 1.0),
                (0.92, 0.66, 0.16, 1.0),
            ]
        )
        base = len(materials)
        for name, color in zip(
            ["armor", "skin", "dark", "weapon", "accent"],
            colors,
        ):
            materials.append(
                {
                    "name": prefix + "_" + name,
                    "pbrMetallicRoughness": {
                        "baseColorFactor": list(color),
                        "metallicFactor": 0.0,
                        "roughnessFactor": 0.9,
                    },
                    "extensions": {"KHR_materials_unlit": {}},
                }
            )
        return base

    def add_mesh_set(prefix, material_base):
        base = len(meshes)
        for name, offset in zip(
            ["armor", "skin", "dark", "weapon", "accent"],
            range(5),
        ):
            meshes.append(
                {
                    "name": prefix + "_" + name,
                    "primitives": [
                        {
                            "attributes": {"POSITION": position_accessor},
                            "indices": index_accessor,
                            "material": material_base + offset,
                        }
                    ],
                }
            )
        return base

    def add_animation(name, times, channels):
        time_accessor = add_accessor(times, "SCALAR", bounds=True)
        samplers = []
        channel_items = []

        for node, path_name, values in channels:
            output = add_accessor(
                values,
                "VEC4" if path_name == "rotation" else "VEC3",
            )
            samplers.append(
                {
                    "input": time_accessor,
                    "output": output,
                    "interpolation": "LINEAR",
                }
            )
            channel_items.append(
                {
                    "sampler": len(samplers) - 1,
                    "target": {"node": node, "path": path_name},
                }
            )

        animations.append(
            {
                "name": name,
                "samplers": samplers,
                "channels": channel_items,
            }
        )

    def add_actor(prefix, goblin, x, face_left):
        material_base = add_material_set(prefix, goblin)
        mesh_base = add_mesh_set(prefix, material_base)
        base = len(nodes)

        wrapper = base
        motion = base + 1
        torso = base + 2
        head = base + 3
        helm = base + 4
        left_pivot = base + 5
        left_arm = base + 6
        shield = base + 7
        right_pivot = base + 8
        right_arm = base + 9
        weapon = base + 10
        left_leg_pivot = base + 11
        left_leg = base + 12
        right_leg_pivot = base + 13
        right_leg = base + 14
        belt = base + 15

        torso_scale = [0.52, 0.58, 0.28] if goblin else [0.48, 0.68, 0.24]
        head_y = 2.0 if goblin else 2.15
        arm_y = 1.65 if goblin else 1.75

        nodes.extend(
            [
                {
                    "name": prefix + "_ArenaRoot",
                    "translation": [x, 0.0, 0.0],
                    "rotation": list(qy(180 if face_left else 0)),
                    "children": [motion],
                },
                {
                    "name": prefix + "_MotionRoot",
                    "children": [
                        torso,
                        head,
                        helm,
                        left_pivot,
                        right_pivot,
                        left_leg_pivot,
                        right_leg_pivot,
                        belt,
                    ],
                },
                {
                    "name": prefix + "_Torso",
                    "mesh": mesh_base,
                    "translation": [0, 1.25, 0],
                    "scale": torso_scale,
                },
                {
                    "name": prefix + "_Head",
                    "mesh": mesh_base + 1,
                    "translation": [0, head_y, 0],
                    "scale": [0.34, 0.28, 0.32] if goblin else [0.29, 0.29, 0.29],
                },
                {
                    "name": prefix + "_Helm",
                    "mesh": mesh_base + 2,
                    "translation": [0, head_y + 0.24, 0],
                    "scale": [0.31, 0.10, 0.31],
                },
                {
                    "name": prefix + "_LeftArmPivot",
                    "translation": [-0.68 if goblin else -0.62, arm_y, 0],
                    "children": [left_arm, shield],
                },
                {
                    "name": prefix + "_LeftArm",
                    "mesh": mesh_base,
                    "translation": [0, -0.49, 0],
                    "scale": [0.13, 0.53, 0.13],
                },
                {
                    "name": prefix + "_Shield",
                    "mesh": mesh_base + 4,
                    "translation": [-0.12, -0.55, 0.18],
                    "scale": [0.28 if goblin else 0.32, 0.32 if goblin else 0.38, 0.07],
                },
                {
                    "name": prefix + "_RightArmPivot",
                    "translation": [0.68 if goblin else 0.62, arm_y, 0],
                    "children": [right_arm, weapon],
                },
                {
                    "name": prefix + "_RightArm",
                    "mesh": mesh_base,
                    "translation": [0, -0.49, 0],
                    "scale": [0.13, 0.53, 0.13],
                },
                {
                    "name": prefix + "_Weapon",
                    "mesh": mesh_base + 3,
                    "translation": [0, -1.18, 0],
                    "scale": [0.07 if goblin else 0.055, 0.58 if goblin else 0.78, 0.07 if goblin else 0.055],
                },
                {
                    "name": prefix + "_LeftLegPivot",
                    "translation": [-0.24, 0.67, 0],
                    "children": [left_leg],
                },
                {
                    "name": prefix + "_LeftLeg",
                    "mesh": mesh_base + 2,
                    "translation": [0, -0.58, 0],
                    "scale": [0.15, 0.66, 0.15],
                },
                {
                    "name": prefix + "_RightLegPivot",
                    "translation": [0.24, 0.67, 0],
                    "children": [right_leg],
                },
                {
                    "name": prefix + "_RightLeg",
                    "mesh": mesh_base + 2,
                    "translation": [0, -0.58, 0],
                    "scale": [0.15, 0.66, 0.15],
                },
                {
                    "name": prefix + "_Belt",
                    "mesh": mesh_base + 4,
                    "translation": [0, 0.93, 0],
                    "scale": [0.50, 0.08, 0.27],
                },
            ]
        )

        add_animation(
            prefix + "_idle",
            [0, 0.5, 1.0],
            [
                (motion, "translation", [(0, 0, 0), (0, 0.035, 0), (0, 0, 0)]),
                (left_pivot, "rotation", [qz(-4), qz(1), qz(-4)]),
                (right_pivot, "rotation", [qz(4), qz(-1), qz(4)]),
            ],
        )

        add_animation(
            prefix + "_attack",
            [0, 0.18, 0.34, 0.55, 0.78],
            [
                (motion, "translation", [(0, 0, 0), (0, 0, 0), (0.42, 0, 0), (0.16, 0, 0), (0, 0, 0)]),
                (right_pivot, "rotation", [qz(5), qz(-70), qz(55), qz(18), qz(5)]),
                (left_pivot, "rotation", [qz(-5), qz(12), qz(-35), qz(-15), qz(-5)]),
            ],
        )

        add_animation(
            prefix + "_hit",
            [0, 0.12, 0.28, 0.55],
            [
                (motion, "translation", [(0, 0, 0), (-0.18, 0, 0), (-0.06, 0, 0), (0, 0, 0)]),
                (motion, "rotation", [qz(0), qz(-12), qz(-4), qz(0)]),
            ],
        )

        add_animation(
            prefix + "_death",
            [0, 0.35, 0.75, 1.2],
            [
                (motion, "rotation", [qx(0), qx(25), qx(72), qx(90)]),
                (motion, "translation", [(0, 0, 0), (0, -0.05, 0), (0, -0.45, 0), (0, -0.72, 0)]),
                (right_pivot, "rotation", [qz(4), qz(35), qz(70), qz(82)]),
            ],
        )

        add_animation(
            prefix + "_victory",
            [0, 0.35, 0.75, 1.15],
            [
                (right_pivot, "rotation", [qz(4), qz(-35), qz(-60), qz(4)]),
                (motion, "translation", [(0, 0, 0), (0, 0.10, 0), (0, 0.04, 0), (0, 0, 0)]),
            ],
        )

        return wrapper

    # Stage actors farther apart so the battlefield reads as a duel,
    # then lower their shared roots so their feet sit on the arena floor.
    player_root = add_actor("player", False, -1.62, False)
    enemy_root = add_actor("enemy", True, 1.62, True)
    nodes[player_root]["translation"][1] = -0.18
    nodes[enemy_root]["translation"][1] = -0.18

    gltf = {
        "asset": {
            "version": "2.0",
            "generator": "Astralforge Depthbound Battle V2 combined arena generator",
        },
        "extensionsUsed": ["KHR_materials_unlit"],
        "scene": 0,
        "scenes": [{"name": "BattleV2Arena", "nodes": [player_root, enemy_root]}],
        "nodes": nodes,
        "meshes": meshes,
        "materials": materials,
        "buffers": [{"byteLength": len(blob)}],
        "bufferViews": views,
        "accessors": accessors,
        "animations": animations,
    }

    json_chunk = json.dumps(gltf, separators=(",", ":")).encode("utf-8")
    while len(json_chunk) % 4:
        json_chunk += b" "
    while len(blob) % 4:
        blob.append(0)

    total_length = 12 + 8 + len(json_chunk) + 8 + len(blob)
    glb = bytearray(struct.pack("<4sII", b"glTF", 2, total_length))
    glb += struct.pack("<I4s", len(json_chunk), b"JSON")
    glb += json_chunk
    glb += struct.pack("<I4s", len(blob), b"BIN\x00")
    glb += blob

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(glb)


def main():
    target = OUT / "models" / "battle_v2_prototype_arena.glb"
    build_scene(target)
    print("Generated", target)


if __name__ == "__main__":
    main()
