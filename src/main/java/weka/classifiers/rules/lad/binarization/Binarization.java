package weka.classifiers.rules.lad.binarization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.compress.utils.Sets;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class Binarization
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class Binarization {

	/* Parameters */
	private double mTolerance = 0.0;

	/** Main Constructor */
	public Binarization(double cutpointTolerance) {
		this.mTolerance = cutpointTolerance;
	}

	/** Method for mapping the instances to cutpoints */
	public Cutpoints findCutpoints2(Instances data) throws Exception {
		//
		Cutpoints cutpoints = new Cutpoints();

		// For each attribute, sort values and find transitions
		for (int attidx = 0; attidx < data.numAttributes(); attidx++) {

			Attribute att = data.attribute(attidx);
			if (att == data.classAttribute())
				continue;

			// Numeric attributes
			if (att.isNumeric()) {

				// Sorting
				data.sort(att);

				// Auxiliary variables
				ArrayList<ValueSet> transitions = new ArrayList<Binarization.ValueSet>();
				ValueSet t = null;

				int instidx = 0;
				while (instidx < data.numInstances()) {
					Instance inst = data.instance(instidx);
					double value = inst.value(attidx);

					t = new ValueSet(value);
					transitions.add(t);

					while (instidx < data.numInstances() && t.value == value) {
						t.labels.add(data.instance(instidx++).classValue());

						try {
							value = data.instance(instidx).value(attidx);
						} catch (Exception e) {
						}
					}
				}

				// Search
				ValueSet t1 = transitions.get(0);
				for (int transidx = 1; transidx < transitions.size(); transidx++) {
					ValueSet t2 = transitions.get(transidx);
					double delta = t2.value - t1.value;

					if (delta > this.mTolerance
							&& (t1.labels.size() > 1 || t2.labels.size() > 1 || !t1.labels.equals(t2.labels))) {
						cutpoints.addCutpoint(attidx, (t1.value + (delta / 2.0)));
						// System.out.printf("%d, %f, %f\n", attidx, t1.value, t2.value);
					}

					t1 = t2;
				}

			} else if (att.isNominal()) { // Nominal attributes
				cutpoints.addCutpoint(attidx, att.numValues());
			}
		}

		return cutpoints;
	}

	/** Checks if is there any setting out of boundary of mistakenly setted */
	public void checkForExceptions() throws Exception {
		if (mTolerance < 0)
			throw new Exception("Binarization: Cutpoint tolerance must be greater than or equal to 0.");
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
	 * Class value set
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Dec 10, 2020
	 * @version 1.0
	 */
	private class ValueSet implements Serializable {

		/** SERIAL ID */
		private static final long serialVersionUID = 1L;

		Set<Double> labels;
		double value;

		public ValueSet(double value) {
			labels = Sets.newHashSet();
			this.value = value;
		}

		@Override
		public String toString() {
			return Double.toString(value) + ": " + labels.toString();
		}
	}

}