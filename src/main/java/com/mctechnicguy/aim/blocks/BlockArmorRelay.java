package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityArmorRelay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockArmorRelay extends BlockAIMDevice {

	public static final String NAME = "armorrelay";
	
	public BlockArmorRelay() {
		super(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
	}
	
	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityArmorRelay();
	}

}
