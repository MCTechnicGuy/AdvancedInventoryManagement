package com.mctechnicguy.aim.gui;

import javax.annotation.Nonnull;

public interface IManualEntry {

    @Nonnull
    String getManualName();

    int getPageCount();

    boolean doesProvideOwnContent();

    @Nonnull
    Object[] getParams(int page);

    boolean needsSmallerFont();
}
