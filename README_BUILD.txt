Arcanum Quest Dumper

What this mod does

Adds a client command:
/dumpquests

Use it after joining the ArcanumLand server and opening the quest book once. It dumps the synced FTB Quests data from client memory into:
.minecraft\quest-dumps

Programs required

1. Temurin JDK 17
2. Internet connection for the first build
3. This project folder

Build steps

1. Extract this ZIP.
2. Open the extracted folder.
3. Double click build.bat.
4. Wait for the build to finish.
5. Copy build\libs\arcanum-quest-dumper-1.0.0.jar into your ArcanumLand mods folder.

Use steps

1. Launch ArcanumLand.
2. Join the server.
3. Open the quest book once.
4. Run /dumpquests in chat.
5. Check the quest-dumps folder in the instance folder.

Notes

This mod uses reflection so it does not need FTB Quests as a compile dependency.
It only works when FTB Quests is loaded in game.
It is client side only.
