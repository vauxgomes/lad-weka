package weka.classifiers.rules.lad.rulegeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.core.BinaryRule;
import weka.classifiers.rules.lad.core.Literal;
import weka.core.Option;
import weka.core.Utils;

/**
 * Class RandomRuleGenerator
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class RandomRuleGenerator extends RuleGenerator {

	/** SERIAL ID */
	private static final long serialVersionUID = 1448802467208122870L;

	/** FINALS */
	private static final int NUMERIC = -1;

	/* Parameters */
	private int mNumRules = 250;
	private int mRandomSeed = 1;
	private int mNumRandomFeatures = 10;
	private double mMinRelativeCoverageOwnClass = 0.01;
	private int[] mBinaryAttributes; // TRUE if Binary Attribute / FALSE if nominal

	/* Variables */
	private Random mRandomObject;

	/** Constructor */
	public RandomRuleGenerator() {
		this.mRandomObject = new Random();
	}

	@Override
	public void fit(BinaryData data) {
		mData = data;
		mRandomObject.setSeed(mRandomSeed);

		mBinaryAttributes = new int[data.numCutpoints()];
		int i = 0;

		for (i = 0; i < mBinaryAttributes.length; i++) {
			if (data.getAttribute(i).isNumeric())
				mBinaryAttributes[i] = NUMERIC;
			else
				mBinaryAttributes[i] = data.getAttribute(i).numValues();
		}

		for (i = 0; i < this.mNumRules; i++)
			for (int j = 0; j < data.numClassLabels(); j++) {
				expand(j);
			}
	}

	/** Method for building decision rules */
	private void expand(final double label) {
		// Indexes
		ArrayList<Integer> indexes = new ArrayList<Integer>(mBinaryAttributes.length);
		for (int i = 0; i < mBinaryAttributes.length; i++)
			indexes.add(i);

		Collections.shuffle(indexes, mRandomObject);

		// Rule
		ArrayList<Literal> rule = new ArrayList<Literal>();
		ArrayList<Literal> literals = new ArrayList<Literal>();

		// Random Rule
		for (Integer i : indexes.subList(0, Math.min(indexes.size(), mNumRandomFeatures))) {
			if (mBinaryAttributes[i] == NUMERIC)
				literals.add(new Literal(i, mRandomObject.nextBoolean()));
			else
				literals.add(new Literal(i, mRandomObject.nextInt(mBinaryAttributes[i])));
		}

		// Coverage: Considering full coverage of the empty rule
		BinaryData covered = new BinaryData(mData);

		// Minimum purity for this round
		double purity = mMinimumPurity;

		/*
		 * Main loop: Here we are looking for the "best" literals to be added to the
		 * rule
		 */
		while (literals.size() > 0) {

			Literal bestLiteral = null;
			BinaryData bestCoverage = null;

			// Testing the chosen literals
			for (Literal literal : literals) {
				//
				BinaryData literalCoverage = getLiteralCoverage(covered, literal, label);

				// Setting the bestLiteral
				if (bestLiteral == null || literalCoverage.getPurity(label) > bestCoverage.getPurity(label)
						|| (literalCoverage.getPurity(label) == bestCoverage.getPurity(label)
								&& literalCoverage.getCoverage(label) > bestCoverage.getCoverage(label))) {
					bestLiteral = literal;
					bestCoverage = literalCoverage;
				}
			}

			if (bestLiteral == null) {
				break;
			}

			rule.add(bestLiteral);
			covered = bestCoverage;
			literals.remove(bestLiteral);

			// If my coverage is bigger than
			if (covered.getCoverage(label) >= mMinRelativeCoverageOwnClass) {
				if (covered.getPurity(label) >= purity) {
					// purity = covered.getPurity(label);

					// Adding rule
					addRule(new BinaryRule(rule, label, purity));

					// Updating round purity
					purity = purity + (1 - purity) / 10;

					if (purity == 1.0)
						break;
				}
			} else
				break;
		}
	}

	/**
	 * Makes the split from a Literal
	 * 
	 * Covered either if:
	 * <ol>
	 * <li>Attribute is missing and instance's class is different from the rule's
	 * class</li>
	 * <li>Literal value is the same of the instance</li>
	 * </ol>
	 */
	private BinaryData getLiteralCoverage(BinaryData data, Literal literal, double label) {
		//
		BinaryData covered = new BinaryData(data.numClassLabels());

		for (BinaryInstance instance : data.getInstances()) {
			if (instance.isMissingAttribute(literal.getAtt())) {
				if (instance.instanceClass() != label)
					covered.add(instance);
			} else if (literal.isIn(instance)) {
				covered.add(instance);
			}
		}

		return covered;
	}

	@Override
	public void checkForExceptions() throws Exception {
		super.checkForExceptions();

		if (mNumRules < 25)
			throw new Exception("Rule Generator: Number of Rules " + "must be greater than or equal to 25.");
		else if (mNumRandomFeatures < 1)
			throw new Exception("Rule Generator: Number of Random Features " + "must be greater than or equal to 2.");
		else if (mMinRelativeCoverageOwnClass < 0.01)
			throw new Exception("Rule Generator: Minimum Relative Coverage " + "must be at least 0.01.");
	}

	/*
	 * ------------------------------------------------------------------------
	 * DISPLAY SETs & GETs
	 * ------------------------------------------------------------------------
	 */

	/** GET of numRules to Display */
	public int getNumRules() {
		return mNumRules;
	}

	/** SET of numRules to Display */
	public void setNumRules(int mNumRules) {
		this.mNumRules = mNumRules;
	}

	/** GET of numFeatures to sort to Display */
	public int getNumRandomFeatures() {
		return mNumRandomFeatures;
	}

	/** SET of numFeatures to sort to Display */
	public void setNumRandomFeatures(int mNumRandomFeatures) {
		this.mNumRandomFeatures = mNumRandomFeatures;
	}

	/** GET of Seed to Display */
	public int getRandomSeed() {
		return mRandomSeed;
	}

	/** SET of Seed to Display */
	public void setRandomSeed(int mSeed) {
		this.mRandomSeed = mSeed;
	}

	/** GET of Minimum Relative Coverage of Own Class */
	public double getMinRelativeCoverageOwnClass() {
		return mMinRelativeCoverageOwnClass;
	}

	/** SET of Minimum Relative Coverage of Own Class */
	public void setMinRelativeCoverageOwnClass(double rel) {
		this.mMinRelativeCoverageOwnClass = rel;
	}

	/*
	 * ----------------------------------------------------------------------
	 * DISPLAY INFORMATIONS & TIP TEXTs
	 * ----------------------------------------------------------------------
	 */

	/** information of the Algorithm */
	public String globalInfo() {
		return "Random rule generation algorithm";
	}

	/** numRules Tip Text */
	public String numRulesTipText() {
		return "Number of rules that the algorithm attempts to generate.";
	}

	/** numFeaturesSelected Tip Text */
	public String numRandomFeaturesTipText() {
		return "Number of features sampled at each iteration during rule construction.";
	}

	/** numRules Tip Text */
	public String randomSeedTipText() {
		return "Seed used by internal random number generator.";
	}

	/** numRules Tip Text */
	public String minRelativeCoverageOwnClassTipText() {
		return "How much coverage of its own class a rule must have in order to be accepted.";
	}

	/*
	 * ----------------------------------------------------------------------
	 * OPTIONS METHODS
	 * ----------------------------------------------------------------------
	 */

	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();

		options.add("-nft");
		options.add("" + getNumRandomFeatures());
		options.add("-nrd");
		options.add("" + getNumRules());
		options.add("-rgs");
		options.add("" + getRandomSeed());
		options.add("-mc");
		options.add("" + getMinRelativeCoverageOwnClass());

		return (String[]) options.toArray(new String[options.size()]);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// Looking for Number Random Features
		String numRandomFeatures = Utils.getOption("nft", options);
		if (numRandomFeatures.length() != 0) {
			setNumRandomFeatures(Integer.parseInt(numRandomFeatures));
		}

		// Looking for Number of Rules
		String numRules = Utils.getOption("nrd", options);
		if (numRules.length() != 0) {
			setNumRules(Integer.parseInt(numRules));
		}

		// Looking for Random Seed
		String randomSeed = Utils.getOption("rgs", options);
		if (randomSeed.length() != 0) {
			setRandomSeed(Integer.parseInt(randomSeed));
		}

		// Looking for Minimum Relative Coverage
		String minRelativeCoverage = Utils.getOption("mc", options);
		if (randomSeed.length() != 0) {
			setMinRelativeCoverageOwnClass(Double.parseDouble(minRelativeCoverage));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		Vector<Option> newVector = new Vector<Option>(2);

		newVector.addElement(new Option("\tNumber of Random Features. Number of features sampled at each\n"
				+ "\titeration during rule construction.\n", "nft", 1, "-nft <rand_features>"));

		newVector.addElement(
				new Option("\tNumber of Rules. Number of rules that the algorithm attempts\n" + "\tto generate.\n",
						"nrd", 1, "-nrd <number_rules>"));

		newVector.addElement(
				new Option("\tRandom Seed. Seed used by internal random number generator.\n", "rgs", 1, "-rgs <seed>"));

		newVector.addElement(new Option("\tMinimum Relative Coverage. How much coverage of its\n"
				+ "\town class a rule must have in order to be accepted.\n", "mc", 1, "-mc <min_rel_cover>"));

		return newVector.elements();
	}
}