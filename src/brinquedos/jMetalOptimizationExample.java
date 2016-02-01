
package brinquedos;

import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.acGA;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.scGA;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.ssGA;
import jmetal.metaheuristics.singleObjective.particleSwarmOptimization.PSO;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;

public class jMetalOptimizationExample
{
	public static void main(String[] args)
	{
		try 
		{
			HashMap<String, Double> parameters = new HashMap<String, Double>();
			Operator mutation;
	
			Problem problem = new jMetalOptimizationExampleProblem();
			Algorithm algorithm = new PSO(problem); 
	
		    algorithm.setInputParameter("swarmSize", 200);
		    algorithm.setInputParameter("maxIterations", 5000);
		    
		    parameters.clear();
		    parameters.put("probability", 1.0 / problem.getNumberOfVariables());
		    parameters.put("distributionIndex", 20.0);
		    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    
		    algorithm.addOperator("mutation", mutation);
		    
			/* Execute the Algorithm */
		    System.out.println("Starting optimization...");
			long initTime = System.currentTimeMillis();
		    SolutionSet population = algorithm.execute();
		    long estimatedTime = System.currentTimeMillis() - initTime;
		    System.out.println("Total execution time: " + estimatedTime + " ms");
		    
		    /* Log messages */
			System.out.println("Objective: " + population.get(0));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
