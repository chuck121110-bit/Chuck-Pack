# AGENTS.md

## Project

**Aero Pack** — a Meteor Client addon mod for Minecraft 1.21.11 (Fabric). Single-module Gradle project, NOT a monorepo. Provides 30 gameplay modules, 3 HUD elements, 2 commands, and a custom pathfinding engine.

## Build

No `gradlew` wrapper exists. The `aero-pack-build.bat` script downloads Gradle 9.2.0 into `gradle-runtime/` on first run.

**Manual build:**
```
gradle build --no-daemon --stacktrace
```

Requires **Java 21**. The `.bat` sets `JAVA_HOME` to a local JDK; adjust if building outside that script.

Output JAR lands in `build/libs/`.

## Structure

```
src/main/java/net/aero/aeropack/
  AeroPack.java                          # Addon entrypoint (registered via fabric.mod.json "meteor" entrypoint)
  autoflypath/                           # Custom pathfinding engine
    PathFlightConfig.java                # Config with waitChunks field
    PathFlightRuntime.java               # Runtime state for flight pathing
    engine/                              # Core pathfinding algorithms
      FlightGrid.java                    # Grid representation for pathfinding
      GridRay.java                       # Ray casting on grid
      LazyThetaStar.java                 # Lazy Theta* pathfinding algorithm
      NetherBiomeRisk.java               # Nether biome risk assessment
      NetherTerrainGenerator.java        # Nether terrain prediction via density functions
    flight/                              # Flight control system
      BetterBlockPos.java                # Block position utility
      FlightController.java              # Main flight control loop (5842 lines — largest file)
      FlightPathfinder.java              # Pathfinding orchestration
      PathCalculationException.java      # Path calculation error
      UnpackedSegment.java               # Path segment data
  commands/
    LocateCommand.java                   # Structure locating
    SeedCommand.java                     # World seed extraction
  gui/screens/
    EditKeywordScreen.java               # Keyword editor GUI
    EditSeedEntryScreen.java             # Seed entry editor GUI
    FlaggedChunksScreen.java             # Flagged chunks display
    KeywordsScreen.java                  # Keywords management GUI
    UiUtilsDocumentationScreen.java      # UiUtils documentation
    oppstats/OppStatsScreen.java         # Opponent stats display
    villagerroller/EnchantmentSelectScreen.java  # Enchantment selection
  hud/
    AutoFlyHud.java                      # ETA/distance/speed overlay
    KeybindsHud.java                     # Keybind display
    PlayerTriangulateHud.java            # Player location display
  mixin/                                 # All Mixin classes (registered in aeropack.mixins.json)
    ui_utils/                            # UI overlay mixins
      ChestSlotOverlayMixin.java         # Chest slot overlays
      ClientConnectionAccessor.java      # Connection access
      UiUtilsBookEditScreenMixin.java    # Book edit screen
      UiUtilsBookScreenMixin.java        # Book screen
      UiUtilsConnectionMixin.java        # Connection UI
      UiUtilsHandledScreenMixin.java     # Handled screen
      UiUtilsSignEditScreenMixin.java    # Sign edit screen
      UiUtilsSleepingChatScreenMixin.java # Sleeping chat screen
    AutoEatMixin.java                    # Auto eat behavior
    AutoLoginMixin.java                  # Auto login behavior
    AutoResourcePackMixin.java           # Auto resource pack
    BlockESPMixin.java                   # Block ESP hooks
    BlockStateMixin.java                 # Block state modifications
    ClientPlayerEntityAccessor.java      # Client player access
    ConfirmScreenAccessor.java           # Confirm screen access
    CountPlacementModifierAccessor.java  # Placement modifier access
    ESPMixin.java                        # ESP hooks
    ExcavatorMixin.java                  # Excavator behavior
    FlightMixin.java                     # Flight hooks
    GuiMapMixin.java                     # Xaero map integration (coordinates, "Auto Fly Here" button)
    HeightRangePlacementModifierAccessor.java # Height range access
    HoveredMapElementHolderMixin.java    # Map element hover
    KillAuraMixin.java                   # Kill aura hooks
    NbtFilterDecoderMixin.java           # NBT filter decoding
    NoRotateMixin.java                   # No rotate behavior
    NukerMixin.java                      # Nuker behavior
    PlayerMoveC2SPacketAccessor.java     # Movement packet access
    RarityFilterPlacementModifierAccessor.java # Rarity filter access
    StorageESPMixin.java                 # Storage ESP hooks
    StorageOutlineShaderMixin.java       # Storage outline shader
    SwarmAutoConnectMixin.java           # Swarm auto connect
    SwarmConnectionMixin.java            # Swarm connection
    SwarmHostMixin.java                  # Swarm host
    SwarmMineMixin.java                  # Swarm mining
    SwarmMixin.java                      # Swarm core
    SwarmWorkerMixin.java               # Swarm worker
    TrueSightEntityMixin.java            # TrueSight entity rendering
    VeinMinerMixin.java                  # Vein miner behavior
    VeinMinerMyBlockAccessor.java        # Vein miner block access
  modules/
    combat/
      MaceDamage.java                    # Mace damage calculation
      SpearKill.java                     # Trident spear attacks
      SwarmGuard.java                    # Swarm team protection
      Untouchable.java                   # PvP evasion (~1274 lines, 9 threat types)
    misc/
      AiChat.java                        # AI chat integration
      AiChatConverse.java                # Persistent AI conversation (~995 lines, memory, web search, file persistence)
      AntiSocial.java                    # Anti-social mode
      AutoInteract.java                  # Auto entity interaction
      AutoLogin.java                     # Auto server login
      BaseAutoLogin.java                 # Base auto login logic
      ChatUtility.java                   # Chat enhancements
      DoubleDoorsInteract.java           # Double door opening
      MapIntegration.java                # Freecam waypoint creation (Meteor module, separate from Map Integration/ mod)
      NbtFilter.java                     # NBT packet attack protection (~1400 lines, quarantine system)
      OppStats.java                      # Opponent statistics tracking
      PlayerTriangulate.java             # Player location triangulation
      StaffMonitor.java                  # Staff detection (~704 lines, multiple detection methods)
      SwarmAutoConnect.java              # Auto swarm connection
      SwarmFixes.java                    # Swarm bug fixes
      UiUtilsMod.java                    # UI utilities module
      oppstats/OppStatsScreen.java       # Opponent stats display
      villagerroller/
        EnchantmentSelectScreen.java     # Enchantment selection GUI
        RollingEnchantment.java          # Enchantment rolling data
        VillagerRoller.java              # Villager trade rolling (~698 lines)
    movement/
      AutoFly.java                       # Auto-fly to waypoints (setback state machine, adaptive speed)
      BedrockEscape.java                 # Escape bedrock
      FlightScrollHandler.java           # Scroll wheel flight control
      JumpFlight.java                    # Jump-based flight
    render/
      CoordinateLogout.java              # Logout spot coordinates
      DeepslateESP.java                  # Deepslate block highlighting
      HoleTunnelStairsESP.java           # Hole/tunnel/stair ESP
      MobGearESP.java                    # Mob equipment display
      NewChunks.java                     # New chunk detection
      PearlChecker.java                  # Ender pearl trajectory
      TrueSight.java                     # See through walls
    world/
      AutoFarming.java                   # Auto farming
      BaseFinder.java                    # Base detection (~779 lines, entity analysis)
      OreSim.java                        # Ore simulation
  render/
    AeroRenderMode.java                  # Render mode handling
    AeroShaderHelper.java                # Shader helper utilities
    AeroShaderSource.java                # Shader source loading
  swarm/
    AutoSwarmConnectHandler.java         # Swarm auto connect handler
    IAutoSwarmConnect.java               # Swarm connect interface
  uiutils/
    UiUtils.java                         # UI utilities
    UiUtilsModAccess.java                # Mod access utilities
    UiUtilsPluginScanner.java            # Plugin scanner
    UiUtilsState.java                    # UI state management
  util/
    Keyword.java                         # Keyword data class
    SetbackDetector.java                 # Global setback detection (500ms cooldown, join/respawn suppression)
    SwarmDropHelper.java                 # Swarm drop helper
    SwarmUsernameManager.java            # Swarm username management
    config/
      AeroConfig.java                    # Main config class
      AeroPackConfigModifier.java        # Config modifier
      CategoryConfig.java                # Category config
      DebugLogger.java                   # Debug logging
      Ore.java                           # Ore data
      Seeds.java                         # Seeds config
      WorldGenUtils.java                 # World gen utilities
      WorldSeedDatabase.java             # World seed database
```

