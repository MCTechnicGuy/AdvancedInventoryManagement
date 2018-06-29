package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.DirectTeleporter;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPositionEditor extends TileEntityAIMDevice {

    private boolean rsStatus = false; //False: Currently no signal, true: signal is given
    private BlockPos destination = null;
    private int destDimension = 0;

    public void transferCardData(ItemStack card, EntityPlayer player) {
        if (card.getTagCompound() != null) {
            NBTTagCompound nbt = card.getTagCompound();
            if (nbt.hasKey("x") && nbt.hasKey("y") && nbt.hasKey("z") && nbt.hasKey("dim")) {
                destination = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y") + 1, nbt.getInteger("z"));
                destDimension = nbt.getInteger("dim");
                if (player != null && !world.isRemote) AIMUtils.sendChatMessageWithArgs("message.cardsaved", player, TextFormatting.AQUA, destination.getX(), destination.getY(), destination.getZ(), destDimension);
            }
        }
    }

    public void onRedstoneChanged(int strength) {
        if (isCoreActive() && !world.isRemote && getCore().Power > AdvancedInventoryManagement.POWER_PER_SLOT_SELECTION) {
            boolean newStatus = strength > 0;
            if (newStatus != rsStatus) {
                if (newStatus) {
                    doTeleport();
                }
                rsStatus = newStatus;
            }
        }
    }

    private void doTeleport() {
        if (getPlayer().dimension != destDimension && !AdvancedInventoryManagement.ALLOW_TELEPORT_BETWEEN_DIMENSIONS) {
            AIMUtils.sendChatMessage("message.noteleportallowed", getPlayer(), TextFormatting.RED);
        } else if (destination == null || isTargetBlocked()) {
            AIMUtils.sendChatMessage("message.teleportblocked", getPlayer(), TextFormatting.RED);
        } else if (getCore().Power <= getPowerDrain()) {
            AIMUtils.sendChatMessage("message.teleportnopower", getPlayer(), TextFormatting.RED);
        } else{
            if (getPlayer().isRiding()) {
                getPlayer().dismountRidingEntity();
            }

            if (getPlayer().dimension != destDimension) {
                if (!world.isRemote) ((EntityPlayerMP)getPlayer()).mcServer.getPlayerList().transferPlayerToDimension((EntityPlayerMP)getPlayer(), destDimension, new DirectTeleporter(((EntityPlayerMP) getPlayer()).getServerWorld()));
            }

            getPlayer().setPositionAndUpdate(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
            getCore().changePower(-getPowerDrain());
        }
    }

    private int getPowerDrain() {
        if (!AdvancedInventoryManagement.DOES_USE_POWER) return 0;
        return destDimension != getPlayer().dimension ? AdvancedInventoryManagement.POWER_PER_TELEPORT * 2 : AdvancedInventoryManagement.POWER_PER_TELEPORT;
    }

    private boolean isTargetBlocked() {
        if (world.isRemote) return false;
        WorldServer dimension = world.getMinecraftServer().getWorld(destDimension);
        if (dimension == null) {
            return true;
        }
        return dimension.getBlockState(destination).getCollisionBoundingBox(dimension, destination) != Block.NULL_AABB || dimension.getBlockState(destination.up()).getCollisionBoundingBox(dimension, destination.up()) != Block.NULL_AABB;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.getBoolean("hasDest")) {
            destination = new BlockPos(nbt.getInteger("destX"), nbt.getInteger("destY"), nbt.getInteger("destZ"));
            destDimension = nbt.getInteger("destDim");
        }
        rsStatus = nbt.getBoolean("rsstatus");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        if (destination != null) {
            nbt.setBoolean("hasDest", true);
            nbt.setInteger("destX", destination.getX());
            nbt.setInteger("destY", destination.getY());
            nbt.setInteger("destZ", destination.getZ());
        }
        nbt.setInteger("destDim", destDimension);
        nbt.setBoolean("rsstatus", rsStatus);
        return nbt;
    }

    @Nullable
    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (destination != null) {
            nbt.setBoolean("hasDest", true);
            nbt.setInteger("destX", destination.getX());
            nbt.setInteger("destY", destination.getY());
            nbt.setInteger("destZ", destination.getZ());
            nbt.setInteger("destDim", destDimension);
        }
        return nbt;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {
        super.handleTagForOverlayUpdate(nbt);
        if (nbt.getBoolean("hasDest")) {
            destination = new BlockPos(nbt.getInteger("destX"), nbt.getInteger("destY"), nbt.getInteger("destZ"));
            destDimension = nbt.getInteger("destDim");
        } else {
            destination = null;
        }
        hasAccurateServerInfo = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        super.renderStatusInformation(renderer);
        renderer.renderTileValues("destinationblock", TextFormatting.GREEN, !hasAccurateServerInfo,
                destination != null ? I18n.format("aimoverlay.destinationblock.coorddimvalue", destination.getX(), destination.getY(), destination.getZ(), destDimension)
                        : I18n.format("aimoverlay.destinationblock.none"));
    }

}
