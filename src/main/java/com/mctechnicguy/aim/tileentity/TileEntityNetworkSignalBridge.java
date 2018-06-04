package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.NetworkUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class TileEntityNetworkSignalBridge extends TileEntityAIMDevice {

    private BlockPos destination = null;

    public void transferCardData(ItemStack card, EntityPlayer player) {
        NBTTagCompound nbt;
        if ((nbt = card.getTagCompound()) != null && nbt.hasKey("x") && nbt.hasKey("y") && nbt.hasKey("z") && nbt.hasKey("dim")) {
            setDestination(new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z")), nbt.getInteger("dim"), player);
        } else {
            clearDestination(player);
        }
    }

    public boolean hasRealConnection(EnumFacing facing) {
        BlockPos neighbor = this.pos.offset(facing);
        if (NetworkUtils.isNetworkDevice(world.getBlockState(neighbor).getBlock())) {
            return true;
        } else if (NetworkUtils.isNetworkCable(world.getBlockState(neighbor).getBlock())) {
            TileEntity cable = world.getTileEntity(neighbor);
            if (cable instanceof TileEntityNetworkCable) {
                ((TileEntityNetworkCable) cable).updateConnections();
                return ((TileEntityNetworkCable) cable).hasRealConnection(facing.getOpposite());
            }
        }
        return false;
    }

    private void setDestination(BlockPos newDestination, int destDimension, EntityPlayer player) {
        if (destDimension == world.provider.getDimension()) {
            if (!newDestination.equals(this.pos)) {
                if (world.getTileEntity(newDestination) instanceof TileEntityNetworkSignalBridge) {
                    TileEntityNetworkSignalBridge destinationBridge = (TileEntityNetworkSignalBridge)world.getTileEntity(newDestination);
                    if (destinationBridge != null && (destinationBridge.destination == null || destinationBridge.destination.equals(this.pos))) {
                        destinationBridge.destination = this.pos;
                        this.clearDestination(null);
                        this.destination = newDestination;
                        if (player != null && !world.isRemote) {
                            AIMUtils.sendChatMessageWithArgs("message.destinationsaved", player, TextFormatting.AQUA, destination.getX(), destination.getY(), destination.getZ(), destDimension);
                        }
                    } else if (player != null && !world.isRemote) {
                        AIMUtils.sendChatMessage("message.destinationoccupied", player, TextFormatting.RED);
                    }
                } else if (player != null && !world.isRemote) {
                    AIMUtils.sendChatMessage("message.carddestinationnotfound", player, TextFormatting.RED);
                }
            } else if (player != null && !world.isRemote) {
                AIMUtils.sendChatMessage("message.destinationredundant", player, TextFormatting.RED);
            }
        } else if (player != null && !world.isRemote) {
            AIMUtils.sendChatMessage("message.carderror", player, TextFormatting.RED);
        }
    }

    public void clearDestination(EntityPlayer player) {
        if (destination != null && world.getTileEntity(destination) instanceof TileEntityNetworkSignalBridge) {
            TileEntityNetworkSignalBridge destinationBridge = (TileEntityNetworkSignalBridge) world.getTileEntity(destination);
            if (destinationBridge != null) {
                destinationBridge.destination = null;
            }
        }
        this.destination = null;
        if (player != null && !world.isRemote) {
            AIMUtils.sendChatMessage("message.destinationcleared", player, TextFormatting.AQUA);
        }
    }

    public BlockPos getDestination() {
        return destination;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.getBoolean("hasDest")) {
            destination = new BlockPos(nbt.getInteger("destX"), nbt.getInteger("destY"), nbt.getInteger("destZ"));
        }
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
        return nbt;
    }
}
