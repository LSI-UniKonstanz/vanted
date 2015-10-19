package org.vanted.animation.interpolators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vanted.animation.data.InterpolatableTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.loopers.Looper;
/**
 * Interpolates the data values as if it were drawing a Bézier Curve.
 * @see <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">https://en.wikipedia.org/wiki/B%C3%A9zier_curve</a>
 * @author - Patrick Shaw
 */
public class BezierInterpolator extends Interpolator {
	/**
	 * Represents pascal's triangle.
	 * @see <a href="https://en.wikipedia.org/wiki/Pascal%27s_triangle">https://en.wikipedia.org/wiki/Pascal%27s_triangle</a>
	 */
	private static List<Long[]> polynomialConstantsList = 
			Arrays.asList(
					new Long[]{1L},
					new Long[]{1L,1L},
					new Long[]{1L,2L,1L},
					new Long[]{1L,3L,3L,1L},
					new Long[]{1L,4L,6L,4L,1L},
					new Long[]{1L,5L,10L,10L,5L,1L},
					new Long[]{1L,6L,15L,20L,15L,6L,1L},
					new Long[]{1L,7L,21L,35L,35L,21L,7L,1L},
					new Long[]{1L,8L,28L,56L,70L,56L,28L,8L,1L},
					new Long[]{1L,9L,36L,84L,126L,126L,84L,36L,9L,1L},
					new Long[]{1L,10L,45L,120L,210L,252L,210L,120L,45L,10L,1L},
					new Long[]{1L,11L,55L,165L,330L,462L,462L,330L,165L,55L,11L,1L},
					new Long[]{1L,12L,66L,220L,495L,792L,924L,792L,495L,220L,66L,12L,1L},
					new Long[]{1L,13L,78L,286L,715L,1287L,1716L,1716L,1287L,715L,286L,78L,13L,1L},
					new Long[]{1L,14L,91L,364L,1001L,2002L,3003L,3432L,3003L,2002L,1001L,364L,91L,14L,1L},
					new Long[]{1L,15L,105L,455L,1365L,3003L,5005L,6435L,6435L,5005L,3003L,1365L,455L,105L,15L,1L},
					new Long[]{1L,16L,120L,560L,1820L,4368L,8008L,11440L,12870L,11440L,8008L,4368L,1820L,560L,120L,16L,1L},
					new Long[]{1L,17L,136L,680L,2380L,6188L,12376L,19448L,24310L,24310L,19448L,12376L,6188L,2380L,680L,136L,17L,1L},
					new Long[]{1L,18L,153L,816L,3060L,8568L,18564L,31824L,43758L,48620L,43758L,31824L,18564L,8568L,3060L,816L,153L,18L,1L},
					new Long[]{1L,19L,171L,969L,3876L,11628L,27132L,50388L,75582L,92378L,92378L,75582L,50388L,27132L,11628L,3876L,969L,171L,19L,1L},
					new Long[]{1L,20L,190L,1140L,4845L,15504L,38760L,77520L,125970L,167960L,184756L,167960L,125970L,77520L,38760L,15504L,4845L,1140L,190L,20L,1L},
					new Long[]{1L,21L,210L,1330L,5985L,20349L,54264L,116280L,203490L,293930L,352716L,352716L,293930L,203490L,116280L,54264L,20349L,5985L,1330L,210L,21L,1L},
					new Long[]{1L,22L,231L,1540L,7315L,26334L,74613L,170544L,319770L,497420L,646646L,705432L,646646L,497420L,319770L,170544L,74613L,26334L,7315L,1540L,231L,22L,1L},
					new Long[]{1L,23L,253L,1771L,8855L,33649L,100947L,245157L,490314L,817190L,1144066L,1352078L,1352078L,1144066L,817190L,490314L,245157L,100947L,33649L,8855L,1771L,253L,23L,1L},
					new Long[]{1L,24L,276L,2024L,10626L,42504L,134596L,346104L,735471L,1307504L,1961256L,2496144L,2704156L,2496144L,1961256L,1307504L,735471L,346104L,134596L,42504L,10626L,2024L,276L,24L,1L},
					new Long[]{1L,25L,300L,2300L,12650L,53130L,177100L,480700L,1081575L,2042975L,3268760L,4457400L,5200300L,5200300L,4457400L,3268760L,2042975L,1081575L,480700L,177100L,53130L,12650L,2300L,300L,25L,1L},
					new Long[]{1L,26L,325L,2600L,14950L,65780L,230230L,657800L,1562275L,3124550L,5311735L,7726160L,9657700L,10400600L,9657700L,7726160L,5311735L,3124550L,1562275L,657800L,230230L,65780L,14950L,2600L,325L,26L,1L},
					new Long[]{1L,27L,351L,2925L,17550L,80730L,296010L,888030L,2220075L,4686825L,8436285L,13037895L,17383860L,20058300L,20058300L,17383860L,13037895L,8436285L,4686825L,2220075L,888030L,296010L,80730L,17550L,2925L,351L,27L,1L},
					new Long[]{1L,28L,378L,3276L,20475L,98280L,376740L,1184040L,3108105L,6906900L,13123110L,21474180L,30421755L,37442160L,40116600L,37442160L,30421755L,21474180L,13123110L,6906900L,3108105L,1184040L,376740L,98280L,20475L,3276L,378L,28L,1L},
					new Long[]{1L,29L,406L,3654L,23751L,118755L,475020L,1560780L,4292145L,10015005L,20030010L,34597290L,51895935L,67863915L,77558760L,77558760L,67863915L,51895935L,34597290L,20030010L,10015005L,4292145L,1560780L,475020L,118755L,23751L,3654L,406L,29L,1L},
					new Long[]{1L,30L,435L,4060L,27405L,142506L,593775L,2035800L,5852925L,14307150L,30045015L,54627300L,86493225L,119759850L,145422675L,155117520L,145422675L,119759850L,86493225L,54627300L,30045015L,14307150L,5852925L,2035800L,593775L,142506L,27405L,4060L,435L,30L,1L},
					new Long[]{1L,31L,465L,4495L,31465L,169911L,736281L,2629575L,7888725L,20160075L,44352165L,84672315L,141120525L,206253075L,265182525L,300540195L,300540195L,265182525L,206253075L,141120525L,84672315L,44352165L,20160075L,7888725L,2629575L,736281L,169911L,31465L,4495L,465L,31L,1L},
					new Long[]{1L,32L,496L,4960L,35960L,201376L,906192L,3365856L,10518300L,28048800L,64512240L,129024480L,225792840L,347373600L,471435600L,565722720L,601080390L,565722720L,471435600L,347373600L,225792840L,129024480L,64512240L,28048800L,10518300L,3365856L,906192L,201376L,35960L,4960L,496L,32L,1L}
					);
	/**
	 * Gets the polynomial constants for an expanded polynomial equation.
	 * @param polynomialOrder
	 * Specifies the order of constants that you are trying to retrieve.
	 */
	private static Long[] getPolynomialConstants(int polynomialOrder)
	{
		assert polynomialOrder >= 1;
		for(int iOrder = polynomialConstantsList.size();iOrder <= polynomialOrder;iOrder++)
		{
			Long[] prevConstants = polynomialConstantsList.get(iOrder - 1);
			Long[] newConstants = new Long[prevConstants.length + 1];
			newConstants[0] = 1L;
			newConstants[newConstants.length - 1] = 1L;
			for(int q = 0; q < prevConstants.length - 1; q++)
			{
				newConstants[q + 1] = prevConstants[q] + prevConstants[q + 1];
			}
			polynomialConstantsList.add(newConstants);
		}
		return polynomialConstantsList.get(polynomialOrder-1);
	} 
	@Override
	protected int getPointsBefore() {
		return 0;
	}

