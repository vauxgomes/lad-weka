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
	private ArrayList<BinaryInstance> mbIArrayPositive;
	private ArrayList<BinaryInstance> mbIArrayNegative;
	private Cutpoints sCutpoints;

	/** Main Constructor */
	public BinaryData(Instances data, Cutpoints cutpoints) {
		this();

		for (Instance instance : data)
			addInstance(new BinaryInstance(instance, cutpoints));

		this.sCutpoints = cutpoints;
	}

	/** Basic Constructor */
	public BinaryData() {
		mbIArrayPositive = new ArrayList<BinaryInstance>();
		mbIArrayNegative = new ArrayList<BinaryInstance>();
	}

	/** Smart Constructor */
	public BinaryData(BinaryData bInsts) {
		this();

		for (BinaryInstance bInst : bInsts.mbIArrayPositive)
			addInstance(bInst);
		for (BinaryInstance bInst : bInsts.mbIArrayNegative)
			addInstance(bInst);
	}

	/** Adds a new instance */
	public void addInstance(BinaryInstance bInst) {
		if (bInst == null)
			return;

		if (bInst.instanceClass()) {
			mbIArrayPositive.add(bInst);
		} else {
			mbIArrayNegative.add(bInst);
		}
	}

	/** Removes an instance */
	public void removeInstance(BinaryInstance bInst) {
		if (bInst.instanceClass()) {
			mbIArrayPositive.remove(bInst);
		} else {
			mbIArrayNegative.remove(bInst);
		}
	}

	/**
	 * GET of a specific instance. The first n indexes return a positive
	 * instance, the m indexes left return a negative instance.
	 */
	public BinaryInstance getInstance(int index) {
		if (index < mbIArrayPositive.size())
			return mbIArrayPositive.get(index);
		else
			return mbIArrayNegative.get(index - mbIArrayPositive.size());
	}

	/** GET of a positive instance */
	public BinaryInstance getPositiveInstance(int index) {
		return mbIArrayPositive.get(index);
	}

	/** GET of a negative instance */
	public BinaryInstance getNegativeInstance(int index) {
		return mbIArrayNegative.get(index);
	}

	/** GET of all positive instances */
	public final ArrayList<BinaryInstance> getPositiveInstances() {
		return mbIArrayPositive;
	}

	/** GET of all negative instances */
	public final ArrayList<BinaryInstance> getNegativeInstances() {
		return mbIArrayNegative;
	}

	/** GET of number of instances */
	public int numInstances() {
		return numPositiveInstances() + numNegativeInstances();
	}

	/** GET of number of positive instances */
	public int numPositiveInstances() {
		return mbIArrayPositive.size();
	}

	/** GET of number of negative instances */
	public int numNegativeInstances() {
		return mbIArrayNegative.size();
	}

	/** GET of number of binary attributes */
	public int numAttributes() {
		return sCutpoints.numCutpoints();
	}

//	public boolean isNumeric(int index) {
//		if (mbIArrayPositive.size() > 0)
//			return mbIArrayPositive.get(0).isNumeric(index);
//		else if (mbIArrayNegative.size() > 0)
//			return mbIArrayNegative.get(0).isNumeric(index);
//		else
//			System.err.println("Both Negative and Positive BInsts are empty!");
//		
//		return false;
//	}

	@Override
	public String toString() {
		String s = "";
		for (BinaryInstance b : mbIArrayPositive)
			s += b + "\n";

		for (BinaryInstance b : mbIArrayNegative)
			s += b + "\n";

		return s;
	}
}