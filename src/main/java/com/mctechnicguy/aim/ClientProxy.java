package com.mctechnicguy.aim;

import com.mctechnicguy.aim.blocks.*;
import com.mctechnicguy.aim.client.render.TileEntityPlayerMonitorRenderer;
import com.mctechnicguy.aim.client.render.TileEntityScannerRenderer;
import com.mctechnicguy.aim.items.*;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketHotbarSlotChanged;
import com.mctechnicguy.aim.network.PacketKeyPressed;
import com.mctechnicguy.aim.network.PacketOpenInfoGUI;
import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import com.mctechnicguy.aim.tileentity.TileEntityScanner;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ClientProxy extends CommonProxy {
	
	public static KeyBinding KeyChangeAccess;

	@Override
	public boolean playerEqualsClient(@Nonnull UUID client) {
		return Minecraft.getMinecraft().player != null && client.equals(Minecraft.getMinecraft().player.getUniqueID());
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScanner.class, new TileEntityScannerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlayerMonitor.class, new TileEntityPlayerMonitorRenderer());
		
		reg(ModElementList.itemAIMInfoProvider, ItemAIMInfoProvider.NAME);
		reg(ModElementList.itemAIMUpgrade, 0, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[0]);
		reg(ModElementList.itemAIMUpgrade, 1, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[1]);
		reg(ModElementList.itemAIMUpgrade, 2, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[2]);
		reg(ModElementList.itemAIMWrench, ItemAIMWrench.NAME);
		reg(ModElementList.itemAIMManual, ItemAIMManual.NAME);
		reg(ModElementList.itemCraftingComponent, 0, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[0]);
		reg(ModElementList.itemCraftingComponent, 1, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[1]);
		reg(ModElementList.itemCraftingComponent, 2, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[2]);
		reg(ModElementList.itemPositionCard, ItemPositionCard.NAME);
		
		reg(ModElementList.blockInventoryRelay, 0, BlockInventoryRelay.NAME);
		reg(ModElementList.blockArmorRelay, 0, BlockArmorRelay.NAME);
		reg(ModElementList.blockEnderChestRelay, 0, BlockEnderChestRelay.NAME);
		reg(ModElementList.blockSlotSelectionRelay, 0, BlockSlotSelectionRelay.NAME);
		reg(ModElementList.blockAIMCore, 0, BlockAIMCore.NAME);
		reg(ModElementList.blockHungerRelay, 0, BlockHungerRelay.NAME);
		reg(ModElementList.blockPotionRelay, 0, BlockPotionRelay.NAME);
		reg(ModElementList.blockXPRelay, 0, BlockXPRelay.NAME);
		reg(ModElementList.blockXPRelayLiquid, 0, BlockXPRelayLiquid.NAME);
		reg(ModElementList.blockEnergyRelay, 0, BlockEnergyRelay.NAME);
		reg(ModElementList.blockScannerBase, 0, BlockScannerBase.NAME);
		reg(ModElementList.blockScanner, 0, BlockScanner.NAME);
		reg(ModElementList.blockNetworkCable, 0, BlockNetworkCable.NAME);
		reg(ModElementList.blockPlayerMonitor, 0, BlockPlayerMonitor.NAME);
		reg(ModElementList.blockGenerator, 0, BlockGenerator.NAME);
		reg(ModElementList.blockSolarGenerator, 0, BlockSolarGenerator.NAME);
		reg(ModElementList.blockToggleCable, 0, BlockToggleCable.NAME);
		reg(ModElementList.blockMotionEditor, 0, BlockMotionEditor.NAME);
		reg(ModElementList.blockHotbarSelectionEditor, 0, BlockHotbarSelectionEditor.NAME);
		reg(ModElementList.blockPositionEditor, 0, BlockPositionEditor.NAME);
		reg(ModElementList.blockNetworkSignalBridge, 0, BlockNetworkSignalBridge.NAME);
		reg(ModElementList.blockShulkerBoxRelay, 0, BlockShulkerBoxRelay.NAME);

		reg(ModElementList.blockMoltenXP, 0, BlockFluidMoltenXP.NAME);
	}

    public void registerFluid(@Nonnull BlockFluidClassic block, String fluidName) {
        ModelBakery.registerItemVariants(Item.getItemFromBlock(block));
        final ModelResourceLocation fluidModel = new ModelResourceLocation(ModInfo.ID + ":fluids", fluidName);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(block), new ItemMeshDefinition() {
            @Nonnull
			@Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return fluidModel;
            }
        });
        ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
            @Nonnull
			@Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return fluidModel;
            }
        });
    }

	private void reg(Item item, String name) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(ModInfo.ID + ":" + name));
	}

	private void reg(Item item, int meta, String name) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ModInfo.ID + ":" + name));
	}
	
	private void reg(@Nonnull Block block, int meta, String name) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(ModInfo.ID + ":" + name));
	}
	
	@Override
	public void registerKeys() {
		KeyChangeAccess = new KeyBinding("key.changeAccess", Keyboard.KEY_Y, "key.categories.aim");
	    ClientRegistry.registerKeyBinding(KeyChangeAccess);
	}
	
	public EntityPlayer getPlayer(@Nonnull MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayer(ctx));
	}
	
	public void addScheduledTask(@Nonnull Runnable run, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(run);
	}

	@Override
	public void registerPackets() {
		PacketHelper.wrapper.registerMessage(PacketOpenInfoGUI.PacketOpenInfoGUIHandler.class, PacketOpenInfoGUI.class, 1, Side.CLIENT);
		PacketHelper.wrapper.registerMessage(PacketKeyPressed.PacketKeyPressedHandler.class, PacketKeyPressed.class, 0, Side.SERVER);
		PacketHelper.wrapper.registerMessage(PacketHotbarSlotChanged.PacketHotbarSlotChangedHandler.class, PacketHotbarSlotChanged.class, 2, Side.CLIENT);
	}
}
