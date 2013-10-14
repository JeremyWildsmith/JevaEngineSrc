JevaEngine - Java Isometric Game Engine
=============

Source Code Repository for JevaEngine - An Isoemtric Java Game Engine

JevaEngine is currently under very early development, and the source code is still being
conditioned for user contribution. In the near future, the following is hoped to be accomplished:

* Sufficiently Flesh Out JavaDoc. Any contributions in this area would be greatly appreciated as there is a
lot of ground ot cover here. If you'd like to participate, please contact me at jeremywildsmith@yahoo.ca

* Add approriate licenese to the code to allow for proper and legal usage.

* The code needs a little cleaning.

Features
========
- Pure Java Implementation. No native dependencies, maximizing cross-platform ability.
- Fully Scriptable. Scriptable scenes, events & NPCs (Scripts are written in JavaScript)
- Centralized Server Networking Implementation for Online RPG Games. Entity scripts, world triggers etc work seamlessly off and on server.
- UI Is entirely skinnable and customizable - Jeva Core provides a solid infastructure for most general UI components.
- Basic particle engine (implemented primary for attack\heal\projectile effects so noting too fancy)
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

Due to some code refactoring, some of those videos will differ from the current version in the repository.


How to Contribute
=================

- You can contact the project administrator (Jeremy. Allen. Wildsmith) at JeremyWildsmith@yahoo.ca
- You must sign and agree to the following CLA: http://www.clahub.com/agreements/JeremyWildsmith/JevaEngineSrc

How to Compile
=================

- Uses Eclipse Project Files
- Check out solution, add the individual projects into a single work-space. They should compile fine.

