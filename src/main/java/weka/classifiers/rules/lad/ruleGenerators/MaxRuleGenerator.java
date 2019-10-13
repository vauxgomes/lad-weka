package weka.classifiers.rules.lad.ruleGenerators;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryInstance;
import weka.classifiers.rules.lad.core.BinaryRule;
import weka.classifiers.rules.lad.core.Literal;
import weka.core.Option;

/**
 * Class MaxRuleGenerator (Max pattern rule generator).
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class MaxRuleGenerator extends RuleGenerator {

	/** SERIAL ID */
	private static final long serialVersionUID = 8175941261292479958L;

	/* Variables */
	private ArrayList<Literal> mLiterals;
	private boolean mInstClass;

	/** Main Constructor */
	public MaxRuleGenerator() {
		this.mLiterals = new ArrayList<Literal>();
	}

	@Override
	public void generateRules(BinaryData trainingData) {
		mTrainingData = trainingData;

		for (int i = 0; i < mTrainingData.numInstances(); i++)
			this.addRule(expand(mTrainingData.getInstance(i)));
	}

	/** Method for building decision rules */
	private BinaryRule expand(BinaryInstance bInst) {
		// Settings
		generateMinterm(bInst);
		this.mInstClass = bInst.instanceClass();

		// Variables
		Node principal = new Node();
		Node naoCobertas = new Node();

		// Initialing NODEs
		for (int i = 0; i < this.mTrainingData.numInstances(); i++) {
			BinaryInstance obs = this.mTrainingData.getInstance(i);

			if (this.verifyCovering(obs)) {
				principal.addInstance(obs);
			} else {
				naoCobertas.addInstance(obs);
			}
		}

		// Enabling Safety Mode
		boolean safetyMode = false;
		if (principal.getPurity(this.mInstClass) < this.mMinimumPurity) {
			safetyMode = true;
		}

		// Maximization
		while (this.mLiterals.size() > 0) {
			Node melhor = null;
			Literal melhorLiteral = null;

			// Auxiliary list
			ArrayList<Literal> literais = new ArrayList<Literal>(this.mLiterals);
			for (Literal l : literais) {
				// Removing
				this.mLiterals.remove(l);

				// Copy from external NODEs
				Node novo = new Node(principal);
				Node listaNaoCobertasDaIteracao = new Node(naoCobertas);

				// UpDate of NODEs
				for (BinaryInstance b : naoCobertas.getPositiveInstances()) {
					if (this.verifyCovering(b)) {
						novo.addInstance(b);
						listaNaoCobertasDaIteracao.removeInstance(b);
					}
				}

				for (BinaryInstance b : naoCobertas.getNegativeInstances()) {
					if (this.verifyCovering(b)) {
						novo.addInstance(b);
						listaNaoCobertasDaIteracao.removeInstance(b);
					}
				}

				// Testing
				if ((novo.getPurity(this.mInstClass) >= this.mMinimumPurity)
						|| ((safetyMode) && (Math.abs(novo
								.getPurity(this.mInstClass)
								- principal.getPurity(this.mInstClass)) < 1.0E4))) {

					// Calculating discrepancy
					double discrP = discrepancy(naoCobertas
							.getPositiveInstances());
					double discrN = discrepancy(naoCobertas
							.getNegativeInstances());

					if (this.mInstClass) {
						novo.setDiscrepancy(discrP / discrN);
					} else {
						novo.setDiscrepancy(discrN / discrP);
					}

					// Decision
					if (melhor == null) {
						melhor = novo;
						melhorLiteral = l;
					}

					else if (this.mInstClass) {
						if (novo.numPositiveInstances() > melhor
								.numPositiveInstances()) {
							melhor = novo;
							melhorLiteral = l;
						} else if (novo.numPositiveInstances() == melhor
								.numPositiveInstances()) {
							if (novo.getDiscrepancy() < melhor.getDiscrepancy()) {
								melhor = novo;
								melhorLiteral = l;
							}
						}
					}

					else {
						if (novo.numNegativeInstances() > melhor
								.numNegativeInstances()) {
							melhor = novo;
							melhorLiteral = l;
						} else if (novo.numNegativeInstances() == melhor
								.numNegativeInstances()) {
							if (novo.getDiscrepancy() < melhor.getDiscrepancy()) {
								melhor = novo;
								melhorLiteral = l;
							}
						}
					}
				}

				// Reinsert the "Literal"
				this.mLiterals.add(l);
			}

			// Break Conditions
			if (melhorLiteral == null) {
				break;
			} else {
				principal = melhor;
				this.mLiterals.remove(melhorLiteral);

				for (BinaryInstance obs : principal.getPositiveInstances())
					naoCobertas.removeInstance(obs);
				for (BinaryInstance obs : principal.getNegativeInstances())
					naoCobertas.removeInstance(obs);
			}
		}

		return new BinaryRule(this.mLiterals, this.mInstClass,
				principal.getPurity(this.mInstClass));
	}

	/** Generates minterm */
	private void generateMinterm(BinaryInstance bInst) {
		// Cleaning Array
		this.mLiterals.clear();

		for (int i = 0; i < bInst.numAttributes(); i++) {
			if (!bInst.isMissingAttribute(i))
				if (bInst.isNumeric(i))
					this.mLiterals.add(new Literal(i, bInst.getBinAt(i)));
				else
					this.mLiterals.add(new Literal(i, bInst.getValeuAt(i)));
		}
	}

	/** Checks if an instance has been covered for the actual list of literals */
	private boolean verifyCovering(BinaryInstance obs) {
		for (Literal l : this.mLiterals) {
			int i = l.getAtt();
			if (i < 0 || (i >= obs.numAttributes()))
				return false;

			if (obs.isMissingAttribute(i)) {
				if (this.mInstClass == obs.instanceClass())
					return false;
			} else if (l.isNumeric()) {
				if (l.getSignal() != obs.getBinAt(i))
					return false;
			} else {
				if (l.getValue() != obs.getValeuAt(i))
					return false;
			}

		}

		return true;
	}

	/** Calculates discrepancy TODO DOUBLE CHECK IT */
	private double discrepancy(ArrayList<BinaryInstance> bInsts) {
		double discrepancia = 0.0;

		for (BinaryInstance bInst : bInsts) {
			int dist = 0;

			for (Literal l : this.mLiterals) {
				if (bInst.isMissingAttribute(l.getAtt())) {
					if (this.mInstClass == bInst.instanceClass())
						dist += 1;
				} else {
					if (l.isNumeric()) {
						if (l.getSignal() != bInst.getBinAt(l.getAtt()))
							dist += 1;
					} else {
						if (l.getValue() != bInst.getValeuAt(l.getAtt()))
							dist += 1;
					}
				}

			}

			discrepancia += dist;
		}

		discrepancia /= (1.0 + (double) bInsts.size());
		return discrepancia;
	}

	/*
	 * --------------------------------------------------------------------
	 * DYSPLAY INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
	 */

	/** information of the Algorithm */
	public String globalInfo() {
		return "Implements the Maximized Prime Patterns heuristic described in the "
				+ "\"Maximum Patterns in Datasets\" paper. It generates one pattern (rule) "
				+ "per observation, while attempting to: (i) maximize the coverage of other "
				+ "observations belonging to the same class, and (ii) preventing the "
				+ "coverage of too many observations from outside that class. The amount of "
				+ "\"outside\" coverage allowed is controlled by the minimum purity parameter "
				+ "(from the main LAD classifier).";
	}

	/*
	 * --------------------------------------------------------------------
	 * OPTIONS METHODS
	 * --------------------------------------------------------------------
	 */

	@Override
	public String[] getOptions() {
		return new String[0];
	}

	@Override
	public void setOptions(String[] options) throws Exception {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration listOptions() {
		return new Vector<Option>(0).elements();
	}
}