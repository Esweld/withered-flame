package net.esweld.witheredflame.item;

import net.esweld.witheredflame.block.ModBlocks;
import net.esweld.witheredflame.block.WitheredFlameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WitheredEmberItem extends Item {
    public WitheredEmberItem() {
        super(new Properties().durability(64));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos flamePos = clicked.relative(face);
        BlockState destination = level.getBlockState(flamePos);
        if (!destination.isAir() && !destination.canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        WitheredFlameBlock.Base base = WitheredFlameBlock.baseOf(level.getBlockState(clicked));
        if (base == null) {
            base = WitheredFlameBlock.Base.NONE;
        }
        if (!level.isClientSide) {
            BlockState flame = ModBlocks.WITHERED_FLAME.get().defaultBlockState()
                    .setValue(WitheredFlameBlock.BASE, base)
                    .setValue(WitheredFlameBlock.FACING, face.getOpposite());
            level.setBlock(flamePos, flame, 3);
            level.scheduleTick(flamePos, ModBlocks.WITHERED_FLAME.get(), 5);
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(),
                    player -> player.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
