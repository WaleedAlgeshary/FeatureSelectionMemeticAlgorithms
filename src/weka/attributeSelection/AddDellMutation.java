
package weka.attributeSelection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class AddDellMutation extends Mutation 
{
	private static final long serialVersionUID = 6537215735173251L;
	
	protected ArrayList<Integer> _usedVariables = null;
	protected ArrayList<Integer> _unusedVariables = null;

	protected Problem _problem = null;

	protected Double _mutationProbability_ = null;
	protected double[] _featureRank = null;
	
	public AddDellMutation(HashMap<String, Object> parameters) 
	{
		super(parameters);
	  
		if (parameters.get("probability") != null)
	  		_mutationProbability_ = (Double) parameters.get("probability");
		
		if (parameters.get("problem") != null)
			_problem = (Problem) parameters.get("problem");
	
		if (parameters.get("featureRank") != null)
			_featureRank = (double[]) parameters.get("featureRank");
	} 

	protected void doBitFlip(Solution solution) throws JMException
	{
		try 
		{
			for (int i = 0; i < solution.getDecisionVariables().length; i++) 
			{
				if (Math.random() < 0.2)
				{
					int chosen_bit = (int) (((Binary) solution.getDecisionVariables()[i]).getNumberOfBits() * Math.random()); 
					((Binary) solution.getDecisionVariables()[i]).bits_.flip(chosen_bit);
				}
			}
		}
		catch (ClassCastException e1) 
		{
			printCastExceptionError(e1);
		}
	}

	protected void updateUsedAndUnsedVariables(Solution solution)
	{
		int numBits;
		Binary bin = ((Binary) solution.getDecisionVariables()[0]); 
		numBits = bin.getNumberOfBits();
		BitSet bits = bin.bits_;

		_usedVariables = new ArrayList<Integer>(bits.cardinality());
		_unusedVariables = new ArrayList<Integer>(numBits - bits.cardinality());
		
		for (int i = 0; i < numBits; i++)
		{
			/**
			 * Ignore the class feature! 
			 */

			if (bits.get(i))
				_usedVariables.add(i);
			else
				_unusedVariables.add(i);
		}

		Comparator<Integer> ascendentRankComparator = new Comparator<Integer>() 
		{ 
			public int compare(Integer a, Integer b)
			{
				if (_featureRank[a] == _featureRank[b]) return 0;
				else if (_featureRank[a] < _featureRank[b]) return -1;
				else return 1;
			}
		};
		
		Comparator<Integer> descendentRankComparator = new Comparator<Integer>() 
		{ 
			public int compare(Integer a, Integer b)
			{
				if (_featureRank[a] == _featureRank[b]) return 0;
				else if (_featureRank[a] > _featureRank[b]) return -1;
				else return 1;
			}
		};

		// sort by rank
		Collections.sort(_usedVariables, ascendentRankComparator); // lower ranks first
		Collections.sort(_unusedVariables, descendentRankComparator); // higher ranks first
	}

	protected void doAdds(Solution solution, int numOperations)
	{
		for (int i = 0; (i < numOperations) && (i < _unusedVariables.size()); i++)
			((Binary) solution.getDecisionVariables()[0]).bits_.set(_unusedVariables.get(i));
	}

	protected void doDels(Solution solution, int numOperations)
	{
		for (int i = 0; (i < numOperations) && (i < _usedVariables.size()); i++)
			((Binary) solution.getDecisionVariables()[0]).bits_.clear(_usedVariables.get(i));
	}

	protected Solution doLocalSearch(Solution solution) 
	{
		try
		{
			// Method parameters 
			int L = 4;
			boolean STOP_AT_FIRST_IMPROVEMENT = true;
			double W = (1.0 / 6.0); //0.5;
			double TREASHOLD = 0.01;

			// only perform the local search with a given probability
			if (PseudoRandom.randDouble() > W)
				return solution;
			
			double diff;
			double bestFitness;
			double initialFitness;
			double localFitness;
			boolean improved = false;
			Solution localSolution; 
			Binary bestBin, localBin;
			Solution bestSolution = solution, initialSolution = solution;
			bestBin = (Binary) bestSolution.getDecisionVariables()[0];
			
			_problem.evaluate(bestSolution);
			initialFitness = bestFitness = bestSolution.getObjective(0);

			updateUsedAndUnsedVariables(bestSolution);
			
			for (int i = 1; i <= L; i++)
			{
				for (int j = 1; j <= L; j++)
				{
					localSolution = new Solution(initialSolution);
					localBin = (Binary) localSolution.getDecisionVariables()[0];
					
					doAdds(localSolution, i);
					doDels(localSolution, j);
					
					_problem.evaluate(localSolution);
					localFitness = localSolution.getObjective(0); 
					
					// DEBUG:
					// System.out.println("LS:: i: " + i + " j: " + j + " Fitness(" + ((Binary) localSolution.getDecisionVariables()[0]).toString() + "): " + localFitness);
					
					diff = localSolution.getObjective(0) - bestSolution.getObjective(0);

					if ((localFitness < bestFitness) || ((localBin.bits_.cardinality() < bestBin.bits_.cardinality()) && (diff < TREASHOLD)))
					{
						bestFitness = localFitness;
						bestSolution = localSolution;
						bestBin = localBin;
						
						improved = true;
						
						if (STOP_AT_FIRST_IMPROVEMENT)
							break;
					}
				}
				
				if (STOP_AT_FIRST_IMPROVEMENT)
					if (improved)
						break;
			}

			return bestSolution;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return solution;
		}
	}
	
	public Solution doMutation(double probability, Solution solution) throws JMException 
	{
		// true mutation
		doBitFlip(solution);
		
		solution = doLocalSearch(solution);

		/**
		 *  If the solution is empty, we set a random bit. As this is a general mutation, maybe it is not the best choice let it be here.
		 *  Maybe handle it in the evaluation is the best option. 
		 */
		
		int random_bit;
		Random rn = new Random(System.currentTimeMillis());
		
		for (int i = 0; i < solution.getDecisionVariables().length; i++) 
		{
			if (((Binary) solution.getDecisionVariables()[i]).bits_.cardinality() == 0)
			{
				/**
				 * Check if it is not the class 
				 */
				Binary bin = (Binary) solution.getDecisionVariables()[i]; 
				
				random_bit = rn.nextInt(bin.getNumberOfBits());
				((Binary) solution.getDecisionVariables()[i]).bits_.set(random_bit);
			}
		}
		
		return solution;
	} 

	public Object execute(Object object) throws JMException 
	{
		Solution solution = (Solution) object;

		if (BinarySolutionType.class != solution.getType().getClass()) 
			printTypeExceptionError(solution.getType());

		solution = doMutation(_mutationProbability_, solution);
		return solution;
	} 
	
	protected void printCastExceptionError(Exception e1) throws JMException
	{
		Class cls = AddDellMutation.class;
		String name = cls.getName();
		Configuration.logger_.severe(name + ".doMutation(): " + "ClassCastException error" + e1.getMessage());
		throw new JMException("Exception in " + name + ".doMutation()");
	}
	
	protected void printTypeExceptionError(SolutionType solutionType) throws JMException
	{
		Class cls = AddDellMutation.class;
		String name = cls.getName();
		
		Configuration.logger_.severe(name + ".execute: the solution is not of the right type. The type should be 'Binary', but " + 
				solutionType + " is obtained");

		throw new JMException("Exception in " + name + ".execute()");
	}
} 
