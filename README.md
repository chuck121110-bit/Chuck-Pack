# Chuck Pack

A feature-rich addon for [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) on Minecraft `26.1.2` (Fabric `0.19.3`, Java `25`). Provides 35+ gameplay modules, 3 HUD elements, and swarm-aware pathfinding, with focus on anarchy, base hunting, and automation. Port of multiple community addons with 1-to-1 logic where noted.

## Features

### Combat
- **MaceDamage** — Mace smash damage calc + HUD
- **SpearKill** — Trident spear aura (Trouser Streak)
- **Untouchable** — 9-threat PvP evasion (Wurst)
- **SwarmGuard** — Swarm following/attack logic for host/workers

### Movement
- **AutoFly** — Waypoint flight with `SetbackDetector` adaptive speed, swarm `swarm fly X Y Z` / `swarm stopfly`
- **BedrockEscape** — Bedrock trap escape (Wurst CevAPI)
- **FlightScrollHandler** — Hold `Shift` + scroll to change `Flight` speed (`scroll-speed`/`scroll-sensitivity` in `Flight`), `no-sprint` prevents sprint while flying (hunger save), hotbar cancel on Shift-scroll
- **NoFall** — `no-ground-mode` (`Disabled`/`NoGround`) ported from Meteor Editions PR #6516 / MeteorPlus `No_Ground` - spoofs `onGround=false` (tag `1337`), `PathManagers` baritone fallback, sent on deactivate
- **Tunnel** — `.tunnel NxN` cross-section digging

### Render
- **CoordinateLogout** — Logout coords
- **DeepslateESP** — Deepslate non-natural highlighting (Nora Tweaks)
- **HoleTunnelStairsESP** — Hole/tunnel/stair ESP (Trouser Streak)
- **MobGearESP** — Mob gear (Trouser Streak)
- **NewChunks** — New/old chunk detection
- **PearlChecker** — Pearl trajectory (Nora Tweaks)
- **TrueSight** — Invisibles

### World
- **AutoFarming** — Farming (Nora Tweaks)
- **BaseFinder** — Base detection via `Block Detectors`/`Entity Detectors` + 7 `Block List` presets, `Entity Cluster`, `Nether Roof`, `Sky Build`, `Bedrock`, `Spawner`, `Sign`, `Portal`, `Bubble Column`. `Base was found [Open GUI]` (`DARK_GRAY` clickable → `FlaggedChunksScreen` `x` counts for blocks/entities/triggers). `Auto delete flagged chunks` + `Delete time` (s) + `Delete radius` (chunks) - continuous stay within radius auto-removes flagged chunk. `FlaggedChunksScreen` shows `Triggers` (Portal etc. `xN`), `Block Data` (`Block xN`), `Entity Data` (`xN`), `Manual Add` proper. `SaveBaseData`/`LoadBaseData`/`AutoReload`.
- **OreSim** — Seed-based vanilla ore simulation (Atomic/Meteor Rejects). Renders ores `chunk-range`, `air-check-mode`, `baritone` `Baritone API` only (was `Supports: ...` lake removed). `getBaritoneGoals` + `WorldScanner`/`FasterWorldScanner`/`CachedWorld` mixins + `injectBaritoneGoals` reflection for `MineProcess` anti-xray bypass. `swarm simulate`/`swarm seed` via `CommandsMixin`/`SwarmWorkerMixin` (`ChuckPack-oresim`/`ChuckPack-seed`) with ore multi-select autocomplete (`coal,iron,gold,redstone,diamond,lapis,copper,emerald,quartz,debris,all`).

