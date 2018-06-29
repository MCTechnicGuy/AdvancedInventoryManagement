package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityInventoryRelay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInventoryRelay extends BlockAIMModulatedDevice {

	public static final String NAME = "inventoryrelay";
	public static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "all", "hotbar", "maininv");
	
	public BlockInventoryRelay() {
		super(NAME);
	}
	
	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityInventoryRelay();
	}

    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }

}
