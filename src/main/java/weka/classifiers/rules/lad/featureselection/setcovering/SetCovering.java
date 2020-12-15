package weka.classifiers.rules.lad.featureselection.setcovering;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class SetCovering
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class SetCovering implements Serializable {

	/** SERIAL ID */
	private static final long serialVersionUID = -4580626902003828381L;

	/* Variables */
	private ArrayList<Set> mSets;
	private ArrayList<Element> mElements;
	private ArrayList<Integer> mSolution;

	/** Main Constructor */
	public SetCovering(int numSets) {
		this.mElements = new ArrayList<Element>();
		this.mSets = new ArrayList<Set>(numSets);
		this.mSolution = new ArrayList<Integer>(numSets);

		for (int i = 0; i < numSets; i++)
			mSets.add(new Set(i));
	}

	/** Adds a new element to our problem */
	public void addElement(ArrayList<Integer> sets) {
		if (sets.size() == 0)
			return;

		Element e = new Element(mElements.size(), sets.size());
		mElements.add(e);

		for (Integer s : sets)
			e.addSet(mSets.get(s));

		for (Integer s : sets)
			mSets.get(s).addElement(e);

	}

	/** Solves the set covering problems using Chvatal's greedy heuristic. */
	public void solve(int k) {
		/* Auxiliary Variables */
		HashMap<Integer, Integer> notCovereds = new HashMap<Integer, Integer>();
		int[] coveringCounter = new int[this.mElements.size()];

		for (Element e : this.mElements)
			notCovereds.put(e.mName, e.mName);

		/*
		 * Step 1. Pre-processing: Find all sets with just one element and add
		 * it to our solution.
		 */

		for (Element elemento : this.mElements) {
			if (elemento.numSets() == 1) {
				for (Set c : elemento.mSets.values()) {
					if (c.mElements.size() != 0) {
						this.mSolution.add(c.mName);

						for (Element e : c.getElementos()) {
							e.autoKill();
							notCovereds.remove(e.mName);
						}

					}

					this.mSets.remove(c);
				}
			}
		}

		/*
		 * Step 2. Greedy algorithm
		 */

		while (notCovereds.size() > 0 && mSets.size() > 0) {
			Set bestSet = null;
			double bestCost = -1;

			for (Set conj : this.mSets) {
				double aux = conj.mCost / conj.getSize();

				if ((aux < bestCost) || (bestSet == null)) {
					bestSet = conj;
					bestCost = aux;
				}
			}

			if (bestSet != null) {
				this.mSolution.add(bestSet.mName);

				for (Element e : bestSet.getElementos()) {
					int nome = e.mName;
					coveringCounter[nome]++;

					if (coveringCounter[nome] == k) {
						e.autoKill();
						notCovereds.remove(nome);
					}
				}

				this.mSets.remove(bestSet);
				for (Iterator<Set> it = this.mSets.iterator(); it.hasNext();) {
					Set c = it.next();

					if (c.getSize() == 0) {
						it.remove();
					}
				}

			} else {
				mSolution.clear();
				this.clear();

				return;
			}
		}

		this.clear();
	}

	/** GET of Solution */
	public ArrayList<Integer> getSolution() {
		return this.mSolution;
	}

	/** Method for writing the Set Covering problem on a file */
	public void write(String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(fileName));

			// Object function
			writer.println("Min Z = ");
			boolean fist = true;
			for (Set c : this.mSets) {
				if (fist) {
					writer.print(" x" + c.mName);
					fist = false;
				} else
					writer.print(" + x" + c.mName);
			}

			writer.println("\nsubject to:");

			// Coverage constraints
			fist = true;
			for (Element e : this.mElements) {
				for (Set c : e.getSets()) {
					if (fist) {
						writer.print("x" + c.mName);
						fist = false;
					} else
						writer.print(" + x" + c.mName);

				}

				writer.println(" >= 1");
			}

			// Binary constraints
			writer.print("Binary ");
			for (Set c : this.mSets)
				writer.print(" x" + c.mName);

			writer.flush();
			writer.close();
		} catch (Exception e) {
		}
	}

	/** Clears all the data stored, but the solution */
	public void clear() {
		mElements.clear();
		mSets.clear();

		// Calling for the Garbage Collector
		System.gc();
	}

	/**
	 * Class Set
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class Set implements Comparable<Set> {

		/* Variables */
		private HashMap<Element, Element> mElements;
		private double mCost;
		private int mName;

		/** Main Constructor */
		public Set(int nome) {
			this.mElements = new HashMap<Element, Element>();
			this.mCost = 1.0;
			this.mName = nome;
		}

		/** GET of elements */
		public ArrayList<Element> getElementos() {
			return new ArrayList<Element>(this.mElements.values());
		}

		/** Get of size */
		public int getSize() {
			return this.mElements.size();
		}

		/** Method for adding a new element */
		public void addElement(Element e) {
			if (e != null) {
				this.mElements.put(e, e);
				e.addSet(this);
			}
		}

		/** Method for removing an element */
		public void removeElement(Element e) {
			this.mElements.remove(e);
		}

		@Override
		public int compareTo(Set s) {
			if (mName > s.mName)
				return -1;
			else if (mName < s.mName)
				return 1;

			return 0;
		}
	}

	/**
	 * Class Element
	 * 
	 * @author Vaux Gomes
	 * @author Tiberius Bonates
	 * 
	 * @since Mar 27, 2014
	 * @version 1.0
	 */
	private class Element implements Comparable<Element> {

		/* Variables */
		private HashMap<Set, Set> mSets;
		private int mName;

		/** Main Constructor */
		public Element(int name, int numSets) {
			this.mSets = new HashMap<Set, Set>(numSets);
			this.mName = name;
		}

		/** GET of number of sets */
		public int numSets() {
			return mSets.size();
		}

		/** GET of sets it belongs to */
		public ArrayList<Set> getSets() {
			return new ArrayList<Set>(this.mSets.values());
		}

		/** Method for adding a set */
		public void addSet(Set c) {
			this.mSets.put(c, c);
		}

		/** Method for removing an element from all the sets it belongs to */
		public void autoKill() {
			for (Set c : this.mSets.values())
				c.removeElement(this);
		}

		@Override
		public int compareTo(Element e) {
			if (mName > e.mName)
				return -1;
			else if (mName < e.mName)
				return 1;

			return 0;
		}
	}
}