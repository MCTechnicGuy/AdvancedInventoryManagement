package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.gui.GuiAIMGuide;
import com.mctechnicguy.aim.gui.ICustomManualEntry;
import com.mctechnicguy.aim.tileentity.TileEntityScanner;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockScanner extends BlockAIMMachine implements ICustomManualEntry{

	public static final String NAME = "scanner";
	public static final PropertyEnum ISACTIVE = PropertyEnum.create("isactive", EnumActivity.class);

	public BlockScanner() {
		super(NAME);
		this.useNeighborBrightness = true;
		this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, EnumActivity.OFFLINE));
	}

	@Override
	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityScanner();
	}

	@Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		if (worldIn.getTileEntity(pos) instanceof TileEntityScanner) {
			return state.withProperty(ISACTIVE,
					((TileEntityScanner) worldIn.getTileEntity(pos)).hasCore()
							? ((TileEntityScanner) worldIn.getTileEntity(pos)).isActive() ? EnumActivity.ONLINE
									: EnumActivity.IDLE : EnumActivity.OFFLINE);
		} else
			return state.withProperty(ISACTIVE, EnumActivity.OFFLINE);
	}

	@Override
	public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, Entity entityIn) {
		if (worldIn.isRemote)
			return;
		if (worldIn.getTileEntity(pos) instanceof TileEntityScanner && entityIn instanceof EntityPlayer) {
			((TileEntityScanner) worldIn.getTileEntity(pos)).doStartCheck((EntityPlayer) entityIn);
		}
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ISACTIVE);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ISACTIVE, EnumActivity.OFFLINE);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN || face == EnumFacing.UP;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}


	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}


	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	public String getManualName() {
		return NAME;
	}

	@Override
	public int getPageCount() {
		return 2;
	}

	@Override
	public boolean doesProvideOwnContent() {
		return true;
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[0];
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
		return page != 0;
	}

	@Override
	public void drawLeftSidePicture(int page, @Nonnull Minecraft mc, @Nonnull GuiAIMGuide gui, float zLevel) {
		GuiAIMGuide.drawScaledTexturedQuad(gui.BgStartX + 15, gui.BgStartY + (GuiAIMGuide.BGY / 2D) - 76, 163, 320, 400, 512, GuiAIMGuide.BGX / 2D - 30, 193 * ((GuiAIMGuide.BGX / 2D - 30) / 237), zLevel);
		GlStateManager.scale(0.75, 0.75, 0.75);
		mc.fontRenderer.drawSplitString(I18n.format("guide.picture.scanner"), (int)Math.round((gui.BgStartX + 15) * (1/0.75D)), (int)Math.round((gui.BgStartY + 130) * (1/0.75D)), (int)Math.round((GuiAIMGuide.BGX / 2 - 30) * (1/0.75D)), 4210752);
		GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
	}

	@Nullable
	@Override
	public ResourceLocation getRecipeForPage(int page) {
		return null;
	}

	@Override
	public boolean showHeaderOnPage(int page) {
		return page == 0;
	}

	public enum EnumActivity implements IStringSerializable {
		OFFLINE(0, "offline"), IDLE(1, "idle"), ONLINE(2, "online");

		private int id;
		private String name;

		private EnumActivity(int id, String name) {
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

	}

}
