/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.util.PluginHelper;

/**
 * @author matthiak
 *
 */
public class SearchAlgorithms {

	public static enum LogicalOp {
		AND,
		OR,
		NOT
	}
	

	/**
	 * Searches Algorithms listed in Vanted, given an array of categories that this algorithm needs to be a member of
	 * The categories are combined with an AND operator 
	 */
	public static List<Algorithm> searchAlgorithms(Category[] categories) {
		
		OperatorOnCategories[] operatorSearchSet = new OperatorOnCategories[] {
				new OperatorOnCategories(categories)
		};
		return searchAlgorithms(operatorSearchSet);
	}
	
	/**
	 * Searches Algorithms listed in Vanted, given an array of operators on categories.
	 * This gives the user more power to filter and select algorithms
	 * The operators currently supported are OR, AND, and NOT
	 * Currently you cannot nest these operators
	 */
	public static List<Algorithm> searchAlgorithms(OperatorOnCategories[] operatorSearchSet) {
		List<Algorithm> retlist = new ArrayList<Algorithm>();
		
		
		List<Algorithm> availableAlgorithms = PluginHelper.getAvailableAlgorithms();
		
		for(Algorithm curAlgorithm : availableAlgorithms) {
			
			boolean hasAllCateogories = false;
			boolean hasAnyCateogories = false;
			boolean hasNotCateogories = false;
			
			Set<Category> setCategory = null;
			try {
				setCategory = curAlgorithm.getSetCategory();
			} catch (AbstractMethodError e) {
				e.printStackTrace();
			}
			
			/*
			 * for now, ignore all algorithms, that do not support setCategory
			 */
			if(setCategory == null)
				continue;
			
			for (OperatorOnCategories curOperatorOnCategory : operatorSearchSet) {
				switch(curOperatorOnCategory.operator){
				case AND:
					hasAllCateogories = true; // inverse check -> if we don't find at least one category it is set to false 
					for(Category curCat : curOperatorOnCategory.category)
						if( ! setCategory.contains(curCat)) {
							hasAllCateogories = false;
							break;
						}
					break;
				case OR:
					for(Category curCat : curOperatorOnCategory.category)
						if( setCategory.contains(curCat)) {
							hasAnyCateogories = true;
							break;
						}
					break;
				case NOT:
					hasNotCateogories = true; // inverse check -> if we find at least one category it is set to false 
					for(Category curCat : curOperatorOnCategory.category)
						if( setCategory.contains(curCat)) {
							hasAllCateogories = false;
							break;
						}
					break;
				default:
				}
			}
			
			boolean hasAllOperator = false;
			boolean hasAnyOperator = false;
			boolean hasNotOperator = false;
			for (OperatorOnCategories curOperatorOnCategory : operatorSearchSet) {
				switch(curOperatorOnCategory.operator){
				case AND:
					hasAllOperator = true;
					break;
				case OR:
					hasAnyOperator = true;
					break;
				case NOT:
					hasNotOperator = true;
					break;
				}
			}
			
			/*
			 *  now all boolean indicators are set to TRUE for operators NOT in the operatorSearchSet
			 */
			if( ! hasAllOperator )
				hasAllCateogories = true;

			if( ! hasAnyOperator )
				hasAnyCateogories = true;
			
			if( ! hasNotOperator )
				hasNotCateogories = true;
			
			if(hasAllCateogories && hasAnyCateogories && hasNotCateogories)
				retlist.add(curAlgorithm);
		}
		
		return retlist;
	}
	
	
	
	public static class OperatorOnCategories {
		LogicalOp operator;
		Category[] category;

		/**
		 * Defines a set of categories with in implicit AND operator
		 * @param category
		 */
		public OperatorOnCategories(Category[] category) {
			super();
			this.operator = LogicalOp.AND;
			this.category = category;
		}
		
		/** Defines an operator combined with a set of categories
		 * @param operator
		 * @param category
		 */
		public OperatorOnCategories(LogicalOp operator, Category[] category) {
			super();
			this.operator = operator;
			this.category = category;
		}
		
	}
}
