package com.lyrad.questdumper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

@Mod(QuestDumper.MOD_ID)
public class QuestDumper {
    public static final String MOD_ID = "questdumper";
    public static final String VERSION = "2.0.0";

    public QuestDumper() {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            dispatcher.register(Commands.literal("dumpquests")
                    .executes(context -> {
                        dumpQuests();
                        return 1;
                    })
                    .then(Commands.literal("dump")
                            .executes(context -> {
                                dumpQuests();
                                return 1;
                            }))
                    .then(Commands.literal("status")
                            .executes(context -> {
                                status();
                                return 1;
                            }))
                    .then(Commands.literal("convert_latest")
                            .executes(context -> {
                                convertLatestDump();
                                return 1;
                            })));
        }

        private static void status() {
            try {
                Object questFile = getClientQuestFile();
                if (questFile == null) {
                    send("No synced FTB Quests data found. Join the server, open quests, then run /dumpquests status again.");
                    return;
                }

                QuestStats stats = collectStats(questFile);
                send("Synced QuestFile found. Chapters=" + stats.chapters + ", quests=" + stats.quests
                        + ", tasks=" + stats.tasks + ", rewards=" + stats.rewards
                        + ", rewardTables=" + stats.rewardTables + ", teams=" + stats.teamData);
            } catch (Throwable t) {
                send("Status failed: " + rootMessage(t));
            }
        }

