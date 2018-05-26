package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityInventoryRelay;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;

import net.minecraft.block.properties.PropertyBool;
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

public class BlockInventoryRelay extends BlockAIMDevice implements IManualEntry{

	public static final String NAME = "inventoryrelay";
	public static final PropertyEnum MODE = PropertyEnum.create("mode", BlockInventoryRelay.EnumType.class);
	public static final PropertyBool ISACTIVE = PropertyBool.create("isactive");
	
	public BlockInventoryRelay() {
		super(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(MODE, EnumType.ALL).withProperty(ISACTIVE, false));
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
		
		if (world.isRemote) return true;
		TileEntity te = (world.getTileEntity(pos));
		if (AIMUtils.isWrench(heldItem) && te instanceof TileEntityAIMDevice && ((TileEntityAIMDevice)te).isPlayerAccessAllowed(player)) {
			int mode = ((TileEntityAIMDevice) te).getDeviceMode();
			if (mode < EnumType.values().length - 1) {
				mode++;
			} else
				mode = 0;
			world.setBlockState(pos, world.getBlockState(pos).withProperty(this.MODE, EnumType.fromID(mode)), 2);
			TextComponentTranslation modeName = new TextComponentTranslation("mode." + EnumType.fromID(mode).getName(), new Object[] {});
			modeName.getStyle().setColor(TextFormatting.AQUA);
			AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
			return true;
		}
		return false;
	}
	
	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityInventoryRelay();
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
		return new Object[0];
	}

	@Override
	public boolean needsSmallerFont() {
		return true;
	}


	public enum EnumType implements IStringSerializable{
		
		ALL(0, "all"),
		HOTBAR(1, "hotbar"),
		MAININV(2, "maininv");
		
		private int id;
		private String name;
		
		
		private EnumType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
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
			case 0: return ALL;
			case 1: return HOTBAR;
			case 2: return MAININV;
			default: return ALL;
			}
		}

		
	}

}
