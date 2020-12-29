package weka.classifiers.rules;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.lad.binarization.Binarization;
import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.classifiers.rules.lad.core.BinaryData;
import weka.classifiers.rules.lad.featureselection.FeatureSelection;
import weka.classifiers.rules.lad.featureselection.GreedySetCover;
import weka.classifiers.rules.lad.rulegeneration.MaxRuleGenerator;
import weka.classifiers.rules.lad.rulegeneration.RuleGenerator;
import weka.classifiers.rules.lad.rulegeneration.RuleManager;
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
 * <p>
 * Logical Analysis of Data (LAD) is a rule-based machine learning algorithm
 * based on ideas from Optimization and Boolean Function Theory.
 * </p>
 * 
 * <p>
 * The LAD methodology was originally conceived by Peter L. Hammer, from Rutgers
 * University, and has been described and developed in a number of papers since
 * the late 80's. It has also been applied to classification problems arising in
 * areas such as Medicine, Economics, and Bioinformatics.
 * </p>
 * 
 * BibTeX:
 * 
 * <pre>
 * &#64;article{boros2000implementation,
 *    title = {An implementation of logical analysis of data},
 *    author = {Boros, Endre and Hammer, Peter L and Ibaraki, Toshihide and Kogan, Alexander and Mayoraz, Eddy and Muchnik, Ilya},
 *    journal = {IEEE Transactions on knowledge and Data Engineering},
 *    volume = {12},
 *    number = {2},
 *    pages = {292--306},
 *    year = {2000},
 *    publisher = {IEEE}
 * }
 * 
 * &#64;article{bonates2008maximum,
 *    title={Maximum patterns in datasets},
 *    author={Bonates, Tib{\'e}rius O and Hammer, Peter L and Kogan, Alexander},
 *    journal={Discrete Applied Mathematics},
 *    volume={156},
 *    number={6},
 *    pages={846--861},
 *    year={2008},
 *    publisher={Elsevier}
 * }
 * 
 * </pre>
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.1
 * 
 * @apiNote Updated to the multiclass on Dec 15, 2020
 */
public class LAD extends AbstractClassifier implements TechnicalInformationHandler {

	/** SERIAL ID */
	private static final long serialVersionUID = -7358699627342342455L;

	/* Hyperparameters */
	private double mCutpointTolerance = 0.0;
	private double mMinimumPurity = 0.85;

	private FeatureSelection mFeatureSelection = new GreedySetCover();
	private RuleGenerator mRuleGenerator = new MaxRuleGenerator();

	/* Variables */
	private CutpointSet mCutpoints = null;
	private RuleManager mRuleManager = null;

	/* Auxiliary */
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

		mCutpoints = binarization.fit(data);
		BinaryData bData = new BinaryData(data, mCutpoints);

		/*
		 * If the separation level required is positive, we need to go through the set
		 * covering phase.
		 */

		if (mFeatureSelection.getSeparationLevel() > 0) {
			// Feature Selection
			mFeatureSelection.checkForExceptions();

			try {
				mFeatureSelection.fit(bData);
				mCutpoints.narrowDown(mFeatureSelection.getSelectedAttArray());
			} catch (OutOfMemoryError e) {
				ERROR = "\n" + LADFileManager.writeSection("Feature Selection: Out Of Memory Error");
				ERROR += " It was impossible to build the set covering model due "
						+ "the large number\n of cutpoints generated. Try "
						+ "increasing the cutpoint tolerance or\n allocating "
						+ "more memory to the JVM when starting WEKA.\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Rule Building
		this.mRuleGenerator.setMinimumPurity(mMinimumPurity);
		this.mRuleGenerator.checkForExceptions();

		try {
			this.mRuleGenerator.fit(bData);
		} catch (OutOfMemoryError e) {
			ERROR += (ERROR.length() > 0 ? "\n" : "");
			ERROR += "\n" + LADFileManager.writeSection("Rule Generation: Out Of Memory Error");
			ERROR += " It was impossible to build the whole set of rules due memory "
					+ "issues.\n All the rules that was possible to generate will be "
					+ "used for the\n classification problem.";
		} catch (Exception e) {
			// Just in case we change something and it goes wrong ;)
			e.printStackTrace();
		}

		// Setting Rules
		this.mRuleManager = new RuleManager(data, mRuleGenerator.getRules(), mCutpoints);
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
		return mRuleManager.distributionForInstance(instance);
	}

	/*
	 * -------------------------------------------------------------------------
	 * SETs & GETs
	 * -------------------------------------------------------------------------
	 */

	/** GET of Cut Points */
	public CutpointSet getCutpoints() {
		return mCutpoints;
	}

	/** GET of Rule Manager */
	public RuleManager getRuleManager() {
		return mRuleManager;
	}

	/*
	 * -------------------------------------------------------------------------
	 * DISPLAY SETs & GETs
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * OTHERS DISPLAY INFORMATIONS & TIP TEXTs
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * OPTION METHODS of CLASSIFIER
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * CAPACIDADES
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * INFORMACOES TECNICAS
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * PRINT FUNCTIONS
	 * -------------------------------------------------------------------------
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
	 * -------------------------------------------------------------------------
	 * MAIN
	 * -------------------------------------------------------------------------
	 */

	public static void main(String args[]) throws Exception {
		// Running the classifier from set options
		runClassifier(new LAD(), args);
	}

	@SuppressWarnings("unused")
	private static Instances loadData(String path) throws IOException {
		java.io.File file = null;
		javax.swing.JFileChooser fc = new javax.swing.JFileChooser();

		if (path.length() == 0) {
			fc.showOpenDialog(null);
			file = fc.getSelectedFile();
		} else {
			file = new java.io.File(path);
		}

		java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));

		weka.core.converters.ArffLoader.ArffReader arff;
		arff = new weka.core.converters.ArffLoader.ArffReader(reader, 1000);

		Instances data = arff.getStructure();
		data.setClassIndex(data.numAttributes() - 1);

		Instance instance;
		while ((instance = arff.readInstance(data)) != null)
			data.add(instance);

		return data;
	}
}