# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build all modules (Fabric + NeoForge)
./gradlew build

# Build a specific platform
./gradlew :fabric:build
./gradlew :neoforge:build

# Clean
./gradlew clean

# Run Minecraft client (NeoForge only — Fabric does not define runs in build.gradle)
./gradlew :neoforge:runClient

# Run dedicated server (NeoForge)
./gradlew :neoforge:runServer
```

- Java 21 required (JDK path configured in `gradle.properties`)
- Gradle 9.3.0 (wrapper bundled)
- Minecraft 1.21.11, mappings: official Mojang

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
│           ├── headslot/             # PlayerScreenHandlerHeadSlotMixin, ScreenHandlerHeadWearClickMixin
│           ├── rendering/            # LivingEntityRendererMixin (armor visibility)
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
├── libs/                             # Bundled jars (Architectury, Fabric API, Trinkets, Cloth Config, etc.)
└── data/                             # Trinkets slot definitions (data-pack overrides)
```

## Architecture Overview

### Multi-platform via Architectury

The mod targets **Fabric** and **NeoForge** simultaneously using [Architectury Loom](https://docs.architectury.dev/). All game logic lives in `:common` and uses Architectury's cross-platform event API (`dev.architectury.event.events.*`). Each platform module is a thin entrypoint that calls `CommonBootstrap.register()` (server) and `ClientBootstrap.register()` (client), then optionally registers platform-specific integrations (ModMenu, NeoForge key mappings).

### Accessory Slot System

The core abstraction is the **accessory slot**: `AccessorySlotProvider` wraps a concrete inventory location (vanilla `EquipmentSlot.HEAD`, Trinkets `head/hat` slot). `AccessorySlotRegistry` aggregates all providers — feature code queries the registry rather than checking individual slots. Providers are registered at init in registration order; first match wins.

- `VanillaHeadSlotProvider` — always available, provides rendering
- `TrinketsHatSlotProvider` — wraps Trinkets via reflection; no-ops when Trinkets is absent

### Capability-Based Item Resolution

Instead of checking item IDs directly, items are resolved to `AccessoryCapability` tags (`PLAYER_RIDE`, `BOAT_PASSENGER`, `HEAD_SHULKER_STORAGE`) via `HeadAccessoryCapabilities.resolve()`. Pattern matching is driven by runtime-configurable rules in `AccessoryPatternRuntimeState`.

### Feature Areas

| Feature | Description | Network |
|---|---|---|
| **Head Slot** | Any item can be placed in the player helmet slot | None |
| **Player Ride** | Right-click a player wearing a saddle-like item to ride them; mount/dismount/launch | C2S + S2C sync |
| **Boat Passenger** | Automatically ride boats when wearing a boat-like head accessory | C2S + S2C sync |
| **Head Shulker** | Open another player's head slot shulker box by right-clicking them | C2S request |
| **Armor Visibility** | Toggle helmet/chestplate/leggings/boots visibility per-slot | C2S update + S2C sync |

### Networking

All networking uses Architectury's raw `NetworkManager` API (`Identifier` + `RegistryFriendlyByteBuf` — not `CustomPayload`). `PlatformNetworking` is the single registration point. Packet direction follows the `c2s/` (client→server) and `s2c/` (server→client) package convention.

### Configuration

Cloth Config is the primary config system, but the mod includes a manual Gson JSON fallback when Cloth Config is absent. `AdvancedAccessorySystemConfigs` is the static facade; `AccessoryConfig` is the Cloth Config POJO with `@Config` annotations.

### Trinkets Integration

Trinkets support is purely reflection-based — no compile-time dependency. `TrinketsCompatInitializer` checks for Trinkets at runtime via `Class.forName`. The slot predicate is injected via data-pack (`data/trinkets/slots/head/hat.json`). `ReflectionTrinketsHatBridge` enables hat-stack queries via reflection.

### Mixins

Registered in `advanced_accessory_system-common.mixins.json`. Categories:
- `headslot/` — helmet slot insertion gates (`ArmorSlot.mayPlace`, `ScreenHandler` head-wear click)
- `rendering/` — LivingEntityRenderer armor visibility injection
- `ride/` — entity riding mechanics, hitbox, critical hit, boat break, dismount handling
