package brinquedos;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.variable.BinaryReal;
import jmetal.util.JMException;

public class jMetalOptimizationExampleProblem extends Problem
{
	public jMetalOptimizationExampleProblem() 
	{
		numberOfVariables_ = 100;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;
		
		upperLimit_ = new double[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];

		// Stores the length of each variable when applicable (e.g., Binary and Permutation variables)
		// Nota: no exemplo Sphere o pessoal da jMetal usa variaveis BinaryReal/Real e nao setam o length, apenas os lower and upper limits. 
		length_ = new int[numberOfVariables_]; 
		
		problemName_ = "AnyFamousFunction";
		// solutionType_ = new BinaryRealSolutionType(this);
		solutionType_ = new RealSolutionType(this);
	    
		for (int i = 0; i < numberOfVariables_; i++)
		{
			upperLimit_[i] = 0.0;
			lowerLimit_[i] = Math.PI;
		}
	}
	
	@Override
	public void evaluate(Solution sol) throws JMException
	{
		double fitness = 0.0;
		
		// fitness = simpleQuadractic(sol);
		// fitness = f4(sol);
		fitness = f7(sol);
		
		sol.setObjective(0, fitness);
	}
	
	protected double simpleQuadractic(Solution sol) throws JMException
	{
		double fitness = 0.0;
		
		for (int i = 0; i < numberOfVariables_; i++)
		{
			// Nota: observe que BinaryReal/Real eh um tipo de VARIAVEL nao de solucao!!!!!!!
			double x = sol.getDecisionVariables()[i].getValue(); 
			fitness += Math.pow(x, 2); 
		}
		
		return fitness;
	}
	
	// easy function (min = 0, searchSpace=[-600, 600])
	protected double f4(Solution sol) throws JMException
	{
		double mult = 1.0;
		double fitness = 0.0;
		
		for (int i = 0; i < numberOfVariables_; i++)
		{
			mult = 1.0;
			
			for (int j = 0; j < numberOfVariables_; j++)
			{
				// Nota: observe que BinaryReal/Real eh um tipo de VARIAVEL nao de solucao!!!!!!!
				double y = sol.getDecisionVariables()[j].getValue();
				y *= (Math.cos(y / Math.sqrt(j + 1)) + 1.0);
			}
			
			// Nota: observe que BinaryReal/Real eh um tipo de VARIAVEL nao de solucao!!!!!!!
			double x = sol.getDecisionVariables()[i].getValue(); 
			fitness += (-Math.pow(x, 2) - mult);  
		}
		
		fitness /= 4000.0;
		return fitness;
	}
	
	// hard function (min = -99.2784, searchSpace=[0, pi])
	protected double f7(Solution sol) throws JMException
	{
		double fitness = 0.0;
		
		for (int i = 0; i < numberOfVariables_; i++)
		{
			// Nota: observe que BinaryReal/Real eh um tipo de VARIAVEL nao de solucao!!!!!!!
			double x = sol.getDecisionVariables()[i].getValue(); 
			fitness += (Math.sin(x) * Math.pow(Math.sin((i + 1) * Math.pow(x, 2)) / Math.PI, numberOfVariables_));  
		}
		
		return (-fitness);
	}
}
