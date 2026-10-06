package net.esweld.witheredflame.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WitheredFlameBlock extends Block {
    public static final EnumProperty<Base> BASE = EnumProperty.create("base", Base.class);
    private static final Direction[] DIRS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private static final int MAX_UPDATES = 100;
    private static long capTick = Long.MIN_VALUE;
    private static int updates;

    private final boolean persistent;

    public WitheredFlameBlock(boolean persistent) {
        super(BlockBehaviour.Properties.of()
                .mapColor(persistent ? MapColor.COLOR_PURPLE : MapColor.COLOR_ORANGE)
                .replaceable()
                .noCollission()
                .instabreak()
                .lightLevel(state -> persistent ? 10 : 15)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        this.persistent = persistent;
        this.registerDefaultState(this.stateDefinition.any().setValue(BASE, Base.DIRT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE);
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
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            level.setBlock(pos, witheredState(state), 3);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER)) {
            level.setBlock(pos, witheredState(state), 3);
            return;
        }
        if (!allowUpdate(level)) {
            level.scheduleTick(pos, this, 1);
            return;
        }
        if (persistent) {
            tickPersistent(state, level, pos, random);
        } else {
            tickNormal(state, level, pos, random);
        }
    }

    private void tickNormal(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double roll = random.nextDouble() * 100.0;
        if (roll < 55.0) {
            spread(level, pos, random);
            level.scheduleTick(pos, this, waitTicks(40.0));
        } else if (roll < 93.0) {
            level.setBlock(pos, witheredState(state), 3);
        } else {
            BlockState next = ModBlocks.PERSISTENT_WITHERED_FLAME.get().defaultBlockState()
                    .setValue(BASE, state.getValue(BASE));
            level.setBlock(pos, next, 3);
            level.scheduleTick(pos, ModBlocks.PERSISTENT_WITHERED_FLAME.get(), waitTicks(20.0));
        }
    }

    private void tickPersistent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextDouble() * 100.0 < 20.0) {
            boolean spread = spread(level, pos, random);
            level.scheduleTick(pos, this, spread ? waitTicks(20.0) : waitTicks(20.0) * 5);
        } else {
            level.scheduleTick(pos, this, waitTicks(20.0));
        }
    }

    private boolean spread(ServerLevel level, BlockPos origin, RandomSource random) {
        BlockPos first = origin.relative(DIRS[random.nextInt(DIRS.length)]);
        if (tryIgnite(level, first)) {
            return true;
        }
        BlockPos second = first.relative(DIRS[random.nextInt(DIRS.length)]);
        return tryIgnite(level, second);
    }

    private boolean tryIgnite(ServerLevel level, BlockPos pos) {
        Base base = baseOf(level.getBlockState(pos));
        if (base == null) {
            return false;
        }
        BlockState flame = ModBlocks.WITHERED_FLAME.get().defaultBlockState().setValue(BASE, base);
        level.setBlock(pos, flame, 3);
        level.scheduleTick(pos, ModBlocks.WITHERED_FLAME.get(), 1);
        return true;
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

    private static int waitTicks(double chance) {
        if (chance <= 0.0) return 100;
        return Math.max(1, (int) Math.ceil(100.0 / chance));
    }

    public enum Base implements StringRepresentable {
        GRASS("grass"),
        DIRT("dirt"),
        STONE("stone"),
        SAND("sand"),
        RED_SAND("red_sand"),
        DEAD_BUSH("dead_bush"),
        LOG("log");

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