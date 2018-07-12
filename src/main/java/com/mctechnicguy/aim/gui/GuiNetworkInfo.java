package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketNetworkInfo;
import com.mctechnicguy.aim.network.PacketRequestServerInfo;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class GuiNetworkInfo extends GuiScreen {

    private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID.toLowerCase(), "textures/gui/guinetworkinfo.png");

    private PacketNetworkInfo info;

    // Width of the gui-image
    private static final int BGX = 400;
    // Height of the gui-image
    private static final int BGY = 240;
    private static final int SCROLLBAR_WIDTH = 24;
    private static final int SCROLLBAR_HEIGHT = 30;
    private static final int SCROLLBAR_OFFSET_X = 352;
    private static final int SCROLLBAR_OFFSET_Y = 64;
    private static final int SCROLLBAR_RANGE = 76;

    private int BgStartX;
    private int BgStartY;
    private int ScrollBarPos;

    private ArrayList<String> problemMessages = new ArrayList<>();

    private boolean ScrollBarActive;
    private int lastMouseY;

    private GuiButton buttonBack;
    private GuiButton buttonRefresh;

    public GuiNetworkInfo(PacketNetworkInfo info) {
        super();
        this.info = info;
    }

    @Override
    public void initGui() {
        BgStartX = (this.width / 2) - (BGX / 2);
        BgStartY = (this.height / 2) - (BGY / 2);
        buttonBack = new GuiButton(0, BgStartX + 232, BgStartY + 201, 66, 20, I18n.format("gui.aiminfo.back"));
        buttonRefresh = new GuiButton(0, BgStartX + 312, BgStartY + 201, 66, 20, I18n.format("gui.aiminfo.refresh"));
        this.buttonList.add(buttonBack);
        this.buttonList.add(buttonRefresh);

        if (info.problems == 0) {
            this.problemMessages.add("gui.aimcore.problems.none");
        } else {
            if ((info.problems & 1) != 0) {
                this.problemMessages.add("gui.aimcore.problems.noplayer");
            }
            if ((info.problems & 2) != 0) {
                this.problemMessages.add("gui.aimcore.problems.wrongsetup");
            }
            if ((info.problems & 4) != 0) {
                this.problemMessages.add("gui.aimcore.problems.nopower");
            }
            if ((info.problems & 8) != 0) {
                this.problemMessages.add("gui.aimcore.problems.playerinaccessible");
            }
            if ((info.problems & 16) != 0) {
                this.problemMessages.add("gui.aimcore.problems.redstonedisabled");
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTexture);
        GuiUtils.drawScaledTexturedQuad(BgStartX, BgStartY, 0, 0, 200, 120, 256, BGX, BGY, zLevel);

        if (info.isLoaded) {
            if (info.power > 0) {
                int k = (int) Math.round(((double) info.power / (double) info.maxPower) * 100);
                GuiUtils.drawScaledTexturedQuad(BgStartX + 26, BgStartY + 126 - k, 0, 170 - (k / 2D), 16, 170, 256, 32, k, zLevel);
            }
            // Scrollbar
            GuiUtils.drawScaledTexturedQuad(scrollBarStartX(), scrollBarStartY(), 0, 170, 12, 185, 256, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT, zLevel);
        }

        fontRenderer.drawString(I18n.format("gui.aiminfo.coreheader"), (BgStartX + 122 - fontRenderer.getStringWidth(I18n.format("gui.aiminfo.coreheader")) / 2), BgStartY + 10, 0xFAFAFA);
        fontRenderer.drawString(I18n.format("gui.aiminfo.networkheader"), (BgStartX + 305 - fontRenderer.getStringWidth(I18n.format("gui.aiminfo.networkheader")) / 2), BgStartY + 10, 0xFAFAFA);

        GuiUtils.drawScaledOnelineString(fontRenderer, I18n.format("gui.aiminfo.corepos", info.corePos.getX(), info.corePos.getY(), info.corePos.getZ(), info.coreDim), BGX - 224, BgStartX + 27, BgStartY + BGY - 37, 0xFAFAFA, 0.75);
        TextComponentTranslation status = new TextComponentTranslation("gui.aiminfo.status." + (info.isLoaded ? info.isActive ? "active" : "inactive" : "unloaded"));
        status.getStyle().setColor(info.isLoaded ? info.isActive ? TextFormatting.GREEN : TextFormatting.RED : TextFormatting.YELLOW);
        GuiUtils.drawScaledOnelineString(fontRenderer, I18n.format("gui.aiminfo.status", status.getFormattedText()), BGX - 224, BgStartX + 27, BgStartY + BGY - 27, 0xFAFAFA, 0.75);

        if (!info.isLoaded) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        double scale = 1 / 0.75F;

        // Core Information
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.power"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 26) * scale), 0xFAFAFA);
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.power.value", info.power, info.maxPower), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 34) * scale), 0xFAFAFA);

        this.fontRenderer.drawString(I18n.format("gui.aiminfo.powerdrain"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 46) * scale), 0xFAFAFA);
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.powerdrain.value", info.powerDrain), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 54) * scale), 0xFAFAFA);

        this.fontRenderer.drawString(I18n.format("gui.aiminfo.connectedplayer"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 66) * scale), 0xFAFAFA);
        this.fontRenderer.drawString((info.playerConnectedName != null && !info.playerConnectedName.isEmpty() ? info.playerConnectedName : I18n.format("gui.aiminfo.noplayer")), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 74) * scale), 1234664);

        this.fontRenderer.drawString(I18n.format("gui.aiminfo.issecured"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 86) * scale), 0xFAFAFA);
        this.fontRenderer.drawString(I18n.format(info.isSecured ? "gui.aiminfo.booltrue" : "gui.aiminfo.boolfalse"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 94) * scale), info.isSecured ? 1238807 : 15208978);

        // Network Information
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.elementsconnected"), (int) ((BgStartX + 242) * scale), (int) ((BgStartY + 30) * scale), 0xFAFAFA);
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.elementsconnected.value", info.numberElementsConnected), (int) ((BgStartX + 242) * scale), (int) ((BgStartY + 40) * scale), 0xFAFAFA);
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.elementlist"), (int) ((BgStartX + 242) * scale), (int) ((BgStartY + 50) * scale), 0xFAFAFA);


        GlStateManager.popMatrix();

        // Element List
        GlStateManager.pushMatrix();
        scale = 1 / 0.6F;
        if (!info.connectedDevices.isEmpty()) {
            int size = info.connectedDevices.size();
            ArrayList<Integer> ids = new ArrayList<>(info.connectedDevices.keySet());
            int ScrollBarListPos = this.getListPosFromScrollbar();
            for (int i = 0; i < Math.min(size, 4); i++) {
                if (size > i) {
                    ItemStack blockStack = new ItemStack(Block.getBlockById(ids.get(ScrollBarListPos + i)));
                    GlStateManager.scale(0.6F, 0.6F, 0.6F);
                    this.drawItemStack(blockStack, (int) ((BgStartX + 320) * scale), (int) ((BgStartY + 68 + 26 * i) * scale));
                    GlStateManager.scale(scale, scale, scale);

                    GuiUtils.drawScaledOnelineString(fontRenderer, blockStack.getDisplayName(), 75,
                            BgStartX + 242, BgStartY + 70 + 26 * i, 0xFAFAFA, 0.6D);
                    GuiUtils.drawScaledOnelineString(fontRenderer, I18n.format("gui.aiminfo.amount", info.connectedDevices.get(ids.get(ScrollBarListPos + i))), 75,
                            BgStartX + 242, BgStartY + 80 + 26 * i, 0xFAFAFA, 0.6D);
                }
            }

        }
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);

        //Status messages
        scale = 1F/0.75F;
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        this.fontRenderer.drawString(I18n.format("gui.aiminfo.problems"), (int) ((BgStartX + 64) * scale), (int) ((BgStartY + 106) * scale), 0xFAFAFA);
        GlStateManager.scale(scale, scale, scale);

        for (int i = 0; i < problemMessages.size(); i++) {
            GuiUtils.drawScaledOnelineString(fontRenderer, I18n.format(problemMessages.get(i)), 146, BgStartX + 70, BgStartY + 114 + 10*i, 15208978, 0.75F, false);
        }

        for (int i = 0; i < problemMessages.size(); i++) {
            if (mouseX >= BgStartX + 70 && mouseX <= BgStartX + 216 && mouseY >= BgStartY + 114 + 10*i && mouseY < BgStartY + 114 + 10*(i+1)) {
                String desc = I18n.format(problemMessages.get(i) + ".desc");
                this.drawHoveringText(Arrays.asList(desc.split("\\\\n")), BgStartX + 70, BgStartY + 114 + 10*i);
                break;
            }
        }


    }

    private void drawItemStack(@Nonnull ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(2F, 2F, 2F);
        itemRender.renderItemIntoGUI(stack, x / 2, y / 2);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
    }

    private int getListPosFromScrollbar() {
        if (this.ScrollBarPos == 0 || info.connectedDevices.size() < 5)
            return 0;
        int i = (int) Math.round((double) info.connectedDevices.size() * ((double) this.ScrollBarPos / (double) SCROLLBAR_RANGE));
        return i < info.connectedDevices.size() - 4 ? i : info.connectedDevices.size() - 4;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.equals(buttonBack)) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLoadingScreen());
            PacketHelper.wrapper.sendToServer(new PacketRequestServerInfo((short) 4));
        } else if (button.equals(buttonRefresh)) {
            PacketHelper.wrapper.sendToServer(new PacketRequestServerInfo((short) 6, info.corePos, info.coreDim));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private final int scrollBarStartX() {
        return BgStartX + SCROLLBAR_OFFSET_X;
    }

    private final int scrollBarStartY() {
        return BgStartY + SCROLLBAR_OFFSET_Y + ScrollBarPos;
    }

    private final int scrollBarEndX() {
        return scrollBarStartX() + SCROLLBAR_WIDTH;
    }

    private final int scrollBarEndY() {
        return scrollBarStartY() + SCROLLBAR_HEIGHT;
    }

    protected void keyTyped(char c, int id) throws IOException {
        if (c == 1 || id == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
        super.keyTyped(c, id);
    }

    protected void mouseClickMove(int x, int y, int button, long t) {
        if (this.ScrollBarActive) {
            ScrollBarPos = y - BgStartY - SCROLLBAR_OFFSET_Y - lastMouseY;
            if (ScrollBarPos < 0)
                ScrollBarPos = 0;
            else if (ScrollBarPos > SCROLLBAR_RANGE)
                ScrollBarPos = SCROLLBAR_RANGE;
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0 && this.ScrollBarActive) {
            this.ScrollBarActive = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void mouseClicked(int x, int y, int button) throws IOException {
        if (button == 0) {
            if (x >= scrollBarStartX() && x <= scrollBarEndX() && y >= scrollBarStartY() && y <= scrollBarEndY()) {
                this.ScrollBarActive = true;
                this.lastMouseY = y - scrollBarStartY();
            }
        }
        super.mouseClicked(x, y, button);
    }

}