	@Override
	protected int getPointsAfter() {
		return 1;
	} 
	@Override
	public <V,T extends InterpolatableTimePoint<V>> V interpolate(double time, double duration,
			int previousIndex, List<T> dataPoints, Looper looper)
	{
		List<T> pointsUsed = looper.getPointsUsed(dataPoints,previousIndex, getPointsBefore(), getPointsAfter());
		double normalizedTime = getNormalizedTime(time,duration, dataPoints,pointsUsed); 
		double tPerPoint = (1/(double)dataPoints.size());
		double t = tPerPoint * previousIndex + normalizedTime * tPerPoint;	
		//System.out.println(t);
		return (V) interpolate(t,dataPoints);
	}
	@Override
	protected double interpolate(double t, double... y) { 
		double value = 0;
		Long polynomialConstants[] = getPolynomialConstants(y.length); 
		//String output = "";
		double tCalculations[] = new double[y.length];
		double tMinusCalculations[] = new double[y.length];
		double tMinusCalculation = 1;
		double tCalculation = 1;
		for(int i = 0; i < y.length;i++)
		{
			tMinusCalculations[y.length - (1+ i)] = tMinusCalculation;
			tCalculations[i] = tCalculation;

			tMinusCalculation *= 1- t;
			tCalculation *= t;
		}
		for(int i = 0;i < y.length;i++)
		{ 
			
			value += polynomialConstants[i] * tCalculations[i] * tMinusCalculations[i] * y[i];
		}
		//System.out.println(output + "\n");
		return value;
	}  
}
