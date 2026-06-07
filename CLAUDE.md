# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build all modules (Fabric + NeoForge)
./gradlew build

# Build a specific platform
./gradlew :fabric:build
./gradlew :neoforge:build

# Clean (removes refmap if any — will regenerate on next build)
./gradlew clean

# Run Minecraft client (NeoForge only — Fabric does not define runs in build.gradle)
./gradlew :neoforge:runClient

# Run dedicated server (NeoForge)
./gradlew :neoforge:runServer
```

- Java 21 required (JDK path configured in `gradle.properties`)
- Gradle 9.3.0 (wrapper bundled)
- Minecraft 1.21.11, mappings: official Mojang

## Important Known Issues

- **No refmap generated**: The Mixin annotation processor does not produce a `refmap.json`. Consequently, **`@Redirect` mixins will crash** in production because the obfuscated method names cannot be resolved. Always use `@Inject` at `HEAD`/`TAIL` for mixin injection points — these work without a refmap because they resolve at the method-entry level rather than the call-site level.
- **Trinkets jar uses intermediary names**: `TrinketInventory` overrides Minecraft's `Container` interface, so method names like `getItem`/`setItem` are remapped to intermediary names (e.g. `method_5438`) in the production jar. Use `findMethodByTypes()` (parameter/return-type matching) rather than `getMethod("getItem", ...)` when reflecting into Trinkets classes. See `ReflectionTrinketsHatBridge.java` for the pattern.

## Project Structure

```
advanced-accessory-system/
├── common/                          # Cross-platform game logic
│   └── src/main/java/com/alonie/advancedaccessorysystem/
│       ├── AdvancedAccessorySystemMod.java   # MOD_ID constant only
│       ├── bootstrap/                # Entry-point registrations per feature
│       │   ├── CommonBootstrap.java          # Server-side init (called by both platforms)
│       │   ├── ClientBootstrap.java          # Client-side init
│       │   └── <Feature>Bootstrap.java       # Per-feature bootstrap hooks
│       ├── bridge/                   # Architectury API facades
│       │   ├── PlatformLifecycle.java        # Lifecycle events (join/quit/tick)
│       │   └── PlatformNetworking.java       # C2S/S2C packet registration
│       ├── client/config/            # Config (Cloth Config with legacy Gson fallback)
│       ├── compat/trinkets/          # Trinkets integration (reflection-based, no compile dep)
│       ├── feature/
│       │   ├── accessory/            # Core: slot registry, capability tags
│       │   │   ├── capability/       # AccessoryCapability enum + HeadAccessoryCapabilities resolver
│       │   │   ├── slot/             # AccessorySlotRegistry + providers (vanilla_head, trinkets_hat)
│       │   │   └── state/            # Runtime pattern cache for item matching
│       │   ├── armorvisibility/      # Per-slot armor hide/show (client + network sync)
│       │   ├── boatpassenger/        # Auto-ride boats via head accessory
│       │   ├── headshulker/          # Open another player's head shulker box
│       │   ├── headslot/             # Allow all items in helmet slot (mixin + item rules)
│       │   └── ride/                 # Player-on-player riding via saddle accessory
│       └── mixin/                    # Mixins organized by area (headslot/, rendering/, ride/)
│           ├── headslot/             # ArmorSlot.mayPlace gate, ScreenHandler head-wear click
│           ├── rendering/            # LivingEntityRendererMixin (armor visibility),
│           │                         # HeadAccessoryTailMixin (@Inject TAIL rendering)
│           └── ride/                 # EntityRidePlayerMixin, RideCommandMixin, etc.
├── fabric/                           # Fabric platform entrypoint
│   └── src/main/java/.../fabric/
│       ├── AdvancedAccessorySystemFabric.java         # ModInitializer
│       ├── AdvancedAccessorySystemFabricClient.java   # ClientModInitializer
│       └── AccessoryModMenuIntegration.java           # ModMenu API
├── neoforge/                         # NeoForge platform entrypoint
│   └── src/main/java/.../neoforge/
│       ├── AdvancedAccessorySystemNeoForge.java       # @Mod
│       └── AdvancedAccessorySystemNeoForgeClient.java # @EventBusSubscriber
└── libs/                             # Bundled jars (Architectury, Fabric API, Trinkets, Cloth Config, etc.)
```

## Architecture Overview

### Multi-platform via Architectury

The mod targets **Fabric** and **NeoForge** simultaneously using [Architectury Loom](https://docs.architectury.dev/). All game logic lives in `:common` and uses Architectury's cross-platform event API (`dev.architectury.event.events.*`). Each platform module is a thin entrypoint that calls `CommonBootstrap.register()` (server) and `ClientBootstrap.register()` (client), then optionally registers platform-specific integrations (ModMenu, NeoForge key mappings).

### Accessory Slot System — the Unified Abstraction Layer

The core abstraction is the **accessory slot** — a unified interface over any head-equipment location:

```
AccessorySlotRegistry (统一查询入口)
  │ 注册
  ├── VanillaHeadSlotProvider   →  EquipmentSlot.HEAD
  └── TrinketsHatSlotProvider   →  Trinkets head/hat (反射桥接)
  │ 查询 (功能)
  ├── RideAccessoryHelper       (骑乘/船类检测)
  ├── ShulkerBoxCompat          (潜影盒检测)
  └── AccessorySlotRegistry.findFirst()
  │ 写入 (交互)
  └── AccessorySlotRegistry.tryEquip()

