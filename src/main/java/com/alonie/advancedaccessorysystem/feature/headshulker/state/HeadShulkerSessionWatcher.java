package com.alonie.advancedaccessorysystem.feature.headshulker.state;

public interface HeadShulkerSessionWatcher {
    boolean isStillValidRemoteUse();

    void forceClose();

    void refreshFromSession();
}
