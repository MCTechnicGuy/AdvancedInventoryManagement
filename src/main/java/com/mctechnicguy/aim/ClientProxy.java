package com.mctechnicguy.aim;

import com.mctechnicguy.aim.client.render.TileEntityPlayerMonitorRenderer;
import com.mctechnicguy.aim.client.render.TileEntityScannerRenderer;
import com.mctechnicguy.aim.gui.GuiAIMCore;
import com.mctechnicguy.aim.gui.GuiAIMGuide;
import com.mctechnicguy.aim.gui.GuiLoadingScreen;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMUpgrade;
import com.mctechnicguy.aim.items.ItemCraftingComponent;
import com.mctechnicguy.aim.network.*;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import com.mctechnicguy.aim.tileentity.TileEntityScanner;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientProxy extends CommonProxy {
	
	public static KeyBinding KeyChangeAccess;

	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScanner.class, new TileEntityScannerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlayerMonitor.class, new TileEntityPlayerMonitorRenderer());
		
		reg(ModElementList.itemAIMInfoProvider);
		reg(ModElementList.itemAIMUpgrade, 0, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[0]);
		reg(ModElementList.itemAIMUpgrade, 1, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[1]);
		reg(ModElementList.itemAIMUpgrade, 2, ItemAIMUpgrade.NAME + "_" + ItemAIMUpgrade.names[2]);
		reg(ModElementList.itemAIMWrench);
		reg(ModElementList.itemAIMManual);
		reg(ModElementList.itemCraftingComponent, 0, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[0]);
		reg(ModElementList.itemCraftingComponent, 1, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[1]);
		reg(ModElementList.itemCraftingComponent, 2, ItemCraftingComponent.NAME + "_" + ItemCraftingComponent.names[2]);
		reg(ModElementList.itemPositionCard);

        reg(ModElementList.blockInventoryRelay);
		reg(ModElementList.blockArmorRelay);
		reg(ModElementList.blockEnderChestRelay);
		reg(ModElementList.blockSlotSelectionRelay);
		reg(ModElementList.blockAIMCore);
		reg(ModElementList.blockHungerRelay);
		reg(ModElementList.blockPotionRelay);
		reg(ModElementList.blockXPRelay);
		reg(ModElementList.blockXPRelayLiquid);
		reg(ModElementList.blockEnergyRelay);
		reg(ModElementList.blockScannerBase);
		reg(ModElementList.blockScanner);
		reg(ModElementList.blockNetworkCable);
		reg(ModElementList.blockPlayerMonitor);
		reg(ModElementList.blockGenerator);
		reg(ModElementList.blockSolarGenerator);
		reg(ModElementList.blockToggleCable);
		reg(ModElementList.blockMotionEditor);
		reg(ModElementList.blockHotbarSelectionEditor);
		reg(ModElementList.blockPositionEditor);
		reg(ModElementList.blockNetworkSignalBridge);
		reg(ModElementList.blockShulkerBoxRelay);

		reg(ModElementList.blockMoltenXP);
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

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, @Nonnull EntityPlayer player, @Nonnull World world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
        if (entity != null) {
            switch (ID) {
                case AdvancedInventoryManagement.guiIDCore:
                    if (entity instanceof TileEntityAIMCore) {
                        return new GuiAIMCore(player.inventory, (TileEntityAIMCore) entity);
                    }
            }
        }

        return null;

    }

	private void reg(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(ModInfo.ID + ":" + item.getRegistryName().getResourcePath()));
	}

	private void reg(Item item, int meta, String name) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ModInfo.ID + ":" + name));
	}
	
	private void reg(@Nonnull Block block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(ModInfo.ID + ":" + block.getRegistryName().getResourcePath()));
	}
	
	@Override
	public void registerKeys() {
		KeyChangeAccess = new KeyBinding("key.changeAccess", Keyboard.KEY_Y, "key.categories.aim");
	    ClientRegistry.registerKeyBinding(KeyChangeAccess);
	}
	
	public EntityPlayer getPlayer(@Nullable MessageContext ctx) {
	    if (ctx == null) return Minecraft.getMinecraft().player;
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            if (ctx.side == Side.SERVER) {
                return super.getPlayer(ctx);
            }
        }
		return Minecraft.getMinecraft().player;
	}
	
	public void addScheduledTask(@Nonnull Runnable run, @Nonnull MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            if (ctx.side == Side.SERVER) {
                super.addScheduledTask(run, ctx);
                return;
            }
        }
	    Minecraft.getMinecraft().addScheduledTask(run);
	}

    @Override
    public void openLoadingGui() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiLoadingScreen());
    }

    @Override
    public void openManualGui(IManualEntry forEntry) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiAIMGuide(forEntry));
    }

    @Override
    public void addPreBlockPagesToGuide() {
        GuiAIMGuide.addPreBlockPages();
    }

    public void addPageToGuide(IManualEntry entry) {
        GuiAIMGuide.content.add(entry);
    }

    @Override
    public void registerPackets() {
        PacketHelper.wrapper.registerMessage(PacketKeyPressed.PacketKeyPressedHandler.class, PacketKeyPressed.class, 0, Side.SERVER);
        PacketHelper.wrapper.registerMessage(PacketOpenInfoGUI.PacketOpenInfoGUIHandler.class, PacketOpenInfoGUI.class, 1, Side.CLIENT);
        PacketHelper.wrapper.registerMessage(PacketHotbarSlotChanged.PacketHotbarSlotChangedHandler.class, PacketHotbarSlotChanged.class, 2, Side.CLIENT);
        PacketHelper.wrapper.registerMessage(PacketUpdateOverlayInfo.PacketUpdateOverlayInfoHandler.class, PacketUpdateOverlayInfo.class, 3, Side.CLIENT);
        PacketHelper.wrapper.registerMessage(PacketNetworkCoreList.PacketOpenNetworkCoreListHandler.class, PacketNetworkCoreList.class, 4, Side.CLIENT);
        PacketHelper.wrapper.registerMessage(PacketRequestServerInfo.PacketRequestServerInfoHandler.class, PacketRequestServerInfo.class, 5, Side.SERVER);
        PacketHelper.wrapper.registerMessage(PacketNetworkInfo.PacketNetworkInfoHandler.class, PacketNetworkInfo.class, 6, Side.CLIENT);
    }

    @Nullable
    public String tryToLocalizeString(String format, Object... args) {
        return I18n.format(format, args);
    }
}
