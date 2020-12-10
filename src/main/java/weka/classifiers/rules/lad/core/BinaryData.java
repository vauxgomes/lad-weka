package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;

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
 * @version 1.0
 */
public class BinaryData implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 2488234257854645289L;

	/* Variables */
	private ArrayList<BinaryInstance> mInstances;
	private int mNumAttributes;

	/** Main Constructor */
	public BinaryData(Instances data, Cutpoints cutpoints) {
		this();

		for (Instance instance : data)
			addInstance(new BinaryInstance(instance, cutpoints));

		this.mNumAttributes = cutpoints.numCutpoints();
	}

	/** Basic Constructor */
	public BinaryData() {
		mInstances = new ArrayList<BinaryInstance>();
	}

	/** Smart Constructor */
	public BinaryData(BinaryData data) {
		this();
		this.mInstances = new ArrayList<BinaryInstance>(data.mInstances);
	}

	/** Adds a new instance */
	public void addInstance(BinaryInstance instance) {
		this.mInstances.add(instance);
	}

	/** Removes an instance */
	public void removeInstance(BinaryInstance instance) {
		mInstances.remove(instance);
	}

	/** GET of a specific instance. */
	public BinaryInstance getInstance(int index) {
		return mInstances.get(index);
	}

	/** GET of number of instances */
	public int numInstances() {
		return mInstances.size();
	}
	
	/** GET of number of attributes */
	public int numAttributes() {
		return mNumAttributes;
	}
}