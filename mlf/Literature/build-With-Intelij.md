# Building Vanted

## Cloning Vanted
- Cloning/pulling our team repository

## Cloning vanted-lib-repository
- git clone https://bitbucket.org/vanted-dev/vanted-lib-repository.git 
cloning directly into the team repository might be prohibited by git.
- copy vanted-libs-repository to path of VANTED(next to it)
- remove the hidden git folder from the copy to avoid conflicts.
- add vanted-libs-repository copy in team repo to team repos ignore.

## Building VANTED Using InteliJ+Ant
- Add Ant to the GUI. Add /VANTED/build.xml
- Change properties (increase max Heap size  and max Stack size.  2GB and 0.5GB did the trick)
- Run building Process. SInce the fatjar is needed it is possible that without additional selection just the core version is built.


## Running VANTED addons Using InteliJ
-Add the Vanted.jar as librabry to the project structure (VANTED\target\jar\vanted-fatjar-ver2.6.5-build-20190527-10.jar)
-include VANTED sources (VANTED\src\main\java)
-Edit Run configuration: 
    Choose MainClass StartVantedWithAddon (given by input prompt)
    set working inventory to the classes working dir.