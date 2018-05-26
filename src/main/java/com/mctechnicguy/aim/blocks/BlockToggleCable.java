package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityToggleCable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockToggleCable extends BlockNetworkCable {

    public static final String NAME = "togglecable";
    private static final PropertyBool UP = PropertyBool.create("up");
    private static final PropertyBool DOWN = PropertyBool.create("down");
    private static final PropertyBool NORTH = PropertyBool.create("north");
    private static final PropertyBool SOUTH = PropertyBool.create("south");
    private static final PropertyBool EAST = PropertyBool.create("east");
    private static final PropertyBool WEST = PropertyBool.create("west");
    private static final PropertyBool ISCOREACTIVE = PropertyBool.create("iscoreactive");
    private static final PropertyBool ISREDSTONEBLOCKED = PropertyBool.create("isrsblocked");

    public BlockToggleCable() {
        this.setUnlocalizedName(NAME);
        this.setSoundType(SoundType.GLASS);
        this.setResistance(2000F);
        this.setHardness(3.5F);
        this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
                .withProperty(WEST, false).withProperty(ISCOREACTIVE, false).withProperty(ISREDSTONEBLOCKED, false));
    }

    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityToggleCable();
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityToggleCable && (((TileEntityToggleCable) worldIn.getTileEntity(pos)).hasCore())) {
            (((TileEntityToggleCable) worldIn.getTileEntity(pos))).getCore().forceNetworkUpdate(2);
        }
    }

    @Nonnull
    @Override
    public IBlockState getActualState(IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        super.updateConnections(worldIn, pos);
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntityToggleCable)) return state.withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
                .withProperty(WEST, false).withProperty(ISCOREACTIVE, false).withProperty(ISREDSTONEBLOCKED, false);
        TileEntityToggleCable cable = (TileEntityToggleCable) te;
        return state.withProperty(UP, cable.hasRealConnection(EnumFacing.UP))
                .withProperty(DOWN, cable.hasRealConnection(EnumFacing.DOWN))
                .withProperty(NORTH, cable.hasRealConnection(EnumFacing.NORTH))
                .withProperty(SOUTH, cable.hasRealConnection(EnumFacing.SOUTH))
                .withProperty(EAST, cable.hasRealConnection(EnumFacing.EAST))
                .withProperty(WEST, cable.hasRealConnection(EnumFacing.WEST))
                .withProperty(ISCOREACTIVE, cable.hasCore() && cable.getCore().isActive())
                .withProperty(ISREDSTONEBLOCKED, cable.isRSBlocked());
    }


    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UP, DOWN, NORTH, SOUTH, EAST, WEST, ISCOREACTIVE, ISREDSTONEBLOCKED);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
                .withProperty(WEST, false).withProperty(ISCOREACTIVE, false).withProperty(ISREDSTONEBLOCKED, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
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
        return new Object[] {};
    }

    @Override
    public boolean needsSmallerFont() {
        return false;
    }

}
