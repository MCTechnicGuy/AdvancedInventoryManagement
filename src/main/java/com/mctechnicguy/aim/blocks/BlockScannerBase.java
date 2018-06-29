package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityScanner;
import com.mctechnicguy.aim.util.IWrenchDestroyable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockScannerBase extends Block implements IWrenchDestroyable {

	public static final String NAME = "scannerbase";
	public static final PropertyEnum<EnumPos> POS = PropertyEnum.create("pos", BlockScannerBase.EnumPos.class);
	
	public BlockScannerBase() {
		super(Material.IRON);
		this.setHardness(3.5F);
		this.setSoundType(SoundType.STONE);
		this.setResistance(2000F);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setHarvestLevel("pickaxe", 0);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(POS, EnumPos.UNDEFINED));
		this.useNeighborBrightness = true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0, 0, 0, 1, 0.125D, 1);
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (this.hasAccess(world, pos, player))
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		else {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return false;
		}
	}
	
	@Override
	public void onBlockPlacedBy(@Nonnull World w, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityLivingBase e, @Nonnull ItemStack stack) {
		if (!this.hasAccess(w, pos, e)) {
			this.breakBlock(w, pos, state);
			if (e instanceof EntityPlayer) {
				this.harvestBlock(w, (EntityPlayer) e, pos, state, w.getTileEntity(pos), stack);
				this.removedByPlayer(state, w, pos, (EntityPlayer) e, true);
			}
		}
	}

	@Override
	public void destroyWithWrench(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack item) {
			if (this.hasAccess(world, pos, player)) {
				this.breakBlock(world, pos, world.getBlockState(pos));
				this.harvestBlock(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos), item);
				this.removedByPlayer(world.getBlockState(pos), world, pos, player, true);
			}	
	}
	
	private boolean hasAccess(@Nonnull World w, @Nonnull BlockPos pos, EntityLivingBase e) {
		if (getScanner(w, pos) != null && e instanceof EntityPlayer) return getScanner(w, pos).isPlayerAccessAllowed((EntityPlayer) e);
		return true;
	}


	private TileEntityScanner getScanner(@Nonnull World w, @Nonnull BlockPos pos) {
		for (EnumFacing f : EnumFacing.HORIZONTALS) {
			if (w.getTileEntity(pos.offset(f)) instanceof TileEntityScanner)
				return (TileEntityScanner) w.getTileEntity(pos.offset(f));
		}
		if (w.getTileEntity(pos.north().west()) instanceof TileEntityScanner)
			return (TileEntityScanner) w.getTileEntity(pos.north().west());
		if (w.getTileEntity(pos.north().east()) instanceof TileEntityScanner)
			return (TileEntityScanner) w.getTileEntity(pos.north().east());
		if (w.getTileEntity(pos.south().west()) instanceof TileEntityScanner)
			return (TileEntityScanner) w.getTileEntity(pos.south().west());
		if (w.getTileEntity(pos.south().east()) instanceof TileEntityScanner)
			return (TileEntityScanner) w.getTileEntity(pos.south().east());
		
		return null;
	}

	@Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

	@Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

	@Override
    public boolean doesSideBlockRendering(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return face != EnumFacing.UP && (face == EnumFacing.DOWN || world.getBlockState(pos.offset(face)).getBlock() == this);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
	    return new BlockStateContainer(this, POS);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	public enum EnumPos implements IStringSerializable{
		
		STRAIGHT_TOP(0, "straight_top"),
		STRAIGHT_BOTTOM(1, "straight_bottom"),
		STRAIGHT_LEFT(2, "straight_left"),
		STRAIGHT_RIGHT(3, "straight_right"),
		CURVE_1(4, "curve_1"),
		CURVE_2(5, "curve_2"),
		CURVE_3(6, "curve_3"),
		CURVE_4(7, "curve_4"),
		UNDEFINED(8, "undefined");
		
		
		private int id;
		private String name;
		
		
		EnumPos(int id, String name) {
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
		public static EnumPos fromID(int id) {
			switch (id) {
			case 0: return STRAIGHT_TOP;
			case 1: return STRAIGHT_BOTTOM;
			case 2: return STRAIGHT_LEFT;
			case 3: return STRAIGHT_RIGHT;
			case 4: return CURVE_1;
			case 5: return CURVE_2;
			case 6: return CURVE_3;
			case 7: return CURVE_4;
			case 8: return UNDEFINED;
			default: return UNDEFINED;
			}
		}
	}
}
