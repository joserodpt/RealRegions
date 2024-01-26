<div align="center">

![Logo](https://i.imgur.com/0fvICMG.png)
## RealRegions
### GUI based World and Region M classanagement Plugin

[![Build](https://img.shields.io/github/actions/workflow/status/joserodpt/RealMines/maven.yml?branch=master)](https://github.com/JoseGamerPT/RealRegions/actions)
![Issues](https://img.shields.io/github/issues-raw/JoseGamerPT/RealRegions)
[![Stars](https://img.shields.io/github/stars/JoseGamerPT/RealRegions)](https://github.com/JoseGamerPT/RealRegions/stargazers)
[![Chat)](https://img.shields.io/discord/817810368649887744?logo=discord&logoColor=white)](https://discord.gg/t7gfnYZKy8)

<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/spigot_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/paper_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/purpur_46h.png" height="35"></a>

</div>

----

## Features
* YAML Configuration
* Simple and performant GUI interface.
* Flag Toggling by GUI.
* Console Commands also Supported.
* Bypass any Region Flag with Permissions
* Create, Import, Unload and Manage Worlds with commands and GUI.

----

## Commands

1. Default Command:
   - Command: `/realregions` or `/rr`
   - Function: Displays plugin information, such as the plugin name and version.

2. Reload Command:
   - Command: `/realregions reload` or `/rr reload`
   - Permission: `realregions.admin`
   - Function: Reloads the configuration files and updates the plugin settings.

3. Worlds Command:
   - Command: `/realregions worlds` or `/rr worlds`
   - Permission: `realregions.admin`
   - Function: Lists all registered worlds and their load status. If executed by a player, opens a graphical menu to view and manage worlds.

4. Create Command:
   - Command: `/realregions create <name>`
   - Permission: `realregions.admin`
   - Function: Allows a player to create a region by selecting a WorldEdit region. The region is then created and associated with the specified name.

5. Create World Command:
   - Command: `/realregions createw <name> <worldtype>`
   - Permission: `realregions.admin`
   - Function: Creates a new world with the specified name and world type.

6. Region Command:
   - Command: `/realregions region <name>` or `/rr reg <name>`
   - Permission: `realregions.admin`
   - Function: Displays detailed information about the specified region. If executed by a player, opens a graphical menu to manage the region.

7. World Command:
   - Command: `/realregions world <name>` or `/rr w <name>`
   - Permission: `realregions.admin`
   - Function: Displays detailed information about the specified world. If executed by a player, opens a graphical menu to manage the world.

8. Teleport World Command:
   - Command: `/realregions tp <name>`
   - Permission: `realregions.admin`
   - Function: Teleports the player to the specified world.

9. Teleport Region Command:
   - Command: `/realregions tpr <name>`
   - Permission: `realregions.admin`
   - Function: Teleports the player to the specified region.

10. View Command:
    - Command: `/realregions view <name>`
    - Permission: `realregions.admin`
    - Function: Toggles the visibility of region boundaries for the specified region.

11. Unload Command:
    - Command: `/realregions unload <name>`
    - Permission: `realregions.admin`
    - Function: Unloads the specified world.

12. Load Command:
    - Command: `/realregions load <name>`
    - Permission: `realregions.admin`
    - Function: Loads the specified world.

13. Unregister Command:
    - Command: `/realregions unregister <name>`
    - Permission: `realregions.admin`
    - Function: Unregisters the specified world from the plugin.

14. Import Command:
    - Command: `/realregions import <name> <worldtype>`
    - Permission: `realregions.admin`
    - Function: Imports a world with the specified name and world type.

15. Delete Region Command:
    - Command: `/realregions delete <name>` or `/rr del <name>`
    - Permission: `realregions.admin`
    - Function: Deletes the specified region.

16. Entities Command:
    - Command: `/realregions entities <name>` or `/rr ents <name>`
    - Permission: `realregions.admin`
    - Function: Displays a graphical menu showing entities present in the specified world.

17. Delete World Command:
    - Command: `/realregions deleteworld <name>` or `/rr delw <name>`
    - Permission: `realregions.admin`
    - Function: Deletes the specified world.

18. Players Command:
    - Command: `/realregions players <name>` or `/rr plrs <name>`
    - Permission: `realregions.admin`
    - Function: Displays a graphical menu showing players present in the specified world.

----

## Requirements
RealRegions requires [WorldEdit](https://dev.bukkit.org/projects/worldedit) or [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) to work.

----

## API

RealRegions offers an API via the [RealRegionsAPI.java](realregions-api%2Fsrc%2Fmain%2Fjava%2Fjoserodpt%2Frealregions.api%2FRealRegionsAPI.java) class. It can be obtained as follows:

```java
var realRegionsAPI = RealRegionsAPI.getInstance()
realRegionsAPI.getRegionManagerAPI()
realRegionsAPI.getWorldManagerAPI()
```

----

## Links
* [SpigotMC](https://www.spigotmc.org/resources/realregions-1-14-to-1-20-1.111629/)
* [Discord Server](https://discord.gg/t7gfnYZKy8)
* [bStats](https://bstats.org/plugin/bukkit/RealRegions/19311)

