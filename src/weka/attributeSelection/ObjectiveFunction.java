package weka.attributeSelection;

import java.util.BitSet;
import java.util.HashMap;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;
import weka.core.Instances;

public class ObjectiveFunction extends Problem 
{
	private static final long serialVersionUID = 73712832183721321L;

	protected SubsetEvaluator _featureSetEvaluator; 
	protected ASEvaluation _featureRanker; 
	protected Instances _data;
	protected int _numFeatures;
	protected int _classIndex;
	
	protected BitSet currentBestBitSet;
	protected double currentBestError;
	
	protected static long numFunctionCalls = 0;
	
	protected HashMap<String, Double> cache;
	
	public ObjectiveFunction(SubsetEvaluator featureSetEvaluator, ASEvaluation featureRanker, Instances data) 
	{
		_data = data;
		_featureRanker = featureRanker;
		_featureSetEvaluator = featureSetEvaluator;
		_numFeatures = data.numAttributes();
		_classIndex = data.classIndex();
		
		numberOfVariables_ = 1;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;

		// Stores the length of each variable when applicable (e.g., Binary and Permutation variables)
		length_ = new int[1];
		length_[0] = data.numAttributes();
		
		problemName_ = "FeatureSelection";
		solutionType_ = new BinarySolutionType(this);
		
		currentBestBitSet = null;
		currentBestError = Double.MAX_VALUE;
		cache = new HashMap<String, Double>();
		
		resetNumFunctionCalls();
	}
	
	@Override
	public void evaluate(Solution sol) throws JMException
	{
		double fitness = 0.0;

		try
		{
			((Binary) sol.getDecisionVariables()[0]).bits_.clear(_classIndex);
			BitSet selectedFeatures = ((Binary) sol.getDecisionVariables()[0]).bits_; 
			
			if (cache.containsKey(selectedFeatures.toString()))
				fitness = cache.get(selectedFeatures.toString());
			else
			{
				// empty solutions are invalid, and receive max fitness
				if (selectedFeatures.cardinality() <= 0)  
					fitness = Double.MAX_VALUE;
				else
					fitness = -_featureSetEvaluator.evaluateSubset(selectedFeatures); // returns the error rate
				
				cache.put(selectedFeatures.toString(), fitness);
				
				if (fitness < currentBestError)
				{
					currentBestError = fitness;
					currentBestBitSet = selectedFeatures;
				}
			}

			if (numFunctionCalls % 100 == 0)
				System.out.println(numFunctionCalls + " - BitSet: " + currentBestBitSet + " Fitness: " + currentBestError);

			numFunctionCalls++;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		sol.setObjective(0, fitness);
	}
	
	public long getNumFunctionCalls()
	{
		return numFunctionCalls;
	}
	
	public void resetNumFunctionCalls()
	{
		numFunctionCalls = 0;
	}
}
