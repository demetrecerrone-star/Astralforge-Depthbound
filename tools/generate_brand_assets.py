#!/usr/bin/env python3
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path
import math, os, random, shutil, struct, subprocess, tempfile, wave

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable-nodpi"
RAW = ROOT / "app/src/main/res/raw"
DRAWABLE.mkdir(parents=True, exist_ok=True)
RAW.mkdir(parents=True, exist_ok=True)

AUTH_OUT = DRAWABLE / "astralforge_auth_landscape.png"
VIDEO_OUT = RAW / "dabsky_intro.mp4"

def font(size, bold=False, serif=False):
    names = []
    if serif and bold:
        names += ["/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf"]
    elif serif:
        names += ["/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"]
    elif bold:
        names += ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"]
    else:
        names += ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]
    for path in names:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()

def smoothstep(x):
    x = max(0.0, min(1.0, x))
    return x * x * (3.0 - 2.0 * x)

def glow(base, center, radius, color, alpha):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    x, y = center
    for i in range(10, 0, -1):
        r = radius * i / 10
        a = int(alpha * (1 - i / 11) ** 1.25)
        d.ellipse((x-r, y-r, x+r, y+r), fill=(*color, a))
    return Image.alpha_composite(base.convert("RGBA"), layer.filter(ImageFilter.GaussianBlur(radius / 5)))

def centered(draw, text, y, fnt, fill, spacing=0, width=1560):
    widths = []
    for ch in text:
        box = draw.textbbox((0, 0), ch, font=fnt)
        widths.append(box[2] - box[0])
    total = sum(widths) + spacing * max(0, len(text)-1)
    x = (width - total) / 2
    for ch, w in zip(text, widths):
        draw.text((x, y), ch, font=fnt, fill=fill)
        x += w + spacing

def make_auth_background():
    w, h = 2340, 1080
    strip = Image.new("RGB", (1, h))
    strip.putdata([
        (int(3 + 4*y/h), int(5 + 2*y/h), int(15 + 14*y/h))
        for y in range(h)
    ])
    img = strip.resize((w, h)).convert("RGBA")
    for cx, cy, r, c, a in [
        (420, 260, 560, (91, 38, 180), 125),
        (840, 120, 470, (42, 78, 180), 88),
        (300, 760, 560, (68, 28, 132), 74),
        (1980, 250, 420, (31, 42, 86), 35),
    ]:
        img = glow(img, (cx, cy), r, c, a)

    rng = random.Random(4242)
    d = ImageDraw.Draw(img)
    for _ in range(340):
        x = rng.randrange(w)
        y = rng.randrange(int(h * .76))
        if 1120 < x < 2240 and 150 < y < 970 and rng.random() < .82:
            continue
        r = rng.choice([1, 1, 1, 2])
        b = rng.randrange(145, 240)
        d.ellipse((x-r, y-r, x+r, y+r), fill=(b, b, 255, rng.randrange(45, 145)))

    horizon = 770
    points = [(0, horizon)]
    x = 0
    while x <= w:
        points.append((x, horizon - rng.randrange(12, 120)))
        x += rng.randrange(95, 165)
    points += [(w, h), (0, h)]
    d.polygon(points, fill=(5, 5, 14, 242))

    d.rectangle((145, 520, 183, 930), fill=(10, 8, 20, 235))
    d.rectangle((585, 520, 623, 930), fill=(10, 8, 20, 235))
    d.arc((140, 420, 628, 785), 190, 350, fill=(23, 15, 44, 240), width=40)
    d.polygon([(0, 850), (180, 750), (315, 860), (405, 1080), (0, 1080)], fill=(4, 4, 10, 255))
    d.polygon([(w, 840), (2160, 770), (1990, 895), (1925, 1080), (w, 1080)], fill=(4, 4, 10, 255))

    title_font = font(82, True, True)
    sub_font = font(34, True)
    small_font = font(24)
    tx, ty = 175, 160
    blur = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    bd = ImageDraw.Draw(blur)
    bd.text((tx, ty), "ASTRALFORGE", font=title_font, fill=(188, 126, 255, 220))
    bd.text((tx+25, ty+96), "DEPTHBOUND", font=sub_font, fill=(175, 145, 240, 190))
    img = Image.alpha_composite(img, blur.filter(ImageFilter.GaussianBlur(16)))
    d = ImageDraw.Draw(img)
    d.text((tx, ty), "ASTRALFORGE", font=title_font, fill=(239, 232, 255, 250))
    d.text((tx+25, ty+96), "DEPTHBOUND", font=sub_font, fill=(201, 178, 248, 240))
    d.text((tx+25, ty+148), "DESCEND. ENDURE. ASCEND.", font=small_font, fill=(156, 149, 189, 190))

    calm = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    cd = ImageDraw.Draw(calm)
    cd.rounded_rectangle((1180, 110, 2270, 1015), 70, fill=(2, 4, 14, 70))
    img = Image.alpha_composite(img, calm.filter(ImageFilter.GaussianBlur(34)))
    img.convert("RGB").save(AUTH_OUT, "PNG", optimize=True)