        private static void dumpQuests() {
            Minecraft mc = Minecraft.getInstance();

            try {
                Object questFile = getClientQuestFile();
                if (questFile == null) {
                    send("No synced FTB Quests data found. Join the server, open the quest book once, then run /dumpquests again.");
                    return;
                }

                Path dumpRoot = mc.gameDirectory.toPath().resolve("quest-dumps");
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
                    send("Quest dump complete. Check: " + dumpRoot.toAbsolutePath());
                } else {
                    send("Quest dump failed. Check: " + infoDump.toAbsolutePath());
                }
            } catch (ClassNotFoundException e) {
                send("FTB Quests is not loaded. This mod only works in a pack with ftb-quests installed.");
            } catch (Throwable t) {
                send("Quest dump failed: " + rootMessage(t));
            }
        }

        private static Object getClientQuestFile() throws ReflectiveOperationException {
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

        private static QuestStats collectStats(Object questFile) {
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

        private static void convertLatestDump() {
            try {
                Path dumpRoot = Minecraft.getInstance().gameDirectory.toPath().resolve("quest-dumps");
                if (!Files.isDirectory(dumpRoot)) {
                    send("No quest-dumps folder found.");
                    return;
                }

                Path latest = findLatestFullDump(dumpRoot);
                if (latest == null) {
                    send("No ftbquests-full dump found.");
                    return;
                }

                String stamp = LocalDateTime.now().format(FILE_TIME);
                Path importRoot = dumpRoot.resolve("import-ready-" + stamp);
                writeImportReadyCopies(latest, importRoot);
                send("Import-ready copy created: " + importRoot.toAbsolutePath());
            } catch (Throwable t) {
                send("convert_latest failed: " + rootMessage(t));
            }
        }

        private static Path findLatestFullDump(Path dumpRoot) throws IOException {
            try (Stream<Path> stream = Files.list(dumpRoot)) {
                return stream
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().startsWith("ftbquests-full-"))
                        .filter(p -> Files.exists(p.resolve("chapters")))
                        .max(Comparator.comparing(p -> p.getFileName().toString()))
                        .orElse(null);
            }
        }

        private static void writeImportReadyCopies(Path sourceFullDump, Path importRoot) throws IOException {
            Path instanceConfig = importRoot.resolve("INSTANCE_CONFIG/ftbquests/quests");
            Path worldServerConfig = importRoot.resolve("WORLD_SERVERCONFIG/ftbquests/quests");

            copyRecursive(sourceFullDump, instanceConfig);
            copyRecursive(sourceFullDump, worldServerConfig);

            String instructions = "Arcanum Quest Dumper import-ready output\n\n"
                    + "The dumped quest files have been placed into two likely FTB Quests load layouts.\n\n"
                    + "Try this first for singleplayer testing BEFORE creating a new world:\n"
                    + "  Copy INSTANCE_CONFIG/ftbquests into your instance config folder:\n"
                    + "  <instance>/config/ftbquests/quests/...\n\n"
                    + "Try this for an existing world:\n"
                    + "  Copy WORLD_SERVERCONFIG/ftbquests into your world serverconfig folder:\n"
                    + "  <instance>/saves/<world>/serverconfig/ftbquests/quests/...\n\n"
                    + "Important: The quests folder matters. The layout should be:\n"
                    + "  ftbquests/quests/chapters\n"
                    + "  ftbquests/quests/reward_tables\n"
                    + "  ftbquests/quests/chapter_groups.snbt\n"
                    + "  ftbquests/quests/data.snbt\n";

            Files.writeString(importRoot.resolve("IMPORT_INSTRUCTIONS.txt"), instructions, StandardCharsets.UTF_8);
        }

        private static void copyRecursive(Path source, Path target) throws IOException {
            if (!Files.exists(source)) {
                return;
            }
            try (Stream<Path> stream = Files.walk(source)) {
                for (Path src : stream.toList()) {
                    Path dest = target.resolve(source.relativize(src).toString());
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        private static void writeInfoFile(Path infoDump,
                                          boolean wroteFolder, Path folderDump, String folderError,
                                          boolean wroteSnbt, Path snbtDump, String snbtError,
                                          boolean wroteSummary, Path summaryDump, String summaryError,
                                          boolean wroteImport, Path importRoot, String importError) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Arcanum Quest Dumper v").append(VERSION).append(" result\n\n");
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

        private static String rootMessage(Throwable throwable) {
            Throwable t = throwable;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            String message = t.getMessage();
            return t.getClass().getName() + (message == null || message.isBlank() ? "" : ": " + message);
        }

        private static void send(String message) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("[QuestDumper] " + message), false);
            } else {
                System.out.println("[QuestDumper] " + message);
            }
        }

        private static final class QuestStats {
            String questFileClass = "unknown";
            int chapters;
            int quests;
            int tasks;
            int rewards;
            int rewardTables;
            int chapterGroups;
            int teamData;
            int allObjects;
            final List<String> chapterNames = new ArrayList<>();

            String toJson(String stamp) {
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                sb.append("  \"tool\": \"Arcanum Quest Dumper\",\n");
                sb.append("  \"version\": \"").append(escape(VERSION)).append("\",\n");
                sb.append("  \"timestamp\": \"").append(escape(stamp)).append("\",\n");
                sb.append("  \"questFileClass\": \"").append(escape(questFileClass)).append("\",\n");
                sb.append("  \"chapters\": ").append(chapters).append(",\n");
                sb.append("  \"quests\": ").append(quests).append(",\n");
                sb.append("  \"tasks\": ").append(tasks).append(",\n");
                sb.append("  \"rewards\": ").append(rewards).append(",\n");
                sb.append("  \"rewardTables\": ").append(rewardTables).append(",\n");
                sb.append("  \"chapterGroups\": ").append(chapterGroups).append(",\n");
                sb.append("  \"teamDataEntries\": ").append(teamData).append(",\n");
                sb.append("  \"allObjects\": ").append(allObjects).append(",\n");
                sb.append("  \"chapterNames\": [\n");
                for (int i = 0; i < chapterNames.size(); i++) {
                    sb.append("    \"").append(escape(chapterNames.get(i))).append("\"");
                    if (i + 1 < chapterNames.size()) sb.append(',');
                    sb.append('\n');
                }
                sb.append("  ]\n");
                sb.append("}\n");
                return sb.toString();
            }

            private static String escape(String s) {
                if (s == null) return "";
                return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            }
        }
    }
}
