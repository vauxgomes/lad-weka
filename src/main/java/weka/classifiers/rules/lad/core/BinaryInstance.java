package weka.classifiers.rules.lad.core;

import java.io.Serializable;

import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.core.Instance;

/**
 * Class BinaryInstance
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class BinaryInstance implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 977143245642898382L;

	/* Variables */
	private Instance mInstance;
	private CutpointSet sCutpoints;

	/** Main Constructor */
	public BinaryInstance(Instance instance, CutpointSet cutpoints) {
		this.mInstance = instance;
		this.sCutpoints = cutpoints;
	}

	/** GET of binary attribute */
	public boolean getBinAt(int index) {
		return sCutpoints.valueAt(index) <= mInstance.value(sCutpoints
				.attAt(index));
	}

	/** GET of binary attribute */
	public double getValueAt(int index) {
		return mInstance.value(sCutpoints.attAt(index));
	}

	/** Compares two attributes */
	public boolean compareAtt(int index, BinaryInstance bInst) {
		if (isMissingAttribute(index) || bInst.isMissingAttribute(index))
			return false;

		if (isNumeric(index))
			return getBinAt(index) == bInst.getBinAt(index);
		else
			return getValueAt(index) == bInst.getValueAt(index);
	}

	/** Checks if a specific attribute is numeric */
	public boolean isNumeric(int index) {
		return mInstance.attribute((sCutpoints.attAt(index))).isNumeric();
	}

	/** Checks if a specific attribute is missing */
	public boolean isMissingAttribute(int index) {
		return mInstance.isMissing(sCutpoints.attAt(index));
	}

	/** GET of the binary instance class */
	public double instanceClass() {
		return mInstance.classValue();
	}

	/** GET of number of attributes */
	public int numAttributes() {
		return sCutpoints.numCutpoints();
	}

	/** GET of the value of a cutpoint */
	public double cutpointValue(int index) {
		return sCutpoints.valueAt(index);
	}

	@Override
	public String toString() {
		return mInstance.toString();
	}
}