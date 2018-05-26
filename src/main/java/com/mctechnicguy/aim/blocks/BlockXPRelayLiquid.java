package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityXPRelayLiquid;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.ModCompatHelper;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockXPRelayLiquid extends BlockAIMDevice implements IManualEntry{

	public static final String NAME = "xprelay_liquid";
	private static final PropertyEnum MODE = PropertyEnum.create("mode", BlockXPRelayLiquid.EnumType.class);

	public BlockXPRelayLiquid() {
		super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
	}

	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityXPRelayLiquid();
	}

	@Override
	public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking() && !heldItem.isEmpty() && heldItem.getItem() != ModElementList.itemAIMWrench && AIMUtils.isWrench(heldItem)) {
			this.destroyWithWrench(player, world, pos, heldItem);
			return true;
		}

		if (world.isRemote || EnumType.getSize() == 1) return true;
		TileEntity te = (world.getTileEntity(pos));
		if (AIMUtils.isWrench(heldItem) && te instanceof TileEntityAIMDevice && ((TileEntityAIMDevice)te).isPlayerAccessAllowed(player)) {
			int mode = ((TileEntityAIMDevice) te).getDeviceMode();
			if (mode < EnumType.getSize() - 1) {
				mode++;
			} else
				mode = 0;
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockXPRelayLiquid.MODE, EnumType.fromID(mode)), 2);
			TextComponentTranslation modeName = new TextComponentTranslation("mode.xp.liquid." + EnumType.fromID(mode).getName(), new Object[] {});
			modeName.getStyle().setColor(TextFormatting.AQUA);
			AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
			return true;
		}
		return false;
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

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumType type = (EnumType) state.getValue(MODE);
		return type.getID();
	}

	@Nonnull
	@Override
	public String getManualName() {
		return NAME;
	}

	@Override
	public int getPageCount() {
		return 1;
	}

	@Override
	public boolean doesProvideOwnContent() {
		return false;
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
