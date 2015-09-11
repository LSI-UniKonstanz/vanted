package org.vanted.animation.animators;

import java.awt.Color;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class TestRainbowAlgorithm {
	public static Color CalcRainbowColour(double input)
	{
		int R = 255,G = 255,B = 255;
		final double rate = 1;
		input *= rate;
		
		while(input >= 1785)
		{
			input -= 1530;
		}
		if(input >= 1531)
		{
			B = 0;
			R = 0;
			input -=1530;
			R = CCUp(R,input);
		}
		else
		{
			if(input >= 1276)
			{
				input -= 1275;
				R = 0;
				B =CCDown(B,input);
			}
			else
			{
				if(input >= 1021)
				{
					input -= 1020;
					R= 0;
					G = 0;
					G = CCUp(G, input);
				}
				else
				{
					if(input >= 766)
					{
						input -= 765;
						G = 0;
						R = CCDown(R, input);
					}
					else
					{
						if(input >= 511)
						{
							G =0;
							B =0;
							input -= 510;
							B = CCUp(B, input);
						}
						else
						{
							if(input >= 256)
							{
								input -= 255;
								B = 0;
								G = CCDown(G,input);
							}
							else
							{
								if(input >= 0)
								{
									B = CCDown(B,input);
								}
							}
						}
					}
				}
			}
		}
		return new Color(G,B,R);
	}
	public static int CCDown(int x, double input)
	{
		if(input >= 255)
		{
			x = 0;
		}
		else
		{
			x -= input;
		}
		return x;
	}
	public static int CCUp(int x,double input)
	{
		if(input >= 255)
		{
			x = 255;
		}
		else
		{
			x += input;
		}
		return x;
	}
}
