package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityMotionEditor;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockMotionEditor extends BlockAIMDevice implements IHasModes {

    public static final String NAME = "motioneditor";
    private static final PropertyEnum MODE = PropertyEnum.create("mode", EnumFacing.class);

    public BlockMotionEditor() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false).withProperty(MODE, EnumFacing.DOWN));
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityMotionEditor) {
            ((TileEntityMotionEditor)worldIn.getTileEntity(pos)).addMotionToPlayer(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityMotionEditor();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MODE, ISACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MODE, EnumFacing.getFront(meta)).withProperty(ISACTIVE, false);
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.POWER_PER_MOTION_EDIT, AdvancedInventoryManagement.POWER_PER_MOTION_EDIT * 225};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }

    @Override
    public int getIDFromState(IBlockState state) {
        EnumFacing type = (EnumFacing) state.getValue(MODE);
        return type.getIndex();
    }

    @Override
    public String getCurrentModeUnlocalizedName(World world, BlockPos pos) {
        return "mode." + EnumFacing.getFront(getIDFromState(world.getBlockState(pos))).getName();
    }

    @Override
    public void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
        int mode = getIDFromState(world.getBlockState(pos));
        if (mode < EnumFacing.values().length - 1) {
            mode++;
        } else
            mode = 0;
        setMode(world, pos, mode, causer);
    }

    @Override
    public void setMode(World world, BlockPos pos, int id, EntityPlayer causer) {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, EnumFacing.getFront(id)), 2);
        if (causer != null) {
            TextComponentTranslation modeName = new TextComponentTranslation("mode." + EnumFacing.getFront(id).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
        }
    }
}
