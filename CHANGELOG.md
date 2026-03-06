# 1.1.0
- Improved the smoothness of weapon sway, making it much more natural, especially when changing vertical speed (when jumping or descending)
- Complete refactoring of the code, making it much more maintainable in the long run
- Fixed a bug where potions with modified duration by the potion sprayer were not applied correctly and instead used the original applicable duration
- Optimised internal functional architecture of the potion sprayer, reducing the overall load on the client when using it
- Fixed a bug where using the potion sprayer in the left hand would not display particles but would consume potion time
- Fixed a bug where the direction of potion sprayer particles on a dedicated server would become out of sync with the player's direction
- Fixed a bug where the potion sprayer particles turned grey when emptying the active potion