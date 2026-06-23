# 1.1.4
- The clockwork drill texture is now separated into individual frames rather than being a full spritesheet
- Now the clockwork drill state will go into idle mode every time its placed on the ground
- Fixed flamethrower's flame particles spawning in the center of the screen when the player was facing perfectly up or down

# 1.1.3
- Attempt to fix a case where clockwork would crash with the mod Legendary Spellbooks present in the same pack

# 1.1.2
- Attempt to fix forge not applying mixins correctly

# 1.1.1
- Added a config option to specify the durability for both scope and barrel crossbows
- Added a config option to specify the spawn weight, count and biomes for the clockwork dragonfly
- Fixed a case where forge would crash due to a wrong mixin remapping

# 1.1.0
- Improved the smoothness of weapon sway, making it much more natural, especially when changing vertical speed (when jumping or descending)
- Complete refactoring of the code, making it much more maintainable in the long run
- A new item has been added, the Clockwork Drill, a small tool capable of mining in a straight line towards wherever it is pointed. It has an inventory for the items it collects, can be picked up, and when it breaks after a certain amount of use, it can be repaired with clockwork gears
- A new item has been added, the Clockwork Flamethrower, which sets fire to whatever is in front of it. It is loaded with 1 unit of blaze powder, which provides 60 seconds of active use
- The Clockwork Wings have been redesigned. The model, animations and textures have been updated slightly. Now, when falling whilst wearing the wings, fall damage reduction is applied, and now the boost increases the player’s horizontal and vertical speed when activated
- Optimised internal logic of the potion sprayer, reducing the overall load on the client when using it
- Now the clockwork arrow goes to find the target as soon as it is fired, rather than when it is near the entity
- The Potion Sprayer now adds time to existing effects, rather than applying a fixed duration of 3 seconds
- Increased firing speed of the crossbow barrel
- Decreased the idle sound of the clockwork dragonfly
- Fixed a bug where potions with modified duration by the potion sprayer were not applied correctly and instead used the original applicable duration
- Fixed a bug where using the potion sprayer in the left hand would not display particles but would consume potion time
- Fixed a bug where the direction of potion sprayer particles on a dedicated server would become out of sync with the player's direction
- Fixed a bug where the potion sprayer particles turned grey when emptying the active potion
- Fixed a bug where the sway in the left hand was calculated in the opposite direction
- Fixed a bug where the clockwork wing boost wouldn't work on certain cases