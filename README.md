# Treasure Map Fix

Server-side Fabric compatibility fix for Minecraft Java Edition 1.21.8.

## What the diagnostics established

The supplied diagnostics reproduced the broken map on a fresh, naturally generated shipwreck map chest. The relevant run started the buried-treasure lookup from approximately `(-634, 51, 51)`, called the vanilla `#minecraft:on_treasure_maps` search with a 50-chunk radius, received no structure position, and left the loot item as `minecraft:map`. The later vanilla `set_name` function then gave that empty map the Buried Treasure Map name.

Other fresh runs resolved buried treasure at approximately 7.50, 8.56, and 25.94 chunks and produced normal `minecraft:filled_map` items. This separates the failure from chest interaction, map use, game mode, and inventory handling. The reproduced failure occurs at the bounded structure lookup.

One automated run ended with `unopened_shipwreck_map_chest_not_found`. That wreck did not expose an unopened `minecraft:chests/shipwreck_map` chest to the test controller. This is a test-harness outcome, not another form of the map lookup failure.

The diagnostics source also has one measurement flaw: `inventorySnapshot()` calls `getStack()` before the controller's explicit `generateLoot(player)` call. On an unopened `LootableInventory`, that access can trigger lazy loot generation. The trace shows a loot-generation event with no player before the controller's explicit generation step, so the recorded `before` inventory snapshot is not genuinely pre-generation. This does not change the structure-search result recorded inside `ExplorationMapLootFunction`.

## Fix behavior

The mod changes only exploration-map functions whose destination is `#minecraft:on_treasure_maps`.

1. The effective structure-search upper bound is raised to 100 chunks. If a data pack already requests a radius above 100, its larger value is retained.
2. If no buried treasure is found within that bound, the unresolved `minecraft:map` is changed to count 0. The rest of the loot-function chain can still run, but the item remains empty and is removed by normal loot handling instead of appearing as a fake Buried Treasure Map.

The mod does not change buried-treasure biome tags, buried-treasure placement frequency, Tectonic world generation, ordinary empty maps, or other explorer-map destinations.

## Why one 100-chunk lookup is used

Minecraft 1.21.8's random-spread structure locator checks search-radius rings from 0 outward and returns after the first ring that contains a matching structure. A maximum radius of 100 therefore does not force a nearby successful lookup to scan all 100 rings.

The earlier fallback idea of first searching 50 chunks and then repeating the lookup at 100 chunks would re-run the first 51 rings whenever the 50-chunk lookup misses. Using a single upper bound avoids that duplicated work.

The 100-chunk value is a bounded workaround, not a claim that every Tectonic world has buried treasure within that distance. The supplied failed sample proves that no destination was found within 50 chunks, but it does not contain the location of the nearest target beyond that radius. If the 100-chunk search also fails, the bad map is discarded.

## Relation to the later vanilla fix

Newer Minecraft versions changed exploration-map handling so an unresolved lookup leaves the item without map data, and vanilla loot tables can discard that unresolved item. Minecraft 1.21.8 does not have that exact later data-pack behavior, so this mod backports the practical outcome without changing the 1.21.8 map format.

## Installation

Put `treasure-map-fix-0.1.0.jar` in the `mods` folder of the environment that runs server logic.

- Singleplayer: install it in the normal Fabric client instance. The integrated server runs the fix.
- Dedicated server: install it on the server. Clients do not need the mod.
- Fabric API is not required.
- Minecraft version: exactly 1.21.8.
- Java: 21 or newer.

The diagnostics mod can remain installed during validation. Its `exploration_map_begin` event reports the loot function's stored radius, normally 50. Its `server_world_locate_begin` event should report the effective radius passed by this fix, which is 100 for `#minecraft:on_treasure_maps`.

## Expected validation

For a fresh shipwreck or ocean-ruin map chest:

- If buried treasure exists within 100 chunks, the diagnostics log should show `radius_chunks: 100`, a non-null locate result, and a resulting `minecraft:filled_map`.
- If no target exists within 100 chunks, the unresolved map stack should have count 0 and should not appear as loot.
- Other exploration-map destinations should retain their original search radius.

## Source layout

The implementation is one mixin:

`src/main/java/dev/lucky/treasuremapfix/mixin/ExplorationMapLootFunctionMixin.java`

The project uses Fabric Loom 1.10.5, Yarn `1.21.8+build.1`, Fabric Loader 0.17.2, and Java 21. Run `gradlew build` on Linux/macOS or `gradlew.bat build` on Windows. `build.cmd` is included as a Windows convenience wrapper.
