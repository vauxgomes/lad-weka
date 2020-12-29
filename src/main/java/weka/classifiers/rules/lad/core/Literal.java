package weka.classifiers.rules.lad.core;

import java.io.Serializable;

/**
 * Class Literal
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 */
public class Literal implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = -3388849798203471234L;

	/* Variables */
	private final int mAtt;
	private final double mValue;
	private final boolean mNumericInfo;

	/* Auxiliary Variables */
	private final static int TRUE = 1;

	/** Numeric Constructor */
	public Literal(int att, boolean sign) {
		this.mAtt = att;
		this.mValue = sign ? TRUE : 0;
		this.mNumericInfo = true;
	}

	/** Nominal constructor */
	public Literal(int att, double value) {
		this.mAtt = att;
		this.mValue = value;
		this.mNumericInfo = false;
	}

	/** Compares values between this Literal and the related Instance attribute */
	public boolean isIn(BinaryInstance instance) {
		return (isNumeric() && instance.getBinAt(mAtt) == getSignal())
				|| (!isNumeric() && instance.getValueAt(mAtt) == getValue());
	}

	/** GET of attribute index */
	public int getAtt() {
		return mAtt;
	}

	/** GET of value */
	public double getValue() {
		return mValue;
	}

	/** GET of numeric info */
	public boolean isNumeric() {
		return mNumericInfo;
	}

	/** GET of sign (if it is numeric) */
	public boolean getSignal() {
		return mValue == TRUE;
	}

	@Override
	public boolean equals(Object obj) {
		Literal l = (Literal) obj;
		return mAtt == l.mAtt && mValue == l.mValue;
	}

	@Override
	public String toString() {
		return "(" + (isNumeric() ? (getSignal() ? "+" : "-") + mAtt : "Att" + mAtt + " = " + (int) getValue()) + ")";
	}
}