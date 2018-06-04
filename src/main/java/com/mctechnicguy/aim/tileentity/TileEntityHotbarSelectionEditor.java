package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketHotbarSlotChanged;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

public class TileEntityHotbarSelectionEditor extends TileEntityAIMDevice {

    private boolean rsStatus = false; //False: Currently no signal, true: signal is given

    public void forceHotbarSelection(int strength) {
        if (isCoreActive() && !world.isRemote && getCore().Power > AdvancedInventoryManagement.POWER_PER_SLOT_SELECTION) {
            boolean newStatus = strength > 0;
            if (newStatus != rsStatus) {
                if (newStatus) {
                    getPlayerInventory().currentItem = Math.min(strength - 1, InventoryPlayer.getHotbarSize() - 1);
                    PacketHelper.sendPacketToClient(new PacketHotbarSlotChanged((short)getPlayerInventory().currentItem), (EntityPlayerMP)getPlayer());
                }
                rsStatus = newStatus;
            }
        }
    }
}
