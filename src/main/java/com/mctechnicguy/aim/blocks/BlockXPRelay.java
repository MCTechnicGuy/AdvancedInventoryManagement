package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityXPRelay;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
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
import java.util.Random;

public class BlockXPRelay extends BlockAIMDevice implements IManualEntry{

	public static final String NAME = "xprelay";
    private final Random rand = new Random();
	private static final PropertyEnum MODE = PropertyEnum.create("mode", BlockXPRelay.EnumType.class);
	
	public BlockXPRelay() {
		super(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(MODE, EnumType.INLET).withProperty(ISACTIVE, false));
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
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockXPRelay.MODE, EnumType.fromID(mode)), 2);
			TextComponentTranslation modeName = new TextComponentTranslation("mode.xp." + EnumType.fromID(mode).getName(), new Object[] {});
			modeName.getStyle().setColor(TextFormatting.AQUA);
			AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
			return true;
		}
		return false;
	}



	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityXPRelay();
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

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (world.getTileEntity(pos) instanceof TileEntityXPRelay) {
            TileEntityXPRelay te = (TileEntityXPRelay) world.getTileEntity(pos);
            ItemStack itemstack = te.getStackInSlot(0);

            if (!itemstack.isEmpty()) {
                float f = this.rand.nextFloat() * 0.8F + 0.1F;
                float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

                while (itemstack.getCount() > 0) {
                    int k1 = this.rand.nextInt(21) + 10;

                    if (k1 > itemstack.getCount()) {
                        k1 = itemstack.getCount();
                    }

                    itemstack.shrink(k1);;
                    EntityItem entityitem = new EntityItem(world, (double) ((float) pos.getX() + f), (double) ((float) pos.getY() + f1),
                            (double) ((float) pos.getZ() + f2), new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));

                    if (itemstack.hasTagCompound()) {
                        entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
                    }

                    float f3 = 0.05F;
                    entityitem.motionX = (double) ((float) this.rand.nextGaussian() * f3);
                    entityitem.motionY = (double) ((float) this.rand.nextGaussian() * f3 + 0.2F);
                    entityitem.motionZ = (double) ((float) this.rand.nextGaussian() * f3);
                    world.spawnEntity(entityitem);
                }
            }
        }
        super.breakBlock(world, pos, state);
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
		return new Object[] {8, 3, 12};
	}

	@Override
	public boolean needsSmallerFont() {
		return false;
	}

	private enum EnumType implements IStringSerializable{
		
		INLET(0, "inlet"),
		OUTLET(1, "outlet");
		
		private int id;
		private String name;

		EnumType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Nonnull
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
			case 0: return INLET;
			case 1: return OUTLET;
			default: return INLET;
			}
		}
		
	}

}
