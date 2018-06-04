package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityEnergyRelay;
import com.mctechnicguy.aim.util.AIMUtils;
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
import javax.annotation.Nullable;

public class BlockEnergyRelay extends BlockAIMDevice implements IHasModes {

    public static final String NAME = "energyrelay";
    public static final PropertyEnum MODE = PropertyEnum.create("mode", BlockEnergyRelay.EnumType.class);

    public BlockEnergyRelay() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityEnergyRelay();
    }

    @Override
    public int getIDFromState(IBlockState state) {
        EnumType type = (EnumType) state.getValue(MODE);
        return type.getID();
    }

    @Override
    public String getCurrentModeUnlocalizedName(World world, BlockPos pos) {
        return "mode.charge." + BlockEnergyRelay.EnumType.fromID(getIDFromState(world.getBlockState(pos))).getName();
    }

    @Override
    public void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
        int mode = getIDFromState(world.getBlockState(pos));
        if (mode < BlockEnergyRelay.EnumType.values().length - 1) {
            mode++;
        } else
            mode = 0;
        world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, BlockEnergyRelay.EnumType.fromID(mode)), 2);
        if (causer != null) {
            TextComponentTranslation modeName = new TextComponentTranslation("mode.charge." + BlockEnergyRelay.EnumType.fromID(mode).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
        }
    }

    @Override
    public void setMode(World world, BlockPos pos, int id, EntityPlayer causer) {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, BlockEnergyRelay.EnumType.fromID(id)), 2);
        if (causer != null) {
            TextComponentTranslation modeName = new TextComponentTranslation("mode.charge." + BlockEnergyRelay.EnumType.fromID(id).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
        }
    }

    public enum EnumType implements IStringSerializable {

        ALL(0, "all"),
        HOTBAR(1, "hotbar"),
        MAINHAND(2, "mainhand"),
        OFFHAND(3, "offhand");

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
                case 2: return MAINHAND;
                case 3: return OFFHAND;
                default: return ALL;
            }
        }


    }
}
