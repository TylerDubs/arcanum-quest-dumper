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

@Mod(QuestDumper.MOD_ID)
public class QuestDumper {
    public static final String MOD_ID = "questdumper";

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
                    }));
        }

        private static void dumpQuests() {
            Minecraft mc = Minecraft.getInstance();

            try {
                Class<?> clientQuestFileClass = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");

                boolean exists = false;
                try {
                    Method existsMethod = clientQuestFileClass.getMethod("exists");
                    Object existsResult = existsMethod.invoke(null);
                    exists = Boolean.TRUE.equals(existsResult);
                } catch (ReflectiveOperationException ignored) {
                    exists = true;
                }

                Field instanceField = clientQuestFileClass.getField("INSTANCE");
                Object questFile = instanceField.get(null);

                if (!exists || questFile == null) {
                    send("No synced FTB Quests data found. Join the server, open the quest book once, then run /dumpquests again.");
                    return;
                }

                Path dumpRoot = mc.gameDirectory.toPath().resolve("quest-dumps");
                Files.createDirectories(dumpRoot);

                String stamp = LocalDateTime.now().format(FILE_TIME);
                Path folderDump = dumpRoot.resolve("ftbquests-full-" + stamp);
                Path snbtDump = dumpRoot.resolve("ftbquests-raw-" + stamp + ".snbt");
                Path infoDump = dumpRoot.resolve("README-" + stamp + ".txt");

                boolean wroteFolder = false;
                String folderError = null;

                try {
                    Method writeDataFull = questFile.getClass().getMethod("writeDataFull", Path.class);
                    writeDataFull.invoke(questFile, folderDump);
                    wroteFolder = Files.exists(folderDump);
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

                writeInfoFile(infoDump, wroteFolder, folderDump, folderError, wroteSnbt, snbtDump, snbtError);

                if (wroteFolder || wroteSnbt) {
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

        private static void writeInfoFile(Path infoDump, boolean wroteFolder, Path folderDump, String folderError,
                                          boolean wroteSnbt, Path snbtDump, String snbtError) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Arcanum Quest Dumper result\n\n");
            sb.append("Full folder dump: ").append(wroteFolder ? "SUCCESS" : "FAILED").append('\n');
            sb.append("Path: ").append(folderDump.toAbsolutePath()).append('\n');
            if (folderError != null) {
                sb.append("Error: ").append(folderError).append('\n');
            }
            sb.append('\n');
            sb.append("Raw SNBT dump: ").append(wroteSnbt ? "SUCCESS" : "FAILED").append('\n');
            sb.append("Path: ").append(snbtDump.toAbsolutePath()).append('\n');
            if (snbtError != null) {
                sb.append("Error: ").append(snbtError).append('\n');
            }
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
    }
}
