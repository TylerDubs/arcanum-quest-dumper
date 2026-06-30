package com.nirmatacipher.questdumper;

import com.nirmatacipher.questdumper.command.DumpCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(QuestDumper.MOD_ID)
public class QuestDumper {
    public static final String MOD_ID = "questdumper";
    public static final String VERSION = "2.1.0";

    public QuestDumper() {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            DumpCommand.register(event.getDispatcher());
        }
    }
}
