import java.net.URI
import java.awt.image.BufferedImage
import java.util.zip.ZipFile
import javax.imageio.ImageIO

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val generatedAstralSpritesDir =
    layout.buildDirectory.dir("generated/astralSpriteAssets")

val generatedBattleV2ThreeDDir =
    layout.buildDirectory.dir("generated/battleV2ThreeDAssets")

val generatedDabskyIntroResDir =
    layout.buildDirectory.dir("generated/dabskyIntroRes")

val generatedAuthUiResDir =
    layout.buildDirectory.dir("generated/authUiRes")

val generateAuthUiAssets = tasks.register("generateAuthUiAssets") {
    val outputDir = generatedAuthUiResDir.map {
        it.dir("drawable-nodpi")
    }

    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        val assets = mapOf(
            "auth_login_bg.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/7dc0866d98f0f5b05df1b5a7eb677c94684d54bcd16669839de6bbc67d743cc6.png",
            "auth_create_bg.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/0a60148aeac8bae1845e538e55459acb315dd9f30654cc3accb20b6b89a79f07.png",
            "auth_btn_signin.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/6d4882adcf9fb5c61e63f399a6ef97458d2698442a286a2c02ea3d7c1673a318.png",
            "auth_btn_create.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/c6e5a7b464c3198b35045e10800002c420e1f6d78ec1d6c4af9e6ed94e297c24.png",
            "auth_btn_google.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/1dd2ca4fcbcf16f6692d9db2c286bdf7a87172a394ecb256cc628e968a968981.png",
            "auth_btn_guest.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/7abebec58a7e584a1b2fb8f9ff6bfc617cba7930b6b47cadbc9898ec63e42ce5.png",
            "auth_field_email.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/964b51b19cc49c4b059948adcff9279dd457eb660be4b2b6c9f9a609a4bffa1a.png",
            "auth_field_password.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/707b8dd5bda601212d14b94a7676771610089cc3f22dc287c4617d86df9340be.png",
            "auth_field_username.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/82c54a4462e1efd2935f9734c39cd24f5a09d699f4fbceb209143866ab40f747.png",
            "auth_field_confirm.png" to "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/0ddd6fbdcfac3a2ecd787999e5e903c52db20dd5bec6d0a11f14c5766f42c003.png"
        )

        assets.forEach { (name, url) ->
            val output = dir.resolve(name)
            URI(url).toURL().openStream().use { input ->
                output.outputStream().use { out ->
                    input.copyTo(out)
                }
            }
            check(output.length() > 10_000L) {
                "Auth UI asset download failed: " + name
            }
        }
    }
}

val generateDabskyCompatIntro =
    tasks.register<org.gradle.api.tasks.Exec>("generateDabskyCompatIntro") {
        val source =
            file("video_sources/dabsky_intro_source.mp4")
        val output =
            generatedDabskyIntroResDir.get().asFile
                .resolve("raw/dabsky_intro.mp4")

        inputs.file(source)
        outputs.file(output)

        doFirst {
            output.parentFile.mkdirs()
        }

        commandLine(
            "ffmpeg",
            "-y",
            "-i", source.absolutePath,
            "-c:v", "libx264",
            "-profile:v", "main",
            "-level:v", "4.0",
            "-preset", "medium",
            "-crf", "20",
            "-maxrate", "10M",
            "-bufsize", "20M",
            "-pix_fmt", "yuv420p",
            "-movflags", "+faststart",
            "-c:a", "aac",
            "-b:a", "160k",
            "-ar", "48000",
            "-ac", "2",
            output.absolutePath
        )
    }

val generateBattleV2ThreeD =
    tasks.register<org.gradle.api.tasks.Exec>("generateBattleV2ThreeD") {
        workingDir(rootProject.projectDir)
        commandLine(
            "python3",
            "tools/generate_battle_v2_3d.py",
            generatedBattleV2ThreeDDir.get().asFile.absolutePath
        )
        inputs.file(
            rootProject.file("tools/generate_battle_v2_3d.py")
        )
        outputs.dir(generatedBattleV2ThreeDDir)
    }

