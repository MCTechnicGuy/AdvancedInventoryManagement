package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;

import javax.annotation.Nonnull;

public class TileEntityMotionEditor extends TileEntityAIMDevice {

    private boolean rsStatus = false; //False: Currently no signal, true: signal is given

    public void addMotionToPlayer(int strenght) {
        if (isCoreActive() && !world.isRemote && getCore().Power > AdvancedInventoryManagement.POWER_PER_MOTION_EDIT * strenght * strenght) {
            boolean newStatus = strenght > 0;
            if (newStatus != rsStatus) {
                if (newStatus) {
                    double toAdd = ((double)strenght) / 7.5D;
                    getPlayer().addVelocity(getDeviceMode() == 5 ? toAdd : getDeviceMode() == 4 ? -toAdd : 0, getDeviceMode() == 1 ? toAdd : getDeviceMode() == 0 ? -toAdd : 0, getDeviceMode() == 3 ? toAdd : getDeviceMode() == 2 ? -toAdd : 0);
                    getCore().changePower(AdvancedInventoryManagement.POWER_PER_MOTION_EDIT * strenght * strenght);
                    ((EntityPlayerMP)getPlayer()).connection.sendPacket(new SPacketEntityVelocity(getPlayer()));
                }
                rsStatus = newStatus;
            }
        }
    }

    @Nonnull
    @Override
    public String getLocalizedName() {
        return "tile.motioneditor.name";
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(ModElementList.blockMotionEditor);
    }
}