### Misc
- **AiChat / AiChatConverse** — AI (OpenAI/Anthropic/Google/Groq) + memory
- **AntiSocial** — Anti-social
- **AutoInteract** — Auto door/trapdoor (Meteorist)
- **AutoLogin** — Auto `/login`/`/register` (Meteorist) - handles `register` first-token vs `login` spaces, `commandsToHandle` list, `saveUsername`/`saveServerIp` filters
- **ChatUtility** — Chat (Nora Tweaks)
- **DoubleDoorsInteract** — Both doors
- **MapIntegration** — Freecam waypoints (`Freecam` active + keybind) + `freecam-waypoint-keybind`
- **NbtFilter** — `Block entity limit`/`Item stack limit`/`Packet size`/`Inventory overload` packet NBT guard (`InspectionResult` record)
- **OppStats** — Opponent gear/history
- **PlayerTriangulate** — Triangulation + `KeybindsHud`/`PlayerTriangulateHud`
- **StaffMonitor** — `Monitor mode switching` (creative/spectator), `Hidden player alerts` (off-tab entity vs tab), `Hidden staff alerts` (`meteor/staff/*.txt`), `Mojang Staff`, `Vanish detection` (`Vanish cycle` via `PlayerInfoRemove/Update` + `AddEntity` 1500ms, `KnownPacks` vanish, `suppressTabRotations`, `vanishKnownStaffOnly`, `vanishPlayerLimit`), `Toast alerts` (`SystemToast`), `Ignore friends`, `Quit on staff enter` + `quitDelay`. Ported Wurst `StaffMonitorHack` 0.59+.
- **SwarmAutoConnect** — Auto swarm connect
- **VillagerRoller** — 1-to-1 `villager-roller-1.4.19+mc26.1.2-build.47.zip` (`maxsuperman/meteor-villager-roller:1.4.19`) exact logic (`State` machine, `RollingEnchantment` inner, `EnchantmentSelectScreen`, `CivBreak`, `interact-retry`, `ProfessionTimeout` etc.)

### HUD
- **AutoFlyHud** — ETA/distance/speed
- **KeybindsHud**
- **PlayerTriangulateHud**

### Commands
- `/seed`, `/seed <seed> [version]`, `/seed list`, `/seed delete` — `Seeds` DB (`meteor/seeds.nbt` + `ChuckPack/WorldGenUtils` `Cubiomes`)
- `/seed-locate feature <type>` — `Cubiomes` nearest structure
- `.tunnel NxN` — Tunnel
- `swarm` — `swarm kill [player]`, `swarm stop/halt/cancel`, `swarm server [ip]`, `swarm simulate|oresim <ores...>` (multi `coal,iron...all`), `swarm seed [seed] [ver]` (all via `ChuckPack-oresim`/`ChuckPack-seed` sync to workers + `#mine` OreSim bypass)
- Baritone `Freecam` click — `PathManagers.moveTo` now `GoalBlock` exact (was `GoalGetToBlock` offset) same as `goto X Y Z` (`BaritonePathManager:GoalGetToBlock` → `GoalBlock`)

## Branches
| Branch | MC | Status |
|--------|----|--------|
| `main` | `26.1.2` | **Stable (current)** `1.0.23` → `1.0.24` |
| `26.1.2-beta` | `26.1.2` | Beta merged `2026-08-26` |
| `1.21.11` | `1.21.11` | Legacy |

## Requirements
- Minecraft `26.1.2`
- `Fabric Loader 0.19.3`
- `Meteor Client 26.1.2-42`
- `Baritone b881568` (`Baritone API` only, `meteor` fork `libs/baritone-meteor-1.21.11.jar`, not `standalone`)
- `Cubiomes 1.22.5` (native `HawtJNI`)
- `Xaero WorldMap 1.44.2`/`Minimap 26.4.2` (compile-only `Auto Fly Here`)
- Java `25` (`zulu_25` via `NoRiskClientV3/meta/java`)

## Installation
1. Fabric + Meteor `26.1.2-42`
2. Download `chuck-pack-1.0.24.jar` from [Releases](https://github.com/chuck121110-bit/Chuck-Pack/releases) (single copy only to `.../Barebones/mods/nrc-26.1.2-fabric/chuck-pack-*.jar` - not double paste)
3. Mods folder already contains `baritone-api`, `cubiomes` natives auto-extract
4. Run `gradle build --no-daemon` → `build/libs/` (also `chuck-pack-build.bat` now removed from git, use `gradle`)

## Config
- `Chuck Pack` config group (`Config.get().settings.createGroup("Chuck Pack")`) has `separate-category` (visible, moves modules to own `Chuck Pack` `Category` `Items.EMERALD` icon `new ItemStack(Items.EMERALD)` always icon, not `CHLIS` text before world), `check-for-updates`, `auto-download-updates`, `debug-logging`
- `NoRisk` `Barebones` `mods/nrc-26.1.2-fabric` is the active mods dir (not `Barebones/mods` double)

## License
MIT
