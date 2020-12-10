package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.rules.lad.binarization.Cutpoints;
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
	private HashMap<Double, Integer> mLabelsCount;

	private int mNumAttributes;
	private boolean mSafetyFlag;

	/** Main Constructor */
	public BinaryData(Instances data, Cutpoints cutpoints) {
		this();

		for (Instance instance : data)
			addInstance(new BinaryInstance(instance, cutpoints));

		for (int i = 0; i < data.numClasses(); i++)
			this.mLabelsCount.put((double) i, 0);

		this.mNumAttributes = cutpoints.numCutpoints();
		this.mSafetyFlag = false;
	}

	/** Basic Constructor */
	public BinaryData() {
		this.mInstances = new ArrayList<BinaryInstance>();
		this.mLabelsCount = new HashMap<Double, Integer>();

		this.mNumAttributes = 0;
		this.mSafetyFlag = true;
	}

	/** Smart Constructor */
	public BinaryData(BinaryData data) {
		this();

		this.mInstances = new ArrayList<BinaryInstance>(data.mInstances);
		this.mLabelsCount = new HashMap<Double, Integer>(data.mLabelsCount);

		this.mNumAttributes = data.mNumAttributes;
		this.mSafetyFlag = true;
	}

	/** Adds a new instance */
	public void addInstance(BinaryInstance instance) {
		this.mInstances.add(instance);

		if (this.mSafetyFlag) {
			double label = instance.instanceClass();
			int count = (this.mLabelsCount.containsKey(label) ? this.mLabelsCount.get(label) : 0) + 1;

			this.mLabelsCount.put(label, count);
		}
	}

	/** Removes an instance */
	public void removeInstance(BinaryInstance instance) {
		mInstances.remove(instance);

		if (this.mSafetyFlag) {
			double label = instance.instanceClass();
			int count = (this.mLabelsCount.containsKey(label) ? this.mLabelsCount.get(label) - 1 : 0);

			this.mLabelsCount.put(label, count);
		}
	}

	/** GET of a specific instance. */
	public BinaryInstance getInstance(int index) {
		return mInstances.get(index);
	}

	/** GET of number of instances */
	public int numInstances() {
		return mInstances.size();
	}
	
	/** GET of number of by class label */
	public int numInstances(double label) {
		return this.mLabelsCount.containsKey(label) ? this.mLabelsCount.get(label) : 0;
	}

	/** GET of number of attributes */
	public int numAttributes() {
		return mNumAttributes;
	}
}