package weka.classifiers.rules;

import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.lad.binarization.Binarization;
import weka.classifiers.rules.lad.binarization.Cutpoints;
import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.core.BinaryRule;
import weka.classifiers.rules.lad.core.NumericalRule;
import weka.classifiers.rules.lad.cutpointSelection.FeatureSelection;
import weka.classifiers.rules.lad.cutpointSelection.IteratedSampling;
import weka.classifiers.rules.lad.ruleGenerators.RandomRuleGenerator;
import weka.classifiers.rules.lad.ruleGenerators.RuleGenerator;
import weka.classifiers.rules.lad.ruleManager.RuleManager;
import weka.classifiers.rules.lad.util.LADFileManager;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * Class Logical Analysis Data (LAD)
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class LAD extends AbstractClassifier implements TechnicalInformationHandler {

	/** SERIAL ID */
	private static final long serialVersionUID = -7358699627342342455L;

	/* Parameters [INITIAL STATES] */
	private double mCutpointTolerance = 0.0;
	private double mMinimumPurity = 0.95;
	private boolean mPrintFile = false;

	private FeatureSelection mFeatureSelection = new IteratedSampling();
	private RuleGenerator mRuleGenerator = new RandomRuleGenerator();

	/* Variables */
	private Cutpoints mCutpoints = null;
	private RuleManager mRuleManager = null;

	/* Auxiliary */
	private LADFileManager mFileManager;
	private String ERROR = "";

	@Override
	/**
	 * Generates the classifier.
	 * 
	 * @param instances set of instances serving as training data
	 * @throws Exception if the classifier has not been generated successfully
	 */
	public void buildClassifier(Instances data) throws Exception {

		// Binarization
		Binarization binarization = new Binarization(mCutpointTolerance);
		binarization.checkForExceptions();

		mCutpoints = binarization.findCutpoints2(data);

		BinaryData trainingData = new BinaryData(data, mCutpoints);

		/*
		 * If the separation level required is positive, we need to go through the set
		 * covering phase.
		 */

		if (mFeatureSelection.getSeparationLevel() > 0) {
			mFeatureSelection.checkForExceptions();
			
			try {
				mFeatureSelection.findSelectedAtts(trainingData);
				mCutpoints.narrowDown(mFeatureSelection.getSelectedAttArray());
			} catch (OutOfMemoryError e) {
				ERROR = "\n" + LADFileManager.writeSection("Feature Selection: Out Of Memory Error");
				ERROR += " It was impossible to build the set covering model due "
						+ "the large number \n of cutpoints generated. Try "
						+ "increasing the cutpoint tolerance or \n allocating "
						+ "more memory to the JVM at startup.\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Generating rules
		this.mRuleGenerator.setMinimumPurity(mMinimumPurity);
		this.mRuleGenerator.checkForExceptions();

		try {
			this.mRuleGenerator.generateRules(trainingData);
		} catch (OutOfMemoryError e) {
			ERROR += (ERROR.length() > 0 ? "\n" : "");
			ERROR += "\n" + LADFileManager.writeSection("Rule Generation: Out Of Memory Error");
			ERROR += " It was impossible to build the whole set of rules due memory "
					+ "issues.\n All the rules that was possible to generate will be "
					+ "used for the\n classification problem.";
		} catch (Exception e) {
			// Just in case we change something and it goes wrong ;)
			e.printStackTrace();
		} finally {
			// Setting Rules
			this.mRuleManager = new RuleManager(data);

			for (BinaryRule bRule : mRuleGenerator.getRules())
				mRuleManager.addRule(new NumericalRule(bRule, mCutpoints));

			mRuleManager.adjustRulesWeight(data);

			// Writing on the file
			if (mPrintFile) {
				mFileManager = new LADFileManager(data.relationName(), true);
				mFileManager.write(this, data.relationName());
				mFileManager.close();
			}
		}
	}

	@Override
	/**
	 * Calculates the class membership probabilities for the given test instance.
	 * 
	 * @param instance the instance to be classified
	 * @return predicted class probability distribution
	 * @throws Exception if there is a problem generating the prediction
	 */
	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] distribution;

		if (mPrintFile) {
			distribution = mRuleManager.distributionForInstancePSR(instance);

			mFileManager.restore();
			mFileManager.write(mRuleManager.getLatestRepresentation());
			mFileManager.close();
		} else {
			distribution = mRuleManager.distributionForInstance(instance);
		}

		return distribution;
	}

	/*
	 * ------------------------------------------------------------------------ SETs
	 * & GETs
	 * ------------------------------------------------------------------------
	 */

	/** GET of Cut Points */
	public Cutpoints getCutpoints() {
		return mCutpoints;
	}

	/** GET of Rule Manager */
	public RuleManager getRuleManager() {
		return mRuleManager;
	}

	/*
	 * ------------------------------------------------------------------------
	 * DYSPLAY SETs & GETs
	 * ------------------------------------------------------------------------
	 */

	/** GET of cutPointTolerance to Display */
	public double getCutpointTolerance() {
		return mCutpointTolerance;
	}

	/** SET of cutPointTolerance to Display */
	public void setCutpointTolerance(double cutpointTolerance) {
		mCutpointTolerance = cutpointTolerance;
	}

	/** GET of minumumPurity to Display */
	public double getMinimumPurity() {
		return mMinimumPurity;
	}

	/** SET of minumumPurity to Display */
	public void setMinimumPurity(double purity) {
		mMinimumPurity = purity;
	}

	/** GET of PrintFile to Display */
	public boolean getPrintFile() {
		return mPrintFile;
	}

	/** SET of PrintFile to Display */
	public void setPrintFile(boolean mPrintFile) {
		this.mPrintFile = mPrintFile;
	}

	/** SET of FeatureSelection Algorithm to Display */
	public void setFeatureSelection(FeatureSelection featureSelection) {
		this.mFeatureSelection = featureSelection;
	}

	/** GET of FeatureSelection Algorithm to Display */
	public FeatureSelection getFeatureSelection() {
		return mFeatureSelection;
	}

	/** SET of RuleGenerator to Display */
	public void setRuleGenerator(RuleGenerator ruleGenerator) {
		this.mRuleGenerator = ruleGenerator;
	}

	/** GET of RuleGenerator to Display */
	public RuleGenerator getRuleGenerator() {
		return mRuleGenerator;
	}

	/*
	 * -------------------------------------------------------------------- OTHERS
	 * DYSPLAY INFORMATIONS & TIP TEXTs
	 * --------------------------------------------------------------------
	 */

	/** Resumo sobre o algoritmo */
	public String globalInfo() {
		return "Implements the Logical Analysis of Data algorithm for classification.";
	}

	/** MinimumPurity Tip Text */
	public String minimumPurityTipText() {
		return "Minimum purity requirement for rules. This is an upper bound on the number of points from "
				+ "another class that are covered by a rule (as a percentage of the total number of points "
				+ "covered by the rule).";
	}

	/** RuleGenerator Tip Text */
	public String ruleGeneratorTipText() {
		return "The algorithm used for generating classsification rules.";
	}

	/** Print File Tip Text */
	public String printFileTipText() {
		return "Whether or not a report file is saved in the default document folder, containing a detailed "
				+ "description of the LAD model produced.";
	}

	/*
	 * -------------------------------------------------------------------- OPTION
	 * METHODS of CLASSIFIER
	 * --------------------------------------------------------------------
	 */

	/**
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		// Looking for CutPoints Tolerance Option
		String cutPointsToleranceOption = Utils.getOption('T', options);
		if (cutPointsToleranceOption.length() != 0) {
			setCutpointTolerance(Double.parseDouble(cutPointsToleranceOption));
		}

		// Looking for Feature Selection Level Option
		String featureSelSeparationClassOption = Utils.getOption('F', options);
		if (featureSelSeparationClassOption.length() != 0) {
			String[] tmpOptions = Utils.splitOptions(featureSelSeparationClassOption);

			featureSelSeparationClassOption = FeatureSelection.class.getPackage().getName() + "." + tmpOptions[0];

			setFeatureSelection((FeatureSelection) Utils.forName(FeatureSelection.class,
					featureSelSeparationClassOption, tmpOptions));

			this.mFeatureSelection.setOptions(tmpOptions);
		}

		// Looking for Minimum Purity Option
		String minimumPurityOption = Utils.getOption('P', options);
		if (minimumPurityOption.length() != 0) {
			setMinimumPurity(Double.parseDouble(minimumPurityOption));
		}

		String ruleGeneratiorClassOption = Utils.getOption('G', options);
		if (ruleGeneratiorClassOption.length() != 0) {
			String[] tmpOptions = Utils.splitOptions(ruleGeneratiorClassOption);

			ruleGeneratiorClassOption = RuleGenerator.class.getPackage().getName() + "." + tmpOptions[0];

			setRuleGenerator((RuleGenerator) Utils.forName(RuleGenerator.class, ruleGeneratiorClassOption, tmpOptions));

			this.mRuleGenerator.setOptions(tmpOptions);
		}

		// Looking for print file option
		String printFileOption = Utils.getOption('A', options);
		if (printFileOption.length() != 0) {
			setPrintFile(Boolean.parseBoolean(printFileOption));
		}

		super.setOptions(options);
	}

	public String[] getOptions() {
		Vector<String> options = new Vector<String>();

		String[] classifierOptions = super.getOptions();
		for (int i = 0; i < classifierOptions.length; i++)
			options.add(classifierOptions[i]);

		options.add("-T");
		options.add("" + getCutpointTolerance());

		options.add("-F");
		options.add("" + mFeatureSelection.getClass().getSimpleName() + " "
				+ Utils.joinOptions(mFeatureSelection.getOptions()));

		options.add("-P");
		options.add("" + getMinimumPurity());
		options.add("-G");
		options.add(
				"" + mRuleGenerator.getClass().getSimpleName() + " " + Utils.joinOptions(mRuleGenerator.getOptions()));

		options.add("-A");
		options.add("" + getPrintFile());

		return (String[]) options.toArray(new String[options.size()]);
	}

	/** Returns an enumeration describing the available options. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration listOptions() {

		Vector<Option> newVector = new Vector<Option>();

		Enumeration classifierList = super.listOptions();
		while (classifierList.hasMoreElements())
			newVector.addElement((Option) classifierList.nextElement());

		newVector.addElement(new Option("\tTolerance for cutpoint generation. A cutpoint will \n"
				+ "\tonly be generated between two values if they differ by\n"
				+ "\tat least this value. (Default = 0.0)\n", "T", 1, "-T <tolerance>"));

		newVector.addElement(new Option("\tFeature selection class.\n", "F", 1,
				"-F <feature_separation_class_simple_name> + <options>"));

		newVector.addElement(new Option("\tMinimum purity requirement for rules. This is an upper\n"
				+ "\tbound on the number of points from another class that\n"
				+ "\tare covered by a rule (as a percentage of the total number\n"
				+ "\tof points covered by the rule).\n", "P", 1, "-P <percentage>"));

		newVector.addElement(new Option("\tThe algorithm used for generating classsification rules.\n", "G", 1,
				"-G <rule_generator_class_name> + <options>"));

		newVector.addElement(new Option(
				"\tWhether or not a report file is saved in the default\n"
						+ "\tdocument folder, containing a detailed description\n" + "\tof the LAD model produced.\n",
				"A", 1, "-A <Boolean>"));

		Enumeration featureSelection = this.mFeatureSelection.listOptions();
		while (featureSelection.hasMoreElements())
			newVector.addElement((Option) featureSelection.nextElement());

		Enumeration ruleGenerator = this.mRuleGenerator.listOptions();
		while (ruleGenerator.hasMoreElements())
			newVector.addElement((Option) ruleGenerator.nextElement());

		return newVector.elements();
	}

	/*
	 * ------------------------------------------------------------------------
	 * CAPACIDADES
	 * ------------------------------------------------------------------------
	 */

	@Override
	public Capabilities getCapabilities() {
		Capabilities capabilities = super.getCapabilities();
		capabilities.disableAll();

		// attributes
		capabilities.enable(Capability.NUMERIC_ATTRIBUTES);
		capabilities.enable(Capability.NOMINAL_ATTRIBUTES);
		capabilities.enable(Capability.BINARY_ATTRIBUTES);
		capabilities.enable(Capability.MISSING_VALUES);

		// class
		capabilities.enable(Capability.NOMINAL_CLASS);
		capabilities.enable(Capability.BINARY_CLASS);

		// instances
		capabilities.setMinimumNumberInstances(2);

		return capabilities;
	}

	/*
	 * ------------------------------------------------------------------------
	 * INFORMACOES TECNICAS
	 * ------------------------------------------------------------------------
	 */

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result, additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "V.S.D. Gomes and T.O. Bonates");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.TITLE,
				"Classificacao Supervisionada de Dados via Otimizacao e Funcoes Booleanas (in Portuguese)");
		result.setValue(Field.BOOKTITLE, "Anais do II Workshop Tecnico-Cientifico de Computacao");
		result.setValue(Field.ADDRESS, "Mossoro, Brazil");
		result.setValue(Field.PAGES, "21-27");

		additional = result.add(Type.ARTICLE);
		additional.setValue(Field.AUTHOR, "T.O. Bonates, P.L. Hammer and A. Kogan");
		additional.setValue(Field.YEAR, "2008");
		additional.setValue(Field.TITLE, "Maximum Patterns in Datasets");
		additional.setValue(Field.JOURNAL, "Discrete Applied Mathematics");
		additional.setValue(Field.VOLUME, "156");
		additional.setValue(Field.PAGES, "846-861");

		additional = result.add(Type.ARTICLE);
		additional.setValue(Field.AUTHOR, "E. Boros, P.L. Hammer, T. Ibaraki, A. Kogan, E. Mayoraz and I. Muchnik");
		additional.setValue(Field.YEAR, "2000");
		additional.setValue(Field.TITLE, "An Implementation of Logical Analysis of Data");
		additional.setValue(Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
		additional.setValue(Field.VOLUME, "12");
		additional.setValue(Field.PAGES, "292-306");

		return result;
	}

	/*
	 * -------------------------------------------------------------------- PRINT
	 * FUNCTIONS
	 * --------------------------------------------------------------------
	 */

	@Override
	public String toString() {
		if (mCutpoints == null && mRuleGenerator.getRules().size() == 0)
			return "LAD: No model built yet.";

		String s = LADFileManager.write(mCutpoints) + "\n";
		s += LADFileManager.write(mRuleManager);
		s += ERROR;

		return s;
	}

	/*
	 * ------------------------------------------------------------------------ MAIN
	 * ------------------------------------------------------------------------
	 */

	public static void main(String args[]) throws Exception {
		// Running the classifier from set options
		runClassifier(new LAD(), args);
	}
}