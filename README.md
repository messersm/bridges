# bridges
A java implementation of the game "Bridges".

## How to run
If you want to open the project in an IDE see below. If you simply
want to compile and run the application, this should do:

```
$ git clone https://github.com/messersm/bridges.git
$ cd bridges
$ javac -d bin src/*/*/*.java
$ cd bin
$ java bridges.gui.Main
```

## Using your IDE
If you would like to work on this project, here are some instructions:

If you want to check in the project into IntelliJ, here's what should work:
Select "File > New > Project from Version Control > Git" and
enter ``https://github.com/messersm/bridges.git`` (or your fork).
Select "yes", when asked, if you want to create an IDEA project,
select "Create project from existing sources" and follow the wizard.
**Important**: When asked, if you want to overwrite ``bridges.iml`` select "Reuse".

As for Eclipse... you're on your own. Basically, you have to add the folders ``src``
and ``test`` as source and add JUnit (4) to your project.
