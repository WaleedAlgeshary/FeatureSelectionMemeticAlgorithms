package brinquedos;

import java.util.BitSet;

import algorithms.BinaryZombieSearch;
import algorithms.SpecifierInterface;


public class TestZombieSearch
{
	public static void main(String[] args)
	{
		try
		{
			SpecifierInterface specifier = new AllOnesSpecifier();
			BinaryZombieSearch searcher = new BinaryZombieSearch();
			searcher.initialize(specifier, new StringBuffer());
			BitSet solution = searcher.run();

			System.out.println("Num Calls: " + searcher.numEvaluations());
			System.out.println("Solution Objective: " + searcher.bestObjective());
			
			for (int i = 0; i < solution.size(); i++)
				if (solution.get(i)) System.out.print(1);
				else System.out.print(0);
			
			System.out.println("");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
