package weka.classifiers.rules.lad.featureselection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.featureselection.setcovering.SetCovering;
import weka.core.Option;
import weka.core.Utils;

/**
 * Class GreedySetCover
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class GreedySetCover extends FeatureSelection {

	/* SERIAL ID */
	private static final long serialVersionUID = 1L;

	/** Constructor */
	public GreedySetCover() {
		super();
	}

	/** Method to find selected attributes */
	public void fit(final BinaryData bInsts) {
		this.mSelectedAttArray = new ArrayList<Integer>();

		// Checking separation level
		if (mSeparationLevel <= 0) {
			for (int i = 0; i < bInsts.numAttributes(); i++)
				this.mSelectedAttArray.add(i);

			return;
		}

		int numSets = bInsts.numAttributes();

		// Auxiliary variable
		ArrayList<Integer> array = new ArrayList<Integer>(numSets);

		// Set Covering instance (Chvátal's Heuristic)
		SetCovering sc = new SetCovering(numSets);

		// Building Set Covering problem
		for (int i = 0; i < bInsts.numInstances(); i++) {
			BinaryInstance A = bInsts.getInstance(i);
			
			for (int j = i + 1; i < bInsts.numInstances(); j++) {
				BinaryInstance B = bInsts.getInstance(j);
				
				// It must be different classes labels
				if (A.instanceClass() == B.instanceClass())
					continue;
				
				// Reseting array
				array.clear();
				
				// Populating array of cutpoints indices
				for (int c = 0; c < numSets; c++)
					if (!A.compareAtt(c, B))
						array.add(c);

				sc.addElement(array);
			}
		}

		// Solving Set Covering problem
		sc.solve(mSeparationLevel);

		// Sorting selected attributes
		Collections.sort(mSelectedAttArray = sc.getSolution());
	}

	/*
	 * -------------------------------------------------------------------- DYSPLAY
	 * INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
	 */

	@Override
	public String globalInfo() {
		return "Implements Chvátal's greedy algorithm for solving the Set Cover instance arising during standard LAD binarization.";
	}

	@Override
	public String separationLevelTipText() {
		return "Separation requirement for feature selection. Every pair of observations must "
				+ "be separated by a minimum number of binary features.";
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

		return (String[]) options.toArray(new String[options.size()]);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// Looking for Feature Selection Level Option
		String featureSelSeparationLevelOption = Utils.getOption("fsl", options);

		if (featureSelSeparationLevelOption.length() != 0) {
			setSeparationLevel(Integer.parseInt(featureSelSeparationLevelOption));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		Vector<Option> newVector = new Vector<Option>();

		newVector.addElement(new Option("\tFeature selection separation level. How many times each\n"
				+ "\tpair of observations should be separated by cutpoints.\n"
				+ "\t(Default = 0, i.e., no separation required)", "fsl", 1, "-fsl <separation_level>"));

		return newVector.elements();
	}
}