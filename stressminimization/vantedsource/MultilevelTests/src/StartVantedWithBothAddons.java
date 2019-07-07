import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

public class StartVantedWithBothAddons {

    public static void main(String[] args) {
        System.out.println("Starting VANTED with both Add-Ons.");
        Main.startVantedExt(args, new String[]{"MultilevelFramework.xml", "Stress-Minimization-Addon.xml"});
    }
}