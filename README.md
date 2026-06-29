# Arcanum Quest Dumper

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen)
![Forge](https://img.shields.io/badge/Forge-47.4.x-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/Status-Stable-success)

> A Forge 1.20.1 client side utility for exporting synchronized FTB Quests data from multiplayer servers into reusable SNBT files.

**Author:** NirmataCipher

---

# Overview

Arcanum Quest Dumper is a lightweight Forge client side mod that exports synchronized FTB Quests data from multiplayer servers into reusable SNBT files.

The exported data can be used for:

* Offline documentation
* Quest analysis
* Backup and archival
* Reverse engineering quest packs
* Converting multiplayer quest packs into usable single player quest packs

The mod is entirely client side. It does **not** modify the server and does **not** require installation on the server.

---

# Features

## Version 1.0

* Client side only
* Forge 1.20.1 support
* Complete FTB Quests export
* Chapter export
* Reward table export
* Chapter group export
* Timestamped exports
* Import ready quest pack generation

---

# Confirmed Working

Version **1.0.0** has been tested successfully using the **ArcanumLand** modpack.

Confirmed functionality:

* Complete quest dump
* Import ready export generation
* Single player import
* Quest progression
* Quest completion
* Reward tables
* Reward claiming

### Import Location

```text
<Minecraft Instance>/config/ftbquests/quests/
```

Use the generated **Import Ready** folder, **not** the raw dump folder.

## Screenshots

### Running the Export using /dumpquest command

![Export](images/export_command1.png)
![Export](images/export_command_success.png)


### Generated Files go into "import-ready-date" 

![Generated Files](images/generated_files.png)

### Export Contents

![Export Contents](images/exported_contents.png)

### Imported into Single Player

![Imported](images/singleplayer_quests_loaded.png)

### Imported into Single Player with edit mode

![Imported](images/singleplayer_quests_editmode.png)

---

# Requirements

### Minecraft

```text
1.20.1
```

### Forge

```text
47.4.x
```

### Java

```text
17
```

---

# Building

Clone the repository:

```bash
git clone https://github.com/TylerDubs/arcanum-quest-dumper.git
```

Enter the project:

```bash
cd arcanum-quest-dumper
```

Build:

```bash
build.bat
```

or

```bash
gradlew build
```

Compiled JAR:

```text
build/libs/
```

---

# Installation

Copy the compiled JAR into:

```text
.minecraft/mods/
```

or

```text
<CurseForge Instance>/mods/
```

Launch Minecraft normally.

---

# In Game Commands and Import Instructions

## Dumping Quests

Join the multiplayer server and wait until the quest book is fully loaded.

Run:

```text
/dumpquests
```

The mod will create export folders inside:

```text
<Instance Folder>/quest-dumps/
```

Look for the newest folder named:

```text
import-ready-YYYY-MM-DD_HH-MM-SS
```

Inside that folder, open:

```text
INSTANCE_CONFIG/
```

You should see:

```text
ftbquests/
```

---

## Importing Into Single Player

Close Minecraft completely.

Copy this folder:

```text
quest-dumps/import-ready-YYYY-MM-DD_HH-MM-SS/INSTANCE_CONFIG/ftbquests
```

Paste it into your Minecraft instance config folder:

```text
<Instance Folder>/config/
```

Final layout must be:

```text
<Instance Folder>/config/ftbquests/quests/
```

Do not place it in:

```text
saves/<world>/serverconfig/
```

Do not place it in:

```text
saves/<world>/ftbquests/
```

Those folders are used for world progress, not the imported quest database.

---

## Testing the Import

After copying the folder:

1. Start Minecraft.
2. Create a brand new single player world.
3. Open the quest book.
4. Confirm chapters appear.
5. Complete a simple quest.
6. Claim a reward.

If chapters appear and rewards work, the import succeeded.
---

# Current Output

Example chapter files:

```text
ars_nouveau.snbt
blood_magic.snbt
create.snbt
mobs.snbt
items.snbt
wizard_reborn.snbt
...
```

Reward tables:

```text
reward_tables/
```

---

# Development Roadmap

## Version 1.0 ✅

* Complete quest export
* Import ready quest pack generation
* Verified single player import

## Version 2

* Dump status command
* Open export folder command
* Automatic export after synchronization
* Metadata generation
* Cleaner output

## Version 3

* Player progress export
* Team progress export
* Claimed reward export
* Quest completion export

## Version 4

* Automatic installer
* Quest validation
* Quest repair
* Pack conversion tools

## Version 5

* Desktop GUI
* JSON exporter
* Standalone quest conversion utility

---

# Repository

https://github.com/TylerDubs/arcanum-quest-dumper

---

# License

License to be determined.

---

# Disclaimer

This project is intended for educational, archival, interoperability, and personal backup purposes.

Users are responsible for complying with the licenses and terms governing any quest packs or servers whose data they export.
