package weka.classifiers.rules.lad.binarization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import weka.core.Attribute;

/**
 * Class Cutpoints
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class CutpointSet implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = 3660537629035514316L;

	/* Variables */
	private Vector<Cutpoint> mCutpoints;
	private ArrayList<Attribute> mAttributes;

	/** Main Constructor */
	public CutpointSet() {
		this.mAttributes = new ArrayList<Attribute>();
		this.mCutpoints = new Vector<Cutpoint>();
	}

	/** Adds a new attribute */
	public void addAttribute(Attribute att) {
		this.mAttributes.add(att);
	}

	/** Adds a new cutpoint */
	public void addCutpoint(int att, double value) {
		this.mCutpoints.add(new Cutpoint(att, value));
	}

	/** Narrows down our cutpoint list keeping just the listed indices. */
	public void narrowDown(ArrayList<Integer> indices) {
		Vector<Cutpoint> newList = new Vector<Cutpoint>();

		for (Integer index : indices)
			newList.add(mCutpoints.get(index));

		newList.trimToSize();
		this.mCutpoints = newList;
	}

	/** GET of a mapped attribute index */
	public int attAt(int index) {
		return this.mCutpoints.get(index).mAtt;
	}

	/** GET of a mapped attribute value */
	public double valueAt(int index) {
		return this.mCutpoints.get(index).mValue;
	}

	/** GET of a mapped attribute value */
	public String valueAt(int index, int valIndex) {
		return mAttributes.get(mCutpoints.get(index).mAtt).value(valIndex);
	}

	/** GET of a mapped attribute name */
	public String nameAt(int index) {
		return this.mCutpoints.get(index).mName;
	}

	/** GET isNumeric of a mapped attribute index */
	public boolean isNumeric(int index) {
		return mAttributes.get(mCutpoints.get(index).mAtt).isNumeric();
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
		String s = String.format("Cutpoints: %d\n", mCutpoints.size());

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
		private final String mName;

		/** Main Constructor */
		public Cutpoint(int att, double value) {
			this.mAtt = att;
			this.mValue = value;

			if (mAttributes.size() > 0)
				this.mName = mAttributes.get(mAtt).name();
			else
				this.mName = "Att";
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
			return String.format(" [ %s : %f ]", mName, mValue);
		}
	}
}