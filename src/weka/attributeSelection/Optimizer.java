
package weka.attributeSelection;

import java.util.BitSet;
import java.util.HashMap;

import algorithms.BinaryZombieSearch;
import algorithms.MultiStart;
import algorithms.SpecifierInterface;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.gGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.encodings.variable.Binary;
import weka.core.Instances;

public class Optimizer
{
	protected double _winnerFitness;
	protected BitSet _winnerIndividual;
	
	public double getWinnerFitness()
	{
		return _winnerFitness;
	}
	
	public double[] computeFeatureRank(ASEvaluation featureRanker, Instances data)
	{
		double[] rank = new double[data.numAttributes()];

		try
		{
			featureRanker.buildEvaluator(data);
			AttributeEvaluator evaluator = (AttributeEvaluator) featureRanker;
			
			for (int i = 0; i < data.numAttributes(); i++)
			{
				rank[i] = evaluator.evaluateAttribute(i);
				System.out.printf("%d: %.20f\n", i, rank[i]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			for (int i = 0; i < data.numAttributes(); i++)
				rank[i] = 1.0 / (double) data.numAttributes();
		}
		
		return rank;
	}
	
	public int[] runGALS(SubsetEvaluator featureSetEvaluator, ASEvaluation featureRanker, Instances data)
	{
		try 
		{
			HashMap<String, Object> parameters = new HashMap<String, Object>();
		    
			Operator crossover; // Crossover operator
		    Operator mutation; // Mutation operator
		    Operator selection; // Selection operator
	
			Problem problem = new ObjectiveFunction(featureSetEvaluator, featureRanker, data);
			Algorithm algorithm = new gGA(problem);
			
		    algorithm.setInputParameter("populationSize", 100);
		    algorithm.setInputParameter("maxEvaluations", 2000);
		    
		    parameters.clear();
		    parameters.put("probability", 0.8);
		    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);                   

		    parameters.clear();
		    parameters.put("probability", 1.0);
		    parameters.put("problem", problem);
		    parameters.put("featureRank", computeFeatureRank(featureRanker, data));
		    mutation = new AddDellMutation(parameters);
		    
		    parameters = null ;
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                            
		    
		    /* Add the operators to the algorithm*/
		    algorithm.addOperator("crossover", crossover);
		    algorithm.addOperator("mutation", mutation);
		    algorithm.addOperator("selection", selection);
		    
			System.out.println("Starting optimization...");
			long initTime = System.currentTimeMillis();

			/* Execute the Algorithm */
			((ObjectiveFunction) problem).resetNumFunctionCalls();
		    SolutionSet population = algorithm.execute();
			
		    /* Get the winner and the winner fitness */
		    _winnerFitness = population.get(0).getObjective(0);
			_winnerIndividual = ((Binary) population.get(0).getDecisionVariables()[0]).bits_;
			_winnerIndividual.clear(data.classIndex());
			
		    /* Log messages */
			long estimatedTime = System.currentTimeMillis() - initTime;
		    System.out.println("Total execution time: " + estimatedTime + " ms");
			System.out.println("Num Function Calls: " + ((ObjectiveFunction) problem).getNumFunctionCalls());
			System.out.println((_winnerFitness) + " [" + _winnerIndividual.cardinality() + "]");
			System.out.println("Winner: " + _winnerIndividual.toString());

			return BitSetToIntArray(_winnerIndividual);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			// ***********************************************
			// RETURNS ALL ATTRIBUTES SET
			// ***********************************************
			
			int[] r = new int[data.numAttributes()];
			
			for (int i = 0; i < data.numAttributes(); i++)
				if (i != data.classIndex())
					r[i] = i;
			
			_winnerIndividual = new BitSet();
			_winnerFitness = Double.MAX_VALUE;
			
			return r;
		}
	}
	
	public int[] runZombieSwarm(SubsetEvaluator featureSetEvaluator, Instances data)
	{
		int[] r;
		
		try
		{
			SpecifierInterface specifier = new FeatureSelectionSpecifier(featureSetEvaluator, data);
			BinaryZombieSearch searcher = new BinaryZombieSearch();
			searcher.initialize(specifier, new StringBuffer());
			
			long initTime = System.currentTimeMillis();
			
			BitSet solution = searcher.run();
			
			_winnerIndividual = solution;
			_winnerFitness = searcher.bestObjective();
			
		    /* Log messages */
			long estimatedTime = System.currentTimeMillis() - initTime;
		    System.out.println("Total execution time: " + estimatedTime + " ms");
			System.out.println("Num Function Calls: " + ((FeatureSelectionSpecifier) specifier).getNumFunctionCalls());
			System.out.println(_winnerFitness + " [" + _winnerIndividual.cardinality() + "]");
			System.out.println("Winner: " + _winnerIndividual.toString());
			
			r = BitSetToIntArray(solution);
		} 
		catch (Exception e)
		{
			r = new int[data.numAttributes()];
			
			for (int i = 0; i < data.numAttributes(); i++)
				if (i != data.classIndex())
					r[i] = i;
			
			_winnerIndividual = new BitSet();
			_winnerFitness = -Double.MAX_VALUE;
		}
		
		return r;
	}
	
	protected int[] runMultiStart(SubsetEvaluator featureSetEvaluator, ASEvaluation featureRanker, Instances data)
	{
		int[] r;
		
		try
		{
			long initTime = System.currentTimeMillis();
			
			MultiStart ms = new MultiStart(featureSetEvaluator, featureRanker, data);
			BitSet solution = ms.getBestSolution();
			r = BitSetToIntArray(solution);
			
			_winnerIndividual = solution;
			_winnerFitness = ms.getBestSolutionFitness();
			
			long estimatedTime = System.currentTimeMillis() - initTime;
		    System.out.println("Total execution time: " + estimatedTime + " ms");
			System.out.println("Num Function Calls: " + ms.getNumFunctionCalls());
			System.out.println(_winnerFitness + " [" + _winnerIndividual.cardinality() + "]");
			System.out.println("Winner: " + _winnerIndividual.toString());
		} 
		catch (Exception e)
		{
			r = new int[data.numAttributes()];
			
			for (int i = 0; i < data.numAttributes(); i++)
				if (i != data.classIndex())
					r[i] = i;
			
			_winnerIndividual = new BitSet();
			_winnerFitness = -Double.MAX_VALUE;
		}
		
		return r;
	}
	
	public int[] run(SubsetEvaluator featureSetEvaluator, ASEvaluation featureRanker, Instances data)
	{
		// return runMultiStart(featureSetEvaluator, featureRanker, data);
		// return runGALS(featureSetEvaluator, featureRanker, data);
		return runZombieSwarm(featureSetEvaluator, data);
	}
	
	protected int[] BitSetToIntArray(BitSet group) 
	{
	    int[] list = new int[group.cardinality()];
	    int count = 0;

	    for (int i = 0; i < group.size(); i++) 
	      if (group.get(i)) 
	        list[count++] = i;

	    return list;
	  }
}
