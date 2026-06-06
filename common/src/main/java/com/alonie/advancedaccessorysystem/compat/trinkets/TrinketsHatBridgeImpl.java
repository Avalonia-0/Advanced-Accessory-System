package com.alonie.advancedaccessorysystem.compat.trinkets;

/**
 * Static holder for the TrinketsHatBridge.
 *
 * <p>On Fabric with Trinkets installed, {@link #register(TrinketsHatBridge)} is
 * called during mod init so the rest of the mod can query the Trinkets hat slot.
 * On NeoForge (or Fabric without Trinkets) the bridge stays null and
 * {@link #isAvailable()} returns false — no Trinkets integration runs.
 */
public final class TrinketsHatBridgeImpl {
    private static TrinketsHatBridge bridge;

    private TrinketsHatBridgeImpl() {
    }

    public static void register(TrinketsHatBridge impl) {
        bridge = impl;
    }

    public static TrinketsHatBridge get() {
        return bridge;
    }

    public static boolean isAvailable() {
        return bridge != null;
    }
}
