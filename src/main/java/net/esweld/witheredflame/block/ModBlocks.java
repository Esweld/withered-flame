package net.esweld.witheredflame.block;

import net.esweld.witheredflame.WitheredFlame;
import net.esweld.witheredflame.item.WitheredEmberItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
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
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_DIRT = BLOCKS.register("withered_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_GRASS_BLOCK = BLOCKS.register("withered_grass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_SAND = BLOCKS.register("withered_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_RED_SAND = BLOCKS.register("withered_red_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_SAND).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_LOG = BLOCKS.register("withered_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_GRAY).strength(0.15F, 0.05F)));
    public static final RegistryObject<Block> WITHERED_DEAD_BUSH = BLOCKS.register("withered_dead_bush",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH).mapColor(MapColor.COLOR_GRAY).noOcclusion().noCollission()));

    public static final RegistryObject<Block> WITHERED_FLAME = BLOCKS.register("withered_flame",
            () -> new WitheredFlameBlock());

    public static final RegistryObject<Item> WITHERED_STONE_ITEM = ITEMS.register("withered_stone",
            () -> new BlockItem(WITHERED_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_DIRT_ITEM = ITEMS.register("withered_dirt",
            () -> new BlockItem(WITHERED_DIRT.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_GRASS_BLOCK_ITEM = ITEMS.register("withered_grass_block",
            () -> new BlockItem(WITHERED_GRASS_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_SAND_ITEM = ITEMS.register("withered_sand",
            () -> new BlockItem(WITHERED_SAND.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_RED_SAND_ITEM = ITEMS.register("withered_red_sand",
            () -> new BlockItem(WITHERED_RED_SAND.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_LOG_ITEM = ITEMS.register("withered_log",
            () -> new BlockItem(WITHERED_LOG.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_DEAD_BUSH_ITEM = ITEMS.register("withered_dead_bush",
            () -> new BlockItem(WITHERED_DEAD_BUSH.get(), new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_EMBER = ITEMS.register("withered_ember", WitheredEmberItem::new);
}