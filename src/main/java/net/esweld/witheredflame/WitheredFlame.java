package net.esweld.witheredflame;

import com.mojang.logging.LogUtils;
import net.esweld.witheredflame.block.ModBlocks;
import net.esweld.witheredflame.particle.ModParticles;
import net.esweld.witheredflame.particle.WitheredEmberParticle;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(WitheredFlame.MOD_ID)
public class WitheredFlame {
    public static final String MOD_ID = "witheredflame";
    private static final Logger LOGGER = LogUtils.getLogger();

    public WitheredFlame(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.WITHERED_STONE_ITEM);
            event.accept(ModBlocks.WITHERED_DIRT_ITEM);
            event.accept(ModBlocks.WITHERED_GRASS_BLOCK_ITEM);
            event.accept(ModBlocks.WITHERED_SAND_ITEM);
            event.accept(ModBlocks.WITHERED_RED_SAND_ITEM);
            event.accept(ModBlocks.WITHERED_LOG_ITEM);
            event.accept(ModBlocks.WITHERED_DEAD_BUSH_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModBlocks.WITHERED_EMBER);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.WITHERED_EMBER.get(), WitheredEmberParticle.Provider::new);
        }
    }
}
