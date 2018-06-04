package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityEnderChestRelay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderChestRelay extends BlockAIMDevice {

    public static final String NAME = "enderchestrelay";

    public BlockEnderChestRelay() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityEnderChestRelay();
    }

}
