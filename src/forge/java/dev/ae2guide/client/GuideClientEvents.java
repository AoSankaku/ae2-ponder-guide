package dev.ae2guide.client;

import dev.ae2guide.GuideConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod.EventBusSubscriber(modid = GuideConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuideClientEvents {
    private GuideClientEvents() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        GuideClient.setup();
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.addListener(GuideClientEvents::tick);
            MinecraftForge.EVENT_BUS.addListener(GuideClientEvents::screenInit);
            MinecraftForge.EVENT_BUS.addListener(GuideClientEvents::registerCommands);
        });
    }

    private static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) GuideClient.tick();
    }

    private static void screenInit(ScreenEvent.Init.Post event) {
        var button = GuideClient.guideButton(event.getScreen());
        if (button != null) event.addListener(button);
    }

    private static void registerCommands(RegisterClientCommandsEvent event) {
        GuideCommands.register(event.getDispatcher());
    }
}
