package com.nirmatacipher.questdumper.export;

import com.nirmatacipher.questdumper.model.ExportResult;
import com.nirmatacipher.questdumper.model.QuestStats;
import com.nirmatacipher.questdumper.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ExportManager {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ExportManager() {
    }

    public static Path dumpRoot() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("quest-dumps");
    }

    public static ExportResult dumpQuests() {
        try {
            Object questFile = getClientQuestFile();
            if (questFile == null) {
                return ExportResult.failure("No synced FTB Quests data found. Join the server, open the quest book once, then run /dumpquests again.");
            }

            Path dumpRoot = dumpRoot();
            Files.createDirectories(dumpRoot);

            String stamp = LocalDateTime.now().format(FILE_TIME);
            Path folderDump = dumpRoot.resolve("ftbquests-full-" + stamp);
            Path snbtDump = dumpRoot.resolve("ftbquests-raw-" + stamp + ".snbt");
            Path infoDump = dumpRoot.resolve("README-" + stamp + ".txt");
            Path summaryDump = dumpRoot.resolve("questfile-summary-" + stamp + ".json");
            Path importRoot = dumpRoot.resolve("import-ready-" + stamp);

            boolean wroteFolder = false;
            String folderError = null;

            try {
                Method writeDataFull = questFile.getClass().getMethod("writeDataFull", Path.class);
                writeDataFull.invoke(questFile, folderDump);
                wroteFolder = Files.exists(folderDump) && Files.exists(folderDump.resolve("chapters"));
            } catch (Throwable t) {
                folderError = rootMessage(t);
            }

            boolean wroteSnbt = false;
            String snbtError = null;

            try {
                CompoundTag tag = new CompoundTag();
                Method writeData = questFile.getClass().getMethod("writeData", CompoundTag.class);
                writeData.invoke(questFile, tag);
                Files.writeString(snbtDump, tag.toString(), StandardCharsets.UTF_8);
                wroteSnbt = true;
            } catch (Throwable t) {
                snbtError = rootMessage(t);
            }

            boolean wroteSummary = false;
            String summaryError = null;

            try {
                QuestStats stats = collectStats(questFile);
                Files.writeString(summaryDump, stats.toJson(stamp), StandardCharsets.UTF_8);
                wroteSummary = true;
            } catch (Throwable t) {
                summaryError = rootMessage(t);
            }

            boolean wroteImport = false;
            String importError = null;

            try {
                if (wroteFolder) {
                    writeImportReadyCopies(folderDump, importRoot);
                    wroteImport = true;
                }
            } catch (Throwable t) {
                importError = rootMessage(t);
            }

            writeInfoFile(infoDump, wroteFolder, folderDump, folderError, wroteSnbt, snbtDump, snbtError,
                    wroteSummary, summaryDump, summaryError, wroteImport, importRoot, importError);

            if (wroteFolder || wroteSnbt || wroteSummary) {
                return ExportResult.success("Quest dump complete", folderDump, snbtDump, summaryDump, importRoot, infoDump);
            }
            return ExportResult.failure("Quest dump failed. Check: " + infoDump.toAbsolutePath(), infoDump);
        } catch (ClassNotFoundException e) {
            return ExportResult.failure("FTB Quests is not loaded. This mod only works in a pack with ftb-quests installed.");
        } catch (Throwable t) {
            return ExportResult.failure(rootMessage(t));
        }
    }

    public static ExportResult convertLatestDump() {
        try {
            Path dumpRoot = dumpRoot();
            if (!Files.isDirectory(dumpRoot)) {
                return ExportResult.failure("No quest-dumps folder found.");
            }

            Path latest = findLatestFullDump(dumpRoot);
            if (latest == null) {
                return ExportResult.failure("No ftbquests-full dump found.");
            }

            String stamp = LocalDateTime.now().format(FILE_TIME);
            Path importRoot = dumpRoot.resolve("import-ready-" + stamp);
            writeImportReadyCopies(latest, importRoot);
            return ExportResult.success("Import ready copy created", latest, null, null, importRoot, null);
        } catch (Throwable t) {
            return ExportResult.failure(rootMessage(t));
        }
    }

    public static Object getClientQuestFile() throws ReflectiveOperationException {
        Class<?> clientQuestFileClass = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");

        boolean exists = true;
        try {
            Method existsMethod = clientQuestFileClass.getMethod("exists");
            exists = Boolean.TRUE.equals(existsMethod.invoke(null));
        } catch (ReflectiveOperationException ignored) {
            exists = true;
        }

        Field instanceField = clientQuestFileClass.getField("INSTANCE");
        Object questFile = instanceField.get(null);
        return exists ? questFile : null;
    }

    public static QuestStats collectStats(Object questFile) {
        QuestStats stats = new QuestStats();
        stats.questFileClass = questFile.getClass().getName();

        List<?> chapters = listFromNoArg(questFile, "getAllChapters");
        stats.chapters = chapters.size();

        for (Object chapter : chapters) {
            String chapterName = stringFromNoArg(chapter, "getRawTitle", "");
            if (chapterName == null || chapterName.isBlank()) {
                chapterName = stringFromNoArg(chapter, "getFilename", "unknown");
            }
            stats.chapterNames.add(chapterName);

            List<?> quests = listFromNoArg(chapter, "getQuests");
            stats.quests += quests.size();

            for (Object quest : quests) {
                stats.tasks += listFromNoArg(quest, "getTasksAsList").size();
                stats.rewards += listFromNoArg(quest, "getRewards").size();
            }
        }

        stats.rewardTables = listFromNoArg(questFile, "getRewardTables").size();
        stats.chapterGroups = listFromNoArg(questFile, "getChapterGroups").size();
        stats.teamData = listFromNoArg(questFile, "getAllTeamData").size();
        stats.allObjects = listFromNoArg(questFile, "getAllObjects").size();
        return stats;
    }

    public static Path findLatestFullDump(Path dumpRoot) throws IOException {
        try (Stream<Path> stream = Files.list(dumpRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("ftbquests-full-"))
                    .filter(p -> Files.exists(p.resolve("chapters")))
                    .max(Comparator.comparing(p -> p.getFileName().toString()))
                    .orElse(null);
        }
    }

    public static Path findLatestImportReady(Path dumpRoot) throws IOException {
        if (!Files.isDirectory(dumpRoot)) {
            return null;
        }
        try (Stream<Path> stream = Files.list(dumpRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("import-ready-"))
                    .max(Comparator.comparing(p -> p.getFileName().toString()))
                    .orElse(null);
        }
    }

    private static void writeImportReadyCopies(Path sourceFullDump, Path importRoot) throws IOException {
        Path instanceConfig = importRoot.resolve("INSTANCE_CONFIG/ftbquests/quests");
        Path worldServerConfig = importRoot.resolve("WORLD_SERVERCONFIG/ftbquests/quests");

        FileUtils.copyRecursive(sourceFullDump, instanceConfig);
        FileUtils.copyRecursive(sourceFullDump, worldServerConfig);

        String instructions = "Arcanum Quest Dumper import-ready output\n\n"
                + "The dumped quest files have been placed into two likely FTB Quests load layouts.\n\n"
                + "Confirmed working singleplayer import method:\n"
                + "  Copy INSTANCE_CONFIG/ftbquests into your instance config folder:\n"
                + "  <instance>/config/ftbquests/quests/...\n\n"
                + "Optional existing-world layout:\n"
                + "  Copy WORLD_SERVERCONFIG/ftbquests into your world serverconfig folder:\n"
                + "  <instance>/saves/<world>/serverconfig/ftbquests/quests/...\n\n"
                + "Important: The quests folder matters. The layout should be:\n"
                + "  ftbquests/quests/chapters\n"
                + "  ftbquests/quests/reward_tables\n"
                + "  ftbquests/quests/chapter_groups.snbt\n"
                + "  ftbquests/quests/data.snbt\n";

        Files.createDirectories(importRoot);
        Files.writeString(importRoot.resolve("IMPORT_INSTRUCTIONS.txt"), instructions, StandardCharsets.UTF_8);
    }

    private static void writeInfoFile(Path infoDump,
                                      boolean wroteFolder, Path folderDump, String folderError,
                                      boolean wroteSnbt, Path snbtDump, String snbtError,
                                      boolean wroteSummary, Path summaryDump, String summaryError,
                                      boolean wroteImport, Path importRoot, String importError) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Arcanum Quest Dumper result\n\n");
        sb.append("Full folder dump: ").append(wroteFolder ? "SUCCESS" : "FAILED").append('\n');
        sb.append("Path: ").append(folderDump.toAbsolutePath()).append('\n');
        if (folderError != null) sb.append("Error: ").append(folderError).append('\n');
        sb.append('\n');
        sb.append("Raw SNBT dump: ").append(wroteSnbt ? "SUCCESS" : "FAILED").append('\n');
        sb.append("Path: ").append(snbtDump.toAbsolutePath()).append('\n');
        if (snbtError != null) sb.append("Error: ").append(snbtError).append('\n');
        sb.append('\n');
        sb.append("QuestFile summary: ").append(wroteSummary ? "SUCCESS" : "FAILED").append('\n');
        sb.append("Path: ").append(summaryDump.toAbsolutePath()).append('\n');
        if (summaryError != null) sb.append("Error: ").append(summaryError).append('\n');
        sb.append('\n');
        sb.append("Import-ready copy: ").append(wroteImport ? "SUCCESS" : "FAILED").append('\n');
        sb.append("Path: ").append(importRoot.toAbsolutePath()).append('\n');
        if (importError != null) sb.append("Error: ").append(importError).append('\n');
        Files.writeString(infoDump, sb.toString(), StandardCharsets.UTF_8);
    }

    private static List<?> listFromNoArg(Object target, String methodName) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            if (value instanceof List<?>) {
                return (List<?>) value;
            }
            if (value instanceof Iterable<?>) {
                List<Object> out = new ArrayList<>();
                for (Object o : (Iterable<?>) value) {
                    out.add(o);
                }
                return out;
            }
        } catch (Throwable ignored) {
        }
        return List.of();
    }

    private static String stringFromNoArg(Object target, String methodName, String fallback) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value == null ? fallback : value.toString();
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static String rootMessage(Throwable throwable) {
        Throwable t = throwable;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        String message = t.getMessage();
        return t.getClass().getName() + (message == null || message.isBlank() ? "" : ": " + message);
    }
}
