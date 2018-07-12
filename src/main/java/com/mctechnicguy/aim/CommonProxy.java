package com.mctechnicguy.aim;

import com.mctechnicguy.aim.container.ContainerAIMCore;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.network.*;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class CommonProxy implements IGuiHandler {

	public void registerRenderers() {
	}

	public void registerKeys() {
	}


	public void registerFluid(BlockFluidClassic block, String name) {}

	public boolean playerEqualsClient(UUID client) {
		return false;
	}


	@Nullable
    @Override
	public Object getServerGuiElement(int ID, @Nonnull EntityPlayer player, @Nonnull World world, int x, int y, int z) {
		TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
		if (entity != null) {
			switch (ID) {
			case AdvancedInventoryManagement.guiIDCore:
				if (entity instanceof TileEntityAIMCore) {
					return new ContainerAIMCore(player.inventory, (TileEntityAIMCore) entity);
				}
			}
		}
		return null;
	}

	@Nullable
    @Override
	public Object getClientGuiElement(int ID, @Nonnull EntityPlayer player, @Nonnull World world, int x, int y, int z) {
	    return getServerGuiElement(ID, player, world, x, y, z);
	}
	
	public void addScheduledTask(@Nonnull Runnable run, @Nonnull MessageContext ctx) {
		ctx.getServerHandler().player.getServer().addScheduledTask(run);
	}

	public EntityPlayer getPlayer(@Nullable MessageContext ctx) {
	    if (ctx == null) return null;
		return ctx.getServerHandler().player;
	}

	public void addPreBlockPagesToGuide() {

    }

    public void openLoadingGui() {

    }

    public void openManualGui(IManualEntry forEntry) {

    }

    public void addPageToGuide(IManualEntry entry) {

    }

    public void registerPackets() {
        PacketHelper.wrapper.registerMessage(PacketKeyPressed.PacketKeyPressedHandler.class, PacketKeyPressed.class, 0, Side.SERVER);
        PacketHelper.wrapper.registerMessage((message, ctx) -> null, PacketOpenInfoGUI.class, 1, Side.CLIENT);
        PacketHelper.wrapper.registerMessage((message, ctx) -> null, PacketHotbarSlotChanged.class, 2, Side.CLIENT);
        PacketHelper.wrapper.registerMessage((message, ctx) -> null, PacketUpdateOverlayInfo.class, 3, Side.CLIENT);
        PacketHelper.wrapper.registerMessage((message, ctx) -> null, PacketNetworkCoreList.class, 4, Side.CLIENT);
        PacketHelper.wrapper.registerMessage(PacketRequestServerInfo.PacketRequestServerInfoHandler.class, PacketRequestServerInfo.class, 5, Side.SERVER);
        PacketHelper.wrapper.registerMessage((message, ctx) -> null, PacketNetworkInfo.class, 6, Side.CLIENT);
    }

    @Nullable
    public String tryToLocalizeString(String format, Object... args) {
	    return null;
    }

}
