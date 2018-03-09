# soapbox-race-freeroam

## What is this?
This is the current source code of the freeroam server used by Apex and World Online PL.
This includes my notes from last summer, when I was working on rewriting the server.

This code isn't exactly the most efficient, so be warned: if you have lots of players, you can expect lots of CPU usage. Sorry. I'll fix it eventually.

## Can I use it?
Short answer: Yes, it just takes some effort

Long answer: Sure, but you'll have to compile it with Gradle. It's easy! You just do `gradle shadowJar`. The .jar file will be in `build/libs/soapbox-race-freeroam-all.jar`.

## Credits
Nilzao: Figured out most of the header... thanks to him, we have this.

leorblx: Made the first working 2 player freeroam, figured out packet grouping and some other stuff; rewrote this in summer 2017.

GamerZ: If he hadn't made his packet dumper, there would be virtually no chance of any multiplayer whatsoever.

## Have fun!
