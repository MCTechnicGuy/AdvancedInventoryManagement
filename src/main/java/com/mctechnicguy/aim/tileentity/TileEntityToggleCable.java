package com.mctechnicguy.aim.tileentity;

import net.minecraft.util.EnumFacing;

public class TileEntityToggleCable extends TileEntityNetworkCable{


    public boolean isRSBlocked() {
        return hasWorld() && world.isBlockIndirectlyGettingPowered(pos) > 0;
    }

    @Override
    public boolean canTransferSignal(EnumFacing dir, boolean comingFromCore) {
        if (comingFromCore) return super.canTransferSignal(dir, true);
        else return super.canTransferSignal(dir, false) && !isRSBlocked();
    }

}
