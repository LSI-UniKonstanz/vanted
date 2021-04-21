package org.graffiti.plugin.parameter;

public class EnumParameter extends AbstractSingleParameter {
	
	Enum<?> enumParameter;
	
	Enum<?>[] allEnums;
	
	public EnumParameter(Enum<?> val, String name, String description) {
		super(val, name, description);
		this.enumParameter = val;
		
		allEnums = enumParameter.getDeclaringClass().getEnumConstants();
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return super.getDescription();
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}
	
	@Override
	public void setValue(Object val) throws IllegalArgumentException {
		if (val instanceof Enum)
			enumParameter = (Enum<?>) val;
	}
	
	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return enumParameter;
	}
	
	public Enum<?> getEnumValue() {
		return enumParameter;
	}
	
	/**
	 * this will return all possible enum values contained in the enum stored in
	 * this object
	 * 
	 * @return
	 */
	public Enum<?>[] getAllEnumValue() {
		return allEnums;
	}
}
