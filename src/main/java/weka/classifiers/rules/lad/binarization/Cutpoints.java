package weka.classifiers.rules.lad.binarization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Class Cutpoints
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class Cutpoints implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = 3660537629035514316L;

	/* Variables */
	private Vector<Cutpoint> mCutpoints;

	/** Main Constructor */
	public Cutpoints() {
		this.mCutpoints = new Vector<Cutpoint>();
	}

	/** Adds a new cutpoint */
	public void addCutpoint(int att, double value) {
		this.mCutpoints.add(new Cutpoint(att, value));
	}

	/** Narrows down our cutpoint list keeping just the listed indices. */
	public void narrowDown(ArrayList<Integer> indices) {
		Vector<Cutpoint> newList = new Vector<Cutpoint>();

		for (Integer index : indices)
			newList.add(new Cutpoint(attAt(index), valueAt(index)));

		newList.trimToSize();
		this.mCutpoints = newList;
	}

	/** GET of a mapped attribute index */
	public int attAt(int index) {
		int caracteristica = -1;

		if ((index >= 0) && (index < this.numCutpoints()))
			caracteristica = this.mCutpoints.get(index).mAtt;
		else
			System.err.println("Call to attAt() before data is sorted.");

		return caracteristica;
	}

	/** GET of a mapped attribute value */
	public double valueAt(int index) {
		double value = -1.0;

		if ((index >= 0) && (index < this.numCutpoints()))
			value = this.mCutpoints.get(index).mValue;
		else
			System.err.println("Call to valueAt() with an invalid index: "
					+ index + ".");

		return value;
	}

	/** GET of the number of cutpoints */
	public int numCutpoints() {
		return this.mCutpoints.size();
	}

	/** SORT of cutpoint */
	public void sort() {
		Collections.sort(this.mCutpoints);
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < this.mCutpoints.size(); i++)
			s += this.mCutpoints.get(i).toString() + "\n";

		return s;
	}

	/**
	 * Class Cutpoint
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class Cutpoint implements Serializable, Comparable<Cutpoint> {

		/** SERIAL ID */
		private static final long serialVersionUID = 4895475778132616358L;

		/* Variables */
		private final int mAtt;
		private final double mValue;

		/** Main Constructor */
		public Cutpoint(int att, double aCorte) {
			this.mAtt = att;
			this.mValue = aCorte;
		}

		@Override
		public int compareTo(Cutpoint arg0) {
			if (arg0 != null) {
				if (arg0.mAtt < this.mAtt)
					return 1;
				else if (arg0.mAtt > this.mAtt)
					return -1;
				else {
					if (arg0.mValue < this.mValue)
						return 1;
					else if (arg0.mValue > this.mValue)
						return -1;
				}
			}

			return 0;
		}

		@Override
		public String toString() {
			return " [ att" + mAtt + " : " + mValue + " ]";
		}
	}
}