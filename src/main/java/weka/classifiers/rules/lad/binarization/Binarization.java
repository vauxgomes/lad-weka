package weka.classifiers.rules.lad.binarization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	public Binarization(double tolerance) {
		mTolerance = tolerance;
	}

	/** Method for mapping the instances to cutpoints */
	public CutpointSet fit(Instances data) throws Exception {
		//
		CutpointSet cutpoints = new CutpointSet();
		Attribute att = null;
		
		// For each attribute, sort values and find transitions
		for (int i = 0; i < data.numAttributes(); i++) {

			att = data.attribute(i);
			cutpoints.addAttribute(att);

			// CLASS ATTRIBUTE
			if (att == data.classAttribute())
				continue;

			// NOMINAL ATTRIBUTES
			if (att.isNominal()) {
				cutpoints.addCutpoint(i, att.numValues());
				continue;
			}

			// NUMERIC ATTRIBUTE
			// Mapping
			HashMap<Double, Set<Double>> map = new HashMap<Double, Set<Double>>();

			for (int j = 0; j < data.numInstances(); j++) {
				Instance instance = data.instance(j);

				// Safety
				if (instance.isMissing(i))
					continue;

				double v = instance.value(i);

				if (!map.containsKey(v))
					map.put(v, Sets.newHashSet());

				map.get(v).add(instance.classValue());
			}

			// Cut point transitions
			ArrayList<Double> keys = new ArrayList<Double>(map.keySet());
			Collections.sort(keys);
			
			// Search
			double v = keys.get(0);
			for (int k = 1; k < keys.size(); k++) {
				double u = keys.get(k);
				double delta = u - v;

				if (delta > this.mTolerance)
					if (map.get(v).size() > 1 || map.get(v).size() > 1 || !map.get(v).equals(map.get(u))) {
						cutpoints.addCutpoint(i, (v + (delta / 2.0)));
					}

				v = u;
			}
		}

		cutpoints.sort();		
		return cutpoints;
	}

	/** Checks if is there any setting out of boundary of mistakenly setted */
	public void checkForExceptions() throws Exception {
		if (mTolerance < 0)
			throw new Exception("Binarization: Cutpoint tolerance must be greater than or equal to 0.");
	}
}