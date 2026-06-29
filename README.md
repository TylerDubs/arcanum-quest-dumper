# arcanum-quest-dumper
# Arcanum Quest Dumper

> A Forge 1.20.1 client side utility for exporting synchronized FTB Quests data from multiplayer servers into reusable SNBT files.

**Author:** NirmataCipher

---

# Overview

Arcanum Quest Dumper is a lightweight client side Forge mod that allows players to export the complete synchronized FTB Quests database after joining a multiplayer server.

The exported data can be used for:

* Offline documentation
* Quest analysis
* Backup and archival
* Reverse engineering quest packs
* Future conversion into a standalone singleplayer FTB Quests pack

The mod **does not modify the server** and does **not require server installation**.

---

# Features

## Current

✔ Client side only

✔ Works on Forge 1.20.1

✔ Dumps complete FTB Quests database

✔ Exports every chapter

✔ Exports reward tables

✔ Exports chapter groups

✔ Timestamped output folders

---
## Confirmed Working

Version 1.0.0 has been tested with ArcanumLand.

Confirmed:

- Quest dump works
- Import ready export works
- Single player world loads quests
- Quest completion works
- Reward tables load
- Rewards can be claimed

Import path:

```text
ArcanumLand/config/ftbquests/quests/

---

# Planned

## Version 0.2

* Better logging
* Cleaner filenames
* Config file
* Dump status command

## Version 0.3

* Automatic dump after quest synchronization
* No manual command required

## Version 0.4

* JSON export

## Version 0.5

* Player progress export

* Team progress export

* Claimed reward export

## Version 0.6

* Singleplayer converter

* Automatic installation into worlds

## Version 0.7

* In game GUI

## Version 1.0

* Full standalone release

---

# Requirements

Minecraft

```
1.20.1
```

Forge

```
47.4.x
```

Java

```
17
```

---

# Building

Clone the repository.

```
git clone https://github.com/TylerDubs/arcanum-quest-dumper.git
```

Enter the project.

```
cd arcanum-quest-dumper
```

Build the mod.

Windows

```
build.bat
```

or

```
gradlew build
```

Compiled jar:

```
build/libs/
```

---

# Installation

Copy the generated jar into:

```
.minecraft/mods/
```

or

```
CurseForge Instance/mods/
```

Launch Minecraft normally.

---

# Usage

Join a server running FTB Quests.

Once fully loaded, execute:

```
/dumpquests
```

The exported quest pack will be created inside:

```
quest-dumps/
```

Example:

```
quest-dumps/
└── ftbquests-full-2026-06-29_14-51-08/
```

Containing:

```
chapters/
reward_tables/
chapter_groups.snbt
data.snbt
```

---

# Current Output

Example:

```
chapters/

ars_nouveau.snbt

blood_magic.snbt

create.snbt

mobs.snbt

items.snbt

wizard_reborn.snbt

...
```

Reward tables:

```
reward_tables/
```

---

# Project Goals

## Phase 1

- [x] Create Forge mod
- [x] Load successfully
- [x] Dump synchronized quest database
- [x] Export chapters
- [x] Export reward tables

## Phase 2

- [ ] Export player progress
- [ ] Export team progress
- [ ] Export completed quests
- [ ] Export claimed rewards

## Phase 3

- [ ] Automatic quest synchronization detection
- [ ] Automatic dumping
- [ ] Configurable output directory

## Phase 4

- [ ] Convert dump into a valid singleplayer FTB Quests pack
- [ ] Automatic installer for worlds
- [ ] Quest validation

## Phase 5

- [ ] GUI
- [ ] JSON exporter
- [ ] Release 1.0

---

# Repository

GitHub

https://github.com/TylerDubs/arcanum-quest-dumper

---

# License

License to be determined.

---

# Disclaimer

This project is intended for educational, archival, interoperability, and personal backup purposes.

Users are responsible for complying with the licenses and terms governing any quest packs or servers whose data they export.