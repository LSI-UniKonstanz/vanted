import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads a native library from a jar the way VANTED does at run time.
 * Run with the bundled JVM after signing: fails if the library in the jar is
 * broken or the hardened runtime refuses to load it.
 *
 *   java NativeLoadCheck <jar> <entry>
 */
public class NativeLoadCheck {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java NativeLoadCheck <jar> <entry>");
            System.exit(2);
        }
        try (ZipFile jar = new ZipFile(args[0])) {
            ZipEntry entry = jar.getEntry(args[1]);
            if (entry == null) {
                throw new IllegalArgumentException(args[1] + " not found in " + args[0]);
            }
            File lib = File.createTempFile("vanted-native-", ".dylib");
            lib.deleteOnExit();
            try (InputStream in = jar.getInputStream(entry)) {
                Files.copy(in, lib.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.load(lib.getAbsolutePath());
            System.out.println("loaded: " + args[1]
                    + " (os.arch=" + System.getProperty("os.arch")
                    + ", java " + System.getProperty("java.version") + ")");
        }
    }
}
