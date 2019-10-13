package weka.classifiers.rules.lad.ruleGenerators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.core.BinaryRule;

/**
 * Class RuleGenerator
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public abstract class RuleGenerator implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = -5645213683696957902L;

	/* Parameters */
	protected double mMinimumPurity = 0.95;

	/* Variables */
	protected BinaryData mTrainingData;
	private ArrayList<BinaryRule> mBinaryRules;

	/** Main Constructor */
	public RuleGenerator() {
		this.mBinaryRules = new ArrayList<BinaryRule>();
	}

	/** Rule generator abstract method */
	public abstract void generateRules(BinaryData trainingData);

	/** Checks if is there any setting out of boundary of mistakenly setted */
	public void checkForExceptions() throws Exception {
		if (mMinimumPurity <= 0.5 || mMinimumPurity > 1.0)
			throw new Exception("Rule Generator: Minimum Purity "
					+ "must be greater than 0.5 and lest then or equal to 1.0.");
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
	protected void addRule(BinaryRule bRule) {
		if (bRule.getPurity() >= mMinimumPurity)
			this.mBinaryRules.add(bRule);
	}

	/*
	 * --------------------------------------------------------------------
	 * OPTIONS METHODS
	 * --------------------------------------------------------------------
	 */

	/** Global informations about the Rule Generator */
	public abstract String globalInfo();

	/** GET of the Rule Generator options */
	public abstract String[] getOptions();

	/** SET of the Rule Generator options */
	public abstract void setOptions(String[] options) throws Exception;

	/** List of descriptions about the options **/
	@SuppressWarnings("rawtypes")
	public abstract Enumeration listOptions();

	/**
	 * Class Node
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	protected class Node {

		/* Variables */
		private double mDiscrepancy;
		private BinaryData mInstances;

		/** Main Constructor */
		public Node() {
			mInstances = new BinaryData();
		}

		/** Smart Constructor */
		public Node(Node node) {
			this(node.mInstances);
		}

		/** Designated Constructor */
		public Node(BinaryData bInsts) {
			this.mInstances = new BinaryData(bInsts);
		}

		/** Adds a new instance */
		public void addInstance(BinaryInstance bInst) {
			mInstances.addInstance(bInst);
		}

		/** Removes an instance */
		public void removeInstance(BinaryInstance bInst) {
			mInstances.removeInstance(bInst);
		}

		/** GET of discrepancy */
		public double getDiscrepancy() {
			return mDiscrepancy;
		}

		/** SET of discrepancy */
		public void setDiscrepancy(double mDiscrepancy) {
			this.mDiscrepancy = mDiscrepancy;
		}

		/** GET of positive instances */
		public ArrayList<BinaryInstance> getPositiveInstances() {
			return mInstances.getPositiveInstances();
		}

		/** GET of negative instances */
		public ArrayList<BinaryInstance> getNegativeInstances() {
			return mInstances.getNegativeInstances();
		}

		/** GET of number of positive instances */
		public Integer numPositiveInstances() {
			return mInstances.numPositiveInstances();
		}

		/** GET of number of negative instances */
		public Integer numNegativeInstances() {
			return mInstances.numNegativeInstances();
		}

		/** GET of purity */
		public double getPurity(boolean aClasse) {
			int np = mInstances.numPositiveInstances();
			int nn = mInstances.numNegativeInstances();

			if (aClasse)
				return np / (double) (np + nn);
			else
				return nn / (double) (np + nn);
		}
	}
}
