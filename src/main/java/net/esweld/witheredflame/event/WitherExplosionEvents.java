package net.esweld.witheredflame.event;

import net.esweld.witheredflame.WitheredFlame;
import net.esweld.witheredflame.block.ModBlocks;
import net.esweld.witheredflame.block.WitheredFlameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = WitheredFlame.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WitherExplosionEvents {
    @SubscribeEvent
    public static void onWitherExplosion(ExplosionEvent.Detonate event) {
        Entity exploder = event.getExplosion().getExploder();
        if (!(exploder instanceof WitherBoss) && !(exploder instanceof WitherSkull)) {
            return;
        }
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        RandomSource random = server.random;
        List<BlockPos> vanillaBreaks = new ArrayList<>();
        List<BlockPos> withered = new ArrayList<>();
        for (BlockPos pos : event.getAffectedBlocks()) {
            BlockState state = server.getBlockState(pos);
            WitheredFlameBlock.Base base = WitheredFlameBlock.baseOf(state);
            if (base == WitheredFlameBlock.Base.LEAVES) {
                server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                continue;
            }
            if (base != null && base != WitheredFlameBlock.Base.NONE) {
                BlockState witheredState = WitheredFlameBlock.witheredState(
                        ModBlocks.WITHERED_FLAME.get().defaultBlockState().setValue(WitheredFlameBlock.BASE, base));
                server.setBlock(pos, witheredState, 3);
                withered.add(pos.immutable());
                placeAround(server, pos, base);
                continue;
            }
            if (WitheredFlameBlock.isWithered(state)) {
                withered.add(pos.immutable());
                placeAround(server, pos, baseForWithered(state));
                continue;
            }
            vanillaBreaks.add(pos);
        }
        event.getAffectedBlocks().clear();
        event.getAffectedBlocks().addAll(vanillaBreaks);

        BlockPos center = BlockPos.containing(event.getExplosion().getPosition());
        for (int i = 0; i < 24; i++) {
            BlockPos spot = center.offset(random.nextInt(7) - 3, random.nextInt(5) - 1, random.nextInt(7) - 3);
            BlockPos ground = spot.below();
            BlockState groundState = server.getBlockState(ground);
            WitheredFlameBlock.Base base = WitheredFlameBlock.baseOf(groundState);
            if (WitheredFlameBlock.isWithered(groundState)) {
                placeFlame(server, spot, baseForWithered(groundState), Direction.DOWN);
            } else if (base != null && base != WitheredFlameBlock.Base.NONE && base != WitheredFlameBlock.Base.LEAVES) {
                server.setBlock(ground, WitheredFlameBlock.witheredState(
                        ModBlocks.WITHERED_FLAME.get().defaultBlockState().setValue(WitheredFlameBlock.BASE, base)), 3);
                placeFlame(server, spot, base, Direction.DOWN);
            }
        }
        for (BlockPos pos : withered) {
            if (random.nextFloat() < 0.65F) {
                placeFlame(server, pos.above(), baseForWithered(server.getBlockState(pos)), Direction.DOWN);
            }
        }
    }

    private static void placeAround(ServerLevel level, BlockPos witheredPos, WitheredFlameBlock.Base base) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            placeFlame(level, witheredPos.relative(direction), base, direction.getOpposite());
        }
    }

    private static void placeFlame(ServerLevel level, BlockPos flamePos, WitheredFlameBlock.Base base, Direction facingFuel) {
        BlockState destination = level.getBlockState(flamePos);
        if (!destination.isAir() && !destination.canBeReplaced()) {
            return;
        }
        if (base == null || base == WitheredFlameBlock.Base.NONE || base == WitheredFlameBlock.Base.LEAVES) {
            base = WitheredFlameBlock.Base.DIRT;
        }
        BlockState flame = ModBlocks.WITHERED_FLAME.get().defaultBlockState()
                .setValue(WitheredFlameBlock.BASE, base)
                .setValue(WitheredFlameBlock.FACING, facingFuel);
        level.setBlock(flamePos, flame, 3);
        level.scheduleTick(flamePos, ModBlocks.WITHERED_FLAME.get(), WitheredFlameBlock.ACTION_INTERVAL);
    }

    private static WitheredFlameBlock.Base baseForWithered(BlockState state) {
        if (state.is(ModBlocks.WITHERED_GRASS_BLOCK.get())) return WitheredFlameBlock.Base.GRASS;
        if (state.is(ModBlocks.WITHERED_DIRT.get())) return WitheredFlameBlock.Base.DIRT;
        if (state.is(ModBlocks.WITHERED_STONE.get())) return WitheredFlameBlock.Base.STONE;
        if (state.is(ModBlocks.WITHERED_SAND.get())) return WitheredFlameBlock.Base.SAND;
        if (state.is(ModBlocks.WITHERED_RED_SAND.get())) return WitheredFlameBlock.Base.RED_SAND;
        if (state.is(ModBlocks.WITHERED_LOG.get())) return WitheredFlameBlock.Base.LOG;
        if (state.is(ModBlocks.WITHERED_DEAD_BUSH.get())) return WitheredFlameBlock.Base.DEAD_BUSH;
        return WitheredFlameBlock.Base.DIRT;
    }
}