val splitAstralSpriteSheets = tasks.register("splitAstralSpriteSheets") {
    val characterIdleZip = file("sprite_sources/characters.zip")
    val characterActionZip = file("sprite_sources/character-frames.zip")
    val monsterIdleZip = file("sprite_sources/monsters-first-dungeon.zip")
    val monsterActionZip = file("sprite_sources/monster-frames.zip")

    inputs.files(
        characterIdleZip,
        characterActionZip,
        monsterIdleZip,
        monsterActionZip
    )
    outputs.dir(generatedAstralSpritesDir)

    doLast {
        val outputRoot =
            generatedAstralSpritesDir.get().asFile.resolve("astral_sprites")
        outputRoot.deleteRecursively()
        outputRoot.mkdirs()

        fun readPngFromZip(
            zipFile: java.io.File,
            entrySuffix: String
        ): BufferedImage {
            ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                var found: java.util.zip.ZipEntry? = null
                while (entries.hasMoreElements()) {
                    val next = entries.nextElement()
                    if (!next.isDirectory &&
                        next.name.endsWith(entrySuffix)
                    ) {
                        found = next
                        break
                    }
                }

                val entry = requireNotNull(found) {
                    "Missing " + entrySuffix + " in " + zipFile.name
                }

                zip.getInputStream(entry).use { input ->
                    return requireNotNull(ImageIO.read(input)) {
                        "Unable to decode " + entrySuffix
                    }
                }
            }
        }

        fun cell(
            image: BufferedImage,
            cols: Int,
            rows: Int,
            column: Int,
            row: Int
        ): BufferedImage {
            val x1 = column * image.width / cols
            val x2 = (column + 1) * image.width / cols
            val y1 = row * image.height / rows
            val y2 = (row + 1) * image.height / rows
            return image.getSubimage(
                x1,
                y1,
                x2 - x1,
                y2 - y1
            )
        }

        fun writePng(
            image: BufferedImage,
            destination: java.io.File
        ) {
            destination.parentFile.mkdirs()
            check(ImageIO.write(image, "png", destination)) {
                "Unable to write " + destination.path
            }
        }

        fun splitIdleSheet(
            image: BufferedImage,
            entityDir: java.io.File,
            battleIdleIndex: Int
        ) {
            var index = 1
            for (row in 0 until 2) {
                for (column in 0 until 4) {
                    val frame = cell(image, 4, 2, column, row)
                    val number = index.toString().padStart(2, '0')
                    writePng(
                        frame,
                        entityDir.resolve("idle/idle_" + number + ".png")
                    )
                    if (index == battleIdleIndex) {
                        writePng(
                            frame,
                            entityDir.resolve("battle_idle.png")
                        )
                    }
                    index += 1
                }
            }
        }

        fun splitActionSheet(
            image: BufferedImage,
            entityDir: java.io.File,
            columns: Int
        ) {
            val actions = listOf("attack", "hit", "death")
            for (row in 0 until 3) {
                for (column in 0 until columns) {
                    val number =
                        (column + 1).toString().padStart(2, '0')
                    val action = actions[row]
                    writePng(
                        cell(image, columns, 3, column, row),
                        entityDir.resolve(
                            action + "/" + action + "_" + number + ".png"
                        )
                    )
                }
            }
        }

        data class CharacterSource(
            val id: String,
            val idleSheet: String,
            val actionSheet: String
        )

        val characters = listOf(
            CharacterSource(
                "knight",
                "pixel_art_knight_spritesheet.png",
                "eight_direction_knight_sprite_sheet.png"
            ),
            CharacterSource(
                "barbarian",
                "eight_direction_barbarian_sprite_sheet.png",
                "red_haired_barbarian_sprite_sheet.png"
            ),
            CharacterSource(
                "rogue",
                "pixel_rogue_assassin_sprite_sheet.png",
                "hooded_rogue_combat_sprite_sheet.png"
            ),
            CharacterSource(
                "mage",
                "eight_direction_blue_crystal_mage_sprite_sheet.png",
                "crystal_mage_rpg_sprite_sheet.png"
            ),
            CharacterSource(
                "ranger",
                "eight_direction_pixel_ranger_sprite_sheet.png",
                "pixel_ranger_archer_sprite_sheet.png"
            ),
            CharacterSource(
                "paladin",
                "eight_direction_paladin_sprite_sheet.png",
                "paladin_sprite_sheet_attack_hurt_death.png"
            ),
            CharacterSource(
                "necromancer",
                "eight_view_necromancer_sprite_sheet.png",
                "necromancer_sprite_sheet_attack_hurt_defeated.png"
            ),
            CharacterSource(
                "dark_knight",
                "dark_knight_eight_direction_sprite_sheet.png",
                "dark_knight_rpg_sprite_sheet.png"
            )
        )

        characters.forEach { source ->
            val entityDir =
                outputRoot.resolve("characters/" + source.id)
            splitIdleSheet(
                readPngFromZip(characterIdleZip, source.idleSheet),
                entityDir,
                battleIdleIndex = 3
            )
            splitActionSheet(
                readPngFromZip(characterActionZip, source.actionSheet),
                entityDir,
                columns = 8
            )
        }

        data class MonsterSource(
            val id: String,
            val idleSheet: String,
            val actionSheet: String
        )

        val monsters = listOf(
            MonsterSource(
                "blue_slime",
                "blue-slime.png",
                "blue-slime-frames.png"
            ),
            MonsterSource(
                "goblin_raider",
                "goblin-raider.png",
                "goblin-raider-frames.png"
            ),
            MonsterSource(
                "skeleton_warrior",
                "skeleton-warrior.png",
                "skeleton-warrior-frames.png"
            ),
            MonsterSource(
                "dire_wolf",
                "dire-wolf.png",
                "dire-wolf-frames.png"
            ),
            MonsterSource(
                "dungeon_boss",
                "dungeon-boss.png",
                "dungeon-boss-frames.png"
            )
        )

        monsters.forEach { source ->
            val entityDir =
                outputRoot.resolve("monsters/" + source.id)
            splitIdleSheet(
                readPngFromZip(monsterIdleZip, source.idleSheet),
                entityDir,
                battleIdleIndex = 8
            )
            splitActionSheet(
                readPngFromZip(monsterActionZip, source.actionSheet),
                entityDir,
                columns = 4
            )
        }

        outputRoot.resolve("README.txt").writeText(
            "Generated battle sprites. Each entity has isolated idle, attack, hit, and death frames."
        )
    }
}

android {
    namespace = "com.demetrecerrone.astralforge"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.demetrecerrone.astralforge"
        minSdk = 26
        targetSdk = 35
        versionCode = 32
        versionName = "0.0.1.0"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("astralforge-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main").assets.srcDir(generatedAstralSpritesDir)
        getByName("main").assets.srcDir(generatedBattleV2ThreeDDir)
        getByName("main").res.srcDir(generatedDabskyIntroResDir)
        getByName("main").res.srcDir(generatedAuthUiResDir)
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(
        splitAstralSpriteSheets,
        generateBattleV2ThreeD,
        generateDabskyCompatIntro,
        generateAuthUiAssets
    )
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.google.android.filament:filament-utils-android:1.75.1")
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