渲染: HeadAccessoryTailMixin
  └── @Inject at TAIL of extractRenderState()
      → 查找所有 Provider 中的任意非空物品
      → 通过引用恒等检查跳过原版头槽物品
      → 注入到 state.headItem → 原版 CustomHeadLayer 自动渲染
```

- `AccessorySlotProvider` (interface) — wraps a concrete head-slot location. Methods: `getStack()`, `setStack()`, `clearIf()`, `providesRender()`.
- `AccessorySlotRegistry` (static registry) — aggregates providers. Registration order determines query priority (first match wins).
- Adding a new head slot (Cosmetic Armor, Curios, etc.) = implement `AccessorySlotProvider` + register it in `CommonBootstrap`. Rendering and feature detection work automatically.

### Capability-Based Item Resolution

Instead of checking item IDs directly, items are resolved to `AccessoryCapability` tags via `HeadAccessoryCapabilities.resolve()`:

| Capability | Default Match Patterns | Feature |
|---|---|---|
| `PLAYER_RIDE` | `saddle`, `*_harness` | Player-on-player riding |
| `BOAT_PASSENGER` | `*_boat`, `*_raft`, `minecraft:minecart` | Auto-attract & mount |
| `HEAD_SHULKER_STORAGE` | Any `BlockItem` + `ShulkerBoxBlock` | Open shulker in head slot |

Pattern matching (`AccessoryPatternRuntimeState`) supports wildcards and exclusion rules, configurable at runtime.

### Rendering — No Custom RenderLayer

Rendering does NOT use a custom `RenderLayer`. Instead, `HeadAccessoryTailMixin` injects at `TAIL` of `LivingEntityRenderer.extractRenderState()` and sets `state.headItem` directly:

1. Skip if vanilla already set `headItem` (non-armor head items like pumpkins/skulls)
2. Find ANY non-empty item from all accessory providers
3. Skip if the found item is the vanilla head equipment (reference-identity check prevents double-rendering)
4. Resolve with `itemModelResolver.updateForLiving()` → vanilla `CustomHeadLayer` renders it

This approach works without a refmap (`@Inject` at `TAIL` is safe) and covers all rendering paths (armor models, skull models, item models) through the vanilla pipeline.

### Trinkets Integration

Trinkets support is purely reflection-based — no compile-time dependency:

- `TrinketsCompatInitializer` — checks for Trinkets at runtime via `Class.forName`, registers the predicate and reflection bridge
- `ReflectionTrinketsHatBridge` — reads/writes the Trinkets `head/hat` slot via reflection
  - Uses `findMethodByTypes()` instead of name-based `getMethod()` because `TrinketInventory` method names are remapped to intermediary in production
- `data/trinkets/slots/head/hat.json` — slot definition with `"replace": true`, custom validator predicate, and `"quick_move_predicates": ["trinkets:none"]` (prevents shift+click auto-equip)
- `data/trinkets/entities/player.json` — associates `head/hat` slot with players

### Feature Areas

| Feature | Description | Network |
|---|---|---|
| **Head Slot** | Any item can be placed in the player helmet slot via pickup-place | None |
| **Player Ride** | Right-click a player wearing a saddle-like item to ride them; mount/dismount/launch | C2S + S2C sync |
| **Boat Passenger** | Automatically ride boats when wearing a boat-like head accessory | C2S + S2C sync |
| **Head Shulker** | Open another player's head slot shulker box by right-clicking them | C2S request |
| **Armor Visibility** | Toggle helmet/chestplate/leggings/boots visibility per-slot | C2S update + S2C sync |
| **Auto-Equip** | Shift+right-click a player with a head-equippable item to equip it to their head | Event-driven |

### Networking

All networking uses Architectury's raw `NetworkManager` API (`Identifier` + `RegistryFriendlyByteBuf` — not `CustomPayload`). `PlatformNetworking` is the single registration point. Packet direction follows the `c2s/` (client→server) and `s2c/` (server→client) package convention.

### Configuration

Cloth Config is the primary config system, but the mod includes a manual Gson JSON fallback when Cloth Config is absent. `AdvancedAccessorySystemConfigs` is the static facade; `AccessoryConfig` is the Cloth Config POJO with `@Config` annotations. Config is synced from GUI to static fields via a save listener.

### Mixins

Registered in `advanced_accessory_system-common.mixins.json`. Categories:
- `headslot/` — helmet slot insertion gates (`ArmorSlot.mayPlace`, `ScreenHandler` head-wear click)
- `rendering/` — armor visibility suppression (`LivingEntityRendererMixin`), head-item injection for non-vanilla slots (`HeadAccessoryTailMixin`)
- `ride/` — entity riding mechanics, hitbox, critical hit, boat break, dismount handling
