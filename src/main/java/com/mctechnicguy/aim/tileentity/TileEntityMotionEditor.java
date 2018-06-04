package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;

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
}
