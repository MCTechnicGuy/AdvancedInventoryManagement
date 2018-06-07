package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.blocks.property.AIMMode;
import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BlockAIMModulatedDevice extends BlockAIMDevice {

    BlockAIMModulatedDevice(@Nonnull String bname) {
        super(bname);
        this.setDefaultState(this.blockState.getBaseState().withProperty(getModeProperty(), this.getModeFromID(-1)).withProperty(ISACTIVE, false));
    }

    int getModeIDFromState(IBlockState state) {
        AIMMode type = state.getValue(this.getModeProperty());
        return type.getID();
    }

    @Nonnull
    AIMMode getModeFromID(int id) {
        return getModeProperty().getModeForID(id);
    }

    protected abstract PropertyAIMMode getModeProperty();

    public String getCurrentModeUnlocalizedName(World world, BlockPos pos) {
        return "mode." + this.getRegistryName().getResourcePath() + "." + this.getModeFromID(getModeIDFromState(world.getBlockState(pos))).getName();
    }

    protected void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
        int mode = getModeIDFromState(world.getBlockState(pos));
        if (mode < getModeProperty().getAllowedValues().size() - 1) {
            mode++;
        } else
            mode = 0;
        setMode(world, pos, mode, causer);
    }


    void setMode(World world, BlockPos pos, int id, EntityPlayer causer) {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(getModeProperty(), this.getModeFromID(id)), 2);
        if (causer != null) {
            TextComponentTranslation modeName = new TextComponentTranslation("mode." + this.getRegistryName().getResourcePath() + "." + this.getModeFromID(id).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
        }
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(this.getModeProperty(), this.getModeFromID(meta)).withProperty(ISACTIVE, false);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getModeProperty(), ISACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.getModeIDFromState(state);
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (AIMUtils.isWrench(heldItem)) {
                if (!world.isRemote) this.cycleToNextMode(world, pos, player);
                return EnumRightClickResult.ACTION_DONE;
            }
            return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

}
