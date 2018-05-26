package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.ModElementList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class TileEntityToggleCable extends TileEntityNetworkCable{


    public boolean isRSBlocked() {
        return hasWorld() && world.isBlockIndirectlyGettingPowered(pos) > 0;
    }

    @Override
    public boolean canTransferSignal(EnumFacing dir, boolean comingFromCore) {
        if (comingFromCore) return super.canTransferSignal(dir, true);
        else return super.canTransferSignal(dir, false) && !isRSBlocked();
    }

    @Nonnull
    @Override
    public String getLocalizedName() {
        return "tile.togglecable.name";
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(ModElementList.blockToggleCable);
    }
}
