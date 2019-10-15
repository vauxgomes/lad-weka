package weka.classifiers.rules.lad.core;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class BinaryRule
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class BinaryRule implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = -5108679627773238991L;

	/* Variables */
	private boolean mClass;
	private double mPurity;
	private ArrayList<Literal> mLiterals;

	/** Main Constructor */
	public BinaryRule(ArrayList<Literal> literals, boolean aClass, double purity) {
		this.mClass = aClass;
		this.mPurity = purity;
		this.mLiterals = new ArrayList<Literal>(literals);
	}

	/** GET of rule's class */
	public boolean getRuleClass() {
		return this.mClass;
	}

	/** GET of purity */
	public double getPurity() {
		return mPurity;
	}

	/** GET of literals */
	public ArrayList<Literal> getLiterais() {
		return new ArrayList<Literal>(this.mLiterals);
	}

	@Override
	public String toString() {
		String s = new String();
		for (Literal l : this.mLiterals)
			s += l;
		return s;
	}
}