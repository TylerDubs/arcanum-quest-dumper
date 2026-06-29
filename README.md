# Arcanum Quest Dumper

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

### Running the Export

![Export](images/export_command1.png)
![Export](images/export_command_success.png)


### Generated Files

![Generated Files](images/generated_files.png)

### Export Contents

![Export Contents](images/exported_contents.png)

### Imported into Single Player

![Imported](images/singleplayer_quests_loaded.png)

### Imported into Single Player with edit mode

![Imported](images/singleplayer_quests_editmode.pngg)

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

# Usage

Join a multiplayer server running FTB Quests.

Once synchronization has completed, execute:

```text
/dumpquests
```

The export will be generated in:

```text
quest-dumps/
```

Example:

```text
quest-dumps/
└── ftbquests-full-2026-06-29_14-51-08/
```

Contents:

```text
chapters/
reward_tables/
chapter_groups.snbt
data.snbt
```

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
