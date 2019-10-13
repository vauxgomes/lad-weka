package weka.classifiers.rules.lad.ruleGenerators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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
 * @version 1.0
 */
public class RandomRuleGenerator extends RuleGenerator {

	/** SERIAL ID */
	private static final long serialVersionUID = 1448802467208122870L;

	/* Parameters */
	private int mNumRules = 250;
	private int mRandomSeed = 1;
	private int mNumRandomFeatures = 10;
	private double mMinRelativeCoverageOwnClass = 0.01;

	/* Variables */
	private Random mRandomObject;
	private int[] mMinterm;
	private int NUM_POSITIVE_INSTANCES;
	private int NUM_NEGATIVE_INSTANCES;

	/* Auxiliary Variables */
	private final int NUMERIC = -1;

	/** Constructor */
	public RandomRuleGenerator() {
		this.mRandomObject = new Random();
		NUM_NEGATIVE_INSTANCES = NUM_POSITIVE_INSTANCES = 0;
	}

	@Override
	public void generateRules(BinaryData trainingData) {
		mTrainingData = trainingData;
		mMinterm = generateMinterm();

		NUM_POSITIVE_INSTANCES = mTrainingData.numPositiveInstances();
		NUM_NEGATIVE_INSTANCES = mTrainingData.numNegativeInstances();

		this.mRandomObject.setSeed(this.mRandomSeed);
		for (int i = 0; i < this.mNumRules; i++) {
			expand(true);
			expand(false);
		}
	}

	/** Method for building decision rules */
	private void expand(boolean aClass) {
		// Filling covered Node
		Node covereds = new Node(mTrainingData);

		// Literais
		ArrayList<Literal> rule = new ArrayList<Literal>();

		int length = mTrainingData.numAttributes();
		int[] literalIndexes = new int[length];

		// Filling literais
		for (int i = 0; i < this.mTrainingData.numAttributes(); i++)
			literalIndexes[i] = i;

		// Minimum purity for this round
		double purity = mMinimumPurity;

		while (length > 0) {
			Literal bestLiteral = null;
			Node bestLiteralCovering = null;
			int bestLiteralIndex = -1;

			// Getting some literais indexes randomly
			Collection<Integer> randomIndexes = randomIndexes(length,
					mNumRandomFeatures);

			// Testing the chosen literais
			for (Integer index : randomIndexes) {
				Literal literal = randomLiteral(literalIndexes[index]);
				Node literalCovering = getLiteralCovering(covereds, literal);

				// Setting the bestLiteral
				if (bestLiteral == null) {
					bestLiteral = literal;
					bestLiteralCovering = literalCovering;
					bestLiteralIndex = index;

				} else if (literalCovering.getPurity(aClass) > bestLiteralCovering
						.getPurity(aClass)) {
					bestLiteral = literal;
					bestLiteralCovering = literalCovering;
					bestLiteralIndex = index;

				} else if (literalCovering.getPurity(aClass) == bestLiteralCovering
						.getPurity(aClass)) {
					if (aClass) {
						if (literalCovering.numPositiveInstances() > bestLiteralCovering
								.numPositiveInstances()) {
							bestLiteral = literal;
							bestLiteralCovering = literalCovering;
							bestLiteralIndex = index;
						}

					} else if (literalCovering.numNegativeInstances() > bestLiteralCovering
							.numNegativeInstances()) {
						bestLiteral = literal;
						bestLiteralCovering = literalCovering;
						bestLiteralIndex = index;
					}
				}
			}

			if (bestLiteral == null) {
				break;
			} else {
				rule.add(bestLiteral);
				covereds = bestLiteralCovering;

				// Teleportation
				literalIndexes[bestLiteralIndex] = literalIndexes[--length];

				// If my coverage is bigger than
				if (coverage(covereds, aClass) >= mMinRelativeCoverageOwnClass) {
					if (covereds.getPurity(aClass) >= purity) {
						// Updating round purity
						purity = covereds.getPurity(aClass);
						purity = purity + (1 - purity) / 10;

						// Adding rule
						addRule(new BinaryRule(rule, aClass,
								covereds.getPurity(aClass)));

						if (purity == 1.0)
							break;
					}
				} else
					break;
			}
		}
	}

	/** Generates minterm */
	private int[] generateMinterm() {
		BinaryInstance bInst = mTrainingData.getInstance(0);
		int[] minterm = new int[bInst.numAttributes()];

		for (int i = 0; i < minterm.length; i++)
			if (bInst.isNumeric(i))
				minterm[i] = NUMERIC;
			else
				// Number of nominal option
				minterm[i] = (int) bInst.cutpointValue(i);

		return minterm;
	}