def make_intro():
    w, h = 1560, 720
    fps, duration = 24, 4.5
    frame_count = int(fps * duration)
    rng = random.Random(777)
    stars = [(rng.randrange(w), rng.randrange(int(h*.54)), rng.choice([1,1,2]), rng.randrange(100,220)) for _ in range(190)]
    particles = [(rng.uniform(-110,110), rng.uniform(-60,60), rng.uniform(-120,120), rng.uniform(-70,45), rng.uniform(1,3)) for _ in range(65)]

    strip = Image.new("RGB", (1, h))
    strip.putdata([(int(2+4*y/h), int(3+2*y/h), int(12+12*y/h)) for y in range(h)])
    base_static = strip.resize((w, h)).convert("RGBA")
    base_static = glow(base_static, (w//2, 155), 350, (94, 39, 180), 105)
    sd = ImageDraw.Draw(base_static)
    for x, y, r, a in stars:
        sd.ellipse((x-r, y-r, x+r, y+r), fill=(220, 212, 255, a))

    d_font = font(225, True, True)
    n_font = font(88, True, True)
    p_font = font(22, True)

    with tempfile.TemporaryDirectory(prefix="dabsky_frames_") as tmp:
        temp = Path(tmp)
        for fi in range(frame_count):
            t = fi / fps
            fade = 1.0 - smoothstep((t - 4.0) / .5)
            img = base_static.copy()
            d = ImageDraw.Draw(img)
            water_y = 438
            d.rectangle((0, water_y, w, h), fill=(2, 4, 14, 242))
            for j in range(21):
                yy = water_y + j * 13
                amp = 7 + j * .3
                pts = [(x, yy + math.sin(x*.016 + t*2.15 + j*.37)*amp) for x in range(0, w+24, 24)]
                d.line(pts, fill=(88, 60, 165, max(8, 67-j*2)), width=2)
            d.line((0, water_y, w, water_y), fill=(155, 112, 234, 95), width=2)

            reveal = smoothstep((t - 1.12) / .68)
            word = smoothstep((t - 1.82) / .58)
            flare = math.exp(-((t - 1.43) / .15) ** 2)
            if flare > .01:
                img = glow(img, (w//2, 360), 180 + 150*flare, (196, 160, 255), int(205*flare))
                d = ImageDraw.Draw(img)
                d.line((w*.20, 365, w*.80, 365), fill=(235, 220, 255, int(220*flare)), width=max(1, int(5*flare)))

            if t > 1.24:
                dt = t - 1.24
                d = ImageDraw.Draw(img)
                for ox, oy, vx, vy, sz in particles:
                    x = w/2 + ox + vx*dt
                    y = 365 + oy + vy*dt
                    a = int(170 * max(0.0, 1.0-dt/2.2))
                    if a:
                        d.ellipse((x-sz,y-sz,x+sz,y+sz), fill=(215, 190, 255, a))

            if reveal > 0:
                logo = Image.new("RGBA", (w,h), (0,0,0,0))
                ld = ImageDraw.Draw(logo)
                scale = .74 + .26*reveal
                f = font(int(225*scale), True, True)
                box = ld.textbbox((0,0), "D", font=f)
                x = (w-(box[2]-box[0]))/2
                y = 170 + (1-reveal)*48
                ld.text((x,y), "D", font=f, fill=(233, 222, 255, int(255*reveal*fade)))
                img = Image.alpha_composite(img, logo.filter(ImageFilter.GaussianBlur(20)))
                img = Image.alpha_composite(img, logo)

            if word > 0:
                text_layer = Image.new("RGBA", (w,h), (0,0,0,0))
                td = ImageDraw.Draw(text_layer)
                centered(td, "DABSKY", 360, n_font, (241, 232, 255, int(246*word*fade)), 10, w)
                img = Image.alpha_composite(img, text_layer.filter(ImageFilter.GaussianBlur(14)))
                img = Image.alpha_composite(img, text_layer)
                if t > 2.45:
                    d = ImageDraw.Draw(img)
                    centered(d, "PRESENTS", 464, p_font, (194, 180, 226, int(155*smoothstep((t-2.45)/.35)*fade)), 6, w)

            if 2.5 < t < 3.2:
                u = (t - 2.5) / .7
                x = int(-240 + u*(w+480))
                sweep = Image.new("RGBA", (w,h), (0,0,0,0))
                sw = ImageDraw.Draw(sweep)
                sw.polygon([(x-110,150),(x+15,150),(x-90,500),(x-215,500)], fill=(255,255,255,42))
                img = Image.alpha_composite(img, sweep.filter(ImageFilter.GaussianBlur(18)))

            if fade < 1:
                img = Image.alpha_composite(img, Image.new("RGBA",(w,h),(0,0,0,int(255*(1-fade)))))
            img.convert("RGB").save(temp / f"frame_{fi:04d}.jpg", quality=87)

        wav_path = temp / "intro.wav"
        sr = 44100
        with wave.open(str(wav_path), "w") as wf:
            wf.setnchannels(2); wf.setsampwidth(2); wf.setframerate(sr)
            for i in range(int(duration*sr)):
                t = i/sr
                env = min(1, t/.7) * min(1, max(0, (duration-t)/.45))
                v = .055*math.sin(2*math.pi*55*t) + .025*math.sin(2*math.pi*82.4*t)
                if t < 1.5:
                    v += .03*(t/1.5)*math.sin(2*math.pi*(110+40*t)*t)
                dt = t-1.43
                if dt >= 0:
                    v += .30*math.exp(-dt*5.5)*math.sin(2*math.pi*(58-12*min(dt,1))*dt)
                dt2 = t-2.15
                if dt2 >= 0:
                    v += .03*math.exp(-dt2*1.6)*math.sin(2*math.pi*440*dt2)
                s = int(max(-1,min(1,v*env))*32767)
                wf.writeframesraw(struct.pack("<hh", s, s))

        ffmpeg = shutil.which("ffmpeg")
        if not ffmpeg:
            raise RuntimeError("ffmpeg is required to generate the DABSKY intro")
        subprocess.run([
            ffmpeg, "-y", "-loglevel", "error",
            "-framerate", str(fps), "-i", str(temp/"frame_%04d.jpg"),
            "-i", str(wav_path), "-c:v", "libx264", "-pix_fmt", "yuv420p",
            "-profile:v", "main", "-crf", "26", "-preset", "medium",
            "-c:a", "aac", "-b:a", "96k", "-shortest", str(VIDEO_OUT)
        ], check=True)

if __name__ == "__main__":
    make_auth_background()
    make_intro()
    print(f"Generated {AUTH_OUT.relative_to(ROOT)}")
    print(f"Generated {VIDEO_OUT.relative_to(ROOT)}")
