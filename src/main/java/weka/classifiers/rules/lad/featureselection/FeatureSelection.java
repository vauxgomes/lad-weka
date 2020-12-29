package weka.classifiers.rules.lad.featureselection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.featureselection.setcovering.SetCovering;

/**
 * Class FeatureSelection
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @version 1.0
 * @date 2/14/13
 */
public abstract class FeatureSelection implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 6374730014984529250L;

	/* Parameters */
	protected int mSeparationLevel = 1;

	/* Variables */
	protected ArrayList<Integer> mSelectedAttArray;
	
	public SetCovering sc;

	/** Constructor */
	public FeatureSelection() {
		this.mSelectedAttArray = null;
	}

	/**
	 * Abstract method to find selected attributes
	 * 
	 * @param bInst
	 * @param separationLevel
	 */
	public abstract void fit(final BinaryData data);

	/** Checks if is there any setting out of boundary of mistakenly setted */
	public void checkForExceptions() throws Exception {
		if (mSeparationLevel < 0)
			throw new Exception("Feature Selection: Separation Level "
					+ "must be greater than or equal to 0.");
	}

	/** GET Separation Level */
	public int getSeparationLevel() {
		return mSeparationLevel;
	}

	/** SET Separation Level */
	public void setSeparationLevel(int level) {
		mSeparationLevel = level;
	}

	/** GET selected attributes */
	public ArrayList<Integer> getSelectedAttArray() {
		Collections.sort(mSelectedAttArray);
		
		return mSelectedAttArray;
	}

	/*
	 * --------------------------------------------------------------------
	 * DISPLAY INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
	 */

	/** Feature Selection Tip Text */
	public abstract String separationLevelTipText();

	/*
	 * --------------------------------------------------------------------
	 * OPTIONS METHODS
	 * --------------------------------------------------------------------
	 */

	/** Global informations about the Rule Generator */
	public abstract String globalInfo();

	/** GET of the Rule Generator options */
	public abstract String[] getOptions();

	/** SET of the Feature Selection options */
	public abstract void setOptions(String[] options) throws Exception;

	/** List of descriptions about the options **/
	@SuppressWarnings("rawtypes")
	public abstract Enumeration listOptions();
}
