package com.mctechnicguy.aim.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtendedWorldData extends WorldSavedData {

	public static final String IDENTIFIER = "AIMData";

	public ExtendedWorldData(@Nonnull String tag) {
		super(tag);
	}

	@Nonnull
    public NBTTagCompound data = new NBTTagCompound();

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		data = compound.getCompoundTag("AIMData");
	}

	@Nonnull
    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("AIMData", data);
		return compound;
	}

	@Nullable
    public static ExtendedWorldData instance(@Nonnull World world) {
		if (world.getMapStorage().getOrLoadData(ExtendedWorldData.class, IDENTIFIER) == null)
			world.getMapStorage().setData(IDENTIFIER, new ExtendedWorldData(IDENTIFIER));
		ExtendedWorldData storage = (ExtendedWorldData) world.getMapStorage().getOrLoadData(ExtendedWorldData.class, IDENTIFIER);
		return storage;
	}

}
