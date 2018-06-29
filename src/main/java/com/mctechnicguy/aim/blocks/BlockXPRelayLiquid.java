package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityXPRelayLiquid;
import com.mctechnicguy.aim.util.ModCompatHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockXPRelayLiquid extends BlockAIMModulatedDevice {

	public static final String NAME = "xprelay_liquid";
	private static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "moltenxp", "xpjuice");

	public BlockXPRelayLiquid() {
		super(NAME);
	}

	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityXPRelayLiquid();
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[] {(double)AdvancedInventoryManagement.XP_PER_BUCKET / 1000D, (double)AdvancedInventoryManagement.XP_PER_BUCKET / 1000D, (double)AdvancedInventoryManagement.XP_PER_BUCKET * 16};
	}


	@Override
	public void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
		int mode = getModeIDFromState(world.getBlockState(pos));
		if (mode < getModeListSize() - 1) {
			mode++;
		} else
			mode = 0;
		setMode(world, pos, mode, causer);
	}

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
	    if (meta > getModeListSize() - 1) {
	        meta = 0;
        }
        return getDefaultState().withProperty(MODE, this.getModeFromID(meta)).withProperty(ISACTIVE, false);
    }

    private static int getModeListSize() {
        int size = 1;
        if (AdvancedInventoryManagement.USE_LIQUID_XP && ModCompatHelper.xpjuice != null) size++;
        return size;
    }


    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }


}
