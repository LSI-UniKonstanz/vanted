package org.vanted.plugins.layout.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;
import de.ipk_gatersleben.ag_nw.graffiti.services.HandlesAlgorithmData;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.vanted.plugins.layout.multilevelframework.MlfHelper.tryMakingNewInstance;

/**
 * Provides a uniform interface for layout algorithms (e.g. getting the GUI interface, running the algorithm).
 */
public class LayoutAlgorithmWrapper {
    private final Algorithm algorithm;
    private final static Graph dummyGraph = new AdjListGraph();
    private final static Selection dummySelection = new Selection();
    private ThreadSafeOptions threadSafeOptions;
    // PatternSpringembedder throws NPEs if getParameters wasn't called before execute
    // but calling it multiple times could result in parameters being reset...
    private boolean getParametersCalled = false;
    // some algorithms, e.g. PatternSpringembedder provide two GUIs: one using Parameter objects and one using
    // ThreadSafeAlgorithm.setControlInterface
    // this boolean is to choose which one to use
    private boolean useThreadSafeGUI = false;
    /**
     * Layout algorithms that can be used with the MLF. They are identified by their
     * return value of <code>getName</code>.
     * Layout algorithms can only be used in an MLF if they have the following properties:
     * <ul>
     * <li>The MLF must have some way to determine that the layout algorithm is done</li>
     * <li>The layout algorithm must not require it graph to be rendered in a `View`</li>
     * </ul>
     * For further criteria, see the filters in {@link #getPluginLayoutAlgs()}.
     * <p>
     * The reason why strings are listed here is to avoid actual dependencies (in the
     * Java sense) on other packages.
     * <p>
     * To include algorithms which are available in the namespace, use
     * {@link #getSuppliedLayoutAlgs()}.
     */
    final static List<String> layoutAlgWhitelist = Arrays.asList("Circle", "Grid Layout",
            "Stress Minimization" + " (\"thread-safe\" GUI)",
            "Move Nodes to Grid-Points", "Null-Layout", "Random", "Remove Node Overlaps");
    // stores the last returned parameter GUI so the parameters can be retrieved
    private ParameterEditPanel oldParameterGUI = null;
    // keeps track of whether or not the "thread safe" GUI was called
    // if not, it needs to be generated because it starts a timer that affects the algorithm's execution
    private boolean getThreadSafeGUITimerStarted = false;
    // cache the GUI
    private JComponent oldThreadSafeGUI = null;
    // because of this, those algorithms also have to have two names, because the user can choose which version
    // to use from the list of algorithms
    private final String guiName;

    /**
     * Create a new {@link LayoutAlgorithmWrapper}. This method is for internal use only. It's package-private so it can
     * be accessed by tests.
     *
     * @param guiName       The name the algorithm has in the GUI. This can be different from
     *                      {@link Algorithm#getName()}, because some algorithms are shown twice in the list of
     *                      algorithms because they provide two different GUI's (see {@code useThreadSafeGUI} below).
     *                      If this is {@code null}, the name returned by {@link Algorithm#getName()} will be used.
     * @param algorithm     The {@link Algorithm} to wrap. Must not be {@code null} and must be a layout algorithm
     *                      (i.e. {@link Algorithm#isLayoutAlgorithm()} must return {@code true}.
     * @param useThreadSafeGUI Some algorithms such as {@link de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder}
     *                      provide a second GUI through
     *                      {@link ThreadSafeAlgorithm#setControlInterface(ThreadSafeOptions, JComponent)}.
     *                      This one will be used if this parameter is set to {@code true}, otherwise the default
     *                      GUI will be used (i.e. using {@link Algorithm#getParameters()}).
     *                      Note that we will fall back to the default GUI if
     *                      {@link ThreadSafeAlgorithm#setControlInterface(ThreadSafeOptions, JComponent)} returns
     *                      {@code false}.
     * @author Gordian
     */
    LayoutAlgorithmWrapper(String guiName, Algorithm algorithm, boolean useThreadSafeGUI) {
        this.algorithm = Objects.requireNonNull(algorithm);
        if (!this.algorithm.isLayoutAlgorithm()) {
            throw new IllegalArgumentException("LayoutAlgorithmWrapper only works with layout algorithms.");
        }
        if (useThreadSafeGUI && !(this.algorithm instanceof ThreadSafeAlgorithm)) {
            throw new IllegalArgumentException("LayoutAlgorithmWrapper can only use the useThreadSafeGUI option "
                    + "for ThreadSafeAlgorithms.");
        }
        this.useThreadSafeGUI = useThreadSafeGUI;
        this.guiName = guiName == null ?
                Objects.toString(algorithm.getName(), algorithm.getClass().getSimpleName()) : guiName;
        if (this.guiName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot initialize LayoutAlgorithmWrapper with empty name.");
        }
    }

