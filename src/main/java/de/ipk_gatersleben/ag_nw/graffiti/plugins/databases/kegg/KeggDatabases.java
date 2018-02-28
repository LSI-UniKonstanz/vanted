package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

public enum KeggDatabases {

	PATHWAY("pathway", "path", "map number", ""), BRITE("brite", "br", "br number", ""), ORTHOLOGY("orthology", "ko",
			"K number", ""), GENES("genes", "genes", "map number",
					"Composite database: consisting of KEGG organisms"), ENZYME("enzyme", "ec", "", ""), COMPOUND(
							"compound", "cpd", "C number", ""), REACTION("reaction", "rn", "R number", "");

	private String dbname;
	private String abbreviation;
	private String kId;
	private String remark;

	private KeggDatabases(String name, String abbreviation, String kId, String remark) {
		this.dbname = name;
		this.abbreviation = abbreviation;
		this.kId = kId;
		this.remark = remark;
	}

	public String getDbname() {
		return dbname;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getkId() {
		return kId;
	}

	public String getRemark() {
		return remark;
	}

}
