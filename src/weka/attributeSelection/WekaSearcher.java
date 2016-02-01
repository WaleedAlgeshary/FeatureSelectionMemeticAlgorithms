package weka.attributeSelection;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

public class WekaSearcher extends ASSearch implements OptionHandler
{
	private static final long serialVersionUID = 91239712938719273L;
	private double _fitness;
	private int[] _result;
	
	/**
	 * Select the method for ranking the features. This ranking will be used in the local search step. 
	 */
	private ASEvaluation _rankingMethodForLocalSearch;
	
	public WekaSearcher()
	{
		_fitness = Double.MAX_VALUE;
		_result = null;
		_rankingMethodForLocalSearch = new ReliefFAttributeEval(); // default attribute evaluator 
	}
	
	public ASEvaluation getRankingMethodForLocalSearch()
	{
		return _rankingMethodForLocalSearch;
	}

	public void setRankingMethodForLocalSearch(ASEvaluation asev) throws Exception 
	{
		if (!(asev instanceof AttributeEvaluator))
			throw new Exception(asev.getClass().getName() + " is not a Attribute evaluator!");
		
		_rankingMethodForLocalSearch = asev;
	}

	public int[] search(ASEvaluation ASEval, Instances data) throws Exception 
	{
		if (!(ASEval instanceof SubsetEvaluator))  
			throw new Exception(ASEval.getClass().getName() + " is not a Subset evaluator!");
		
		Optimizer opt = new Optimizer();
		
		if (_rankingMethodForLocalSearch == null)
			throw new Exception("WekaSearcher::search()::_rankingMethodForLocalSearch is null!!");

		_result = opt.run((SubsetEvaluator) ASEval, _rankingMethodForLocalSearch, data); 
		_fitness = opt.getWinnerFitness();
		
		return _result;
	}
	
	@Override
	public String toString()
	{
		StringBuffer BfString = new StringBuffer();
		BfString.append("\tWekaSearcher\n");
		BfString.append("\tFitness of best subset found: " + (-_fitness) + "\n");
		return BfString.toString();
	}
	
	public Enumeration<Option> listOptions()
	{
		Vector<Option> newVector = new Vector<Option>(1);

		newVector.addElement(new Option(
			"\tClass name of the evaluator to rank the features for LocalSearch.\n"
					+ "\tPlace any extra options LAST on the command line\n"
					+ "\tfollowing a \"--\". eg.:\n"
					+ "\t\t-B weka... -- -K\n"
					+ "\t(default: RealifFEvaluator)",
			"B", 1, "-B <base learner>"));

		if ((_rankingMethodForLocalSearch != null) && (_rankingMethodForLocalSearch instanceof OptionHandler)) 
		{
		      newVector.addElement(new Option("", "", 0, "\nOptions specific to scheme " + _rankingMethodForLocalSearch.getClass().getName() + ":"));
		      newVector.addAll(Collections.list(((OptionHandler) _rankingMethodForLocalSearch).listOptions()));
		}
		
		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception
	{
		resetOptions();
		
		String optionString;
		optionString = Utils.getOption('B', options);

		if (optionString.length() == 0)
			optionString = ReliefFAttributeEval.class.getName();

		ASEvaluation featureRanker = ASEvaluation.forName(optionString, Utils.partitionOptions(options));
		setRankingMethodForLocalSearch(featureRanker);
	}

	protected void resetOptions()
	{
		_rankingMethodForLocalSearch = new ReliefFAttributeEval();
	}

	public String[] getOptions()
	{
		int i, p;
		String[] generalOptions = new String[0]; // now the algorithm has no general options
		String[] rankerOptions = new String[0];

		if (_rankingMethodForLocalSearch != null)
		{
			if (_rankingMethodForLocalSearch instanceof OptionHandler)
			{
				String[] localOptions = ((OptionHandler) _rankingMethodForLocalSearch).getOptions();	
				rankerOptions = new String[3 + localOptions.length];
				
				rankerOptions[0] = "-B";
				rankerOptions[1] = getRankingMethodForLocalSearch().getClass().getName();
				rankerOptions[2] = "--";
				
				System.arraycopy(localOptions, 0, rankerOptions, 3, localOptions.length);
			}
		}

		String[] options = new String[generalOptions.length + rankerOptions.length];
		p = 0;
		
		for (i = 0; i < generalOptions.length; i++, p++)
			options[p] = generalOptions[i];
		
		for (i = 0; i < rankerOptions.length; i++, p++)
			options[p] = rankerOptions[i];
		
		return options;
	}
}
