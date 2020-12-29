package weka.classifiers.rules.lad.rulegeneration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.core.BinaryRule;
import weka.classifiers.rules.lad.core.Literal;
import weka.core.Option;

/**
 * Class MaxRuleGenerator (Max pattern rule generator).
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class MaxRuleGenerator extends RuleGenerator {

	/** SERIAL ID */
	private static final long serialVersionUID = 8175941261292479958L;

	@Override
	public void fit(BinaryData trainingData) {
		mData = trainingData;

		for (int i = 0; i < mData.numInstances(); i++) {
			expand(mData.getInstance(i));
		}
	}

	/** Method for building decision rules */
	private void expand(BinaryInstance instance) {
		//
		double label = instance.instanceClass();

		// Rule
		ArrayList<Literal> rule = new ArrayList<Literal>();

		//
		for (int i = 0; i < instance.numAttributes(); i++) {
			if (!instance.isMissingAttribute(i))
				if (instance.isNumeric(i))
					rule.add(new Literal(i, instance.getBinAt(i)));
				else
					rule.add(new Literal(i, instance.getValueAt(i)));
		}

		// Coverage
		BinaryData covered = getRuleCoverage(mData, rule, label);
		BinaryData uncovered = new BinaryData(mData);

		for (BinaryInstance i : covered.getInstances())
			uncovered.remove(i);

		// Enabling Safety Mode
		boolean safetyMode = covered.getPurity(label) < mMinimumPurity; // Basically always true
		double purity = mMinimumPurity;

		// Maximization
		while (rule.size() > 0) {

			Literal bestLiteral = null;
			BinaryData bestCoverage = null;
			double bestDiscrepancy = 0;

			// Rule copy
			ArrayList<Literal> literals = new ArrayList<Literal>(rule);

			for (Literal literal : literals) {
				// Shortening rule
				rule.remove(literal);

				//
				BinaryData literalCoverage = getRuleCoverage(uncovered, rule, label);

				// Testing
				if ((covered.getMergedPurity(literalCoverage, label) >= purity)
						|| ((safetyMode) && (Math.abs(covered.getMergedPurity(literalCoverage, label)
								- covered.getMergedPurity(bestCoverage, label)) < 1.0E4))) {

//					// Try to increase purity of the current rule
//					if (covered.getMergedPurity(literalCoverage, label) >= purity) {
//						purity = Math.min(1, purity + (1 - purity) / 10);
//					}

					// Calculating discrepancy
					double literalDiscrepancy = discrepancy(uncovered, rule, label);

					// Decision
					if (bestCoverage == null
							|| covered.getMergedCoverage(literalCoverage, label) > covered
									.getMergedCoverage(bestCoverage, label)
							|| (covered.getMergedCoverage(literalCoverage, label) == covered
									.getMergedCoverage(bestCoverage, label) && literalDiscrepancy < bestDiscrepancy)) {

//					if (bestCoverage == null
//							|| covered.getMergedPurity(literalCoverage, label) > covered.getMergedPurity(bestCoverage,
//									label)
//							|| (covered.getMergedPurity(literalCoverage, label) == covered.getMergedPurity(bestCoverage,
//									label) && literalDiscrepancy < bestDiscrepancy)) {

						bestLiteral = literal;
						bestCoverage = literalCoverage;
						bestDiscrepancy = literalDiscrepancy;

					}
				}

				// Reinsert the "Literal"
				rule.add(literal);
			}

			// Break Conditions
			if (bestLiteral == null) {
				break;
			} else {
				rule.remove(bestLiteral);
				covered.add(bestCoverage);
				
				for (BinaryInstance inst : bestCoverage.getInstances())
					uncovered.remove(inst);

			}
		}

		if (rule.size() > 0) {
			addRule(new BinaryRule(rule, label, covered.getPurity(label)));
		}
	}

	/** Makes the split using a list of literals */
	private BinaryData getRuleCoverage(BinaryData data, ArrayList<Literal> rule, double label) {
		//
		BinaryData covered = new BinaryData(data.numClassLabels());

		for (BinaryInstance instance : data.getInstances()) {
			boolean cover = true;

			for (Literal literal : rule) {
				if (instance.isMissingAttribute(literal.getAtt())) {
					if (instance.instanceClass() == label) {
						cover = false;
						break;
					}
				} else if (!literal.isIn(instance)) {
					cover = false;
					break;
				}
			}

			if (cover)
				covered.add(instance);
		}

		return covered;
	}

	/** Calculates discrepancy */
	private double discrepancy(BinaryData data, ArrayList<Literal> rule, double label) {
		double sameClassDistance = 0.0;
		double otherClassDistance = 0.0;

		for (BinaryInstance instance : data.getInstances()) {
			for (Literal literal : rule)
				if (instance.isMissingAttribute(literal.getAtt())) {
					if (instance.instanceClass() == label)
						sameClassDistance += 1;

				} else if (!literal.isIn(instance)) {
					if (instance.instanceClass() == label)
						sameClassDistance += 1;
					else
						otherClassDistance += 1;
				}

		}

		return (sameClassDistance / (1 + data.getCoverage(label)))
				/ (otherClassDistance / (1 + data.numInstances() - data.getCoverage(label)));
	}

	@Override
	public String toString() {
		return getRules().toString();
	}

	/*
	 * ----------------------------------------------------------------------
	 * DYSPLAY INFORMATIONS & TIP TEXTs
	 * ----------------------------------------------------------------------
	 */

	/** information of the Algorithm */
	public String globalInfo() {
		return "Implements the Maximized Prime Patterns heuristic described in the "
				+ "\"Maximum Patterns in Datasets\" paper. It generates one pattern (rule) "
				+ "per observation, while attempting to: (i) maximize the coverage of other "
				+ "observations belonging to the same class, and (ii) preventing the "
				+ "coverage of too many observations from outside that class. The amount of "
				+ "\"outside\" coverage allowed is controlled by the minimum purity parameter "
				+ "(from the main LAD classifier).";
	}

	/*
	 * ----------------------------------------------------------------------
	 * OPTIONS METHODS
	 * ----------------------------------------------------------------------
	 */

	@Override
	public String[] getOptions() {
		return new String[0];
	}

	@Override
	public void setOptions(String[] options) throws Exception {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		return new Vector<Option>(0).elements();
	}
}