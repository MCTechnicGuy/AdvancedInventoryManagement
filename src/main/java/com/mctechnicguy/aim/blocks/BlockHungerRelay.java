package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityHungerRelay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHungerRelay extends BlockAIMModulatedDevice {

	public static final String NAME = "hungerrelay";
	private static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "hunger", "efficiency", "always");

	public BlockHungerRelay() {
		super(NAME);
	}

	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityHungerRelay();
	}

	@Override
	public boolean needsSmallerFont() {
		return true;
	}

    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }

}
