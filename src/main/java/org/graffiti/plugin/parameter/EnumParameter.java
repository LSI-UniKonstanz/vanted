package org.graffiti.plugin.parameter;

public class EnumParameter extends AbstractSingleParameter {

	Enum<?> enumParameter;
	
	public EnumParameter(Enum<?> val, String name, String description) {
		super(val, name, description);
		this.enumParameter = val;
		
		Enum<?>[] allEnums = enumParameter.getDeclaringClass().getEnumConstants();
	}

	
}
