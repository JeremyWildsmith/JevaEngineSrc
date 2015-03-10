#This Repository is Deprecated. The engine has been refactored & reorganized. Below is the following repositories now containing JevaEngine.
- https://github.com/JeremyWildsmith/singleplayer A single player game/demo based off of the engine.
- https://github.com/JeremyWildsmith/jevaengine JevaEngine core engine implementation.
- https://github.com/JeremyWildsmith/rpg Rpg game framework that is build over JevaEngine.
- https://github.com/JeremyWildsmith/usr USR Game System that integrates into the RPG Game Framework.
- https://github.com/JeremyWildsmith/worldbuilder World builder, for designing game worlds for JevaEngine

The project is being actively developed. Please be patient with bugs. This is a new way of organizing the engine, and it will hopefully allow it to be more easily worked with.

Thanks. Jeremy Wildsmith.



![Logo](http://i.imgur.com/RM2ndqI.jpg)

JevaEngine - Java Isometric Game Engine
=============

Project Page: http://jevaengine.com


Source Code Repository for JevaEngine - An Isoemtric Java Game Engine

JevaEngine is currently under very early development, and the source code is still being
conditioned for user contribution.

License
========
JevaEngine is open-sourced under the GPLv3 License:
http://www.gnu.org/licenses/gpl-3.0.html

Features
========
- Pure Java Implementation. No native dependencies, maximizing cross-platform ability.
- Fully Scriptable. Scriptable scenes, events & NPCs (Scripts are written in JavaScript)
- Centralized Server Networking Implementation for Online RPG Games. Entity scripts, world triggers etc work seamlessly off and on server.
- Networking is cleanly isolated from the engine, allowing for varios networking models to be implemented seamlessly.
- UI Is entirely skinnable and customizable - Jeva Core provides a solid infastructure for most general UI components.
- Basic particle engine (implemented primary for attack\heal\projectile effects so nothing too fancy)
- Entities can save states and reload states (essential for game saves). States are can be saved on an integrated back-end that works over GameJolt API for Achievements, Scoring and Cloud Saving
- Fully capable quest system & dialogue system (dialogue can be complex with various pathways which effect any internal variables of the character (i.e. moral))
- Most of the engine is entirely extensible - with logical partitions in implementation logic between Java and external scripts.
- Map Editor & Dialogue editor
- Dynamic lighting and scriptable weather subsystem
- Powerful debugging console - very powerful user interface via scripts to engine. Both through the console and entity configuration files.

Demonstration
=============

You can take a look at some of the demonstration videos for jevaengine here.

http://www.youtube.com/watch?v=rWA8bajpVXg

http://www.youtube.com/watch?v=eOQo7KmqtFM

![alt tag](http://i.imgur.com/gEHj6K5.png)
![alt tag](http://i.imgur.com/lHYPmUq.png)

Due to some code refactoring, some of those videos will differ from the current version in the repository.

How to Contribute
=================

- You can contact the project administrator (Jeremy. Allen. Wildsmith) at JeremyWildsmith@yahoo.ca
- You must sign and agree to the CLA (Contributor License Agreement) <a href="http://www.clahub.com/agreements/JeremyWildsmith/JevaEngineSrc" target="_blank">here.</a> the
contributor license agreement allows JevaEngine developers (and the user-base of JevaEngine) to use your contributions to the project under the GPLV3 license.

How to Compile\Use
=================

JevaEngine is fairly straight-forward to compile. As JevaEngine uses Maven as its build platform, the project
is very portable accross various IDEs and dependency resolution is very easy. You can read a short guide on how
to setup JevaEngine <a href="https://github.com/JeremyWildsmith/JevaEngineSrc/wiki/Compiling-JevaEngine" target="_blank">here.</a>

You can find some decent quick-start guides on the project's wiki page.
