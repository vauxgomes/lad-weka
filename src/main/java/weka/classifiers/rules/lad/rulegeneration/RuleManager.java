package weka.classifiers.rules.lad.rulegeneration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
 * @version 1.1
 */
public class RuleManager implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 1956931334238019791L;

	/* Variables */
	private ArrayList<NumericalRule> mRules;
	private HashMap<Integer, String> mLabels;
	private HashMap<Integer, Integer> mCounts;

	/* Auxiliary */
	private Integer mLargerClass = null;

	/* Final variables */
	private final static double STEP = 0.01;

	/** Constructor */
	public RuleManager(Instances data) {
		mLabels = new HashMap<Integer, String>();
		mRules = new ArrayList<NumericalRule>();
		mCounts = new HashMap<Integer, Integer>();

		for (int i = 0; i < data.classAttribute().numValues(); i++) {
			mLabels.put(i, data.classAttribute().value(i));
			mCounts.put(i, 0);
		}
	}

	/** Adds a numerical rule */
	public void add(NumericalRule rule) {
		// Check if the rule is really necessary.
		if (!mRules.contains(rule)) {
			mRules.add(rule);
			mCounts.put(rule.getLabel(), mCounts.containsKey(rule.getLabel()) ? mCounts.get(rule.getLabel()) + 1 : 1);
		}
	}

	/** Adjust the weights of the current rules */
	public void adjustRulesWeight(Instances data) {

		// Obtaining larger class information
		mLargerClass = 0;
		for (int i = 1; i < data.classAttribute().numValues(); i++) {
			if (data.attributeStats(data.classIndex()).nominalCounts[i] > data
					.attributeStats(data.classIndex()).nominalCounts[mLargerClass])
				mLargerClass = i;
		}

		// Initial weights
		for (NumericalRule r : mRules)
			r.setWeight(1.0 / mCounts.get(r.getLabel()));

		HashMap<Integer, Double> weights = new HashMap<Integer, Double>(mLabels.size());
		for (Integer i : mLabels.keySet())
			weights.put(i, 1.0);

		// Adjusting active rules weights
		double distribution[] = null;

		for (Instance instance : data) {
			distribution = distributionForInstance(instance);

			int argmax = 0;
			for (int i = 1; i < distribution.length; i++) {
				if (distribution[i] > distribution[argmax])
					argmax = i;
			}

			for (NumericalRule r : mRules) {
				if (r.isCovering(instance)) {
					if (r.getLabel() == argmax) {
						r.increaseWeight(STEP);
						weights.put(r.getLabel(), weights.get(r.getLabel()) + STEP);
					} else {
						r.decreaseWeight(STEP);
						weights.put(r.getLabel(), weights.get(r.getLabel()) - STEP);
					}
				}
			}
		}

		for (NumericalRule r : mRules)
			r.setWeight(r.getWeight() / weights.get(r.getLabel()));
	}

	/** Method similar to {@link AbstractClassifier}.distributionForInstance */
	public double[] distributionForInstance(Instance instance) {
		//
		double distribution[] = new double[mLabels.size()];

		for (NumericalRule r : mRules)
			if (r.isCovering(instance))
				distribution[r.getLabel()] += r.getWeight();

		//
		double value = distribution[0];
		for (int i = 0; i < distribution.length; i++)
			if (distribution[i] != value)
				return distribution;

		//
		if (mLargerClass != null)
			distribution[mLargerClass] = 1.0;

		return distribution;
	}

	@Override
	public String toString() {
		String s = "";

		for (Integer i : mLabels.keySet()) {
			s += String.format(" # Pattern class %s: %d\n", mLabels.get(i), mCounts.get(i));

			for (NumericalRule r : mRules)
				if (r.getLabel() == i)
					s += String.format("%s\n", r.toString());

			s += "\n\n";
		}

		return s;
	}
}
