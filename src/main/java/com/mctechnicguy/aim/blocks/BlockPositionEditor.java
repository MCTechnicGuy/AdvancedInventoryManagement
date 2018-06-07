package com.mctechnicguy.aim.blocks;


import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.tileentity.TileEntityPositionEditor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPositionEditor extends BlockAIMDevice {

    public static final String NAME = "positioneditor";

    public BlockPositionEditor() {
        super(NAME);
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (heldItem.getItem() == ModElementList.itemPositionCard) {
                if (tileEntity instanceof TileEntityPositionEditor && !world.isRemote) {
                    ((TileEntityPositionEditor)tileEntity).transferCardData(heldItem, player);
                }
                return EnumRightClickResult.ACTION_DONE;
            } else return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityPositionEditor) {
            ((TileEntityPositionEditor)worldIn.getTileEntity(pos)).onRedstoneChanged(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityPositionEditor();
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.POWER_PER_TELEPORT};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }
}
