package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.OpenFileDialogService;
import org.ReleaseInfo;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class ExportDataTableAlgorithm extends AbstractEditorAlgorithm {

	private final CopyDataTableAlgorithm alg;
	// private ExportType type;
	private boolean correlationmatrix;

	public ExportDataTableAlgorithm() {
		alg = new CopyDataTableAlgorithm();
	}

	@Override
	public String getDescription() {
		return alg.getDescription();
	}

	@Override
	public void attach(Graph graph, Selection selection) {
		super.attach(graph, selection);
		alg.attach(graph, selection);
	}

	@Override
	public boolean activeForView(View v) {
		return v != null && v.getGraph() != null && v.getGraph().getGraphElements().size() > 0;
	}

	@Override
	public Parameter[] getParameters() {
		Parameter[] params = new Parameter[alg.getParameters().length + 2];
		int i = 0;
		for (Parameter p : alg.getParameters())
			params[i++] = p;

		params[params.length - 1] = new BooleanParameter(correlationmatrix, "n:n Correlation",
				"The correlation of this node to any other node (matrix)");
		// params[params.length - 1] = new ObjectListParameter(ExportType.EXCEL,
		// "Export", "Determines how the table will be exported", ExportType.values());
		return params;
	}

	@Override
	public void setParameters(Parameter[] params) {
		Parameter[] params2 = new Parameter[params.length - 1];
		int i = 0;
		for (Parameter p : params)
			if (i < params2.length)
				params2[i++] = p;
		correlationmatrix = (Boolean) params[params.length - 1].getValue();
		// type = (ExportType) params[params.length - 1].getValue();
		alg.setParameters(params2);
	}

	@Override
	public void execute() {
		try {
			StringBuilder result = new StringBuilder();
			result = alg.doIt(result);
			if (correlationmatrix) {
				result.append("\n\n##n:n correlation\n");
				HashSet<NodePair> pairs = new HashSet<NodePair>();
				for (GraphElement ed : getSelectedOrAllGraphElements()) {
					if (!(ed instanceof Edge))
						continue;
					Double valr = (Double) AttributeHelper.getAttributeValue(ed, "statistics", "correlation_r", null,
							new Double(1d));
					Double valp = (Double) AttributeHelper.getAttributeValue(ed, "statistics", "correlation_prob", null,
							new Double(1d));
					pairs.add(new NodePair(((Edge) ed).getSource(), ((Edge) ed).getTarget(), valr, valp));
				}
				for (NodePair np : pairs) {
					String pval = "";
					String rval = "";
					if (np.p != null && np.p != Double.NEGATIVE_INFINITY)
						pval = Double.toString(np.p);
					if (np.r != null && np.r != Double.NEGATIVE_INFINITY)
						rval = Double.toString(np.r);
					result.append(np.getLabel1() + "\t" + np.getLabel2() + "\t" + rval + "\t" + pval + "\n");
				}
			}

			File f = null;
			f = OpenFileDialogService.getSaveFile(new String[] { "txt" }, "tab-delimited Text file (*.txt)");
			if (f != null)
				TextFile.write(f.getAbsolutePath(), result.toString());
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}

	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Calculated Data";
		else
			return null;
	}

	@Override
	public String getCategory() {
		return "file.Export";
	}

	private enum ExportType {
		EXCEL, TXT, CLIPBOARD;

		@Override
		public String toString() {
			switch (this) {
			case EXCEL:
				return "as Excel file";
			case TXT:
				return "as Text file";
			case CLIPBOARD:
				return "into Clipboard";
			}
			;
			return "";
		}

	}

	private class NodePair {

		private final Node nd1;
		private final Node nd2;
		private final Double r;
		private final Double p;

		public NodePair(Node nd1, Node nd2, Double r, Double p) {
			this.nd1 = nd1;
			this.nd2 = nd2;
			this.r = r;
			this.p = p;
		}

		String getLabel1() {
			return new NodeHelper(nd1).getLabel();
		}

		String getLabel2() {
			return new NodeHelper(nd2).getLabel();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;

			return (nd1.equals(((NodePair) obj).nd1) && nd2.equals(((NodePair) obj).nd2))
					|| (nd1.equals(((NodePair) obj).nd2) && nd2.equals(((NodePair) obj).nd1));
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return (nd1.toString() + ";" + Double.toString(r) + ";" + nd2.toString() + ";" + Double.toString(p))
					.hashCode();
		}

	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.EXPORT, Category.COMPUTATION));
	}
}
