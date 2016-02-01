package algorithms;

import java.util.BitSet;

public interface SpecifierInterface {
	public double calcSubsetMerit(BitSet selection) throws Exception;
	public int numAttributes();
	public int numDimensions();
}
