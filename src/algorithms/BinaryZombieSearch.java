package algorithms;

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Random;

import weka.attributeSelection.FeatureSelectionSpecifier;



public class BinaryZombieSearch
{
	protected class Human
	{
		double objective;
		BitSet coords;
		double fear;
	}

	protected class Zombie
	{
		boolean achievedLocalMinima;
		BitSet coords;
		boolean chaser;
	}

	protected int[] _closestHumans; 
	protected StringBuffer _logger;
	protected int _iterCounter;

	protected int _maxIterations = 4000;
	protected int _populationSize = 100;
	protected double _setBitRate = 0.4;
	protected double _zombificationRate = 0.5;
	protected double _chaserRate = 0.2;
	protected int _humanHeadstart = 50;

	protected BitSet _bestFound;
	protected double _bestFoundValue;
	protected SpecifierInterface _specifier;
	protected int _dim, _nAttr; // what is the difference between dim and nAttr?

	protected ArrayList<Human> _humans;
	protected ArrayList<Zombie> _zombies;

	protected int _numEvaluations;
	protected double _probabilityCheaseNearestHuman = 0.05;
	protected double _prob_follow_best_human = 0.05;
	
	public int numEvaluations()
	{
		return _numEvaluations;
	}
	
	public double bestObjective()
	{
		return _bestFoundValue;
	}
	
	protected BitSet randomCoords()
	{
		BitSet result = new BitSet(this._dim);

		for (int i = 0; i < this._nAttr; ++i)
			if (Math.random() < this._setBitRate)
				result.set(i);

		return result;
	}

	protected double calcMerit(BitSet coords) throws Exception
	{
		_numEvaluations++;
		
		double avail = _specifier.calcSubsetMerit(coords);

		if (avail > this._bestFoundValue)
		{
			System.out.println("Better solution found [ " + this._bestFoundValue
					+ " -> " + avail + "]");

			this._logger.append("\n\t[" + this._iterCounter + "]: " + coords
					+ " -> " + avail);

			this._bestFound = (BitSet) coords.clone();
			this._bestFoundValue = avail;
		}

		return avail;
	}

	protected void init() throws Exception
	{
		_numEvaluations = 0;
		
		for (int i = 0; i < this._populationSize; ++i)
		{
			Human new_human = new Human();
			
			new_human.coords = this.randomCoords();
			new_human.fear = Math.random() * 0.7;
			new_human.objective = calcMerit(new_human.coords);
			
			this._humans.add(new_human);
		}
		
		_closestHumans = new int[this._populationSize];
	}

	static protected int hammingDistance(BitSet a, BitSet b)
	{
		int distance = 0;

		for (int i = 0; i < a.size(); ++i)
			if (a.get(i) != b.get(i))
				++distance;

		return distance;
	}

	static protected boolean isNeighbor(BitSet a, BitSet b)
	{
		int differences = 0;

		for (int i = 0; (i < a.size()) && (differences < 2); ++i)
			if (a.get(i) != b.get(i))
				++differences;

		return (differences < 2);
	}

	protected void moveHumans() throws Exception
	{
		if (((FeatureSelectionSpecifier) _specifier).getNumFunctionCalls() > _maxIterations) 
			return;
		
		for (Human human : this._humans)
		{
			int nearZombies = 0;
			double new_value;
			
			int chosenAtt = (int) (Math.random() * this._nAttr);
			human.coords.flip(chosenAtt);

			for (Zombie zombie : this._zombies)
				if (BinaryZombieSearch.isNeighbor(human.coords, zombie.coords))
					++nearZombies;

			new_value = this.calcMerit(human.coords);
			new_value -= human.fear * nearZombies;

			if (human.objective > new_value)
				human.coords.flip(chosenAtt);
			else
				human.objective = new_value;

			if (((FeatureSelectionSpecifier) _specifier).getNumFunctionCalls() > _maxIterations) 
				return;

			if (Math.random() < _prob_follow_best_human)
			{
				// make one of the bits of the human closer to the best.
				// Note: this always change the first bits first. Maybe it is a best strategy save
				// all bits that are different and choose a random one to change. However the computer
				// cost of this solution is higher.
				for (int i = 0; i < human.coords.size(); i++)
				{
					if (human.coords.get(i) != this._bestFound.get(i))
					{
						human.coords.flip(i);
						
						nearZombies = 0;
						
						for (Zombie zombie : this._zombies)
							if (BinaryZombieSearch.isNeighbor(human.coords, zombie.coords))
								++nearZombies;
	
						new_value = this.calcMerit(human.coords);
						new_value -= human.fear * nearZombies;
	
						human.objective = new_value;
						break;
					}
				}
			}
		}
	}

