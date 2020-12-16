package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Binary Instance class.
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class BinaryData implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 2488234257854645289L;

	/* Variables */
	private ArrayList<BinaryInstance> mInstances;
	private ArrayList<Attribute> mAttributes;
	private HashMap<Double, Integer> mCounts;
	private CutpointSet mCutpoints;

	/** Main Constructor */
	public BinaryData(Instances data, CutpointSet cutpoints) {
		this(data.numClasses());

		for (Instance instance : data)
			add(new BinaryInstance(instance, cutpoints));

		for (int i = 0; i < data.numAttributes(); i++)
			mAttributes.add(data.attribute(i));
		
		mCutpoints = cutpoints;
	}

	/** Basic Constructor */
	public BinaryData(int numLabels) {
		mInstances = new ArrayList<BinaryInstance>();
		mCounts = new HashMap<Double, Integer>();
		mAttributes = new ArrayList<Attribute>();

		for (int i = 0; i < numLabels; i++)
			mCounts.put((double) i, 0);
	}

	/** Smart Constructor */
	public BinaryData(BinaryData data) {
		mInstances = new ArrayList<BinaryInstance>(data.mInstances);
		mAttributes = new ArrayList<Attribute>(data.mAttributes);
		mCounts = new HashMap<Double, Integer>(data.mCounts);
		mCutpoints = data.mCutpoints;
	}

	/** Adds an instance */
	public void add(BinaryInstance instance) {
		if (this.mInstances.add(instance))
			mCounts.put(instance.instanceClass(), mCounts.get(instance.instanceClass()) + 1);
	}

	/** Adds all instances from a BinaryData */
	public void add(BinaryData data) {
		for (BinaryInstance instance : data.mInstances)
			add(instance);
	}

	/** Removes an instance */
	public void remove(BinaryInstance instance) {
		if (mInstances.remove(instance))
			mCounts.put(instance.instanceClass(), mCounts.get(instance.instanceClass()) - 1);
	}
	
	public void remove(BinaryData data) {
		for (BinaryInstance instance : data.mInstances)
			remove(instance);		
	}

	/** GET of a specific instance. */
	public BinaryInstance getInstance(int index) {
		return mInstances.get(index);
	}

	/** GET of a instances. */
	public ArrayList<BinaryInstance> getInstances() {
		return mInstances;
	}

	/** GET of number of instances */
	public int numInstances() {
		return mInstances.size();
	}

	/** GET of number of attributes */
	public int numAttributes() {
		return mAttributes.size();
	}
	
	/** GET of number of cut points */
	public int numCutpoints() {
		return mCutpoints.numCutpoints();
	}

	/** GET of number of class labels */
	public int numClassLabels() {
		return mCounts.size();
	}

	/** GET of an attribute */
	public Attribute getAttribute(int index) {
		return mAttributes.get(mCutpoints.attAt(index));
	}

	/** Stats: Purity */
	public double getPurity(double label) {
		return mInstances.size() == 0 ? 0 : (mCounts.get(label) / (double) mInstances.size());
	}

	/** Stats: Merged purity */
	public double getMergedPurity(BinaryData data, double label) {
		return (mInstances.size() + data.mInstances.size()) == 0 ? 0
				: (mCounts.get(label) + data.mCounts.get(label)) / (double) (mInstances.size() + data.mInstances.size());
	}

	/** Stats: Coverage */
	public int getCoverage(double label) {
		return mCounts.get(label);
	}
	
	public double getMergedCoverage(BinaryData data, double label) {
		return getCoverage(label) + data.getCoverage(label);
	}
	
	@Override
	public String toString() {
		String s = String.format("Covered: %d\n", mInstances.size());
		for (int i = 0; i < mCounts.size(); i++) {
			s+= String.format("[%d] (%f, %d)\n", i, getPurity(i), getCoverage(i));
		}
		
		return s; 
	}
}