`Map Integration/` is a **separate Fabric mod** with its own `build.gradle` (same build toolchain, no Meteor dependency). Has its own git repo at `C:\Users\Areo\source\repos\Map Integration`.

## Adding a module

1. Create class extending `Module` in the appropriate `modules/` subpackage.
2. Register it in `AeroPack.onInitialize()` via `addModule(new YourModule())`.
3. The `addModule` call stores the module's natural category — this powers the separate-category toggle.

## Mixins

Declared in `src/main/resources/aeropack.mixins.json`. New mixins must be added to the `"client"` array. `defaultRequire: 0` means missing targets won't crash at load.

## Access widener

`src/main/resources/aeropack.accesswidener` widens Minecraft internals (PalettedContainer, TrackedWaypoint fields). Extend it when you need access to non-public MC members.

## Dependencies (local jars)

All in `libs/`:
- `baritone-meteor-1.21.11.jar` — runtime dep, bundled via `implementation`
- `xaeroworldmap-fabric-*.jar`, `xaerominimap-fabric-*.jar`, `xaerolib-fabric-*.jar` — compile-only, not bundled

Cubiomes native bindings (HawtJNI) are pulled from Maven (`dev.duti.acheong:cubiomes:1.22.5`).

## Key repos

- `maven.meteordev.org/releases` and `/snapshots` — Meteor Client
- `maven.duti.dev/releases` — Cubiomes

## Gotchas

- **No test suite.** Verify changes by building and testing in-game.
- **No CI.** No lint/format tooling is configured.
- **Two build outputs** exist in this repo — root `build/libs/` and `Map Integration/build/libs/`. Don't confuse them.
- `Source References/` contains third-party reference code (trouser-streak, cevapi, xaero sources). It is not part of the build.
- The `setback-plan.txt` documents the AutoFly setback detection state machine — useful context when editing `AutoFly.java` or `SetbackDetector.java`.

## Largest Files (by line count)

| File | Lines | Purpose |
|------|-------|---------|
| FlightController.java | 5842 | Main flight control loop |
| NbtFilter.java | ~1400 | NBT packet attack protection |
| Untouchable.java | ~1274 | PvP evasion |
| AiChatConverse.java | ~995 | Persistent AI conversation |
| BaseFinder.java | ~779 | Base detection |
| StaffMonitor.java | ~704 | Staff detection |
| VillagerRoller.java | ~698 | Villager trade rolling |
