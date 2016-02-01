package brinquedos;

import java.util.BitSet;

import algorithms.SpecifierInterface;

public class AllOnesSpecifier implements SpecifierInterface
{
	public double calcSubsetMerit(BitSet selection) 
	{
		return selection.cardinality();
	}
	
	public int numAttributes()
	{
		return 100;
	}
	
	public int numDimensions()
	{
		return 100;
	}
}
