package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.blocks.BlockFluidMoltenXP;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class GuiAIMGuide extends GuiScreen {

    private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID, "textures/gui/guiaimguide.png");
    static final ResourceLocation LogoTexture = new ResourceLocation(ModInfo.ID, "textures/gui/guiaimguide_2.png");


    // Width of the gui-image
    public static final int BGX = 320;
    // Height of the gui-image
    public static final int BGY = 197;
    // How many buttons to show before a button is needed to scroll through the content list
    private static final int BUTTONS_PER_CONTENT_PAGE = 24;
    public int BgStartX;
    public int BgStartY;
    private int currentPage;
    private int currentSubPage;
    private int tableOfContentButtonOffset = 0;
    private GuideTexturedButton pageBack;
    private GuideTexturedButton pageForward;
    private GuideTexturedButton buttonListUp;
    private GuideTexturedButton buttonListDown;
    @Nonnull
    private StringBuilder searchInput = new StringBuilder();

    @Nonnull
    public static ArrayList<IManualEntry> content = new ArrayList<>();
    @Nonnull
    public static HashMap<IManualEntry, ArrayList<IRecipe>> contentRecipes = new HashMap<>();
    @Nonnull
    private ArrayList<GuideInvisibleTextButton> contentButtons = new ArrayList<>();
    @Nonnull
    private ArrayList<GuideInvisibleTextButton> craftingButtons = new ArrayList<>();

    public static void addPreBlockPages() {
        content.add(new GuidePageGetStarted());
        content.add(new GuidePageNetworking());
        content.add(new GuidePagePower());
        content.add(new GuidePagePlayerAccess());
    }

    public GuiAIMGuide() {
        super();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(GuiTexture);
        GuiUtils.drawTexturedQuad(BgStartX, BgStartY, 0, 0, BGX, BGY, 512, zLevel);
        if (currentPage > 1) pageBack.drawButton(mc, mouseX, mouseY, partialTicks);
        if (currentPage <= content.size()) pageForward.drawButton(mc, mouseX, mouseY, partialTicks);

        if (currentPage > 1) {
            if (content.get(currentPage - 2) instanceof ICustomManualEntry) {
                ICustomManualEntry entry = (ICustomManualEntry)content.get(currentPage - 2);
                if (entry.showCraftingRecipe(currentSubPage)) displayCraftingRecipe();
                else if (entry.hasLeftSidePicture(currentSubPage)) entry.drawLeftSidePicture(currentSubPage, mc, this, zLevel);
                if (entry.showHeaderOnPage(currentSubPage)) displayHeader();
                displayPageText();
            } else {
                displayHeader();
                if (currentSubPage == 0) displayCraftingRecipe();
                displayPageText();
            }
        } else {
            if (buttonListUp.enabled) buttonListUp.drawButton(mc, mouseX, mouseY, partialTicks);
            if (buttonListDown.enabled) buttonListDown.drawButton(mc, mouseX, mouseY, partialTicks);
            displayIntroduction();
            displayTableOfContents(mouseX, mouseY);
        }
    }

    private void displayHeader() {
        GlStateManager.scale(0.9, 0.9, 0.9);
        String header = I18n.format("guide.header." + content.get(currentPage - 2).getManualName());
        fontRenderer.drawString(header, (int)Math.round((BgStartX + (BGX / 4) + 4 - (fontRenderer.getStringWidth(header) / 2)) * (1/0.9)), (int)Math.round((BgStartY + 15) * (1/0.9)), 4210752);
        GlStateManager.scale(1/0.9,1/0.9,1/0.9);
    }

    private void displayIntroduction() {
        mc.getTextureManager().bindTexture(LogoTexture);
        GuiUtils.drawScaledTexturedQuad(BgStartX + 15, BgStartY + 30, 0, 512, 512, 512 + 210,512, (BGX / 2 - 30), 210 * ((BGX / 2D - 30) / 512D), zLevel);
        String textToPrint = I18n.format("guide.content.introduction", mc.gameSettings.keyBindInventory.getDisplayName());
        GuiUtils.drawScaledMultilineString(fontRenderer, textToPrint, BgStartX +  + 20, BgStartY + 90, BGX / 2 - 30, (int)Math.round(BGY - (210 * ((BGX / 2D - 30) / 512D)) - 30), 4210752, 0.1, 0.75);
    }

    private void displayTableOfContents(int mouseX, int mouseY) {
        GlStateManager.scale(0.9, 0.9, 0.9);
        String header = searchInput.length() == 0 ? I18n.format("guide.header.tableofcontents") : fontRenderer.getStringWidth(searchInput.toString()) > BGX / 2 - 5 && searchInput.length() > 30 ? searchInput.toString().substring(0, 30) + "..." : searchInput.toString();
        fontRenderer.drawString(header, (int)Math.round((BgStartX + (BGX * 0.75) + 4 - (fontRenderer.getStringWidth(header) / 2)) * (1/0.9)), (int)Math.round((BgStartY + 10) * (1/0.9)), 4210752);
        GlStateManager.scale(1/0.9,1/0.9,1/0.9);
        for (GuideInvisibleTextButton contentButton : contentButtons) {
            contentButton.drawButtonWithCustomText(mc, mouseX, mouseY, I18n.format("guide.header." + content.get(contentButton.id - 12).getManualName()));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { //ESCAPE
            if (currentPage == 1) super.keyTyped(typedChar, keyCode);
            else {
                setPage(1);
            }
        } else if (currentPage > 1 && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            setPage(1);
        } else if (currentPage == 1 && typedChar > 31 && typedChar < 127) {
            searchInput.append(typedChar);
            tableOfContentButtonOffset = 0;
            buttonListUp.enabled = false;
            updateSearch(false);
        } else if (currentPage == 1 && searchInput.length() > 0 && (typedChar == 127 || typedChar == 8)) {
            searchInput.delete(searchInput.length() - 1, searchInput.length());
            tableOfContentButtonOffset = 0;
            buttonListUp.enabled = false;
            updateSearch(searchInput.length() == 0);
        }
    }

    private void updateSearch(boolean reset) {
        if (reset) {
            buttonList.removeAll(contentButtons);
            contentButtons.clear();
            for (int i = 0; i < Math.min(content.size(), BUTTONS_PER_CONTENT_PAGE); i++) {
                GuideInvisibleTextButton button = new GuideInvisibleTextButton(i + 12, BgStartX + (BGX / 2) - 15, BgStartY + 20 + i * 7, BGX / 5, 7, I18n.format("guide.header." + content.get(i).getManualName()));
                buttonList.add(button);
                contentButtons.add(button);
            }
            if (buttonListDown != null) buttonListDown.enabled = true;
        } else {
            buttonList.removeAll(contentButtons);
            contentButtons.clear();
            ArrayList<Integer> results = new ArrayList<>();
            int btnCount = 0;

            for (int i = 0; i < content.size(); i++) {
                if (I18n.format("guide.header." + content.get(i).getManualName()).toLowerCase().contains(searchInput.toString().toLowerCase())) {
                    results.add(i);
                }
            }

            int deletionOffset = 0;
            if (results.size() > BUTTONS_PER_CONTENT_PAGE) {
                deletionOffset = tableOfContentButtonOffset;
                buttonListDown.enabled = true;
            }
            else buttonListDown.enabled = false;

            for (Integer result : results) {

                if (deletionOffset > 0) {
                    deletionOffset--;
                    continue;
                }
                GuideInvisibleTextButton button = new GuideInvisibleTextButton(result + 12, BgStartX + (BGX / 2) - 15, BgStartY + 20 + btnCount * 7, BGX / 5, 7, I18n.format("guide.header." + content.get(result).getManualName()));
                buttonList.add(button);
                contentButtons.add(button);
                btnCount++;
                if (btnCount >= BUTTONS_PER_CONTENT_PAGE) break;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 1) { //RMB
            setPage(1);
        } else super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    private void displayCraftingRecipe() {
        ArrayList<IRecipe> foundRecipes = contentRecipes.get(content.get(currentPage - 2));
        if (foundRecipes != null && !foundRecipes.isEmpty() && foundRecipes.size() > currentSubPage && foundRecipes.get(currentSubPage) != null) {
            IRecipe recipeToShow = foundRecipes.get(currentSubPage);
            RenderHelper.enableGUIStandardItemLighting();

            GlStateManager.scale(4D, 4D, 4D);
            itemRender.renderItemIntoGUI(recipeToShow.getRecipeOutput(), (int)Math.round((BgStartX + 45.7D) * 0.25D), (int)Math.round((BgStartY + 30) * 0.25D));
            GlStateManager.scale(0.25D, 0.25D, 0.25D);

            NonNullList<Ingredient> ings = recipeToShow.getIngredients();

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (ings.size() > x * 3 + y && ings.get(x * 3 + y) != Ingredient.EMPTY) {
                        if (ings.get(x * 3 + y).getMatchingStacks().length > 0) {
                            ItemStack toRender = ings.get(x * 3 + y).getMatchingStacks()[0];
                            itemRender.renderItemIntoGUI(toRender, BgStartX + 48 + (y * 20), BgStartY + 100 + (x * 20));
                        }
                    }
                }
            }
        } else if (content.get(currentPage - 2) instanceof BlockFluidMoltenXP) { //Extra-case for molten xp (which has no crafting recipe)
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.scale(4D, 4D, 4D);
            itemRender.renderItemIntoGUI(new ItemStack(ModElementList.blockMoltenXP), (int)Math.round((BgStartX + 45.7D) * 0.25D), (int)Math.round((BgStartY + 30) * 0.25D));
            GlStateManager.scale(0.25D, 0.25D, 0.25D);
        }
    }

    private void displayPageText() {
        String textToPrint = I18n.format("guide.content." + content.get(currentPage - 2).getManualName() + (currentSubPage > 0 ? String.valueOf(currentSubPage) : ""), content.get(currentPage - 2).getParams(currentSubPage)).replace("\\n", "\n");
        GuiUtils.drawScaledMultilineString(fontRenderer, textToPrint, BgStartX + (BGX / 2) + 15, BgStartY + 15, BGX / 2 - 30, BGY - 50, 4210752, 0.1, 0.75);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        contentButtons.clear();
        craftingButtons.clear();
        setPage(1);
        BgStartX = (this.width / 2) - (BGX / 2);
        BgStartY = (this.height / 2) - (BGY / 2);
        pageBack = new GuideTexturedButton(0, BgStartX + 15, BgStartY + BGY - 25, 29, 16, 0, BGY);
        pageForward = new GuideTexturedButton(1, BgStartX + BGX - 40, BgStartY + BGY - 25, 29, 16, 30, BGY);
        this.buttonList.add(pageBack);
        this.buttonList.add(pageForward);
        buttonListUp = new GuideTexturedButton(10001, BgStartX + BGX - 45, BgStartY + 25, 14, 8, 67, 197);
        buttonListDown = new GuideTexturedButton(10002, BgStartX + BGX - 45, BgStartY + BGY - 50, 14, 8, 67, 205);
        buttonListUp.enabled = false;
        this.buttonList.add(buttonListUp);
        this.buttonList.add(buttonListDown);


        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                GuideInvisibleTextButton craftingButton = new GuideInvisibleTextButton(x * 3 + y + 2, BgStartX + 48 + (y * 20), BgStartY + 100 + (x * 20), 16, 16, "");
                buttonList.add(craftingButton);
                craftingButtons.add(craftingButton);
            }
        }

        for (int i = 0; i < Math.min(content.size(), BUTTONS_PER_CONTENT_PAGE); i++) {
            GuideInvisibleTextButton button = new GuideInvisibleTextButton(i + 12, BgStartX + (BGX / 2) - 15, BgStartY + 20 + i * 7, BGX / 5, 7, I18n.format("guide.header." + content.get(i).getManualName()));
            buttonList.add(button);
            contentButtons.add(button);
        }
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        if (button == pageBack) {
            if (currentPage > 1 && currentSubPage > 0) setCurrentSubPage(currentSubPage - 1);
            else if (currentPage > 1) {
                setPage(currentPage - 1);
            }
        }
        else if (button == buttonListUp && currentPage == 1) {
            if (tableOfContentButtonOffset > 0) {
                tableOfContentButtonOffset--;
                updateSearch(!(searchInput.length() > 0) && tableOfContentButtonOffset <= 0);
                buttonListDown.enabled = true;
            }
            if (tableOfContentButtonOffset <= 0) {
                button.enabled = false;
            }
        }
        else if (button == buttonListDown && currentPage == 1) {
            if (BUTTONS_PER_CONTENT_PAGE + tableOfContentButtonOffset < content.size()) {
                tableOfContentButtonOffset++;
                updateSearch(false);
                buttonListUp.enabled = true;
            }
            if (BUTTONS_PER_CONTENT_PAGE + tableOfContentButtonOffset >= content.size()) {
                button.enabled = false;
            }
        }
        else if (button == pageForward) {
            if (currentPage > 1 && currentSubPage < content.get(currentPage - 2).getPageCount() - 1) setCurrentSubPage(currentSubPage + 1);
            else if (currentPage <= content.size()) {
                setPage(currentPage + 1);
            }
        } else if (button.id > 1 && button.id < 11 && currentPage > 1) { //Crafting field buttons
            ArrayList<IRecipe> foundRecipes = contentRecipes.get(content.get(currentPage - 2));
            if (foundRecipes != null && !foundRecipes.isEmpty() && foundRecipes.size() > currentSubPage) {
                IRecipe recipeToShow = foundRecipes.get(currentSubPage);
                if (recipeToShow != null) {

                    NonNullList<Ingredient> ings = recipeToShow.getIngredients();

                    if (ings.size() > button.id - 2 && ings.get(button.id - 2) != Ingredient.EMPTY && ings.get(button.id - 2).getMatchingStacks().length > 0) {
                        ItemStack clickedItem = ings.get(button.id - 2).getMatchingStacks()[0];
                        if (clickedItem.getItem() instanceof ItemBlock && Block.getBlockFromItem(clickedItem.getItem()) instanceof IManualEntry) {
                            if (content.indexOf(Block.getBlockFromItem(clickedItem.getItem())) > 1) {
                                setPage(content.indexOf(Block.getBlockFromItem(clickedItem.getItem())) + 2);
                            }
                        } else if (clickedItem.getItem() instanceof IManualEntry) {
                            if (content.indexOf(clickedItem.getItem()) > 1) {
                                setPage(content.indexOf(clickedItem.getItem()) + 2);
                            }
                        }
                    }
                }
            }

        } else if (button.id > 11 && button.id < 1000 && currentPage == 1) { //Table of content buttons
            setPage(button.id - 10);
        }

    }

    private void setPage(int page) {
        currentPage = page;
        currentSubPage = 0;

        if (currentPage > 1) {
            for (GuiButton b : contentButtons) b.enabled = false;
            if (!(content.get(currentPage - 2) instanceof ICustomManualEntry) || (((ICustomManualEntry)content.get(currentPage - 2)).showCraftingRecipe(currentSubPage))) {
                for (GuiButton b : craftingButtons) b.enabled = true;
            }
            buttonListDown.enabled = false;
            buttonListUp.enabled = false;
            tableOfContentButtonOffset = 0;
        } else {
            for (GuiButton b : contentButtons) b.enabled = true;
            for (GuiButton b : craftingButtons) b.enabled = false;
            tableOfContentButtonOffset = 0;
            if (buttonListDown != null) buttonListDown.enabled = true;
            if (buttonListUp != null) buttonListUp.enabled = false;
            if (searchInput.length() > 0) {
                updateSearch(false);
            }
        }
    }

    private void setCurrentSubPage(int subPage) {
        currentSubPage = subPage;
        if (currentPage > 1 && (!(content.get(currentPage - 2) instanceof ICustomManualEntry)) || (((ICustomManualEntry)content.get(currentPage - 2)).showCraftingRecipe(currentSubPage))) {
            for (GuiButton b : craftingButtons) b.enabled = true;
        }
    }

}
