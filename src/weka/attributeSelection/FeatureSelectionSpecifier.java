package weka.attributeSelection;

import java.util.BitSet;
import java.util.HashMap;

import weka.core.Instances;
import algorithms.SpecifierInterface;

public class FeatureSelectionSpecifier implements SpecifierInterface
{
	protected Instances _data;
	protected SubsetEvaluator _featureSetEvaluator;
	
	protected int _classIndex;
	
	protected BitSet _currentBestBitSet;
	protected double _currentBestError;
	
	protected static long _numFunctionCalls = 0;
	
	protected HashMap<String, Double> _cache;
	
	public FeatureSelectionSpecifier(SubsetEvaluator featureSetEvaluator, Instances data)
	{
		_data = data;
		_featureSetEvaluator = featureSetEvaluator;
		
		_classIndex = data.classIndex();
		
		_currentBestBitSet = null;
		_currentBestError = -Double.MAX_VALUE;
		_cache = new HashMap<String, Double>();
		
		resetNumFunctionCalls();
	}
	
	public double calcSubsetMerit(BitSet selection) throws Exception
	{
		if (selection == null) 
			throw new Exception("SOLUTION IS NULL!!!");
		
		double fitness = 0.0;

		try
		{
			selection.clear(_classIndex);
			
			if (_cache.containsKey(selection.toString()))
				fitness = _cache.get(selection.toString());
			else
			{
				// empty solutions are invalid, and receive max fitness
				if (selection.cardinality() <= 0)  
					fitness = -Double.MAX_VALUE;
				else
					fitness = _featureSetEvaluator.evaluateSubset(selection); // returns the error rate
				
				_cache.put(selection.toString(), fitness);
				
				if (fitness > _currentBestError)
				{
					_currentBestError = fitness;
					_currentBestBitSet = selection;
				}
			}

			_numFunctionCalls++;
			
			if (_numFunctionCalls % 100 == 0)
				System.out.println(_numFunctionCalls + " - BitSet: " + _currentBestBitSet + " Fitness: " + _currentBestError);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return fitness;
	}
	
	public long getNumFunctionCalls()
	{
		return _numFunctionCalls;
	}
	
	public void resetNumFunctionCalls()
	{
		_numFunctionCalls = 0;
	}
	
	public int numAttributes()
	{
		return _data.numAttributes() - 1;
	}
	
	public int numDimensions()
	{
		return _data.numAttributes();
	}
}
