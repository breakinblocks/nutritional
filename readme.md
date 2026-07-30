# Nutritional

Nutritional adds a diet system to Minecraft. The food you eat counts toward five food groups, and keeping them in balance makes you stronger, while living on a single food works against you. It is a NeoForge 1.21.1 rewrite of [WesCook's Nutrition](https://github.com/WesCook/Nutrition).

Everything in the mod is built to be retuned by pack makers without writing code. See [For pack makers and developers](#for-pack-makers-and-developers) below.

[Changelogs](metadata/changelogs/)

## How it works

Every food belongs to one or more of five groups: dairy, fruit, grain, protein, and vegetable. Eating raises the groups that food belongs to, and each group slowly drops over time as you get hungry. The goal is to keep all five reasonably topped up rather than spamming one food.

Your overall balance is graded into a diet tier, from worst to best:

`starving`, `malnourished`, `surviving`, `nourished`, `gourmand`

A poor diet leaves you in a low tier with penalties. A varied, well-fed diet pushes you into a high tier that grants bonuses to things like max health, movement speed, and armor. Eating well also makes future meals more nourishing and slows how fast your groups drop, so good habits build on themselves.

On top of the tiers:

- **Status effects.** Crossing certain nutrient levels can give you status effects, both helpful ones for eating well and penalties for eating badly.
- **Long-term rewards.** Keep a good diet going for several Minecraft days in a row and you earn a permanent bonus. It sticks until you die or let your diet slip too far.
- **Harsher dimensions.** By default the Nether and the End are tougher on you: your groups drain faster and food does less there.

## Seeing your diet

- Press **N** to open a screen with a bar for every food group and your current diet tier.
- A small widget in the top-right corner shows your current tier at a glance. The diet screen has buttons to toggle it and to move it: pick **Move HUD**, then drag the widget wherever you like, or nudge it with the arrow keys. It snaps to screen edges and centers, and keeps its place when you change resolution or GUI scale.
- Hovering a food shows which groups it fills and how much.
- If you use JEI, looking up a food shows a "Provides nutrients" entry with the same information.

## Advancements

The mod ships a small advancement tree for eating from each food group, eating a varied diet, and reaching the higher diet tiers and rewards.

## Configuration

Most behavior can be changed in two config files:

- `nutritional-client.toml` for personal display options like the tooltip, the HUD widget, and the diet screen button.
- `nutritional-server.toml` for world rules like how fast groups drain, the death penalty, and overall difficulty multipliers.

Pack makers can change far more than this through datapacks, covered next.

## For pack makers and developers

Nutritional is data-driven. Food groups, the items in them, diet tiers, status-effect rules, rewards, and per-dimension difficulty are all defined in datapacks, so they can be added, rebalanced, or removed without touching Java.

### Data model

Six first-class datapack registries:

| Registry | Path |
| --- | --- |
| `nutritional:nutrient` | `data/<pack>/nutritional/nutrient/<id>.json` |
| `nutritional:effect` | `data/<pack>/nutritional/effect/<id>.json` |
| `nutritional:food_hint` | `data/<pack>/nutritional/food_hint/<id>.json` |
| `nutritional:diet_tier` | `data/<pack>/nutritional/diet_tier/<id>.json` |
| `nutritional:sustained_reward` | `data/<pack>/nutritional/sustained_reward/<id>.json` |
| `nutritional:dimension_modifier` | `data/<pack>/nutritional/dimension_modifier/<dim_namespace>/<dim_path>.json` |

Effects support four detection modes: `any`, `average`, `all`, and `cumulative`. The `nourished`, `malnourished`, and `toughness` mob effects ship by default.

### Items and scaling

Which group an item belongs to comes from tags (`#nutritional:nutrient/<id>`). Per-item scale overrides come from a NeoForge DataMap (`nutritional:nutrient_scales` on the `minecraft:item` registry). Most foods need only a tag entry.

### Custom attributes

Two attributes are added and can be modified by equipment, mob effects, and other mods:

- `nutritional:nutrient_absorption`, how much food gives. Higher means meals are more nourishing.
- `nutritional:nutrient_decay_rate`, how fast groups drop. Lower means they drain slower.

### Commands

- `/nutritional get|set|add|subtract|reset <player> <nutrient> [<value>]`
- `/nutritional reload` reloads datapacks
- `/nutritional-food` prints the nutrient yield for the held item

### Advancement triggers

Two custom criterion triggers, `nutritional:tier_reached` and `nutritional:reward_earned`, are available for datapacks to build their own advancement trees against.

### Profiling

The repeating diet calculations report under the profiler labels `nutritional:decay`, `nutritional:evaluate.effects`, `nutritional:evaluate.tier`, and `nutritional:evaluate.reward`, so Spark and the F3 profiler can attribute their cost.

### API

A thin API jar is published alongside the main artifact. It contains only the public surface: codecs, datapack registry keys, attribute and mob effect holders, the inverted nutrient index accessor, and the two criterion triggers. Compile against the api classifier, run against the full mod:

```groovy
repositories {
    maven { url 'https://maven.breakinblocks.com/snapshots' }   // or /releases for stable
}
dependencies {
    compileOnly 'com.breakinblocks.nutritional:nutritional:1.21.1-1.0.0:api'
    runtimeOnly 'com.breakinblocks.nutritional:nutritional:1.21.1-1.0.0'
}
```

## Credits

- [WesCook](https://github.com/WesCook), original [Nutrition](https://github.com/WesCook/Nutrition) author. The original 1.12 mod inspired this rewrite's core concepts: nutrients, thresholds, and decay tied to hunger.
- Original 1.12 contributors are listed in this repo's git history, preserved on the `1.12` branch.
