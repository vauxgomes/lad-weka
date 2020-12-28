package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.core.Instance;

/**
 * Class NumericalRule
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class NumericalRule implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = 7849846023533714647L;

	/* Variables */
	private int mLabel;
	private double mPurity;
	private double mWeight;

	/* My conditions */
	private ArrayList<Condition> mConditions;

	/** Main Constructor */
	public NumericalRule(final BinaryRule rule, final CutpointSet cutpoints) {

		this.mWeight = 0.0;
		this.mPurity = rule.getPurity();
		this.mLabel = rule.getLabel();
		this.mConditions = new ArrayList<Condition>();

		//
		ArrayList<Condition> conditions = new ArrayList<Condition>();

		//
		for (Literal literal : rule.getLiterais()) {
			int index = literal.getAtt();

			// Numeric condition
			if (literal.isNumeric()) {
				conditions.add(new Condition(cutpoints.attAt(index), cutpoints.nameAt(index), cutpoints.valueAt(index),
						literal.getSignal()));
			} else // Nominal condition
				conditions.add(new Condition(cutpoints.attAt(index), cutpoints.nameAt(index), literal.getValue(),
						cutpoints.valueAt(index, (int) literal.getValue())));
		}

		// Sorting
		Collections.sort(conditions);

		// Shortening
		Condition aux = conditions.get(0);
		for (int i = 1; i < conditions.size(); i++) {
			Condition c = conditions.get(i);

			if (aux.mAtt != c.mAtt || (aux.mAtt == c.mAtt && aux.mRelation != c.mRelation)) {
				mConditions.add(aux);
				aux = c;
			} else if (aux.mRelation == Condition.LESS_EQUAL_THAN) {
				aux = c;
			}
		}

		if (!mConditions.contains(aux))
			mConditions.add(aux);
	}

	/** GET of purity */
	public double getPurity() {
		return mPurity;
	}

	/** GET of class Label */
	public int getLabel() {
		return mLabel;
	}

	/** GET of rule's weights */
	public double getWeight() {
		/*
		 * If a rule has negative weight we stop using that rule by return 0.0 as its
		 * weight
		 */
		return Math.max(0, mWeight);
	}

	/** SET of weight */
	public void setWeight(double weight) {
		this.mWeight = weight;
	}

	/** INCREASE of weight */
	public void increaseWeight(double inc) {
		this.mWeight += inc;
	}

	/** DECREASE of weight */
	public void decreaseWeight(double dec) {
		this.mWeight -= dec;
	}

	/**
	 * Checks if a rule covers a given instance. It is adapted to Missing values
	 */
	public boolean isCovering(Instance inst) {
		for (Condition nCond : this.mConditions) {
			int att = nCond.mAtt;

			if (att < 0 || att >= inst.numAttributes() - 1)
				return false;

			if (inst.isMissing(att))
				return false;

			switch (nCond.mRelation) {
			case Condition.BIGGER_THAN:
				if (inst.value(att) <= nCond.mValue)
					return false;
				break;
			case Condition.LESS_EQUAL_THAN:
				if (inst.value(att) > nCond.mValue)
					return false;
				break;
			case Condition.EQUALS_TO:
				if (inst.value(att) != nCond.mValue)
					return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		NumericalRule nRule = (NumericalRule) o;

		if (this.mLabel != nRule.mLabel || this.mConditions.size() != nRule.mConditions.size())
			return false;

		for (Condition nCond : nRule.mConditions)
			if (!this.mConditions.contains(nCond))
				return false;

		return true;
	}

	@Override
	public String toString() {
		String s = "{" + this.mWeight + "}";
		for (Condition nCond : this.mConditions)
			s += " " + nCond.toString();

		return s;
	}

	/**
	 * Condition
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.1
	 */
	private class Condition implements Serializable, Comparable<Condition> {

		/** SERIAL ID */
		private static final long serialVersionUID = -145069372562872753L;

		/* Static variables */
		protected final static int BIGGER_THAN = 0;
		protected final static int LESS_EQUAL_THAN = 1;
		protected final static int EQUALS_TO = 2;

		/* Variables */
		private final int mAtt;
		private final double mValue;

		private final String mValueName;
		private final String mAttName;

		private final int mRelation;

		/** Main Constructor */
		public Condition(int att, String attName, double value, String valueName, int relation) {
			this.mAtt = att;
			this.mValue = value;

			this.mAttName = attName;
			this.mValueName = valueName;

			this.mRelation = relation;
		}

		/** Designated Constructor */
		public Condition(int att, String attName, double value, boolean relation) {
			// true: > | false: <=
			this(att, attName, value, null, (relation ? BIGGER_THAN : LESS_EQUAL_THAN));
		}

		/** Designated Constructor */
		public Condition(int att, String attName, double value, String valueName) {
			this(att, attName, value, valueName, EQUALS_TO);
		}

		@Override
		public String toString() {
//			if (mValueName != null)
//				return String.format("[%s (%d) = %s (%d)]", mAttName, mAtt, mValueName, (int) mValue);
//
//			return String.format("[%s (%d) %s %f]", mAttName, mAtt, (mRelation == BIGGER_THAN ? ">" : "<="), mValue);

			return String.format("[att%d %s " + mValue + "]", mAtt,
					(mRelation == BIGGER_THAN ? ">" : (mRelation == LESS_EQUAL_THAN ? "<=" : "=")));
		}

		@Override
		public boolean equals(Object o) {
			Condition c = (Condition) o;
			return mAtt == c.mAtt && Math.abs(this.mValue - c.mValue) < 0.001 && mRelation == c.mRelation;
		}

		@Override
		public int compareTo(Condition o) {
			// mAtt == o.mAtt
			if (mAtt < o.mAtt)
				return -1;
			else if (mAtt > o.mAtt)
				return 1;

			// Relation
			else if (mRelation < o.mRelation)
				return -1;
			else if (mRelation > o.mRelation)
				return 1;

			else if (mRelation == BIGGER_THAN)
				if (mValue < o.mValue)
					return -1;
				else if (mValue > o.mValue)
					return 1;

				else if (mRelation == LESS_EQUAL_THAN)
					if (mValue > o.mValue)
						return 1;
					else if (mValue < o.mValue)
						return -1;

			return 0;
		}
	}
}
