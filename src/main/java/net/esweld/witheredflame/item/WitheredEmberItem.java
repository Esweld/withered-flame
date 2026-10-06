package net.esweld.witheredflame.item;

import net.esweld.witheredflame.block.ModBlocks;
import net.esweld.witheredflame.block.WitheredFlameBlock;
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
        BlockState state = level.getBlockState(context.getClickedPos());
        WitheredFlameBlock.Base base = WitheredFlameBlock.baseOf(state);
        if (base == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockState flame = ModBlocks.PERSISTENT_WITHERED_FLAME.get().defaultBlockState()
                    .setValue(WitheredFlameBlock.BASE, base);
            level.setBlock(context.getClickedPos(), flame, 3);
            level.scheduleTick(context.getClickedPos(), ModBlocks.PERSISTENT_WITHERED_FLAME.get(), 1);
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(),
                    player -> player.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}