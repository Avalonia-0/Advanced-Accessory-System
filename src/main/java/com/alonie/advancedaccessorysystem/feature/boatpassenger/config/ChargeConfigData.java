package com.alonie.advancedaccessorysystem.feature.boatpassenger.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record ChargeConfigData(
        BoatAutoPickUpRules allowedRules,
        double increaseValue,
        int chargeTime
) {
    private static final String INCREASE_VALUE_KEY = "increase_value";
    private static final String CHARGE_TIME_KEY = "charge_time";

    public static final ChargeConfigData DEFAULT = of(
            BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON
    );

    public static ChargeConfigData of(String rawJson) {
        if (rawJson == null || rawJson.isBlank() || BoatPassengerConfigHelper.isMalformedJson(rawJson)) {
            return new ChargeConfigData(
                    BoatAutoPickUpRules.parseChargeRules(BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON),
                    BoatPassengerConfigHelper.DEFAULT_CHARGE_INCREASE_VALUE,
                    BoatPassengerConfigHelper.DEFAULT_CHARGE_TIME
            );
        }

        return parse(com.google.gson.JsonParser.parseString(rawJson));
    }

    public static ChargeConfigData parse(JsonElement element) {
        if (!(element instanceof JsonObject object)) {
            return DEFAULT;
        }

        return new ChargeConfigData(
                BoatAutoPickUpRules.parseChargeRules(object),
                readDouble(object, INCREASE_VALUE_KEY, BoatPassengerConfigHelper.DEFAULT_CHARGE_INCREASE_VALUE),
                readInt(object, CHARGE_TIME_KEY, BoatPassengerConfigHelper.DEFAULT_CHARGE_TIME)
        );
    }

    public ChargeConfigData {
        allowedRules = allowedRules == null ? BoatAutoPickUpRules.createDefaultChargeRules() : allowedRules.copy();
        increaseValue = BoatPassengerConfigHelper.sanitizeChargeIncreaseValue(increaseValue);
        chargeTime = BoatPassengerConfigHelper.sanitizeChargeTime(chargeTime);
    }

    public ChargeConfigData withValues(double newIncreaseValue, int newChargeTime) {
        return new ChargeConfigData(this.allowedRules, newIncreaseValue, newChargeTime);
    }

    public boolean allows(Entity entity) {
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        return this.allowedRules.allows(entityId);
    }

    public JsonObject toJsonObject() {
        JsonObject object = this.allowedRules.toJsonObject();
        object.addProperty(INCREASE_VALUE_KEY, this.increaseValue);
        object.addProperty(CHARGE_TIME_KEY, this.chargeTime);
        return object;
    }

    public String toJsonString() {
        return toJsonObject().toString();
    }

    private static double readDouble(JsonObject object, String key, double fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? element.getAsDouble()
                : fallback;
    }

    private static int readInt(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? element.getAsInt()
                : fallback;
    }
}
