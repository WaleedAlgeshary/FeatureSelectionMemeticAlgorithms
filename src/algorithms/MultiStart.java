package algorithms;

import java.util.ArrayList;
import java.util.BitSet;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.SubsetEvaluator;
import weka.core.Instances;

public class MultiStart
{
	protected SubsetEvaluator _featureSetEvaluator; 
	protected ASEvaluation _featureRanker; 
	protected Instances _data;

	protected BitSet _bestFound;
	protected double _bestFoundValue;

	protected ArrayList<BitSet> _solutions;
	protected ArrayList<Double> _objectives;
	protected ArrayList<Integer> _iter_without_improvement;

	protected int _num_function_calls;
	protected int _num_regenarations;
	
	protected static int _NUM_SOLUTIONS = 100;
	protected static double _PROB_USE_RANK = 0.0;
	protected static int _TOLERANCE_BEFORE_RESTART = 20; // number of iterations without improvement before restart
	protected static int _MAX_FUNCTION_CALLS = 4000;
	
	protected double _evaluate(BitSet solution) throws Exception
	{
		double fitness = _featureSetEvaluator.evaluateSubset(solution);
		
		if (fitness > _bestFoundValue)
		{
			_bestFoundValue = fitness;
			_bestFound = solution;
		}

		_num_function_calls++;
		
		if (_num_function_calls % 100 == 0)
			System.out.println("Call: " + _num_function_calls + " Current Best: " + _bestFoundValue);
		
		return fitness;
	}
	
	protected BitSet _newRandomSolution()
	{
		BitSet solution = new BitSet(_data.numAttributes() - 1);

		for (int j = 0; j < (_data.numAttributes() - 1); ++j)
			if (Math.random() < 0.5)
				solution.set(j);

		return solution;
	}
	
	protected void _initializeSolutions() throws Exception
	{
		for (int i = 0; i < _NUM_SOLUTIONS; i++)
		{
			double objective;
			BitSet solution = _newRandomSolution();
			
			objective = _evaluate(solution);
			
			_solutions.add(solution);
			_objectives.add(objective);
			_iter_without_improvement.add(0);
		}
	}
	
	protected void _optimize() throws Exception
	{
		int bitIndex = -1;
		double fitness = -1;
		boolean regenerated = false;

		while (_num_function_calls < _MAX_FUNCTION_CALLS)
		{
			for (int i = 0; i < _solutions.size(); i++)
			{
				regenerated = false;
				
				if (_iter_without_improvement.get(i) >= _TOLERANCE_BEFORE_RESTART)
				{
					_solutions.set(i, _newRandomSolution());
					regenerated = true;
					_num_regenarations++;
				}
				else
				{
					bitIndex = (int) (Math.random() * ((double) _data.numAttributes() - 1));
					_solutions.get(i).flip(bitIndex);
				}
				
				fitness = _evaluate(_solutions.get(i));
				
				if (fitness > _objectives.get(i))
				{
					_objectives.set(i, fitness);
					_iter_without_improvement.set(i, 0);
				}
				else
				{
					if (!regenerated)
						_solutions.get(i).flip(bitIndex);
					
					_iter_without_improvement.set(i, _iter_without_improvement.get(i) + 1);
				}
			}
		}
		
		System.out.println("Num regenerations: " + _num_regenarations);
	}
	
	public MultiStart(SubsetEvaluator featureSetEvaluator, ASEvaluation featureRanker, Instances data) throws Exception
	{
		_data = data;
		_featureRanker = featureRanker;
		_featureSetEvaluator = featureSetEvaluator;

		_bestFound = null;
		_bestFoundValue = -Double.MAX_VALUE;
		_num_function_calls = 0;
		
		_solutions = new ArrayList<BitSet>();
		_objectives = new ArrayList<Double>();
		_iter_without_improvement = new ArrayList<Integer>();
		
		_num_regenarations = 0;
		
		_initializeSolutions();
		_optimize();
	}
	
	public BitSet getBestSolution()
	{
		return _bestFound;
	}
	
	public double getBestSolutionFitness()
	{
		return _bestFoundValue;
	}
	
	public int getNumFunctionCalls()
	{
		return _num_function_calls;
	}
}
