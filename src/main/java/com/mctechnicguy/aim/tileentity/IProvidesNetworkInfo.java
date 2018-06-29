package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public interface IProvidesNetworkInfo {

    @SideOnly(Side.CLIENT)
    String getNameForOverlay();

    @SideOnly(Side.CLIENT)
    void renderStatusInformation(NetworkInfoOverlayRenderer renderer);

    @SideOnly(Side.CLIENT)
    void invalidateServerInfo();

    /** Will only ever be called server-side **/
    @Nullable
    NBTTagCompound getTagForOverlayUpdate();

    /** Will only ever be called client-side **/
    void handleTagForOverlayUpdate(NBTTagCompound nbt);

}
