import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * Checks that Java can still load the installer jar embedded in the EXE, the
 * way the launch4j header does in classpath mode. A launch4j EXE is a Windows
 * stub with the jar appended; the Authenticode signature is appended after
 * that (see packaging/launch4j/vanted-l4j.xml).
 *
 *   java CheckExeJar <exe>
 */
public class CheckExeJar {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: java CheckExeJar <exe>");
            System.exit(2);
        }
        File exe = new File(args[0]);
        String mainClass;
        try (JarFile jar = new JarFile(exe)) {
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            System.out.println(exe.getName() + ": " + jar.size()
                    + " entries, Main-Class " + mainClass);
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[] { exe.toURI().toURL() }, null)) {
            Class.forName(mainClass, false, loader);
        }
        System.out.println("OK: the installer jar inside the EXE is readable");
    }
}