    /**
     * Find layout algorithms supplied by plugins.
     *
     * @return A map from algorithm name to wrapper instance
     * @see LayoutAlgorithmWrapper
     */
    public static Map<String, LayoutAlgorithmWrapper> getPluginLayoutAlgs() {
        Collection<PluginEntry> entries = GravistoService.getInstance().getMainFrame().getPluginManager().getPluginEntries();
        Map<String, LayoutAlgorithmWrapper> layoutAlgs = new HashMap<>();
        for (PluginEntry pe : entries) {
            Algorithm[] algorithms = pe.getPlugin().getAlgorithms();
            Arrays.stream(algorithms)
                    .filter(Algorithm::isLayoutAlgorithm)
                    // the multilevel framework should not apply itself at the different levels
                    .filter(a -> !(a instanceof MultilevelFrameworkLayout))
                    // some algorithms seem to have no name
                    .filter(a -> a.getName() != null && !a.getName().trim().isEmpty())
                    .forEach(a -> {
                        // ThreadSafeAlgorithms such as PatternSpringembedder have two GUIs, so they appear twice
                        if (a instanceof ThreadSafeAlgorithm) {
                            final String alternateName = a.getName() + " (\"thread-safe\" GUI)";
                            if (layoutAlgWhitelist.contains(alternateName)) {
                                layoutAlgs.put(a.getName(), new LayoutAlgorithmWrapper(a.getName(),
                                        tryMakingNewInstance(a), true));
                            }
                            final String parameterName = a.getName() + " (\"parameter\" GUI)";
                            if (layoutAlgWhitelist.contains(parameterName)) {
                                layoutAlgs.put(parameterName, new LayoutAlgorithmWrapper(parameterName,
                                        tryMakingNewInstance(a), false));
                            }
                        } else if (layoutAlgWhitelist.contains(a.getName())) {
                            layoutAlgs.put(a.getName(), new LayoutAlgorithmWrapper(null,
                                    tryMakingNewInstance(a), false));
                        }
                    });
        }
        return layoutAlgs;
    }

