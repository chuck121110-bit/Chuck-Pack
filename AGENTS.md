# AGENTS.md

## Project

**Aero Pack** — a Meteor Client addon mod for Minecraft 1.21.11 (Fabric). Single-module Gradle project, NOT a monorepo.

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
  AeroPack.java          # Addon entrypoint (registered via fabric.mod.json "meteor" entrypoint)
  modules/               # All gameplay modules
    combat/              # MaceDamage, SpearKill, Untouchable, SwarmGuard
    misc/                # AiChat, AutoLogin, StaffMonitor, OppStats, MapIntegration, UiUtilsMod, etc.
    movement/            # AutoFly, BedrockEscape, JumpFlight
    render/              # DeepslateESP, NewChunks, TrueSight, PearlChecker, HoleTunnelStairsESP, etc.
    world/               # OreSim, BaseFinder, AutoFarming
  mixin/                 # All Mixin classes (registered in aeropack.mixins.json)
    ui_utils/            # UI overlay mixins
  hud/                   # HUD elements (KeybindsHud, PlayerTriangulateHud, AutoFlyHud)
  commands/              # SeedCommand, LocateCommand
  render/                # AeroShaderHelper, AeroShaderSource, AeroRenderMode
  swarm/                 # AutoSwarmConnectHandler
  uiutils/               # UiUtils helpers
  util/                  # SetbackDetector, Keyword, config classes
```

`Map Integration/` is a **separate Fabric mod** with its own `build.gradle` (same build toolchain, no Meteor dependency).

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
