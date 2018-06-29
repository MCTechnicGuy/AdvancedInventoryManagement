package com.mctechnicguy.aim.gui;

import javax.annotation.Nonnull;

public interface IManualEntry {

    @Nonnull
    String getManualName();

    int getPageCount();

    @Nonnull
    Object[] getParams(int page);
}
