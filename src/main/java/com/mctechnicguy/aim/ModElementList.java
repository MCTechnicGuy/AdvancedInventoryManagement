package com.mctechnicguy.aim;

import com.mctechnicguy.aim.blocks.*;
import com.mctechnicguy.aim.gui.GuiAIMGuide;
import com.mctechnicguy.aim.gui.ICustomManualEntry;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.*;
import com.mctechnicguy.aim.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ModElementList {

	//Items
	public static ItemAIMUpgrade itemAIMUpgrade;
	public static ItemAIMWrench itemAIMWrench;
	public static ItemAIMInfoProvider itemAIMInfoProvider;
	public static ItemAIMManual itemAIMManual;
	public static ItemCraftingComponent itemCraftingComponent;
	public static ItemPositionCard itemPositionCard;
	
	//Blocks
	public static BlockAIMCore blockAIMCore;
	public static BlockNetworkCable blockNetworkCable;
	public static BlockInventoryRelay blockInventoryRelay;
	public static BlockArmorRelay blockArmorRelay;
    public static BlockEnderChestRelay blockEnderChestRelay;
	public static BlockSlotSelectionRelay blockSlotSelectionRelay;
	public static BlockHungerRelay blockHungerRelay;
	public static BlockPotionRelay blockPotionRelay;
	public static BlockXPRelay blockXPRelay;
	public static BlockXPRelayLiquid blockXPRelayLiquid;
	public static BlockEnergyRelay blockEnergyRelay;
	public static BlockScanner blockScanner;
	public static BlockScannerBase blockScannerBase;
	public static BlockPlayerMonitor blockPlayerMonitor;
	public static BlockGenerator blockGenerator;
	public static BlockSolarGenerator blockSolarGenerator;
	public static BlockToggleCable blockToggleCable;
	public static BlockMotionEditor blockMotionEditor;
	public static BlockHotbarSelectionEditor blockHotbarSelectionEditor;
	public static BlockPositionEditor blockPositionEditor;
	public static BlockNetworkSignalBridge blockNetworkSignalBridge;
	public static BlockShulkerBoxRelay blockShulkerBoxRelay;

	//Fluids
	@Nonnull
    public static Fluid fluidMoltenXP = new Fluid("fluid_moltenxp", new ResourceLocation(ModInfo.ID + ":blocks/moltenxp_still"), new ResourceLocation(ModInfo.ID + ":blocks/moltenxp_flow"));
    public static BlockFluidClassic blockMoltenXP;
	
	static void createBlocks() {
		blockAIMCore = new BlockAIMCore();
		blockNetworkCable = new BlockNetworkCable();
		blockInventoryRelay = new BlockInventoryRelay();
		blockArmorRelay = new BlockArmorRelay();
        blockEnderChestRelay = new BlockEnderChestRelay();
		blockSlotSelectionRelay = new BlockSlotSelectionRelay();
		blockHungerRelay = new BlockHungerRelay();
		blockPotionRelay = new BlockPotionRelay();
		blockXPRelay = new BlockXPRelay();
		blockXPRelayLiquid = new BlockXPRelayLiquid();
		blockEnergyRelay = new BlockEnergyRelay();
		blockScanner = new BlockScanner();
		blockScannerBase = new BlockScannerBase();
		blockPlayerMonitor = new BlockPlayerMonitor();
		blockGenerator = new BlockGenerator();
		blockSolarGenerator = new BlockSolarGenerator();
		blockToggleCable = new BlockToggleCable();
		blockMotionEditor = new BlockMotionEditor();
		blockHotbarSelectionEditor = new BlockHotbarSelectionEditor();
		blockPositionEditor = new BlockPositionEditor();
	    blockNetworkSignalBridge = new BlockNetworkSignalBridge();
	    blockShulkerBoxRelay = new BlockShulkerBoxRelay();
	}

	static void createFluids() {
        fluidMoltenXP.setViscosity(3000);
        fluidMoltenXP.setLuminosity(5);
        FluidRegistry.registerFluid(fluidMoltenXP);
        FluidRegistry.addBucketForFluid(fluidMoltenXP);
        blockMoltenXP = new BlockFluidMoltenXP(fluidMoltenXP);
	}

	static void createItems() {
		itemAIMUpgrade = new ItemAIMUpgrade();
		itemAIMWrench = new ItemAIMWrench();
		itemAIMInfoProvider = new ItemAIMInfoProvider();
		itemAIMManual = new ItemAIMManual();
		itemCraftingComponent = new ItemCraftingComponent();
		itemPositionCard = new ItemPositionCard();
	}

	@Mod.EventBusSubscriber(modid = ModInfo.ID)
	private static class RegistryHandler {

        static final Block[] blocks = {
                blockAIMCore, blockNetworkCable, blockInventoryRelay, blockEnderChestRelay, blockArmorRelay, blockSlotSelectionRelay,
                blockHungerRelay, blockPotionRelay, blockXPRelay, blockXPRelayLiquid, blockScanner, blockEnergyRelay, blockScannerBase,
                blockPlayerMonitor, blockGenerator, blockSolarGenerator, blockToggleCable, blockMotionEditor, blockHotbarSelectionEditor, blockPositionEditor, blockMoltenXP, blockNetworkSignalBridge,
                blockShulkerBoxRelay
        };

        static final Item[] items = {
                itemAIMWrench, itemAIMUpgrade, itemAIMInfoProvider, itemAIMManual, itemCraftingComponent, itemPositionCard
        };


        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
        	IForgeRegistry<IRecipe> reg = event.getRegistry();
        	if (AdvancedInventoryManagement.isClient) {
		        for (Block block : blocks) {
                    if (block instanceof IManualEntry)
                        registerRecipesForGuide((IManualEntry)block, reg, block.getRegistryName());
		        }
                for (Item item : items) {
                    if (item instanceof IManualEntry)
                        registerRecipesForGuide((IManualEntry)item, reg, item.getRegistryName());
                }
	        }
        }

        private static void registerRecipesForGuide(IManualEntry entry, IForgeRegistry<IRecipe> reg, ResourceLocation name) {
            ArrayList<IRecipe> recipes = new ArrayList<>();
            if (entry instanceof ICustomManualEntry) {
                for (int page = 0; page < entry.getPageCount(); page++) {
                    if (((ICustomManualEntry)entry).getRecipeForPage(page) == null) {
                        recipes.add(null);
                    } else {
                        recipes.add(reg.getValue(((ICustomManualEntry)entry).getRecipeForPage(page)));
                    }
                }
            } else {
                recipes.add(reg.getValue(name));
            }
            GuiAIMGuide.contentRecipes.put(entry, recipes);
        }

		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event) {
			IForgeRegistry<Block> reg = event.getRegistry();
			reg.registerAll(blocks);
			for (Block b : blocks) {
                if (AdvancedInventoryManagement.isClient && b instanceof IManualEntry) {
                    GuiAIMGuide.content.add((IManualEntry)b);
                }
            }
		}

		@SubscribeEvent
        public static void registerItemBlocks(final RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> reg = event.getRegistry();
            for (Block b : blocks) {
               reg.register(new ItemBlock(b).setUnlocalizedName(b.getUnlocalizedName()).setRegistryName(b.getRegistryName()));
            }

            registerTileEntities();

        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> reg = event.getRegistry();
            reg.registerAll(items);
            for (Item i : items) {
                if (AdvancedInventoryManagement.isClient && i instanceof IManualEntry) {
                    GuiAIMGuide.content.add((IManualEntry)i);
                }
            }
	        OreDictionary.registerOre("oreWrench", itemAIMWrench);
        }

        @SubscribeEvent
        public static void onModelRegister(ModelRegistryEvent event) {
            AdvancedInventoryManagement.proxy.registerRenderers();
        }

        private static void registerTileEntities() {
            GameRegistry.registerTileEntity(TileEntityAIMCore.class, new ResourceLocation(ModInfo.ID, "AIMCore"));
            GameRegistry.registerTileEntity(TileEntityNetworkCable.class, new ResourceLocation(ModInfo.ID, "AIMNetworkCable"));
            GameRegistry.registerTileEntity(TileEntityInventoryRelay.class, new ResourceLocation(ModInfo.ID, "AIMInventoryRelay"));
            GameRegistry.registerTileEntity(TileEntityEnderChestRelay.class, new ResourceLocation(ModInfo.ID, "AIMEnderChestRelay"));
            GameRegistry.registerTileEntity(TileEntityArmorRelay.class, new ResourceLocation(ModInfo.ID, "AIMArmorRelay"));
            GameRegistry.registerTileEntity(TileEntitySlotSelectionRelay.class, new ResourceLocation(ModInfo.ID, "AIMSlotSelectionRelay"));
            GameRegistry.registerTileEntity(TileEntityHungerRelay.class, new ResourceLocation(ModInfo.ID, "AIMHungerRelay"));
            GameRegistry.registerTileEntity(TileEntityPotionRelay.class, new ResourceLocation(ModInfo.ID, "AIMPotionRelay"));
            GameRegistry.registerTileEntity(TileEntityXPRelay.class, new ResourceLocation(ModInfo.ID, "AIMXPRelay"));
            GameRegistry.registerTileEntity(TileEntityXPRelayLiquid.class, new ResourceLocation(ModInfo.ID, "AIMXPRelayLiquid"));
            GameRegistry.registerTileEntity(TileEntityEnergyRelay.class, new ResourceLocation(ModInfo.ID, "AIMEnergyRelay"));
            GameRegistry.registerTileEntity(TileEntityScanner.class, new ResourceLocation(ModInfo.ID, "AIMScanner"));
            GameRegistry.registerTileEntity(TileEntityPlayerMonitor.class, new ResourceLocation(ModInfo.ID, "AIMPlayerMonitor"));
            GameRegistry.registerTileEntity(TileEntityGenerator.class, new ResourceLocation(ModInfo.ID, "AIMGenerator"));
            GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, new ResourceLocation(ModInfo.ID, "AIMSolarGenerator"));
            GameRegistry.registerTileEntity(TileEntityToggleCable.class, new ResourceLocation(ModInfo.ID, "AIMToggleCable"));
            GameRegistry.registerTileEntity(TileEntityMotionEditor.class, new ResourceLocation(ModInfo.ID, "AIMMotionEditor"));
            GameRegistry.registerTileEntity(TileEntityHotbarSelectionEditor.class, new ResourceLocation(ModInfo.ID, "AIMHotbarSelectionEditor"));
            GameRegistry.registerTileEntity(TileEntityPositionEditor.class, new ResourceLocation(ModInfo.ID, "AIMPositionEditor"));
            GameRegistry.registerTileEntity(TileEntityNetworkSignalBridge.class, new ResourceLocation(ModInfo.ID, "AIMNetworkSignalBridge"));
            GameRegistry.registerTileEntity(TileEntityShulkerBoxRelay.class, new ResourceLocation(ModInfo.ID, "AIMShulkerBoxRelay"));
        }
	}
}
