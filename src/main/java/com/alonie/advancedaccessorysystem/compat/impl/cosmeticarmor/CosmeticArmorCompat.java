package com.alonie.advancedaccessorysystem.compat.impl.cosmeticarmor;

import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;
import com.mojang.datafixers.util.Function3;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public final class CosmeticArmorCompat {
    private static final Identifier COSMETIC_HEAD_PREDICATE_ID = Identifier.of("cosmeticarmor", "head");
    private static final TagKey<Item> COSMETIC_BLACKLIST_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of("cosmeticarmor", "blacklist"));
    private static final AtomicBoolean RENDERER_REGISTERED = new AtomicBoolean(false);

    private CosmeticArmorCompat() {
    }

    public static boolean isAvailable() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded("trinkets") && loader.isModLoaded("cosmeticarmor");
    }

    public static void initCommon() {
        if (!isAvailable()) {
            return;
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> registerHeadPredicateOverride());
    }

    public static void initClient() {
        if (!isAvailable()) {
            return;
        }

        registerHeadPredicateOverride();
        registerHeadRenderer();
    }

    private static void registerHeadPredicateOverride() {
        try {
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Method registerPredicate = trinketsApiClass.getMethod(
                    "registerTrinketPredicate",
                    Identifier.class,
                    Function3.class
            );

            @SuppressWarnings({"rawtypes", "unchecked"})
            Function3 predicate = new Function3<ItemStack, Object, LivingEntity, TriState>() {
                @Override
                public TriState apply(ItemStack stack, Object slotReference, LivingEntity entity) {
                    return evaluateHeadPredicate(stack, entity);
                }
            };

            registerPredicate.invoke(null, COSMETIC_HEAD_PREDICATE_ID, predicate);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void registerHeadRenderer() {
        if (!RENDERER_REGISTERED.compareAndSet(false, true)) {
            return;
        }

        try {
            Class<?> rendererInterface = Class.forName("dev.emi.trinkets.api.client.TrinketRenderer");
            Class<?> registryClass = Class.forName("dev.emi.trinkets.api.client.TrinketRendererRegistry");
            Method registerRenderer = registryClass.getMethod("registerRenderer", Item.class, rendererInterface);

            InvocationHandler handler = (proxy, method, args) -> {
                if (!"render".equals(method.getName()) || args == null || args.length < 9) {
                    return null;
                }

                ItemStack stack = (ItemStack) args[0];
                Object slotReference = args[1];
                @SuppressWarnings("unchecked")
                EntityModel<?> contextModel = (EntityModel<?>) args[2];
                MatrixStack matrices = (MatrixStack) args[3];
                OrderedRenderCommandQueue renderQueue = (OrderedRenderCommandQueue) args[4];
                int light = (Integer) args[5];

                if (stack == null || stack.isEmpty()) {
                    return null;
                }
                if (!isCosmeticHeadSlot(slotReference)) {
                    return null;
                }
                if (!AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem())) {
                    return null;
                }

                renderStackOnHead(MinecraftClient.getInstance(), stack, contextModel, matrices, renderQueue, light);
                return null;
            };

            Object proxy = Proxy.newProxyInstance(
                    CosmeticArmorCompat.class.getClassLoader(),
                    new Class<?>[]{rendererInterface},
                    handler
            );

            for (Item item : Registries.ITEM) {
                if (!AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(item)) {
                    continue;
                }
                registerRenderer.invoke(null, item, proxy);
            }
        } catch (Throwable ignored) {
        }
    }

    private static TriState evaluateHeadPredicate(ItemStack stack, LivingEntity entity) {
        if (stack.isIn(COSMETIC_BLACKLIST_TAG)) {
            return TriState.FALSE;
        }

        if (entity.getPreferredEquipmentSlot(stack) == EquipmentSlot.HEAD) {
            return TriState.TRUE;
        }

        if (AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem())) {
            return TriState.TRUE;
        }

        return TriState.DEFAULT;
    }



    public record CosmeticHeadSlotRef(Object inventory, int slotIndex, ItemStack stack) {
    }

    public static CosmeticHeadSlotRef getCosmeticHeadSlotRef(LivingEntity entity) {
        if (!isAvailable() || entity == null) {
            return null;
        }

        try {
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Method getTrinketComponent = trinketsApiClass.getMethod("getTrinketComponent", LivingEntity.class);
            Object optionalComponent = getTrinketComponent.invoke(null, entity);
            if (!(optionalComponent instanceof java.util.Optional<?> optional) || optional.isEmpty()) {
                return null;
            }

            Object component = optional.get();
            Method getInventory = component.getClass().getMethod("getInventory");
            Object inventoryObject = getInventory.invoke(component);
            if (!(inventoryObject instanceof java.util.Map<?, ?> inventoryMap)) {
                return null;
            }

            Object headGroup = inventoryMap.get("head");
            if (!(headGroup instanceof java.util.Map<?, ?> headSlots)) {
                return null;
            }

            Object cosmeticInventory = headSlots.get("cosmetic");
            return extractFirstSlotRef(cosmeticInventory);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static ItemStack getCosmeticHeadStack(LivingEntity entity) {
        if (!isAvailable()) {
            return ItemStack.EMPTY;
        }
        if (entity == null) {
            return ItemStack.EMPTY;
        }

        try {
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Method getTrinketComponent = trinketsApiClass.getMethod("getTrinketComponent", LivingEntity.class);
            Object optionalComponent = getTrinketComponent.invoke(null, entity);
            if (!(optionalComponent instanceof java.util.Optional<?> optional)) {
                return ItemStack.EMPTY;
            }
            if (optional.isEmpty()) {
                return ItemStack.EMPTY;
            }

            Object component = optional.get();
            Method getInventory = component.getClass().getMethod("getInventory");
            Object inventoryObject = getInventory.invoke(component);
            if (!(inventoryObject instanceof java.util.Map<?, ?> inventoryMap)) {
                return ItemStack.EMPTY;
            }

            Object headGroup = inventoryMap.get("head");
            if (!(headGroup instanceof java.util.Map<?, ?> headSlots)) {
                return ItemStack.EMPTY;
            }

            Object cosmeticInventory = headSlots.get("cosmetic");
            CosmeticHeadSlotRef slotRef = extractFirstSlotRef(cosmeticInventory);
            return slotRef == null ? ItemStack.EMPTY : slotRef.stack();
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean clearCosmeticHeadStack(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (!isAvailable() || entity == null || predicate == null) {
            return false;
        }

        CosmeticHeadSlotRef slotRef = getCosmeticHeadSlotRef(entity);
        if (slotRef == null || slotRef.stack() == null || slotRef.stack().isEmpty() || !predicate.test(slotRef.stack())) {
            return false;
        }

        if (!writeStack(slotRef.inventory(), slotRef.slotIndex(), ItemStack.EMPTY)) {
            return false;
        }

        if (entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            player.playerScreenHandler.sendContentUpdates();
        }

        return true;
    }

    private static CosmeticHeadSlotRef extractFirstSlotRef(Object inventory) {
        if (inventory == null) {
            return null;
        }

        Class<?> inventoryClass = inventory.getClass();

        Method sizeMethod = findNoArgMethod(inventoryClass, "size", "method_5439");
        Method getStackMethod = findIntArgMethod(inventoryClass, "getStack", "method_5438");

        if (sizeMethod != null && getStackMethod != null) {
            try {
                Object sizeResult = sizeMethod.invoke(inventory);
                if (sizeResult instanceof Integer size) {
                    for (int i = 0; i < size; i++) {
                        Object stackObject = getStackMethod.invoke(inventory, i);
                        if (stackObject instanceof ItemStack stack && !stack.isEmpty()) {
                            return new CosmeticHeadSlotRef(inventory, i, stack);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        if (getStackMethod != null) {
            try {
                Object stackObject = getStackMethod.invoke(inventory, 0);
                if (stackObject instanceof ItemStack stack) {
                    return new CosmeticHeadSlotRef(inventory, 0, stack);
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static Method findNoArgMethod(Class<?> type, String... names) {
        for (String name : names) {
            try {
                Method method = type.getMethod(name);
                method.setAccessible(true);
                return method;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static Method findIntArgMethod(Class<?> type, String... names) {
        for (String name : names) {
            try {
                Method method = type.getMethod(name, int.class);
                method.setAccessible(true);
                return method;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static boolean writeStack(Object inventory, int slotIndex, ItemStack stack) {
        if (inventory == null) {
            return false;
        }

        Method setStackMethod = findInventorySetStackMethod(inventory.getClass());
        if (setStackMethod == null) {
            return false;
        }

        try {
            setStackMethod.invoke(inventory, slotIndex, stack);
            Method markDirtyMethod = findVoidNoArgMethod(inventory.getClass(), "markDirty", "method_5431");
            if (markDirtyMethod != null) {
                markDirtyMethod.invoke(inventory);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Method findInventorySetStackMethod(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() == 2
                    && method.getParameterTypes()[0] == int.class
                    && ItemStack.class.isAssignableFrom(method.getParameterTypes()[1])
                    && method.getReturnType() == Void.TYPE) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    private static Method findVoidNoArgMethod(Class<?> type, String... names) {
        for (String name : names) {
            try {
                Method method = type.getMethod(name);
                if (method.getReturnType() == Void.TYPE) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static boolean isCosmeticHeadSlot(Object slotReference) {
        if (slotReference == null) {
            return false;
        }
        try {
            Method inventoryMethod = slotReference.getClass().getMethod("inventory");
            Object inventory = inventoryMethod.invoke(slotReference);
            if (inventory == null) {
                return false;
            }
            Method getSlotTypeMethod = inventory.getClass().getMethod("getSlotType");
            Object slotType = getSlotTypeMethod.invoke(inventory);
            if (slotType == null) {
                return false;
            }
            Method getGroupMethod = slotType.getClass().getMethod("getGroup");
            Method getNameMethod = slotType.getClass().getMethod("getName");
            String group = (String) getGroupMethod.invoke(slotType);
            String name = (String) getNameMethod.invoke(slotType);
            return "head".equals(group) && "cosmetic".equals(name);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void renderStackOnHead(MinecraftClient client,
                                          ItemStack stack,
                                          EntityModel<?> contextModel,
                                          MatrixStack matrices,
                                          OrderedRenderCommandQueue renderQueue,
                                          int light) {
        if (!(contextModel instanceof ModelWithHead modelWithHead)) {
            return;
        }

        matrices.push();
        modelWithHead.getHead().applyTransform(matrices);
        matrices.translate(0.0F, -0.25F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(0.625F, -0.625F, -0.625F);

        ItemRenderState itemRenderState = new ItemRenderState();
        client.getItemModelManager().clearAndUpdate(
                itemRenderState,
                stack,
                ItemDisplayContext.HEAD,
                client.world,
                null,
                0
        );
        itemRenderState.render(
                matrices,
                renderQueue,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );
        matrices.pop();
    }
}
