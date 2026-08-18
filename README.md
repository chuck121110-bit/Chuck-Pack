# Chuck Pack

A feature-rich addon for [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) on Minecraft. Provides 29 gameplay modules, 3 HUD elements, 2 commands, and a custom pathfinding engine.

## Features

### Combat
- **MaceDamage** — Mace damage calculation
- **SpearKill** — Trident spear attacks
- **SwarmGuard** — Swarm team protection
- **Untouchable** — PvP evasion with 9 threat types

### Movement
- **AutoFly** — Auto-fly to waypoints with setback detection and adaptive speed
- **BedrockEscape** — Escape bedrock from above/below
- **FlightScrollHandler** — Scroll wheel flight control
- **JumpFlight** — Jump-based flight

### Render
- **CoordinateLogout** — Logout spot coordinates
- **DeepslateESP** — Deepslate block highlighting
- **HoleTunnelStairsESP** — Hole/tunnel/stair ESP
- **MobGearESP** — Mob equipment display
- **NewChunks** — New/old chunk detection
- **PearlChecker** — Ender pearl trajectory preview
- **TrueSight** — See through walls

### World
- **AutoFarming** — Automated farming
- **BaseFinder** — Base detection via entity analysis
- **OreSim** — Ore simulation

### Misc
- **AiChat** — AI chat integration
- **AiChatConverse** — Persistent AI conversations with memory
- **AutoLogin** — Auto server login
- **ChatUtility** — Chat enhancements
- **NbtFilter** — NBT packet attack protection
- **OppStats** — Opponent statistics tracking
- **PlayerTriangulate** — Player location triangulation
- **StaffMonitor** — Staff detection
- **VillagerRoller** — Villager trade rolling for enchantments

### HUD
- **AutoFlyHud** — ETA, distance, speed overlay
- **KeybindsHud** — Keybind display
- **PlayerTriangulateHud** — Player location display

### Commands
- **/locate** — Structure locating
- **/seed** — World seed extraction

## Branches

| Branch | MC Version | Status |
|--------|-----------|--------|
| `26.1.2-beta` | 26.1.2 | Current development |
| `1.21.11` | 1.21.11 | Stable |

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
