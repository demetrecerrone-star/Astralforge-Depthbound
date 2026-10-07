#!/usr/bin/env python3
import json
import math
import struct
import sys
from pathlib import Path

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/generated/threeDModelAssets")

CUBE_VERTS = [
    (-1, -1, -1), (1, -1, -1), (1, 1, -1), (-1, 1, -1),
    (-1, -1, 1), (1, -1, 1), (1, 1, 1), (-1, 1, 1),
]

CUBE_INDICES = [
    0, 1, 2, 0, 2, 3, 4, 6, 5, 4, 7, 6,
    0, 4, 5, 0, 5, 1, 3, 2, 6, 3, 6, 7,
    1, 5, 6, 1, 6, 2, 0, 3, 7, 0, 7, 4,
]


def qz(degrees):
    radians = math.radians(degrees) / 2
    return (0.0, 0.0, math.sin(radians), math.cos(radians))


def qx(degrees):
    radians = math.radians(degrees) / 2
    return (math.sin(radians), 0.0, 0.0, math.cos(radians))


def build_actor(path, goblin=False):
    blob = bytearray()
    views = []
    accessors = []

    def align4():
        while len(blob) % 4:
            blob.append(0)

    def add_data(data, target=None):
        align4()
        offset = len(blob)
        blob.extend(data)
        view = {
            "buffer": 0,
            "byteOffset": offset,
            "byteLength": len(data),
        }
        if target:
            view["target"] = target
        views.append(view)
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

        view_index = add_data(raw, target)
        accessor = {
            "bufferView": view_index,
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
        CUBE_VERTS,
        "VEC3",
        target=34962,
        bounds=True,
    )
    index_accessor = add_accessor(
        CUBE_INDICES,
        "SCALAR",
        component=5123,
        target=34963,
        bounds=True,
    )

    if goblin:
        colors = [
            (0.24, 0.43, 0.18, 1.0),
            (0.38, 0.66, 0.26, 1.0),
            (0.10, 0.08, 0.07, 1.0),
            (0.45, 0.41, 0.38, 1.0),
            (0.55, 0.18, 0.12, 1.0),
        ]
    else:
        colors = [
            (0.12, 0.22, 0.50, 1.0),
            (0.92, 0.70, 0.53, 1.0),
            (0.06, 0.07, 0.11, 1.0),
            (0.72, 0.78, 0.88, 1.0),
            (0.82, 0.58, 0.16, 1.0),
        ]

    materials = []
    for name, color in zip(
        ["armor", "skin", "dark", "weapon", "accent"],
        colors,
    ):
        materials.append(
            {
                "name": name,
                "pbrMetallicRoughness": {
                    "baseColorFactor": list(color),
                    "metallicFactor": 0.0,
                    "roughnessFactor": 0.9,
                },
                "extensions": {
                    "KHR_materials_unlit": {}
                },
            }
        )

    meshes = []
    for name, material_index in zip(
        ["armor", "skin", "dark", "weapon", "accent"],
        range(5),
    ):
        meshes.append(
            {
                "name": name,
                "primitives": [
                    {
                        "attributes": {
                            "POSITION": position_accessor
                        },
                        "indices": index_accessor,
                        "material": material_index,
                    }
                ],
            }
        )

    torso_scale = (
        [0.52, 0.58, 0.28]
        if goblin
        else [0.48, 0.68, 0.24]
    )
    head_y = 2.0 if goblin else 2.15
    arm_y = 1.65 if goblin else 1.75

    nodes = [
        {
            "name": "Root",
            "children": [1, 2, 3, 4, 7, 10, 12, 14],
        },
        {
            "name": "Torso",
            "mesh": 0,
            "translation": [0, 1.25, 0],
            "scale": torso_scale,
        },
        {
            "name": "Head",
            "mesh": 1,
            "translation": [0, head_y, 0],
            "scale": (
                [0.34, 0.28, 0.32]
                if goblin
                else [0.29, 0.29, 0.29]
            ),
        },
        {
            "name": "Helm",
            "mesh": 2,
            "translation": [0, head_y + 0.24, 0],
            "scale": [0.31, 0.10, 0.31],
        },
        {
            "name": "LeftArmPivot",
            "translation": [
                -0.68 if goblin else -0.62,
                arm_y,
                0,
            ],
            "children": [5, 6],
        },
        {
            "name": "LeftArm",
            "mesh": 0,
            "translation": [0, -0.49, 0],
            "scale": [0.13, 0.53, 0.13],
        },
        {
            "name": "Shield",
            "mesh": 4,
            "translation": [-0.12, -0.55, 0.18],
            "scale": [
                0.28 if goblin else 0.32,
                0.32 if goblin else 0.38,
                0.07,
            ],
        },
        {
            "name": "RightArmPivot",
            "translation": [
                0.68 if goblin else 0.62,
                arm_y,
                0,
            ],
            "children": [8, 9],
        },
        {
            "name": "RightArm",
            "mesh": 0,
            "translation": [0, -0.49, 0],
            "scale": [0.13, 0.53, 0.13],
        },
        {
            "name": "Weapon",
            "mesh": 3,
            "translation": [0, -1.18, 0],
            "scale": [
                0.07 if goblin else 0.055,
                0.58 if goblin else 0.78,
                0.07 if goblin else 0.055,
            ],
        },
        {
            "name": "LeftLegPivot",
            "translation": [-0.24, 0.67, 0],
            "children": [11],
        },
        {
            "name": "LeftLeg",
            "mesh": 2,
            "translation": [0, -0.58, 0],
            "scale": [0.15, 0.66, 0.15],
        },
        {
            "name": "RightLegPivot",
            "translation": [0.24, 0.67, 0],
            "children": [13],
        },
        {
            "name": "RightLeg",
            "mesh": 2,
            "translation": [0, -0.58, 0],
            "scale": [0.15, 0.66, 0.15],
        },
        {
            "name": "Belt",
            "mesh": 4,
            "translation": [0, 0.93, 0],
            "scale": [0.50, 0.08, 0.27],
        },
    ]

    animations = []

    def animation(name, times, channels):
        time_accessor = add_accessor(
            times,
            "SCALAR",
            bounds=True,
        )
        samplers = []
        targets = []

        for node, path_name, values in channels:
            value_accessor = add_accessor(
                values,
                "VEC4" if path_name == "rotation" else "VEC3",
            )
            samplers.append(
                {
                    "input": time_accessor,
                    "output": value_accessor,
                    "interpolation": "LINEAR",
                }
            )
            targets.append(
                {
                    "sampler": len(samplers) - 1,
                    "target": {
                        "node": node,
                        "path": path_name,
                    },
                }
            )

        animations.append(
            {
                "name": name,
                "samplers": samplers,
                "channels": targets,
            }
        )

    animation(
        "idle",
        [0, 0.5, 1.0],
        [
            (
                0,
                "translation",
                [(0, 0, 0), (0, 0.035, 0), (0, 0, 0)],
            ),
            (
                4,
                "rotation",
                [qz(-4), qz(1), qz(-4)],
            ),
            (
                7,
                "rotation",
                [qz(4), qz(-1), qz(4)],
            ),
        ],
    )

    animation(
        "attack",
        [0, 0.18, 0.34, 0.55, 0.78],
        [
            (
                0,
                "translation",
                [
                    (0, 0, 0),
                    (0, 0, 0),
                    (0.35, 0, 0),
                    (0.15, 0, 0),
                    (0, 0, 0),
                ],
            ),
            (
                7,
                "rotation",
                [
                    qz(5),
                    qz(-70),
                    qz(55),
                    qz(18),
                    qz(5),
                ],
            ),
            (
                4,
                "rotation",
                [
                    qz(-5),
                    qz(12),
                    qz(-35),
                    qz(-15),
                    qz(-5),
                ],
            ),
        ],
    )

    animation(
        "hit",
        [0, 0.12, 0.28, 0.55],
        [
            (
                0,
                "translation",
                [
                    (0, 0, 0),
                    (-0.18, 0, 0),
                    (-0.06, 0, 0),
                    (0, 0, 0),
                ],
            ),
            (
                0,
                "rotation",
                [qz(0), qz(-12), qz(-4), qz(0)],
            ),
        ],
    )

    animation(
        "death",
        [0, 0.35, 0.75, 1.2],
        [
            (
                0,
                "rotation",
                [qx(0), qx(25), qx(72), qx(90)],
            ),
            (
                0,
                "translation",
                [
                    (0, 0, 0),
                    (0, -0.05, 0),
                    (0, -0.45, 0),
                    (0, -0.72, 0),
                ],
            ),
            (
                7,
                "rotation",
                [qz(4), qz(35), qz(70), qz(82)],
            ),
        ],
    )

    animation(
        "victory",
        [0, 0.35, 0.75, 1.15],
        [
            (
                7,
                "rotation",
                [qz(4), qz(-35), qz(-60), qz(4)],
            ),
            (
                0,
                "translation",
                [
                    (0, 0, 0),
                    (0, 0.10, 0),
                    (0, 0.04, 0),
                    (0, 0, 0),
                ],
            ),
        ],
    )

    gltf = {
        "asset": {
            "version": "2.0",
            "generator": "Astralforge Depthbound 3D prototype generator",
        },
        "extensionsUsed": ["KHR_materials_unlit"],
        "scene": 0,
        "scenes": [
            {
                "name": "BattleActor",
                "nodes": [0],
            }
        ],
        "nodes": nodes,
        "meshes": meshes,
        "materials": materials,
        "buffers": [
            {
                "byteLength": len(blob),
            }
        ],
        "bufferViews": views,
        "accessors": accessors,
        "animations": animations,
    }

    json_chunk = json.dumps(
        gltf,
        separators=(",", ":"),
    ).encode("utf-8")

    while len(json_chunk) % 4:
        json_chunk += b" "

    while len(blob) % 4:
        blob.append(0)

    total_length = (
        12
        + 8
        + len(json_chunk)
        + 8
        + len(blob)
    )

    glb = bytearray(
        struct.pack(
            "<4sII",
            b"glTF",
            2,
            total_length,
        )
    )
    glb += struct.pack(
        "<I4s",
        len(json_chunk),
        b"JSON",
    )
    glb += json_chunk
    glb += struct.pack(
        "<I4s",
        len(blob),
        b"BIN\x00",
    )
    glb += blob

    path.parent.mkdir(
        parents=True,
        exist_ok=True,
    )
    path.write_bytes(glb)


def main():
    model_dir = OUT / "models"
    model_dir.mkdir(
        parents=True,
        exist_ok=True,
    )

    build_actor(
        model_dir / "prototype_knight.glb",
        goblin=False,
    )
    build_actor(
        model_dir / "prototype_goblin.glb",
        goblin=True,
    )

    print(
        "Generated Depthbound 3D prototype models in",
        model_dir,
    )


if __name__ == "__main__":
    main()
