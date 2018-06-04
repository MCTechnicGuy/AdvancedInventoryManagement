package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityXPRelayLiquid;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.ModCompatHelper;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockXPRelayLiquid extends BlockAIMDevice implements IHasModes {

	public static final String NAME = "xprelay_liquid";
	private static final PropertyEnum MODE = PropertyEnum.create("mode", BlockXPRelayLiquid.EnumType.class);

	public BlockXPRelayLiquid() {
		super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
	}

	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityXPRelayLiquid();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MODE, ISACTIVE);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(MODE, EnumType.fromID(meta)).withProperty(ISACTIVE, false);
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[] {(double)AdvancedInventoryManagement.XP_PER_BUCKET / 1000D, (double)AdvancedInventoryManagement.XP_PER_BUCKET / 1000D, (double)AdvancedInventoryManagement.XP_PER_BUCKET * 16};
	}

	@Override
	public boolean needsSmallerFont() {
		return true;
	}

	@Override
	public int getIDFromState(IBlockState state) {
        EnumType type = (EnumType) state.getValue(MODE);
        return type.getID();
	}

	@Override
	public String getCurrentModeUnlocalizedName(World world, BlockPos pos) {
		return "mode.xp.liquid." + EnumType.fromID(getIDFromState(world.getBlockState(pos))).getName();
	}

	@Override
	public void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
		int mode = getIDFromState(world.getBlockState(pos));
		if (mode < EnumType.values().length - 1) {
			mode++;
		} else
			mode = 0;
		setMode(world, pos, mode, causer);
	}

	@Override
	public void setMode(World world, BlockPos pos, int id, EntityPlayer causer) {
		world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, EnumType.fromID(id)), 2);
		if (causer != null) {
			TextComponentTranslation modeName = new TextComponentTranslation("mode.xp.liquid." + EnumType.fromID(id).getName());
			modeName.getStyle().setColor(TextFormatting.AQUA);
			AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
		}
	}

	private enum EnumType implements IStringSerializable{

		MOLTENXP(0, "moltenxp"),
		LIQUIDXP(1, "xpjuice");

		private int id;
		private String name;


		EnumType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

        public static int getSize() {
            int size = 1;
            if (AdvancedInventoryManagement.USE_LIQUID_XP && ModCompatHelper.xpjuice != null) size++;
            return size;
        }

		public int getID() {
			return id;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Nonnull
		public static EnumType fromID(int id) {
			switch(id) {
				case 0: return MOLTENXP;
				case 1: return LIQUIDXP;
				default: return MOLTENXP;
			}
		}

	}

}
