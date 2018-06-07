package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.gui.GuiAIMGuide;
import com.mctechnicguy.aim.gui.ICustomManualEntry;
import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPlayerMonitor extends BlockAIMDevice implements ICustomManualEntry {

    public static final String NAME = "playermonitor";

    public BlockPlayerMonitor() {
        super(NAME);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityPlayerMonitor();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
        return blockAccess.getTileEntity(pos) instanceof TileEntityPlayerMonitor ? ((TileEntityPlayerMonitor)blockAccess.getTileEntity(pos)).getPowerLevel() : 0;
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, @Nullable TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (AIMUtils.isWrench(heldItem)) {
                if (world.isRemote) return EnumRightClickResult.ACTION_DONE;
                if (!side.equals(EnumFacing.UP)) {
                    int mode = ((TileEntityPlayerMonitor)tileEntity).getDeviceMode();
                    if (mode < BlockPlayerMonitor.EnumMode.values().length - 1) {
                        mode++;
                    } else
                        mode = 0;
                    ((TileEntityPlayerMonitor)tileEntity).setDeviceMode(mode);
                    TextComponentTranslation modeName = new TextComponentTranslation("mode.monitor." + BlockPlayerMonitor.EnumMode.fromID(mode).getName());
                    modeName.getStyle().setColor(TextFormatting.AQUA);
                    AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
                    ((TileEntityAIMDevice)tileEntity).updateBlock();
                    return EnumRightClickResult.ACTION_DONE;
                } else {
                    int mode = ((TileEntityPlayerMonitor) tileEntity).getRedstone_behaviour();
                    if (mode < ((TileEntityPlayerMonitor) tileEntity).getMaxRSMode()) {
                        mode++;
                    } else
                        mode = 0;
                    ((TileEntityPlayerMonitor)tileEntity).setRedstone_behaviour(mode);
                    TextComponentTranslation modeName = new TextComponentTranslation("rsmode.monitor." + mode);
                    modeName.getStyle().setColor(TextFormatting.AQUA);
                    AIMUtils.sendChatMessageWithArgs("message.rsmodechange", player, TextFormatting.RESET, modeName);
                    ((TileEntityAIMDevice)tileEntity).updateBlock();
                    return EnumRightClickResult.ACTION_DONE;
                }
            }
            ((TileEntityAIMDevice)tileEntity).updateBlock();
            return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    @Override
    public int getPageCount() {
        return 2;
    }

    @Override
    public boolean doesProvideOwnContent() {
        return true;
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }

    @Override
    public boolean showCraftingRecipe(int page) {
        return page == 0;
    }

    @Override
    public boolean hasLeftSidePicture(int page) {
        return page > 0;
    }

    @Override
    public void drawLeftSidePicture(int page, @Nonnull Minecraft mc, @Nonnull GuiAIMGuide gui, float zLevel) {
        String textToPrint = I18n.format("guide.content.playermonitor_modes").replace("\\n", "\n");
        double scale = 0.66666D;
        GlStateManager.scale(scale, scale, scale);
        mc.fontRenderer.drawSplitString(textToPrint, (int)Math.round((gui.BgStartX + 15) * (1 / scale)), (int)Math.round((gui.BgStartY + 15) * (1 / scale)), (int)Math.round((GuiAIMGuide.BGX / 2 - 30) * (1 / scale)), 4210752);
        GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
    }

    @Nullable
    @Override
    public ResourceLocation getRecipeForPage(int page) {
        return new ResourceLocation(ModInfo.ID + ":" + NAME);
    }

    @Override
    public boolean showHeaderOnPage(int page) {
        return page == 0;
    }

    public enum EnumMode implements IStringSerializable {

        HEALTH(0, "health"),
        XP(1, "xp"),
        HUNGER(2, "hunger"),
        SATURATION(3, "saturation"),
        AIR(4, "air"),
        MOTIONX(5, "motionx"),
        MOTIONY(6, "motiony"),
        MOTIONZ(7, "motionz"),
        ARMOR(8, "armor"),
        POSX(9, "posx"),
        POSY(10, "posy"),
        POSZ(11, "posz"),
        ISBURNING(12, "isburning"),
        ISINWATER(13, "isinwater"),
        ISINLAVA(14, "isinlava"),
        ISAIRBORNE(15, "isairborne"),
        ISSNEAKING(16, "issneaking"),
        ISSPRINTING(17, "issprinting"),
        ISFALLING(18, "isfalling"),
        SELECTEDSLOT(19, "selectedslot"),
        DIMENSION(20, "dimension");

        private int id;
        private String name;

        EnumMode(int id, String name) {
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

        public static BlockPlayerMonitor.EnumMode fromID(int id) {
            return values()[id];
        }
    }



}
