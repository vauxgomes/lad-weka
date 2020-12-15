package weka.classifiers.rules.lad.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;

import weka.classifiers.rules.LAD;
import weka.classifiers.rules.lad.binarization.CutpointSet;
import weka.classifiers.rules.lad.rulegeneration.RuleManager;
import weka.core.Utils;

/**
 * Class LADFileManager
 * 
 * @author Vaux Gomes
 * @author Tiberius Bonates
 * 
 * @since Mar 27, 2014
 * @version 1.0
 */
public class LADFileManager implements Serializable {

	/* SERIAL ID */
	private static final long serialVersionUID = 940441734407639248L;

	/* Variables */
	private String pathname = new JFileChooser().getFileSystemView()
			.getDefaultDirectory().toString();
	private boolean writable;
	private BufferedWriter writer;

	/* Auxiliary */
	private static final String newline = System.getProperty("line.separator");

	/** Main Constructor */
	public LADFileManager(String name, boolean addtime) {
		pathname += File.separator + "LAD " + name + " "
				+ (addtime ? getTime() : "") + ".txt";

		this.setFile(new File(pathname));
	}

	/** Method to write on the file */
	public void write(String txt) {
		if (isWritable()) {
			try {
				writer.append(txt + newline);
			} catch (IOException e) {
				e.printStackTrace();
				setFile(null);
			}
		}
	}

	/** SET file */
	public void setFile(File file) {
		if (file != null && !file.getPath().isEmpty() && !file.isDirectory()) {
			try {
				this.writer = new BufferedWriter(new FileWriter(file, true));
				this.writable = true;
			} catch (IOException e) {
				e.printStackTrace();
				this.setFile(null);
			}
		} else {
			this.writer = null;
			this.writable = false;
		}
	}

	/** GET Writable flag */
	public boolean isWritable() {
		return writable;
	}

	/** Start using the file again */
	public void restore() {
		setFile(new File(pathname));
	}

	/** Method to close the file writer */
	public void close() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				this.setFile(null);
			}
		}
	}

	/** Method for writing the classifier easily */
	public void write(LAD lad, String dataName) {
		if (isWritable()) {
			// Header
			write(writeHeader("LAD-WEKA pattern-space representation"));

			String s = writeSection("Run Informations");
			s += " Scheme:" + TAB(3) + LAD.class.getName() + " ";
			s += Utils.joinOptions(lad.getOptions());

			write(s);

			write(" Training Set:" + TAB(2) + dataName);
			write(" Generation Date: " + TAB(1) + getTime() + "\n");

			// Cutpoints
			lad.getCutpoints().sort();
			write(write(lad.getCutpoints(), true));

			// Rules
			write(write(lad.getRuleManager()));

//			// Patter-space section
//			write(writeHeader("Pattern-space representation of test set"));
//
//			// Printing ARFF model
//			write("@RELATION " + dataName + "_PS\n");
//
//			int numRules = lad.getRuleManager().numPositiveRules();
//			for (int i = 0; i < numRules; i++)
//				write("@ATTRIBUTE P" + (i + 1) + " NUMERIC");
//
//			numRules = lad.getRuleManager().numNegativeRules();
//			for (int i = 0; i < numRules; i++)
//				write("@ATTRIBUTE N" + (i + 1) + " NUMERIC");
//
//			write("@ATTRIBUTE class {0,1}");
//			write("\n@DATA");
		}
	}

	/*
	 * --------------------------------------------------------------------
	 * STATIC METHODS
	 * --------------------------------------------------------------------
	 */

	/** Method for writing the cutpoints easily */
	public static String write(CutpointSet cutpoints) {
		return write(cutpoints, false);
	}

	/** Method for writing the cutpoints easily */
	public static String write(CutpointSet cutpoints, boolean listAll) {
		if (cutpoints == null)
			return "";

		String s = "";
		if (listAll)
			s += writeSection("Cutpoints", cutpoints);
		else
			s += writeSection("Cutpoints");

		return s + " # Total: " + cutpoints.numCutpoints() + " cutpoints\n";
	}

	/** Method for writing the rule manager easily */
	public static String write(RuleManager ruleManager) {
		return writeSection("Summary of Patterns", ruleManager);
	}

	/** Method to simplify the way we generate headers for the PSF document */
	public static String writeHeader(String title) {
		return "=== " + title + " ===\n";
	}

	/** Method to simplify the way we generate titles for the PSF document */
	public static String writeSection(String title) {
		return " == " + title + " ===\n\n";
	}

	/** */
	public static String write(String title, String msg) {
		return writeSection(title) + msg;
	}

	/** Method to simplify the way we generate the titles for the PSF document */
	public static String writeSection(String title, Object o) {
		return writeSection(title) + (o != null ? o.toString() : "") + "\n";
	}

	/** Method to write new line elements */
	public static String TAB(int times) {
		String s = "";
		for (int i = 0; i < times; i++)
			s += "\t";

		return s;
	}

	/** GET the current day and hour */
	public static String getTime() {
		return new SimpleDateFormat("MM.dd.yyyy HH.mm.ss").format(new Date());
	}
}
