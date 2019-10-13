package weka.classifiers.rules.lad.binarization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Class Binarization
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class Binarization {

	/* Parameters */
	private double mTolerance = 0.0;

	/* Variables */
	private HashMap<Double, TransitionElement> mFeatures;

	/** Main Constructor */
	public Binarization(double cutpointTolerance) {
		this.mTolerance = cutpointTolerance;
		this.mFeatures = new HashMap<Double, TransitionElement>();
	}

	/** Method for mapping the instances to cutpoints */
	public Cutpoints findCutpoints(Instances insts) throws Exception {
		// Exception
		if (mTolerance < 0)
			throw new Exception("Binarization: Cutpoint tolerance "
					+ "must be greater than or equal to 0.");

		Cutpoints cutpoints = new Cutpoints();
		for (int att = 0; att < insts.numAttributes(); att++) {
			if (insts.classIndex() != att) {
				if (insts.attribute(att).isNumeric()) {
					// Map transitions for current feature
					mapTransitions(insts, att);

					// Sorting transitions
					ArrayList<Double> array = mapTransitions(insts, att);
					Collections.sort(array);

					// Looking for cutpoints
					int o1 = 0;
					int next = 1;

					while (array.size() > next) {
						int o2 = next++;
						double variation = Math.abs(array.get(o1)
								- array.get(o2));

						if (variation > mTolerance)
							if (hasTransition(array.get(o1), array.get(o2))) {
								double media = array.get(o1) + variation / 2.0;

								// Adding cutpoint to our list
								cutpoints.addCutpoint(att, media);
							}

						o1 = o2;
					}
				} else if (insts.attribute(att).isNominal()) {
					// Nominal Cutpoint
					cutpoints
							.addCutpoint(att, insts.attribute(att).numValues());
				}
			}
		}

		if (cutpoints.numCutpoints() == 0)
			throw new Exception("Binarizarion: No cutpoint was found.");

		mFeatures.clear();
		return cutpoints;
	}

	/** Generates a map of features values ignoring missing values */
	private ArrayList<Double> mapTransitions(Instances insts, int att) {

		this.mFeatures.clear();
		int numInstances = insts.numInstances();

		for (int index = 0; index < numInstances; index++) {
			Instance inst = insts.instance(index);

			// Preventing that missing values be used in the map
			if (!inst.isMissing(att)) {

				double valor = inst.value(att);
				TransitionElement trans = this.mFeatures.get(valor);

				if (trans == null) {
					trans = new TransitionElement();
					this.mFeatures.put(valor, trans);
				}

				// Updating transition element
				trans.set(inst.classValue());
			}
		}

		// Returning array with keys
		return new ArrayList<Double>(this.mFeatures.keySet());
	}

	/** Checks if there is a transition point between those two values */
	private boolean hasTransition(double c1, double c2) {
		if (this.mFeatures.get(c1).positive)
			if (this.mFeatures.get(c2).negative)
				return true;

		if (this.mFeatures.get(c1).negative)
			if (this.mFeatures.get(c2).positive)
				return true;

		return false;
	}

	/** GET of cutpoints tolerance */
	public double getTolerance() {
		return mTolerance;
	}

	/** SET of cutpoints tolerance */
	public void setTolerancia(double mTolerancia) {
		this.mTolerance = mTolerancia;
	}

	/**
	 * Class TransitionElement
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class TransitionElement implements Serializable {

		/* SERIAL ID */
		private static final long serialVersionUID = -4184858552280919519L;

		/* Variables */
		boolean positive = false;
		boolean negative = false;

		public void set(double aClass) {
			if (aClass == 0.0)
				this.positive = true;
			else
				this.negative = true;
		}
	}
}