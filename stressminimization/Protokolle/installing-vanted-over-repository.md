# Guide to compiling vanted from the repository

* Pull the latest changes using `git pull`
* Our vanted copy resides in `/vantedsource/`
* Import the three projects into Eclipse using `File > Import > General > Existing Projects into Workspace` choose the `/vantedsource/` directory and check all projects and import.
* If not already so, change the names of the source folders to src/main/java, src/main/resources and src/main/test resprectively
* Open the Eclipse preferences. Under `Java > Installed JREs` choose Java 8. You probably will have to install java 8 first (visit java.org or use `sudo apt install openjdk-8-jdk` on Ubuntu). If Java 8 is not listed, cklick Search... and find your java installation (check the location tab of the listed jdks in the installed JREs panel)
* Open the properties window of the vanted project (right click on the project in the package explorer). Under `Java Compiler` set compiler compliance level to `1.7`. Do the same in the addon project.
* Add the JDK jar to the vanted project properties under `Java Build Path > Libraries Tab` using Add Library and choose JREs System Library
* Also add all .jar files from the  `vanted-lib-repository` project in the same tab as above using Add JARs and selecting all .jar files under the `vanted-lib-repository` navigation item.
* Compile vanted