	protected void turnZombie(int index)
	{
		Zombie new_zombie = new Zombie();
		
		new_zombie.coords = this._humans.get(index).coords;
		new_zombie.chaser = (Math.random() < this._chaserRate);
		new_zombie.achievedLocalMinima = true;
		
		this._zombies.add(new_zombie);
		this._humans.remove(index);

		System.out.println("** Human " + index + " became zombie!! " + this._humans.size() + " humans to go");
	}

	public BinaryZombieSearch initialize(SpecifierInterface specifier, StringBuffer outer)
	{
		this._specifier = specifier;
		this._dim = specifier.numDimensions();
		this._nAttr = specifier.numAttributes();
		this._bestFoundValue = Double.MIN_VALUE;
		this._logger = outer;
		
		return this;
	}

	public BitSet run() throws Exception
	{
		this._humans = new ArrayList<Human>();
		this._zombies = new ArrayList<Zombie>();
		this._iterCounter = 0;

		this.init();
		
		for (int i = 0; i < this._humanHeadstart; ++i)
			this.moveHumans();

		Zombie first_zombie = new Zombie();

		first_zombie.coords = this.randomCoords();
		first_zombie.chaser = true;
		first_zombie.achievedLocalMinima = false;
		
		_zombies.add(first_zombie);

		int numClosestHumans = 0;
		
		// while humans still alive
		while (!this._humans.isEmpty() && ((FeatureSelectionSpecifier) _specifier).getNumFunctionCalls() < _maxIterations)
		{
			int zombiePopulation = this._zombies.size();
			this.moveHumans();

			// for each zombie
			for (int idx = 0; (idx < zombiePopulation) && (this._humans.size() > 0); ++idx)
			{
				Zombie zombie = this._zombies.get(idx);

				int zombieDistance = Integer.MAX_VALUE;
				int candidateDistance;

				int humanIdx = 0;
				numClosestHumans = 0;
				
				// find the nearest human. if the zombie are in the exactly same position
				// than the human, transform it with a given probability.
				// TODO: test if using a min distance to transform improves (may lead to loops).
				while (humanIdx < this._humans.size())
				{
					candidateDistance = BinaryZombieSearch.hammingDistance(
						zombie.coords, this._humans.get(humanIdx).coords);

					if (0 == candidateDistance)
					{
						if (Math.random() < this._zombificationRate)
							this.turnZombie(humanIdx);
						else
							++humanIdx;

						zombie.achievedLocalMinima = true;
						zombieDistance = 0;
						continue; 
					}

					if (candidateDistance < zombieDistance)
					{
						zombieDistance = candidateDistance;
			
						numClosestHumans = 1;
						_closestHumans[0] = humanIdx;
					}
					else if (candidateDistance == zombieDistance)
						_closestHumans[numClosestHumans++] = humanIdx;

					++humanIdx;
				}

				System.out.println("Zumbi " + idx + " is " + zombieDistance
						+ " steps away from a human");

				// prevents the zombie from leaving local minima if it's not a chaser.
				if (!zombie.chaser && zombie.achievedLocalMinima)
					continue;
				
				// if the zombie isn't in the exactly same position than a human...
				if (zombieDistance > 0)
				{
					int chosenAtt = -1; 
					
					if (numClosestHumans > 0 && Math.random() < _probabilityCheaseNearestHuman)
					{
						// moves a step closer to one of the nearest humans
						int humanToChase = _closestHumans[(int) (Math.random() * (numClosestHumans - 1) + 0.5)]; 
					
						for (int i = 0; i < zombie.coords.size(); i++)
						{
							if (zombie.coords.get(i) != _humans.get(humanToChase).coords.get(i))
							{
								zombie.coords.flip(i);
								chosenAtt = i;
								break;
							}
						}
					}
					else
					{
						// perform a random move 
						chosenAtt = (int) (Math.random() * (this._nAttr)); 
						zombie.coords.flip(chosenAtt);
					}
					
					zombie.achievedLocalMinima = false;
					boolean foundBetter = false;

					humanIdx = 0;

					// check if it gets closer to any human
					while (humanIdx < this._humans.size())
					{
						candidateDistance = BinaryZombieSearch.hammingDistance(zombie.coords,
							this._humans.get(humanIdx).coords);

						if (0 == candidateDistance)
						{
							if (Math.random() < this._zombificationRate)
								this.turnZombie(humanIdx);
							else
								++humanIdx;

							zombie.achievedLocalMinima = true;
							foundBetter = true;
							continue; 
						}

						++humanIdx;

						if (candidateDistance < zombieDistance)
							foundBetter = true;
					}

					if (!foundBetter && chosenAtt != -1)
						zombie.coords.flip(chosenAtt);
				}
			}

			++this._iterCounter;
		}

		return (BitSet) this._bestFound.clone();
	}
}
