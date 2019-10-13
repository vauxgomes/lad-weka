package weka.classifiers.rules.lad.ruleManager;

import java.io.Serializable;
import java.util.ArrayList;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.lad.core.NumericalRule;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class RuleManager
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class RuleManager implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 1956931334238019791L;

	/* Variables */
	private ArrayList<NumericalRule> mPositiveRules;
	private ArrayList<NumericalRule> mNegativeRules;

	/* Auxiliary */
	private Integer largerClass = null;
	private final static double IncDecValue = 0.01;

	private String mPSRepresentation;
	private ArrayList<NumericalRule> mActiveRules;

	private String pClass;
	private String nClass;

	private Instances mReferencialdata;

	/** Constructor */
	public RuleManager(Instances data) {
		mReferencialdata = data;
		this.setClassNames(data);
		
		mPositiveRules = new ArrayList<NumericalRule>();
		mNegativeRules = new ArrayList<NumericalRule>();
		mActiveRules = new ArrayList<NumericalRule>();
	}

	/** Method to add a rule to the rule manager */
	public void addRule(NumericalRule nRule) {
		// TODO Check if the rule is really necessary.
		if (nRule.isPositive() && !mPositiveRules.contains(nRule))
			mPositiveRules.add(nRule);
		else if (nRule.isNegative() && !mNegativeRules.contains(nRule))
			mNegativeRules.add(nRule);
	}

	/** Method to adjust the weights of the rules */
	public void adjustRulesWeight(Instances trainingSet) {
		double normalProportion = 1 / (double) mPositiveRules.size();
		for (NumericalRule r : this.mPositiveRules)
			r.setWeight(normalProportion);

		normalProportion = 1 / (double) mNegativeRules.size();
		for (NumericalRule r : this.mNegativeRules)
			r.setWeight(normalProportion);

		// Adjusting active rules weights
		double dist[] = null;
		for (Instance inst : trainingSet) {
			dist = distributionForInstance(inst);

			if (inst.classValue() == 0 && (dist[1] > dist[0])) {
				for (NumericalRule r : this.mActiveRules)
					if (r.isPositive())
						r.increaseWeight(IncDecValue);
					else
						r.decreaseWeight(IncDecValue);
			} else if (inst.classValue() == 1 && (dist[0] > dist[1])) {
				for (NumericalRule r : this.mActiveRules)
					if (r.isNegative())
						r.increaseWeight(IncDecValue);
					else
						r.decreaseWeight(IncDecValue);
			}
		}

		ArrayList<NumericalRule> pR = new ArrayList<NumericalRule>();
		ArrayList<NumericalRule> nR = new ArrayList<NumericalRule>();

		// Normalizing weights
		normalProportion = 0.0;
		for (NumericalRule r : this.mPositiveRules)
			normalProportion += r.getWeight();

		for (NumericalRule r : this.mPositiveRules)
			if (r.getWeight() != 0) {
				r.setWeight(r.getWeight() / normalProportion);
				pR.add(r);
			}

		normalProportion = 0.0;
		for (NumericalRule r : this.mNegativeRules)
			normalProportion += r.getWeight();

		for (NumericalRule r : this.mNegativeRules)
			if (r.getWeight() != 0) {
				r.setWeight(r.getWeight() / normalProportion);
				nR.add(r);
			}

		this.mPositiveRules.clear();
		this.mNegativeRules.clear();

		this.mPositiveRules = pR;
		this.mNegativeRules = nR;

		// Obtaining larger class information
		if (trainingSet.attributeStats(trainingSet.classIndex()).nominalCounts[0] > trainingSet
				.attributeStats(trainingSet.classIndex()).nominalCounts[1])
			largerClass = 0;
		else
			largerClass = 1;
	}

	/** Method similar to {@link AbstractClassifier}.distributionForInstance */
	public double[] distributionForInstance(Instance inst) {
		int P = 0, N = 1;
		mActiveRules.clear();

		double distribution[] = new double[2];
		distribution[P] = 0.0;
		distribution[N] = 0.0;

		for (NumericalRule r : this.mPositiveRules)
			if (r.isCovering(inst)) {
				distribution[P] += r.getWeight();
				mActiveRules.add(r);
			}

		for (NumericalRule r : this.mNegativeRules)
			if (r.isCovering(inst)) {
				distribution[N] += r.getWeight();
				mActiveRules.add(r);
			}

		if (distribution[P] == distribution[N] && largerClass != null) {
			distribution[P] = distribution[N] = 0;
			distribution[largerClass] = 1.0;
		}

		return distribution;
	}

	/**
	 * Method similar to {@link AbstractClassifier}.distributionForInstance. It
	 * also saves the PSR (Pattern Space Representation) for the instance it is
	 * testing.
	 */
	public double[] distributionForInstancePSR(Instance inst) {
		int P = 0, N = 1;
		mActiveRules.clear();

		double distribution[] = new double[2];
		distribution[P] = 0.0;
		distribution[N] = 0.0;

		mPSRepresentation = "";
		for (NumericalRule r : this.mPositiveRules)
			if (r.isCovering(inst)) {
				distribution[P] += r.getWeight();
				mActiveRules.add(r);
				mPSRepresentation += "1,";
			} else {
				mPSRepresentation += "0,";
			}

		mPSRepresentation += "";
		for (NumericalRule r : this.mNegativeRules)
			if (r.isCovering(inst)) {
				distribution[N] += r.getWeight();
				mActiveRules.add(r);
				mPSRepresentation += "1,";
			} else {
				mPSRepresentation += "0,";
			}

		if (distribution[P] == distribution[N] && largerClass != null) {
			distribution[P] = distribution[N] = 0;
			distribution[largerClass] = 1.0;
			mPSRepresentation += " " + largerClass;
		} else if (distribution[P] != distribution[N]) {
			mPSRepresentation += " "
					+ (distribution[P] > distribution[N] ? P : N);
		}

		return distribution;
	}

	/** SET of attributes names */
	private void setClassNames(final Instances data) {
		this.pClass = "\"" + data.attribute(data.classIndex()).value(0) + "\"";
		this.nClass = "\"" + data.attribute(data.classIndex()).value(1) + "\"";
	}

	/** GET of positive rules */
	public ArrayList<NumericalRule> getPositiveRules() {
		return mPositiveRules;
	}

	/** GET of number of positive rules */
	public int numPositiveRules() {
		return this.mPositiveRules.size();
	}

	/** GET of negative rules */
	public ArrayList<NumericalRule> getNegativeRules() {
		return mNegativeRules;
	}

	/** GET of number of negative rules */
	public int numNegativeRules() {
		return this.mNegativeRules.size();
	}

	/** GET last representation from distributionForInstance method */
	public String getLatestRepresentation() {
		return mPSRepresentation;
	}

	/** Prints RuleManager to PSF format */
	public String toPSF() {
		String s = " @ Class " + pClass + " patterns:\n\n";
		for (NumericalRule r : mPositiveRules)
			s += r.toString(mReferencialdata) + "\n";

		s += "\n # Total: " + mPositiveRules.size() + " patterns\n";

		s += "\n @ Class " + nClass + " patterns:\n\n";
		for (NumericalRule r : mNegativeRules)
			s += r.toString(mReferencialdata) + "\n";

		s += "\n # Total: " + mNegativeRules.size() + " patterns";

		return s;
	}

	@Override
	public String toString() {
		String s = " # Patterns class " + pClass + ": " + mPositiveRules.size()
				+ "\n";

		return s + "\n # Patterns class " + nClass + ": "
				+ mNegativeRules.size();
	}
}
