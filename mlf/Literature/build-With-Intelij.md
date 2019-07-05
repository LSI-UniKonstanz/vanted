# Building VANTED Add-Ons with IntelliJ

## Cloning VANTED
- Cloning/pulling our team repository

## Cloning `vanted-lib-repository`
- `git clone https://bitbucket.org/vanted-dev/vanted-lib-repository.git`
  Cloning directly into the team repository might be prohibited by git.
- Copy `vanted-libs-repository` to path of VANTED (next to it)
- Remove the hidden git folder from the copy to avoid conflicts.
- Add `vanted-libs-repository` copy in team repo to team repos ignore.

## Building VANTED Using InteliJ+Ant
- Add Ant to the GUI (View > Tool Windows > Ant Build). Add `/VANTED/build.xml`
- Change properties (increase max Heap size and max Stack size of Ant. 2GB and 0.5GB did the trick)
- Run build process. Since the fatjar is needed it is possible that without additional selection just the core version is built.


## Running VANTED Addons Using InteliJ
- Add the VANTED fatjar JAR file as librabry to the project structure (`VANTED/target/jar/vanted-fatjar-ver2.6.5-build-[...].jar`)
- Include VANTED sources (`VANTED/src/main/java`)
- Edit Run configuration: 
    - Choose MainClass StartVantedWithAddon (given by input prompt)
    - (Maybe set working directory to the classes working dir.)
    - Make sure assertions are disabled if performance is a concern (VM Option `-da`)
    - If you need to layout large graphs, you might need to increase the Heap size of the JVM (e.g. `-Xmx4g`)