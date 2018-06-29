package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityGenerator;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGenerator extends BlockAIMBase implements IAIMGenerator {

    public static final PropertyInteger BURNING_STAGE = PropertyInteger.create("burning_stage", 0, 5);
    public static final String NAME = "generator";

    public BlockGenerator() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BURNING_STAGE, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityGenerator();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BURNING_STAGE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BURNING_STAGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BURNING_STAGE);
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (!player.isSneaking() && !heldItem.isEmpty() && TileEntityFurnace.isItemFuel(heldItem) && !(heldItem.getItem() instanceof ItemBucket)) {

                if (tileEntity instanceof TileEntityGenerator && !world.isRemote && TileEntityFurnace.getItemBurnTime(heldItem) <= TileEntityGenerator.MAX_BURN_TIME - ((TileEntityGenerator)tileEntity).burnTimeRemaining) {
                    ((TileEntityGenerator)tileEntity).addBurnTime(TileEntityFurnace.getItemBurnTime(heldItem));
                    heldItem.shrink(1);
                    if (heldItem.getCount() <= 0) heldItem = ItemStack.EMPTY;
                    player.setHeldItem(hand, heldItem);
                }
                return EnumRightClickResult.ACTION_DONE;
            } else return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.UP || face == EnumFacing.DOWN;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state) {
        return state.getValue(BURNING_STAGE) * 3;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.MAX_GENERATOR_POWER_OUTPUT};
    }

}
