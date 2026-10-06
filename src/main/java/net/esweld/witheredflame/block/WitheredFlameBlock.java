package net.esweld.witheredflame.block;

import net.esweld.witheredflame.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.phys.Vec3;

public class WitheredFlameBlock extends Block {
    public static final EnumProperty<Base> BASE = EnumProperty.create("base", Base.class);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final int ACTION_INTERVAL = 5;
    private static final int MAX_UPDATES = 100;
    private static long capTick = Long.MIN_VALUE;
    private static int updates;

    public WitheredFlameBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .replaceable()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 15)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BASE, Base.NONE)
                .setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE, FACING);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !entity.isAlive()) {
            return;
        }
        if (entity instanceof WitherBoss wither) {
            if (level.getGameTime() % 10 == 0) {
                wither.heal(2.0F);
            }
            return;
        }
        if (entity instanceof WitherSkeleton skeleton) {
            if (level.getGameTime() % 10 == 0) {
                skeleton.heal(1.0F);
            }
            return;
        }
        entity.setSecondsOnFire(3);
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true));
        }
        if (entity instanceof PathfinderMob mob && level.getGameTime() % 10 == 0) {
            Vec3 away = entity.position().subtract(Vec3.atCenterOf(pos));
            if (away.lengthSqr() < 0.01) {
                away = new Vec3(level.random.nextDouble() - 0.5, 0.0, level.random.nextDouble() - 0.5);
            }
            away = away.normalize().scale(8.0);
            mob.getNavigation().moveTo(entity.getX() + away.x, entity.getY(), entity.getZ() + away.z, 1.45);
            mob.setTarget(null);
        }
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !oldState.is(this)) {
            level.scheduleTick(pos, this, ACTION_INTERVAL);
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double y = pos.getY() + 0.2 + random.nextDouble() * 0.6;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        level.addParticle(ModParticles.WITHERED_EMBER.get(), x, y, z, 0.0, 0.03, 0.0);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) || !hasSupport(level, pos, state)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (!allowUpdate(level)) {
            level.scheduleTick(pos, this, 1);
            return;
        }
        if (isOnWitheredBlock(level, pos, state)) {
            tickPersistent(level, pos, random);
        } else {
            tickNormal(state, level, pos, random);
        }
    }

    private void tickNormal(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Base base = state.getValue(BASE);
        double roll = random.nextDouble() * 100.0;
        double spreadChance = spreadChance(base);
        if (roll < spreadChance) {
            spread(level, pos, random);
            level.scheduleTick(pos, this, ACTION_INTERVAL);
            return;
        }
        if (base == Base.LEAVES) {
            clearFuel(level, pos, state);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (roll < spreadChance + 38.0) {
            witherFuel(level, pos, state);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            witherFuel(level, pos, state);
            level.scheduleTick(pos, this, ACTION_INTERVAL);
        }
    }

    private double spreadChance(Base base) {
        return switch (base) {
            case LEAVES -> 90.0;
            case LOG -> 75.0;
            default -> 55.0;
        };
    }

    private void tickPersistent(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextDouble() * 100.0 < 20.0) {
            boolean spread = spread(level, pos, random);
            level.scheduleTick(pos, this, spread ? ACTION_INTERVAL : ACTION_INTERVAL * 5);
        } else {
            level.scheduleTick(pos, this, ACTION_INTERVAL);
        }
    }

    private boolean spread(ServerLevel level, BlockPos origin, RandomSource random) {
        Direction[] dirs = Direction.values();
        BlockPos first = origin.relative(dirs[random.nextInt(dirs.length)]);
        if (tryIgniteNear(level, first)) {
            return true;
        }
        BlockPos second = first.relative(dirs[random.nextInt(dirs.length)]);
        return tryIgniteNear(level, second);
    }

    private boolean tryIgniteNear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.canBeReplaced()) {
            return placeOnNearbyFuel(level, pos);
        }
        Base base = baseOf(state);
        if (base == null) {
            return false;
        }
        for (Direction dir : Direction.values()) {
            BlockPos air = pos.relative(dir);
            BlockState airState = level.getBlockState(air);
            if ((airState.isAir() || airState.canBeReplaced()) && placeFlame(level, air, dir.getOpposite(), base)) {
                return true;
            }
        }
        return false;
    }

    private boolean placeOnNearbyFuel(ServerLevel level, BlockPos airPos) {
        for (Direction dir : Direction.values()) {
            Base base = baseOf(level.getBlockState(airPos.relative(dir)));
            if (base != null && placeFlame(level, airPos, dir, base)) {
                return true;
            }
        }
        return false;
    }

    private boolean placeFlame(ServerLevel level, BlockPos pos, Direction facingFuel, Base base) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.canBeReplaced()) {
            return false;
        }
        BlockState flame = ModBlocks.WITHERED_FLAME.get().defaultBlockState()
                .setValue(BASE, base)
                .setValue(FACING, facingFuel);
        level.setBlock(pos, flame, 3);
        level.scheduleTick(pos, ModBlocks.WITHERED_FLAME.get(), ACTION_INTERVAL);
        return true;
    }

    private boolean hasSupport(ServerLevel level, BlockPos flamePos, BlockState flame) {
        BlockState support = level.getBlockState(flamePos.relative(flame.getValue(FACING)));
        return support.isFaceSturdy(level, flamePos.relative(flame.getValue(FACING)), flame.getValue(FACING).getOpposite())
                || baseOf(support) != null
                || isWithered(support);
    }

    private boolean isOnWitheredBlock(ServerLevel level, BlockPos flamePos, BlockState flame) {
        return isWithered(level.getBlockState(flamePos.relative(flame.getValue(FACING))));
    }

    private void clearFuel(ServerLevel level, BlockPos flamePos, BlockState flame) {
        BlockPos fuelPos = flamePos.relative(flame.getValue(FACING));
        if (baseOf(level.getBlockState(fuelPos)) == Base.LEAVES) {
            level.setBlock(fuelPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private void witherFuel(ServerLevel level, BlockPos flamePos, BlockState flame) {
        if (flame.getValue(BASE) == Base.NONE || flame.getValue(BASE) == Base.LEAVES) {
            return;
        }
        BlockPos fuelPos = flamePos.relative(flame.getValue(FACING));
        if (baseOf(level.getBlockState(fuelPos)) == flame.getValue(BASE)) {
            level.setBlock(fuelPos, witheredState(flame), 3);
        }
    }

    public static boolean isWithered(BlockState state) {
        Block block = state.getBlock();
        return block == ModBlocks.WITHERED_STONE.get()
                || block == ModBlocks.WITHERED_DIRT.get()
                || block == ModBlocks.WITHERED_GRASS_BLOCK.get()
                || block == ModBlocks.WITHERED_SAND.get()
                || block == ModBlocks.WITHERED_RED_SAND.get()
                || block == ModBlocks.WITHERED_LOG.get()
                || block == ModBlocks.WITHERED_DEAD_BUSH.get();
    }

    public static BlockState witheredState(BlockState flame) {
        return switch (flame.getValue(BASE)) {
            case GRASS -> ModBlocks.WITHERED_GRASS_BLOCK.get().defaultBlockState();
            case DIRT -> ModBlocks.WITHERED_DIRT.get().defaultBlockState();
            case STONE -> ModBlocks.WITHERED_STONE.get().defaultBlockState();
            case SAND -> ModBlocks.WITHERED_SAND.get().defaultBlockState();
            case RED_SAND -> ModBlocks.WITHERED_RED_SAND.get().defaultBlockState();
            case DEAD_BUSH -> ModBlocks.WITHERED_DEAD_BUSH.get().defaultBlockState();
            case LOG -> ModBlocks.WITHERED_LOG.get().defaultBlockState();
            case LEAVES, NONE -> Blocks.AIR.defaultBlockState();
        };
    }

    public static Base baseOf(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.GRASS_BLOCK) return Base.GRASS;
        if (block == Blocks.DIRT) return Base.DIRT;
        if (block == Blocks.STONE) return Base.STONE;
        if (block == Blocks.SAND) return Base.SAND;
        if (block == Blocks.RED_SAND) return Base.RED_SAND;
        if (block == Blocks.DEAD_BUSH) return Base.DEAD_BUSH;
        if (state.is(BlockTags.LEAVES)) return Base.LEAVES;
        if (state.is(BlockTags.LOGS)) return Base.LOG;
        return null;
    }

    private static boolean allowUpdate(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime != capTick) {
            capTick = gameTime;
            updates = 0;
        }
        if (updates >= MAX_UPDATES) {
            return false;
        }
        updates++;
        return true;
    }

    public enum Base implements StringRepresentable {
        NONE("none"),
        GRASS("grass"),
        DIRT("dirt"),
        STONE("stone"),
        SAND("sand"),
        RED_SAND("red_sand"),
        DEAD_BUSH("dead_bush"),
        LOG("log"),
        LEAVES("leaves");

        private final String name;

        Base(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
