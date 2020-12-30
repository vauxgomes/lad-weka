package weka.classifiers.rules.lad.rulegeneration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryRule;
import weka.core.Option;

/**
 * Class RuleGenerator
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public abstract class RuleGenerator implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = -5645213683696957902L;

	/* Parameters */
	protected double mMinimumPurity = 0.85;

	/* Variables */
	protected BinaryData mData;
	private ArrayList<BinaryRule> mBinaryRules;

	/** Main Constructor */
	public RuleGenerator() {
		this.mBinaryRules = new ArrayList<BinaryRule>();
	}

	/** Rule generator abstract method */
	public abstract void fit(BinaryData trainingData);

	/** Checks if is there any setting out of boundary of mistakenly set */
	public void checkForExceptions() throws Exception {
		if (mMinimumPurity <= 0.5 || mMinimumPurity > 1.0)
			throw new Exception(
					"Rule Generator: Minimum Purity must be greater than 0.5 and lest then or equal to 1.0.");
	}

	/** SET of the Minimum Purity */
	public void setMinimumPurity(double purity) {
		mMinimumPurity = purity;
	}

	/** GET of the Array of Rules */
	public ArrayList<BinaryRule> getRules() {
		return mBinaryRules;
	}

	/** Adds a new rule */
	protected void addRule(BinaryRule rule) {
		if (rule.getPurity() >= mMinimumPurity)
			this.mBinaryRules.add(rule);
	}

	protected boolean contains(BinaryRule rule) {
		return this.mBinaryRules.contains(rule);
	}

	/*
	 * ----------------------------------------------------------------------
	 * DISPLAY INFORMATIONS & TIP TEXTs
	 * ----------------------------------------------------------------------
	 */

	/** Global informations about the Rule Generator */
	public abstract String globalInfo();

	/*
	 * ----------------------------------------------------------------------
	 * OPTIONS METHODS
	 * ----------------------------------------------------------------------
	 */

	/** GET of the Rule Generator options */
	public String[] getOptions() {
		return new String[0];
	}

	/** SET of the Rule Generator options */
	public void setOptions(String[] options) throws Exception {
	}

	@SuppressWarnings("rawtypes")
	/** List of descriptions about the options **/
	public Enumeration listOptions() {
		return new Vector<Option>(0).elements();
	}
}