	/** Makes the split from a Literal */
	private Node getLiteralCovering(Node node, Literal literal) {

		int index = literal.getAtt();
		Node covered = new Node();

		if (literal.isNumeric()) {
			boolean sign = literal.getSignal();

			for (BinaryInstance obs : node.getPositiveInstances()) {
				if (!obs.isMissingAttribute(index)
						&& obs.getBinAt(index) == sign)
					covered.addInstance(obs);
			}

			for (BinaryInstance obs : node.getNegativeInstances()) {
				if (!obs.isMissingAttribute(index)
						&& obs.getBinAt(index) == sign)
					covered.addInstance(obs);
			}
		} else {
			double value = literal.getValue();
			for (BinaryInstance obs : node.getPositiveInstances()) {
				if (!obs.isMissingAttribute(index)
						&& obs.getValeuAt(index) == value)
					covered.addInstance(obs);
			}

			for (BinaryInstance obs : node.getNegativeInstances()) {
				if (!obs.isMissingAttribute(index)
						&& obs.getValeuAt(index) == value)
					covered.addInstance(obs);
			}
		}

		return covered;
	}

	/** Returns n random indexes */
	private Collection<Integer> randomIndexes(int length, int quantity) {
		if (length > 0) {
			if (quantity > length)
				quantity = length;
		} else
			return new ArrayList<Integer>(0);

		int counter = 0;
		HashMap<Integer, Integer> sortedIndexes = new HashMap<Integer, Integer>();

		while (counter < quantity) {
			// we want to make possible a literal and its doppelganger to be
			// both selected
			int index = this.mRandomObject.nextInt(length);

			if (!sortedIndexes.containsKey(index)) {
				sortedIndexes.put(index, index);
				counter++;
			}
		}

		return sortedIndexes.values();
	}

	/** Generates a Random Literal from the minterm */
	private Literal randomLiteral(int index) {
		if (mMinterm[index] == NUMERIC)
			return new Literal(index, mRandomObject.nextBoolean());
		else
			return new Literal(index, mRandomObject.nextInt(mMinterm[index]));
	}

	/** Calculates coverage based on a node and a class */
	private double coverage(Node n, boolean aClass) {
		if (aClass)
			return (double) n.numPositiveInstances()
					/ (double) NUM_POSITIVE_INSTANCES;

		return (double) n.numNegativeInstances()
				/ (double) NUM_NEGATIVE_INSTANCES;
	}

	@Override
	public void checkForExceptions() throws Exception {
		super.checkForExceptions();

		if (mNumRules < 25)
			throw new Exception("Rule Generator: Number of Rules "
					+ "must be greater than or equal to 25.");
		else if (mNumRandomFeatures < 1)
			throw new Exception("Rule Generator: Number of Random Features "
					+ "must be greater than or equal to 2.");
		else if (mMinRelativeCoverageOwnClass < 0.01)
			throw new Exception("Rule Generator: Minimum Relative Coverage "
					+ "must be at least 0.01.");
	}

	/*
	 * ------------------------------------------------------------------------
	 * DYSPLAY SETs & GETs
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
	 * --------------------------------------------------------------------
	 * DYSPLAY INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
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
	 * --------------------------------------------------------------------
	 * OPTIONS METHODS
	 * --------------------------------------------------------------------
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
			setMinRelativeCoverageOwnClass(Double
					.parseDouble(minRelativeCoverage));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		Vector<Option> newVector = new Vector<Option>(2);

		newVector.addElement(new Option(
				"\tNumber of Random Features. Number of features sampled at each\n"
						+ "\titeration during rule construction.\n", "nft", 1,
				"-nft <rand_features>"));

		newVector.addElement(new Option(
				"\tNumber of Rules. Number of rules that the algorithm attempts\n"
						+ "\tto generate.\n", "nrd", 1, "-nrd <number_rules>"));

		newVector
				.addElement(new Option(
						"\tRandom Seed. Seed used by internal random number generator.\n",
						"rgs", 1, "-rgs <seed>"));

		newVector
				.addElement(new Option(
						"\tMinimum Relative Coverage. How much coverage of its\n"
								+ "\town class a rule must have in order to be accepted.\n",
						"mc", 1, "-mc <min_rel_cover>"));

		return newVector.elements();
	}
}