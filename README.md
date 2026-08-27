# Chuck Pack

A feature-rich addon for [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) on Minecraft. Provides 30+ gameplay modules, 3 HUD elements, 2 commands, swarm support, and a custom pathfinding engine.

## Features

### Combat
- **MaceDamage** — Mace damage calculation
- **SpearKill** — Trident spear attacks (ported from Trouser Streak)
- **Untouchable** — PvP evasion with 9 threat types

### Movement
- **AutoFly** — Auto-fly to waypoints with setback detection, adaptive speed, and swarm support
- **BedrockEscape** — Escape bedrock from above/below (ported from Wurst CevAPI)
- **FlightScrollHandler** — Scroll wheel flight control
- **NoFall (enhanced)** — Adds `no-ground-spoof` setting to Meteor's NoFall (No_Ground mode from meteor-plus by nekiplay) — spoofs `onGround=false`
- **Tunnel** — Cross-section tunnel digging (`.tunnel NxN` in chat)

### Render
- **CoordinateLogout** — Logout spot coordinates
- **DeepslateESP** — Deepslate block highlighting (ported from Nora Tweaks)
- **HoleTunnelStairsESP** — Hole/tunnel/stair ESP (ported from Trouser Streak)
- **MobGearESP** — Mob equipment display (ported from Trouser Streak)
- **NewChunks** — New/old chunk detection
- **PearlChecker** — Ender pearl trajectory preview (ported from Nora Tweaks)
- **TrueSight** — See through walls

### World
- **AutoFarming** — Automated farming (ported from Nora Tweaks)
- **BaseFinder** — Base detection via entity analysis (ported from Trouser Streak)
- **OreSim** — Ore simulation

### Misc
- **AiChat** — AI chat integration (OpenAI, Anthropic, Google, Groq)
- **AiChatConverse** — Persistent AI conversations with memory
- **AntiSocial** — Anti-social utilities
- **AutoInteract** — Auto door/trapdoor interaction (ported from Meteorist)
- **AutoLogin** — Auto server login (ported from Meteorist)
- **ChatUtility** — Chat enhancements (ported from Nora Tweaks)
- **DoubleDoorsInteract** — Open both doors with one interaction (ported from Meteorist)
- **MapIntegration** — Waypoint creation from FreeCam/Xaero's map
- **NbtFilter** — NBT packet attack protection
- **OppStats** — Opponent statistics tracking
- **PlayerTriangulate** — Player location triangulation
- **StaffMonitor** — Staff detection with auto-quit
- **SwarmAutoConnect** — Auto-connect swarm workers to host
- **VillagerRoller** — Villager trade rolling for enchantments

### HUD
- **AutoFlyHud** — ETA, distance, speed overlay
- **KeybindsHud** — Keybind display
- **PlayerTriangulateHud** — Player location display

### Commands
- **/locate** — Structure locating
- **/seed** — World seed extraction
- **.tunnel NxN** — Set tunnel cross-section
- **.swarm fly X Y Z** — Swarm fly to coordinates

## Branches

| Branch | MC Version | Status |
|--------|-----------|--------|
| `main` | 26.1.2 | **Stable (current)** |
| `26.1.2-beta` | 26.1.2 | Beta — merged into `main` (2026-08-26) |
| `1.21.11` | 1.21.11 | Legacy stable |

## Requirements

- Minecraft (version depends on branch)
- [Fabric Loader](https://fabricmc.net/)
- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- Java 21+

## Installation

1. Install Fabric Loader and Meteor Client
2. Download the latest `chuck-pack-*.jar` from [Releases](https://github.com/chuck121110-bit/Chuck-Pack/releases)
3. Place the JAR in your Minecraft `mods/` folder
4. Launch Minecraft

### Building from source

```bash
# Windows
chuck-pack-build.bat

# Manual
gradle build --no-daemon --stacktrace
```

Output JARs land in `build/libs/` (Chuck Pack) and `Map Integration/build/libs/` (Map Integration).

## Dependencies

All bundled or available at runtime:
- [Baritone](https://github.com/cabaletta/baritone) — Pathfinding (bundled via `libs/`)
- [Cubiomes](https://github.com/moulins/cubiomes) — Native structure search bindings (lazy-loaded)
- [Xaero's World Map](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) — Map integration (compile-only)
- [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) — Minimap integration (compile-only)

## License

MIT
