package dev.ae2guide.client;

import dev.ae2guide.GuideConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = GuideConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuideClientEvents {
    private GuideClientEvents() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        GuideClient.setup();
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(GuideClientEvents::tick);
            NeoForge.EVENT_BUS.addListener(GuideClientEvents::screenInit);
            NeoForge.EVENT_BUS.addListener(GuideClientEvents::registerCommands);
        });
    }

    private static void tick(ClientTickEvent.Post event) { GuideClient.tick(); }

    private static void screenInit(ScreenEvent.Init.Post event) {
        var button = GuideClient.guideButton(event.getScreen());
        if (button != null) event.addListener(button);
    }

    private static void registerCommands(RegisterClientCommandsEvent event) {
        GuideCommands.register(event.getDispatcher());
    }
}