    /**
     * Find all currently available layout algorithms and returns those whitelisted.
     *
     * @return A {@link Map} of {@link LayoutAlgorithmWrapper}s. The maps keys are the {@link
     * Algorithm}s' names. Only returns algorithms whose names are contained in {@link
     * LayoutAlgorithmWrapper#layoutAlgWhitelist}. Note that the Multilevel Framework Algorithm
     * itself (i.e. instances of {@link MultilevelFrameworkLayout} is excluded from this list.
     * @author Gordian
     */
    public static Map<String, LayoutAlgorithmWrapper> getLayoutAlgorithms() {
        try {
            return Stream.concat(
                    getSuppliedLayoutAlgs().entrySet().stream(),
                    getPluginLayoutAlgs().entrySet().stream()
            ).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
            ));
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Name collision in supplied layout algorithms");
        }

    }

    /**
     * @return the "GUI name", see the constructor ({@link LayoutAlgorithmWrapper#LayoutAlgorithmWrapper(String,
     * Algorithm, boolean)}) for details
     * @author Gordian
     */
    public String getGUIName() {
        return this.guiName;
    }

    /**
     * @return the wrapped {@link Algorithm}
     * @author Gordian
     */
    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Explicitly add other algorithms via symbolic reference
     *
     * @return A map from algorithm name to wrapper instance.
     * @see MultilevelFrameworkPlugin#initializeAddon()
     */
    public static Map<String, LayoutAlgorithmWrapper> getSuppliedLayoutAlgs() {
        Map<String, LayoutAlgorithmWrapper> layoutAlgs = new HashMap<>();
        // in fact, we would only require a Class<Algorithm> and not an instance
        // add force-directed (provided by VANTED core)
        Algorithm forceDirectedWrapper = new ForceDirectedLayoutWrapper();
        layoutAlgs.put(
                forceDirectedWrapper.getName(),
                new LayoutAlgorithmWrapper(forceDirectedWrapper.getName(),
                        tryMakingNewInstance(forceDirectedWrapper),
                        true
                )
        );
        return layoutAlgs;
    }

    /**
     * @return the GUI where the user can set parameters for the algorithm.
     * @author Gordian
     */
    public JComponent getGUI() {
        if (this.algorithm instanceof ThreadSafeAlgorithm && this.useThreadSafeGUI) {
            ThreadSafeAlgorithm tsa = (ThreadSafeAlgorithm) this.algorithm;
            if (this.threadSafeOptions == null) {
                this.threadSafeOptions = new ThreadSafeOptions();
            }
            if (this.oldThreadSafeGUI == null) {
                this.oldThreadSafeGUI = LayoutAlgorithmWrapper.getThreadsafeGUI(tsa, this.threadSafeOptions);
            }
            this.getThreadSafeGUITimerStarted = true;
            if (this.oldThreadSafeGUI == null) {
                this.threadSafeOptions = null;
                this.useThreadSafeGUI = false;
                this.getParametersCalled = false;
            } else {
                return this.oldThreadSafeGUI;
            }
            tsa.reset();
        }
        JComponent res = getParameterGUI(this.algorithm);
        if (res != null) {
            res.setBackground(null);
        }
        // MainFrame.showMessageDialog("Could not get GUI for algorithm: " + this.algorithm.getName() + ".", "Error");
        return res;
    }

    /**
     * Execute the layout algorithm with the given {@link Graph} and {@link Selection}. Note that
     * this is in an early stage and hasn't been thoroughly tested.
     *
     * @param graph     The {@link Graph}. Must not be {@code null}.
     * @param selection The {@link Selection}. Must not be {@code null}.
     * @author Gordian
     */
    public void execute(Graph graph, Selection selection) {
        if (this.useThreadSafeGUI && this.algorithm instanceof ThreadSafeAlgorithm) {
            try {
                if (this.threadSafeOptions == null) {
                    this.threadSafeOptions = new ThreadSafeOptions();
                }
                this.threadSafeOptions.setGraphInstance(graph);
                this.threadSafeOptions.setSelection(selection);
                this.threadSafeOptions.doRandomInit = false;
                this.threadSafeOptions.redraw = false;
                this.threadSafeOptions.autoRedraw = false;
                if (!this.getThreadSafeGUITimerStarted) {
                    // the "thread safe GUI" starts a timer that runs every 200 ms and sets a value
                    // I have no clue as to why, but if I don't do this, things start to get weird...
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeAndWait(this::getGUI); // needs to be in the event-dispatcher thread
                    } else {
                        this.getGUI();
                    }
                    this.getThreadSafeGUITimerStarted = true;
                }
                ((ThreadSafeAlgorithm) this.algorithm).executeThreadSafe(this.threadSafeOptions);
                // this fixes IndexOutOfBoundsExceptions that occur when the levels are displayed
                // and seems to work more reliably otherwise as well
                if (this.algorithm instanceof PatternSpringembedder) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                return;
            } catch (Exception e) {
                this.threadSafeOptions = null;
                MainFrame.showMessageDialog("Threadsafe execution failed. Trying normal execution.", "Error");
            }
        }
        // some algorithms only work if getParameters was called, see above
        if (!this.getParametersCalled) {
            try {
                this.algorithm.getParameters();
            } catch (NullPointerException npe) { // some algorithms require attach *before* getParameters()
                this.algorithm.attach(dummyGraph, dummySelection);
                this.algorithm.getParameters();
            }
        }
        this.algorithm.reset();
        // set parameters in case the user changed them using the "parameter" GUI
        if (!this.useThreadSafeGUI && this.oldParameterGUI != null) {
            this.algorithm.setParameters(this.oldParameterGUI.getUpdatedParameters());
        }
        this.algorithm.attach(graph, selection);
        try {
            this.algorithm.check();
            this.algorithm.execute();
        } catch (PreconditionException e) {
            e.printStackTrace();
            MainFrame.showMessageDialog(e.getMessage(), "Multilevel Framework Error");
        }
    }

    /**
     * Get the GUI of a threadsafe algorithm by using
     * {@link ThreadSafeAlgorithm#setControlInterface(ThreadSafeOptions, JComponent)}.
     *
     * @param alg     The {@link ThreadSafeAlgorithm}. Must not be {@code null}.
     * @param options The {@link ThreadSafeOptions} object where the options will be stored. Must not be {@code null}.
     * @return The GUI or {@code null} if {@link ThreadSafeAlgorithm#setControlInterface(ThreadSafeOptions, JComponent)}
     * returns {@code false}.
     * @author Gordian
     */
    private static JComponent getThreadsafeGUI(ThreadSafeAlgorithm alg, ThreadSafeOptions options) {
        JPanel pluginContent = new JPanel();
        // `setControlInterface` actively modifies `pluginContent`
        if (alg.setControlInterface(options, pluginContent)) {
            JScrollPane sp = new JScrollPane(pluginContent);
            sp.setBorder(null);
            return sp;
        } else return null;
    }

    /**
     * Make a GUI based on {@link Parameter} objects, similar to the one generated by
     * {@link de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog#initAlgorithmPreferencesPanel(JDialog, Algorithm, Graph, Selection, HandlesAlgorithmData, boolean)}.
     * @return a GUI built by {@link ParameterEditPanel}
     * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog#initAlgorithmPreferencesPanel(JDialog, Algorithm, Graph, Selection, HandlesAlgorithmData, boolean)
     */
    private JComponent getParameterGUI(Algorithm algorithm) {
        // adapted from PreferencesDialog.initAlgorithmPreferencesPanel
        JPanel progressAndStatus = new JPanel();
        double border = 5;
        double[][] size = {{border, TableLayoutConstants.FILL, border}, // Columns
                {border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
                        TableLayoutConstants.PREFERRED, border}}; // Rows

        progressAndStatus.setLayout(new TableLayout(size));

        String desc = algorithm.getDescription();
        JLabel info = new JLabel(desc);
        info.setBorder(BorderFactory.createLoweredBevelBorder());
        info.setOpaque(false);

        if (desc != null && desc.length() > 0)
            progressAndStatus.add(info, "1,3");
        final EditComponentManager editComponentManager = MainFrame.getInstance().getEditComponentManager();

        ParameterEditPanel paramPanel;
        algorithm.attach(LayoutAlgorithmWrapper.dummyGraph, LayoutAlgorithmWrapper.dummySelection);
        if (this.oldParameterGUI != null) {
            this.algorithm.setParameters(this.oldParameterGUI.getUpdatedParameters());
            return this.oldParameterGUI;
        }
        this.getParametersCalled = true;
        if (algorithm.getParameters() != null) {
            paramPanel = new ParameterEditPanel(algorithm.getParameters(), editComponentManager.getEditComponents(), LayoutAlgorithmWrapper.dummySelection,
                    algorithm.getName(), true, algorithm.getName());
            JScrollPane sp = new JScrollPane(paramPanel);
            sp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            sp.setOpaque(false);
            sp.setBackground(null);
            progressAndStatus.add(sp, "1,2");
        } else {
            return null;
        }

        progressAndStatus.validate();

        this.oldParameterGUI = paramPanel;

        return new JScrollPane(progressAndStatus);
    }

    /**
     * @author autogenerated by IntelliJ
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "LayoutAlgorithmWrapper{" +
                "algorithm=" + algorithm +
                ", threadSafeOptions=" + threadSafeOptions +
                ", getParametersCalled=" + getParametersCalled +
                ", threadSafeGUI=" + useThreadSafeGUI +
                ", guiName='" + guiName + '\'' +
                '}';
    }

    /**
     * Execute the algorithm, assuming an empty selection
     *
     * @param graph
     */
    public void execute(Graph graph) {
        this.execute(graph, new Selection());
    }
}
