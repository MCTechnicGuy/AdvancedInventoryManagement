package com.mctechnicguy.aim.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;

public class ModCompatHelper {

    @Nullable
    public static Fluid xpjuice = null;
    public static boolean isIC2Loaded = false;

    public static void searchForModFluids() {
        if (FluidRegistry.isFluidRegistered("xpjuice")) xpjuice = FluidRegistry.getFluid("xpjuice");
    }

    public static void searchForCompatMods() {
        isIC2Loaded = Loader.isModLoaded("ic2");
    }

}
