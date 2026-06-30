package com.nirmatacipher.questdumper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.nirmatacipher.questdumper.QuestDumper;
import com.nirmatacipher.questdumper.export.ExportManager;
import com.nirmatacipher.questdumper.model.ExportResult;
import com.nirmatacipher.questdumper.model.QuestStats;
import com.nirmatacipher.questdumper.util.ChatUtils;
import com.nirmatacipher.questdumper.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.nio.file.Path;

public final class DumpCommand {
    private DumpCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dumpquests")
                .executes(context -> runDump())
                .then(Commands.literal("dump")
                        .executes(context -> runDump()))
                .then(Commands.literal("status")
                        .executes(context -> runStatus()))
                .then(Commands.literal("path")
                        .executes(context -> runPath()))
                .then(Commands.literal("open")
                        .executes(context -> runOpen()))
                .then(Commands.literal("clean")
                        .executes(context -> runClean()))
                .then(Commands.literal("convert_latest")
                        .executes(context -> runConvertLatest())));
    }

    private static int runDump() {
        ExportResult result = ExportManager.dumpQuests();
        if (result.success()) {
            ChatUtils.success("Quest dump complete.");
            if (result.importReadyPath() != null) {
                ChatUtils.info("Import ready: " + result.importReadyPath().toAbsolutePath());
            }
            ChatUtils.info("Output folder: " + ExportManager.dumpRoot().toAbsolutePath());
        } else {
            ChatUtils.error("Quest dump failed: " + result.message());
            if (result.infoPath() != null) {
                ChatUtils.info("Details: " + result.infoPath().toAbsolutePath());
            }
        }
        return result.success() ? 1 : 0;
    }

    private static int runStatus() {
        try {
            Object questFile = ExportManager.getClientQuestFile();
            Path dumpRoot = ExportManager.dumpRoot();
            Path latestImport = ExportManager.findLatestImportReady(dumpRoot);

            ChatUtils.info("Arcanum Quest Dumper v" + QuestDumper.VERSION);
            ChatUtils.info("Output folder: " + dumpRoot.toAbsolutePath());

            if (questFile == null) {
                ChatUtils.warn("Quest database loaded: No");
                ChatUtils.warn("Join a server, open the quest book, then try again.");
                return 0;
            }

            QuestStats stats = ExportManager.collectStats(questFile);
            ChatUtils.success("Quest database loaded: Yes");
            ChatUtils.info("Chapters: " + stats.chapters());
            ChatUtils.info("Quests: " + stats.quests());
            ChatUtils.info("Tasks: " + stats.tasks());
            ChatUtils.info("Rewards: " + stats.rewards());
            ChatUtils.info("Reward tables: " + stats.rewardTables());
            ChatUtils.info("Chapter groups: " + stats.chapterGroups());

            if (latestImport != null) {
                ChatUtils.info("Latest import ready export: " + latestImport.toAbsolutePath());
            } else {
                ChatUtils.warn("No import ready exports found yet.");
            }
            return 1;
        } catch (ClassNotFoundException e) {
            ChatUtils.error("FTB Quests is not loaded.");
            return 0;
        } catch (Throwable t) {
            ChatUtils.error("Status failed: " + ExportManager.rootMessage(t));
            return 0;
        }
    }

    private static int runPath() {
        ChatUtils.info("Quest dump folder: " + ExportManager.dumpRoot().toAbsolutePath());
        return 1;
    }

    private static int runOpen() {
        try {
            Path root = ExportManager.dumpRoot();
            FileUtils.ensureDirectory(root);
            boolean opened = FileUtils.openDirectory(root);
            if (opened) {
                ChatUtils.success("Opened quest dump folder.");
                return 1;
            }
            ChatUtils.error("Could not open folder automatically: " + root.toAbsolutePath());
            return 0;
        } catch (Throwable t) {
            ChatUtils.error("Open failed: " + ExportManager.rootMessage(t));
            return 0;
        }
    }

    private static int runClean() {
        try {
            Path root = ExportManager.dumpRoot();
            int removed = FileUtils.cleanGeneratedExports(root);
            ChatUtils.success("Clean complete. Removed " + removed + " generated export items.");
            ChatUtils.warn("Only generated folders/files were removed. Manually renamed exports were kept.");
            return 1;
        } catch (Throwable t) {
            ChatUtils.error("Clean failed: " + ExportManager.rootMessage(t));
            return 0;
        }
    }

    private static int runConvertLatest() {
        ExportResult result = ExportManager.convertLatestDump();
        if (result.success()) {
            ChatUtils.success("Import ready copy created.");
            if (result.importReadyPath() != null) {
                ChatUtils.info("Import ready: " + result.importReadyPath().toAbsolutePath());
            }
            return 1;
        }
        ChatUtils.error("convert_latest failed: " + result.message());
        return 0;
    }

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
