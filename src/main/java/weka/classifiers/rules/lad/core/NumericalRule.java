package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

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
	private ArrayList<NumericalCondition> mConditions;

	/** Main Constructor */
	public NumericalRule(final BinaryRule bRule, final CutpointSet cutpoints) {
		this.mLabel = bRule.getLabel();
		this.mWeight = 0.0;
		this.mConditions = new ArrayList<NumericalCondition>();
		this.mPurity = bRule.getPurity();

		int att, index;
		double value;
		boolean relation;

		HashMap<Integer, NumericalCondition> condMap = new HashMap<Integer, NumericalCondition>();
		NumericalCondition nCondAux = null;

		for (Literal u : bRule.getLiterais()) {
			index = u.getAtt();
			att = cutpoints.attAt(index);

			nCondAux = condMap.get(index);
			if (u.isNumeric()) {
				value = cutpoints.valueAt(index);
				relation = u.getSignal();

				if (nCondAux != null) {
					// A > 3, A > 4 :: A > 4
					if (relation && nCondAux.mValue < value)
						condMap.put(index, new NumericalCondition(att, value,
								relation));
					// B <= 5, B <= 6 :: B <= 5
					else if (!relation && nCondAux.mValue > value)
						condMap.put(index, new NumericalCondition(att, value,
								relation));
				} else
					condMap.put(index, new NumericalCondition(att, value,
							relation));
			} else {
				if (nCondAux == null)
					condMap.put(index, new NumericalCondition(att,
							u.getValue(), NumericalCondition.EQUALS_TO));
				else
					System.out.println("Tacagota!");
			}
		}

		this.mConditions.addAll(condMap.values());
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
		 * If a rule has negative weight we stop using that rule by return 0.0
		 * as its weight
		 */
		if (this.mWeight < 0.0)
			return 0.0;
		else
			return mWeight;
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
		for (NumericalCondition nCond : this.mConditions) {
			int att = nCond.mAtt;

			if (att < 0 || att >= inst.numAttributes() - 1) {
				System.out.println("Quem j� viu?");
				return false;
			}

			if (inst.isMissing(att))
				return false;

			switch (nCond.mRelation) {
			case NumericalCondition.BIGGER_THAN:
				if (inst.value(att) <= nCond.mValue)
					return false;
				break;
			case NumericalCondition.LESS_EQUAL_THAN:
				if (inst.value(att) > nCond.mValue)
					return false;
				break;
			case NumericalCondition.EQUALS_TO:
				if (inst.value(att) != nCond.mValue)
					return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		NumericalRule nRule = (NumericalRule) o;
		if (this.mLabel != nRule.mLabel
				|| this.mConditions.size() != nRule.mConditions.size())
			return false;

		for (NumericalCondition nCond : nRule.mConditions)
			if (!this.mConditions.contains(nCond))
				return false;

		return true;
	}

	/** It uses the original attributes names when printing */
	public String toString(Instances data) {
		if (data == null)
			return toString();

		String s = "{" + this.mWeight + "}";
		for (NumericalCondition nCond : this.mConditions)
			s += " " + nCond.toString(data);

		return s;
	}

	@Override
	public String toString() {
		String s = "{" + this.mWeight + "}";
		for (NumericalCondition nCond : this.mConditions)
			s += " " + nCond;

		return s;
	}

	/**
	 * Numerical Condition
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class NumericalCondition implements Serializable {

		/** SERIAL ID */
		private static final long serialVersionUID = -145069372562872753L;

		/* Variables */
		private final Integer mAtt;
		private final Double mValue;
		private final int mRelation;

		protected final static int BIGGER_THAN = 0;
		protected final static int LESS_EQUAL_THAN = 1;
		protected final static int EQUALS_TO = 2;

		/** Main Constructor */
		public NumericalCondition(int att, double value, int relation) {
			this.mAtt = att;
			this.mValue = value;
			this.mRelation = relation;
		}

		/** Older Constructor */
		public NumericalCondition(int att, double value, boolean relation) {
			// true: > | false: <=
			this(att, value, relation ? BIGGER_THAN : LESS_EQUAL_THAN);
		}

		/** Print numeric model */
		private String toString(String attName) {
			return "["
					+ attName
					+ (mRelation == BIGGER_THAN ? " > "
							: (mRelation == LESS_EQUAL_THAN ? " <= " : " = "))
					+ mValue + "]";
		}

		/** Print nominal model */
		private String toString(String attName, String attValue) {
			return "[" + attName + " = " + attValue + "]";
		}

		private String toString(Instances data) {
			if (mRelation == EQUALS_TO) { // Nominal
				Attribute att = data.attribute(mAtt);
				return toString(att.name(),
						att.value((int) mValue.doubleValue()));
			} else {
				return toString(data.attribute(mAtt).name());
			}
		}

		@Override
		public String toString() {
			return toString("att" + mAtt);
		}

		@Override
		public boolean equals(Object o) {
			NumericalCondition nCond = (NumericalCondition) o;

			boolean test = this.mAtt == nCond.mAtt;
			test &= Math.abs(this.mValue - nCond.mValue) < 0.001;
			test &= this.mRelation == nCond.mRelation;

			return test;
		}
	}
}