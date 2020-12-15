package weka.classifiers.rules.lad.featureselection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.featureselection.setcovering.SetCovering;
import weka.core.Option;
import weka.core.Utils;

/**
 * Class IteratedSampling. Based on the random subspaces technique.
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class IteratedSampling extends FeatureSelection {

	/** SERIAL ID */
	private static final long serialVersionUID = 1557597598188635948L;

	/* Parameters */
	private int mNumRounds = 100;
	private double mSampleSize = 1.0;
	private int mSolutionSize = 30;
	private long mSeed = 2;

	/* Variables */
	private Random mRnd = new Random(mSeed);

	@Override
	public void fit(BinaryData data) {
		mSelectedAttArray = new ArrayList<Integer>();

		// Checking separation level
		if (mSeparationLevel <= 0) {
			for (int i = 0; i < data.numAttributes(); i++)
				mSelectedAttArray.add(i);

			return;
		}

		// Boundaries
		int numAttributes = (int) (data.numAttributes() * mSampleSize);
		int numInstances = (int) (data.numInstances() * mSampleSize);

		//
		HashMap<Integer, Persistence> persistences = new HashMap<Integer, Persistence>(numAttributes);

		// Indexes
		ArrayList<Integer> attributes = new ArrayList<Integer>();
		for (int i = 0; i < numAttributes; i++) {
			attributes.add(i);
		}

		ArrayList<Integer> instances = new ArrayList<Integer>();
		for (int i = 0; i < numInstances; i++) {
			instances.add(i);
		}

		//
		ArrayList<Integer> array = new ArrayList<Integer>(numAttributes);

		// Set Covering instance
		SetCovering sc = null;

		// Rounds
		for (int r = 0; r < mNumRounds; r++) {
			Collections.shuffle(attributes, mRnd);
			Collections.shuffle(instances, mRnd);

			// Instantiating SC
			sc = new SetCovering(numAttributes);

			// Building a Sampled Set Covering problem
			for (Integer i : instances.subList(0, numInstances)) {
				BinaryInstance A = data.getInstance(i);

				for (Integer j : instances.subList(1, numInstances)) {
					BinaryInstance B = data.getInstance(j);

					// It must be different classes labels
					if (A.instanceClass() == B.instanceClass())
						continue;

					// Reseting array
					array.clear();

					// Populating array of cutpoints indices
					for (Integer c : attributes.subList(0, numAttributes))
						if (!A.compareAtt(c, B))
							array.add(c);

					sc.addElement(array);
				}
			}

			// Solving Set Covering problem
			sc.solve(mSeparationLevel);

			// Updating sampled cutpoints
			for (Integer c : attributes.subList(0, numAttributes)) {
				Persistence p = persistences.get(c);

				if (p == null) {
					p = new Persistence(c);
					persistences.put(p.id, p);
				}

				p.numSamples++;
			}

			// Updating selected cutpoints
			Integer[] cutpoints = attributes.toArray(new Integer[0]);

			for (Integer s : sc.getSolution()) {
				Persistence p = persistences.get(cutpoints[s]);
				p.numSelections++;
			}
		}

		// Sorting cutpoints by their quality
		ArrayList<Persistence> persistences_ = new ArrayList<IteratedSampling.Persistence>(persistences.values());
		Collections.sort(persistences_);

		// Selecting k cutpoints,
		int k = (mSolutionSize > data.numAttributes()) ? data.numAttributes() : mSolutionSize;

		for (Persistence p : persistences_.subList(0, k))
			mSelectedAttArray.add(p.id);
	}

	@Override
	public void checkForExceptions() throws Exception {
		super.checkForExceptions();

		if (mNumRounds < 10)
			throw new Exception("Feature Selection: Number of samples " + "must be at least 10.");
		else if (mSampleSize < 0.05)
			throw new Exception("Feature Selection: Sample size " + "must be at least 0.05.");
		else if (mSolutionSize < 1)
			throw new Exception("Feature Selection: Solution size " + "must be at least 1.");
	}

	/*
	 * ------------------------------------------------------------------------ SETs
	 * & GETs
	 * ------------------------------------------------------------------------
	 */

	/** GET of mNumRounds */
	public int getNumRounds() {
		return mNumRounds;
	}

	/** SET of mNumRounds */
	public void setNumRounds(int numRounds) {
		if (numRounds >= 1)
			this.mNumRounds = numRounds;
	}

	/** GET of mSampleSize */
	public double getSampleSize() {
		return mSampleSize;
	}

	/** SET of mSampleSize to set */
	public void setSampleSize(double size) {
		if (size >= 0.05 && size <= 1.0)
			this.mSampleSize = size;
	}

	/** GET of Solution Size */
	public int getSolutionSize() {
		return mSolutionSize;
	}

	/** SET of Solution Size */
	public void setSolutionSize(int solutionSize) {
		if (solutionSize >= 1)
			this.mSolutionSize = solutionSize;
	}

	/** GET of seed */
	public long getRandomSeed() {
		return mSeed;
	}

	/** SET of seed */
	public void setRandomSeed(long seed) {
		this.mSeed = seed;
	}

	/*
	 * -------------------------------------------------------------------- OTHERS
	 * DYSPLAY INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
	 */

	@Override
	public String globalInfo() {
		return "Iterative procedure for selecting a set of cutpoints that separates a "
				+ "large number of pairs of observations. The technique samples the "
				+ "(possibly very large) cutpoint selection problem, producing a set cover "
				+ "problem of smaller size (both in terms of constraints and variables). "
				+ "A greedy solution is found for the subproblem and the process is iterated. "
				+ "The cutpoints that are most frequently selected to take part in the greedy "
				+ "solutions are returned.";
	}

	/** Number of Samples Tip Text */
	public String numSamplesTipText() {
		return "This is the number of times the cutpoint selection problem will be sampled.";
	}

	/** Samples Size Tip Text */
	public String sampleSizeTipText() {
		return "This specifies the fraction of the cutpoint selection problem that is sampled "
				+ "in order to create each subproblem. The same percentage factor is used to sample "
				+ "a subset of the constraints (pairs of observations) of the cutpoint selection problem. "
				+ "Half of the observations selected are positive, half are negative, and all pairs are "
				+ "used as constraints. If \"sampleCutpoints\" is set to true, a subset of the problem's "
				+ "variables (cutpoints) is also sampled. In either case, sampling is done uniformly and "
				+ "without replacement.";
	}

	/** Solution Size Tip Text */
	public String solutionSizeTipText() {
		return "Number of cutpoints returned by the procedure. The best ranked cutpoints "
				+ "(with respect to frequency of participation in solutions) are returned.";
	}

	/** Seed Tip Text */
	public String randomSeedTipText() {
		return "Value used as seed to the pseudo-random number generator employed during the " + "sampling process.";
	}

	/** Mode Flag Tip Text */
	public String sampleCutpointsTipText() {
		return "Flag indicating whether or not cutpoints are sampled (in addition to observations) "
				+ "when forming each subproblem. If set to false, all cutpoints separating the sampled "
				+ "observations are included in the subproblem.";
	}

	@Override
	public String separationLevelTipText() {
		return "Separation requirement for cutpoint selection. Every pair of observations in "
				+ "a subproblem must be separated by a minimum number of cutpoints.";
	}

	/*
	 * -------------------------------------------------------------------- OPTIONS
	 * METHODS --------------------------------------------------------------------
	 */

	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();

		options.add("-fsl");
		options.add("" + getSeparationLevel());

		options.add("-nrd");
		options.add("" + getNumRounds());

		options.add("-fss");
		options.add("" + getRandomSeed());

		options.add("-sps");
		options.add("" + getSampleSize());

		options.add("-sls");
		options.add("" + getSolutionSize());

		return (String[]) options.toArray(new String[options.size()]);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// Looking for Feature Selection Level Option
		String featureSelSeparationLevelOption = Utils.getOption("fsl", options);

		if (featureSelSeparationLevelOption.length() != 0) {
			setSeparationLevel(Integer.parseInt(featureSelSeparationLevelOption));
		}

		String numSamplesOption = Utils.getOption("nsp", options);
		if (numSamplesOption.length() != 0) {
			setNumRounds(Integer.parseInt(numSamplesOption));
		}

		String randomSeedOption = Utils.getOption("fss", options);
		if (numSamplesOption.length() != 0) {
			setRandomSeed(Integer.parseInt(randomSeedOption));
		}

		String sampleSizeOption = Utils.getOption("sps", options);
		if (numSamplesOption.length() != 0) {
			setSampleSize(Double.parseDouble(sampleSizeOption));
		}

		String solutionSizeOption = Utils.getOption("sls", options);
		if (numSamplesOption.length() != 0) {
			setSolutionSize(Integer.parseInt(solutionSizeOption));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		Vector<Option> newVector = new Vector<Option>();

		newVector.addElement(new Option("\tFeature selection separation level. How many times each\n"
				+ "\tpair of observations should be separated by cutpoints.\n"
				+ "\t(Default = 0, i.e., no separation required)\n", "fsl", 1, "-fsl <separation_level>"));

		newVector.addElement(
				new Option("\tThis is the number of times the cutpoint selection problem\n" + "\twill be sampled.\n",
						"nsp", 1, "-nsp <number_of_samples>"));

		newVector.addElement(new Option("\tValue used as seed to the pseudo-random number\n"
				+ "\tgenerator employed during the sampling process.\n", "fss", 1, "-fss <seed>"));

		newVector.addElement(new Option("\tThis specifies the fraction of the cutpoint selection problem \n"
				+ "\tthat is sampled in order to create each subproblem. The \n"
				+ "\tsame percentage factor is used to sample a subset of the \n"
				+ "\tconstraints (pairs of observations) of the cutpoint selection \n"
				+ "\tproblem. Half of the observations selected are positive, \n"
				+ "\thalf are negative, and all pairs are used as constraints. \n"
				+ "\tIf \"sampleCutpoints\" is set to true, a subset of the problem's \n"
				+ "\tvariables (cutpoints) is also sampled. In either case, \n"
				+ "\tsampling is done uniformly and without replacement.\n", "sps", 1, "-sps <sample_size>"));

		newVector.addElement(new Option(
				"\tNumber of cutpoints returned by the procedure.\n" + "\tThe best ranked cutpoints (with respect to\n"
						+ "\tfrequency of participation in solutions) are returned.\n",
				"sls", 1, "-sls <seed>"));

		newVector.addElement(new Option("\tFlag indicating whether or not cutpoints are sampled (in addition \n"
				+ "\tto observations) when forming each subproblem. If set to false, \n"
				+ "\tall cutpoints separating the sampled observations are included \n" + "\tin the subproblem.\n",
				"sps", 1, "-scm <boolean>"));

		return newVector.elements();
	}

	/**
	 * Class Persistence
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class Persistence implements Comparable<Persistence>, Serializable {

		/** SERIAL ID */
		private static final long serialVersionUID = -6177074127558835505L;

		final int id;
		int numSamples;
		int numSelections;

		public Persistence(int id) {
			this(id, 0, 0);
		}

		public Persistence(int id, int numSamples, int numSelections) {
			this.id = id;
			this.numSamples = numSamples;
			this.numSelections = numSelections;
		}

		public double quality() {
			if (numSamples == 0)
				return 0;

			return (double) numSelections / numSamples;
		}

		@Override
		public int compareTo(Persistence p) {
			if (quality() < p.quality())
				return 1;
			else if (quality() > p.quality())
				return -1;
			else {
				if (numSamples < p.numSamples)
					return 1;
				else if (numSamples > p.numSamples)
					return -1;

				return 0;
			}
		}

		@Override
		public String toString() {
			return "" + quality();
		}
	}
}