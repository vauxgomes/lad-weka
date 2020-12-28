package weka.classifiers.rules.lad.rulegeneration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.classifiers.rules.lad.core.BinaryRule;
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
	public RuleManager(Instances data, ArrayList<BinaryRule> rules, CutpointSet cutpoints) {
		mRules = new ArrayList<NumericalRule>();
		mCounts = new HashMap<Integer, Integer>();

		for (BinaryRule rule : rules) {
			NumericalRule n = new NumericalRule(rule, cutpoints);

			if (!mRules.contains(n)) {
				mRules.add(n);
				mCounts.put(n.getLabel(), mCounts.containsKey(n.getLabel()) ? mCounts.get(n.getLabel()) + 1 : 1);
			}
		}

		mLabels = new HashMap<Integer, String>();
		for (int i = 0; i < data.classAttribute().numValues(); i++)
			mLabels.put(i, data.classAttribute().value(i));

		adjustRulesWeight(data);
	}

	/** GET of Rules */
	public ArrayList<NumericalRule> getRules() {
		return mRules;
	}

	/** Adjust the weights of the current rules */
	private void adjustRulesWeight(Instances data) {
		// Initial weights
		for (NumericalRule r : mRules)
			r.setWeight(1.0 / mCounts.get(r.getLabel()));

		//
		double distribution[] = null;

		for (Instance instance : data) {
			distribution = distributionForInstance(instance);

			int argmax = 0;
			for (int i = 1; i < distribution.length; i++) {
				if (distribution[i] > distribution[argmax])
					argmax = i;
			}

			// In case it predicted right or didn't have a predict
			if (instance.classValue() == argmax || distribution[argmax] == 0)
				continue;

			// Increasing the weights of rules that were right and
			// decreasing the weights of rules that were wrong
			for (NumericalRule r : mRules)
				if (r.isCovering(instance))
					if (r.getLabel() == instance.classValue())
						r.increaseWeight(STEP);
					else
						r.decreaseWeight(STEP);
		}

		HashMap<Integer, Double> weights = new HashMap<Integer, Double>(mLabels.size());
		ArrayList<NumericalRule> weightless = new ArrayList<NumericalRule>();

		for (Integer l : mLabels.keySet()) { // Resetting weights and counts
			weights.put(l, 0.0);
			mCounts.put(l, 0);
		}

		for (NumericalRule rule : mRules) {
			if (rule.getWeight() != 0) {
				weights.put(rule.getLabel(), weights.get(rule.getLabel()) + rule.getWeight());
				mCounts.put(rule.getLabel(), mCounts.get(rule.getLabel()) + 1);
			} else {
				weightless.add(rule);
			}
		}

		mRules.removeAll(weightless);

		// Normalizing weights
		for (NumericalRule r : mRules)
			r.setWeight(r.getWeight() / weights.get(r.getLabel()));

		// Obtaining larger class information
		mLargerClass = 0;
		for (int i = 1; i < data.classAttribute().numValues(); i++) {
			if (data.attributeStats(data.classIndex()).nominalCounts[i] > data
					.attributeStats(data.classIndex()).nominalCounts[mLargerClass])
				mLargerClass = i;
		}
	}

	/** Method similar to {@link AbstractClassifier}.distributionForInstance */
	public double[] distributionForInstance(Instance instance) {
		//
		double distribution[] = new double[mLabels.size()];

		for (NumericalRule r : mRules)
			if (r.isCovering(instance))
				distribution[r.getLabel()] += r.getWeight();

		// Test whether all values are equal
		double value = distribution[0];
		for (int i = 1; i < distribution.length; i++)
			if (distribution[i] != value)
				return distribution;

		//
		if (mLargerClass != null) {
			for (int i = 0; i < distribution.length; i++)
				distribution[i] = 0;

			distribution[mLargerClass] = 1.0;
		}

		return distribution;
	}

	@Override
	public String toString() {
		String s = "";
		boolean first = true;

		for (Integer i : mLabels.keySet()) {
			s += String.format("%s # Patterns class \"%s\": %d\n\n", first ? "" : "\n", mLabels.get(i), mCounts.get(i));

			for (NumericalRule r : mRules)
				if (r.getLabel() == i)
					s += String.format("%s\n", r.toString());

			first = false;
		}

		return s;
	}

	public Integer numRules(int i) {
		return mCounts.get(i);
	}
}
