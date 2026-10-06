package net.esweld.witheredflame.block;

import net.esweld.witheredflame.WitheredFlame;
import net.esweld.witheredflame.item.WitheredEmberItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WitheredFlame.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WitheredFlame.MOD_ID);

    public static final RegistryObject<Block> WITHERED_STONE = BLOCKS.register("withered_stone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_DIRT = BLOCKS.register("withered_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_GRASS_BLOCK = BLOCKS.register("withered_grass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.COLOR_GRAY).randomTicks()));//.setId()
    public static final RegistryObject<Block> WITHERED_SAND = BLOCKS.register("withered_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_RED_SAND = BLOCKS.register("withered_red_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_SAND).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_LOG = BLOCKS.register("withered_stripped_oak_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_DEAD_BUSH = BLOCKS.register("withered_dead_bush",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH).mapColor(MapColor.COLOR_GRAY).noOcclusion().sound(SoundType.GRASS)));

    public static final RegistryObject<Block> WITHERED_FLAME = BLOCKS.register("withered_flame",
            () -> new WitheredFlameBlock(false));
    public static final RegistryObject<Block> PERSISTENT_WITHERED_FLAME = BLOCKS.register("persistent_withered_flame",
            () -> new WitheredFlameBlock(true));

    public static final RegistryObject<Item> WITHERED_STONE_ITEM = blockItem(WITHERED_STONE);
    public static final RegistryObject<Item> WITHERED_DIRT_ITEM = blockItem(WITHERED_DIRT);
    public static final RegistryObject<Item> WITHERED_GRASS_BLOCK_ITEM = blockItem(WITHERED_GRASS_BLOCK);
    public static final RegistryObject<Item> WITHERED_SAND_ITEM = blockItem(WITHERED_SAND);
    public static final RegistryObject<Item> WITHERED_RED_SAND_ITEM = blockItem(WITHERED_RED_SAND);
    public static final RegistryObject<Item> WITHERED_LOG_ITEM = blockItem(WITHERED_LOG);
    public static final RegistryObject<Item> WITHERED_DEAD_BUSH_ITEM = blockItem(WITHERED_DEAD_BUSH);
    public static final RegistryObject<Item> WITHERED_EMBER = ITEMS.register("withered_ember", WitheredEmberItem::new);

    private static RegistryObject<Item> blockItem(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }
}