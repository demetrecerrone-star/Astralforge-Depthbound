#!/usr/bin/env python3
from pathlib import Path
import math
import os
import random
import shutil
import struct
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable-nodpi"
RAW = ROOT / "app/src/main/res/raw"
DRAWABLE.mkdir(parents=True, exist_ok=True)
RAW.mkdir(parents=True, exist_ok=True)

AUTH_OUT = DRAWABLE / "astralforge_auth_landscape.png"
VIDEO_OUT = RAW / "dabsky_intro.mp4"

FFMPEG = shutil.which("ffmpeg")
if not FFMPEG:
    raise RuntimeError("ffmpeg is required to generate brand assets")

SERIF = "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf"
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

def run(args):
    subprocess.run(args, check=True)

def make_auth_background():
    rng = random.Random(4242)
    filters = ["format=rgba"]
    for _ in range(95):
        x = rng.randrange(20, 2320)
        y = rng.randrange(20, 700)
        size = rng.choice([1, 1, 2, 2, 3])
        alpha = rng.choice([0.16, 0.22, 0.30, 0.42])
        filters.append(
            f"drawbox=x={x}:y={y}:w={size}:h={size}:"
            f"color=0xded8ff@{alpha}:t=fill"
        )

    filters += [
        "drawbox=x=0:y=735:w=2340:h=4:color=0x8e58d8@0.46:t=fill",
        "drawbox=x=0:y=751:w=2340:h=3:color=0x6540a8@0.28:t=fill",
        "drawbox=x=0:y=772:w=2340:h=2:color=0x51347e@0.20:t=fill",
        "drawbox=x=125:y=500:w=40:h=430:color=0x0b0816@0.92:t=fill",
        "drawbox=x=590:y=500:w=40:h=430:color=0x0b0816@0.92:t=fill",
        "drawbox=x=1180:y=110:w=1090:h=905:color=0x02040e@0.30:t=fill",
        f"drawtext=fontfile={SERIF}:text='ASTRALFORGE':fontsize=82:"
        "fontcolor=0xeee6ff:x=175:y=150:shadowcolor=0x7f44d8@0.85:"
        "shadowx=0:shadowy=0",
        f"drawtext=fontfile={SANS}:text='DEPTHBOUND':fontsize=34:"
        "fontcolor=0xcab1f8:x=200:y=252",
        f"drawtext=fontfile={SANS}:text='DESCEND. ENDURE. ASCEND.':fontsize=24:"
        "fontcolor=0x968eb8:x=200:y=310",
    ]

    run([
        FFMPEG, "-y", "-loglevel", "error",
        "-f", "lavfi",
        "-i", "color=c=0x050816:s=2340x1080:d=1",
        "-vf", ",".join(filters),
        "-frames:v", "1",
        str(AUTH_OUT),
    ])

def make_intro():
    width, height = 1560, 720
    fps, duration = 24, 4.5
    rng = random.Random(777)

    with tempfile.TemporaryDirectory(prefix="dabsky_brand_") as temp_name:
        temp = Path(temp_name)
        wav_path = temp / "intro.wav"
        sample_rate = 44100

        with wave.open(str(wav_path), "w") as wf:
            wf.setnchannels(2)
            wf.setsampwidth(2)
            wf.setframerate(sample_rate)
            for i in range(int(duration * sample_rate)):
                t = i / sample_rate
                env = min(1.0, t / 0.55) * min(1.0, max(0.0, (duration - t) / 0.5))
                value = (
                    0.050 * math.sin(2 * math.pi * 55 * t)
                    + 0.020 * math.sin(2 * math.pi * 82.4 * t)
                )
                if t < 1.5:
                    value += (
                        0.025
                        * (t / 1.5)
                        * math.sin(2 * math.pi * (110 + 38 * t) * t)
                    )

                impact = t - 1.43
                if impact >= 0:
                    value += (
                        0.29
                        * math.exp(-impact * 5.2)
                        * math.sin(2 * math.pi * 58 * impact)
                    )

                shimmer = t - 2.15
                if shimmer >= 0:
                    value += (
                        0.026
                        * math.exp(-shimmer * 1.7)
                        * math.sin(2 * math.pi * 440 * shimmer)
                    )

                sample = int(max(-1.0, min(1.0, value * env)) * 32767)
                wf.writeframesraw(struct.pack("<hh", sample, sample))

        filters = ["format=rgba"]

        for _ in range(95):
            x = rng.randrange(10, width - 10)
            y = rng.randrange(10, 365)
            size = rng.choice([1, 1, 2, 2, 3])
            alpha = rng.choice([0.18, 0.25, 0.34, 0.44])
            filters.append(
                f"drawbox=x={x}:y={y}:w={size}:h={size}:"
                f"color=0xe4dcff@{alpha}:t=fill"
            )

        filters += [
            "drawbox=x=0:y=438:w=1560:h=282:color=0x02040e@0.96:t=fill",
            "drawbox=x=0:y=438:w=1560:h=2:color=0xb98cff@0.52:t=fill",
        ]

        for index in range(15):
            y = 448 + index * 14
            alpha = max(0.06, 0.23 - index * 0.01)
            filters.append(
                "drawbox="
                f"x=120+20*sin(2*PI*t+{index}):"
                f"y={y}+4*sin(2*PI*t*0.55+{index}):"
                f"w=1320:h=2:color=0x8558d0@{alpha}:t=fill"
            )

        filters += [
            f"drawtext=fontfile={SERIF}:text='D':fontsize=225:"
            "fontcolor=0xeee4ff:x=(w-text_w)/2:y=165:"
            "alpha='if(lt(t,1.10),0,if(lt(t,1.80),(t-1.10)/0.70,"
            "if(lt(t,4.00),1,(4.50-t)/0.50)))'",
            f"drawtext=fontfile={SERIF}:text='DABSKY':fontsize=88:"
            "fontcolor=0xf0e8ff:x=(w-text_w)/2:y=355:"
            "alpha='if(lt(t,1.80),0,if(lt(t,2.35),(t-1.80)/0.55,"
            "if(lt(t,4.00),1,(4.50-t)/0.50)))'",
            f"drawtext=fontfile={SANS}:text='P R E S E N T S':fontsize=21:"
            "fontcolor=0xc5b4e5:x=(w-text_w)/2:y=470:"
            "alpha='if(lt(t,2.45),0,if(lt(t,2.80),(t-2.45)/0.35,"
            "if(lt(t,4.00),0.65,0.65*(4.50-t)/0.50)))'",
            "drawbox=x='-220+(t-2.50)*3000':y=140:w=90:h=360:"
            "color=white@0.16:t=fill:enable='between(t,2.50,3.20)'",
            "fade=t=out:st=4.0:d=0.5",
        ]

        run([
            FFMPEG, "-y", "-loglevel", "error",
            "-f", "lavfi",
            "-i", f"color=c=0x03040e:s={width}x{height}:r={fps}:d={duration}",
            "-i", str(wav_path),
            "-vf", ",".join(filters),
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            "-profile:v", "main",
            "-crf", "26",
            "-preset", "medium",
            "-c:a", "aac",
            "-b:a", "96k",
            "-movflags", "+faststart",
            "-shortest",
            str(VIDEO_OUT),
        ])

if __name__ == "__main__":
    make_auth_background()
    make_intro()
    print("Generated", AUTH_OUT.relative_to(ROOT))
    print("Generated", VIDEO_OUT.relative_to(ROOT))
