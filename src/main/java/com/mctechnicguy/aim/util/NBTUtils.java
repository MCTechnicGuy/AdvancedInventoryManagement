package com.mctechnicguy.aim.util;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class NBTUtils {

	public static void writeToNBTFile(@Nonnull TileEntityAIMCore core, @Nonnull UUID playerID) {
		try {
			if (!core.getWorld().isRemote) {
				NBTTagCompound mainTag = readNBTFromFile(core.getWorld());
				writeDataToTag(mainTag, core, playerID);
				ExtendedWorldData.instance(core.getWorld()).markDirty();
			}
		} catch (Exception exception) {
			AdvancedInventoryManagement.log.log(Level.ERROR, "Error writing file to world directory.");
			exception.printStackTrace();
		}
	}

	@Nonnull
    private static NBTTagCompound getCoreInfoTag(@Nonnull TileEntityAIMCore core) {
		NBTTagCompound coreInfo = new NBTTagCompound();
		coreInfo.setInteger("DimID", core.getWorld().provider.getDimension());
		coreInfo.setString("DimName", core.getWorld().provider.getDimensionType().getName());
		coreInfo.setInteger("coreX", core.getPos().getX());
		coreInfo.setInteger("coreY", core.getPos().getY());
		coreInfo.setInteger("coreZ", core.getPos().getZ());
		return coreInfo;
	}

	private static void writeDataToTag(@Nonnull NBTTagCompound mainTag, @Nonnull TileEntityAIMCore core, @Nonnull UUID playerID) {
		NBTBase coreTags = mainTag.getTag(playerID.toString());
		if (coreTags == null || !(coreTags instanceof NBTTagList))
			coreTags = new NBTTagList();
		boolean writeNew = true;
		for (int i = 0; i < ((NBTTagList) coreTags).tagCount(); i++) {
			NBTTagCompound ct = ((NBTTagList) coreTags).getCompoundTagAt(i);
			if (ct.getInteger("DimID") == core.getWorld().provider.getDimension() && ct.getInteger("coreX") == core.getPos().getX()
					&& ct.getInteger("coreY") == core.getPos().getY() && ct.getInteger("coreZ") == core.getPos().getZ()) {
				ct.setString("DimName", core.getWorld().provider.getDimensionType().getName());
				writeNew = false;
			}
		}
		if (writeNew) {
			((NBTTagList) coreTags).appendTag(getCoreInfoTag(core));
			mainTag.setTag(playerID.toString(), coreTags);
		}
	}

	@Nonnull
	private static NBTTagCompound readNBTFromFile(@Nonnull World world) {
		return ExtendedWorldData.instance(world).data;
	}

	@Nullable
	public static NBTTagList readPlayerInfo(@Nonnull UUID playerID, @Nonnull World world) {
		if (!world.isRemote) {
			NBTTagCompound mainTag = readNBTFromFile(world);
			if (!(mainTag.getTag(playerID.toString()) instanceof NBTTagList))
				return null;
			else
				return (NBTTagList) mainTag.getTag(playerID.toString());
		}
		return null;
	}

	public static void deleteCoreInfoFromTagList(@Nonnull UUID playerID, @Nonnull TileEntityAIMCore core) {
		if (!core.getWorld().isRemote) {
			NBTTagList list = readPlayerInfo(playerID, core.getWorld());
			if (list == null)
				return;
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound ct = list.getCompoundTagAt(i);
				if (ct.getInteger("DimID") == core.getWorld().provider.getDimension() && ct.getInteger("coreX") == core.getPos().getX()
						&& ct.getInteger("coreY") == core.getPos().getY() && ct.getInteger("coreZ") == core.getPos().getZ()) {
					list.removeTag(i);
				}
			}
			if (list.tagCount() == 0) {
				NBTTagCompound mainTag = readNBTFromFile(core.getWorld());
				mainTag.removeTag(playerID.toString());
			}
			ExtendedWorldData.instance(core.getWorld()).markDirty();
		}
	}
}
