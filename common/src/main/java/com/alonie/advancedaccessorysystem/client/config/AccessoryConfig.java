package com.alonie.advancedaccessorysystem.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Cloth Config POJO — replaces the manual Gson-based JSON persistence
 * in {@link AdvancedAccessorySystemConfigs}.
 *
 * <p>Annotated with {@code @Config} so Cloth Config AutoConfig automatically
 * handles serialization, deserialization, and GUI generation.
 */
@SuppressWarnings("FieldMayBeFinal")
@Config(name = "advanced_accessory_system")
public class AccessoryConfig implements ConfigData {

    // ---- Armor Visibility --------------------------------------------------

    @ConfigEntry.Category("armor_visibility")
    @ConfigEntry.Gui.Tooltip
    public boolean hideHelmet = false;

    @ConfigEntry.Category("armor_visibility")
    @ConfigEntry.Gui.Tooltip
    public boolean hideChestplate = false;

    @ConfigEntry.Category("armor_visibility")
    @ConfigEntry.Gui.Tooltip
    public boolean hideLeggings = false;

    @ConfigEntry.Category("armor_visibility")
    @ConfigEntry.Gui.Tooltip
    public boolean hideBoots = false;

    // ---- Parameters --------------------------------------------------------

    @ConfigEntry.Category("parameters")
    @ConfigEntry.Gui.Tooltip
    public double boatPassengerAutoRideRadius = 2.0;

    @ConfigEntry.Category("parameters")
    @ConfigEntry.Gui.Tooltip
    public double dismountPassengerLaunchSpeed = 0.8;

    @ConfigEntry.Category("parameters")
    @ConfigEntry.Gui.Tooltip
    public double chargeIncreaseValue = 0.08;

    @ConfigEntry.Category("parameters")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 200)
    @ConfigEntry.Gui.Tooltip
    public int chargeTime = 40;

    @Override
    public void validatePostLoad() throws ValidationException {
        if (boatPassengerAutoRideRadius < 0.0) boatPassengerAutoRideRadius = 2.0;
        if (dismountPassengerLaunchSpeed < 0.0) dismountPassengerLaunchSpeed = 0.8;
        if (chargeIncreaseValue < 0.0) chargeIncreaseValue = 0.08;
        if (chargeTime < 1) chargeTime = 40;
    }
}
