package ie.gmit.sw.fuzzy;

import java.io.File;

import net.sourceforge.jFuzzyLogic.FIS;

public class FuzzyLogic {

	private File fileName;
	
	public FuzzyLogic(File fileName) {
		
		this.fileName = fileName;
		
		
	}
	
	
	public int getFuzzyHerustic(int title, int headings, int body) {
		// Load from 'FCL' file

		FIS fis = FIS.load(fileName.getAbsolutePath(), true);

		// Error while loading?
		if (fis == null) {
			System.err.println("Can't load file: '" + fileName + "'");
			return 0;
		}

		// Set inputs
		fis.setVariable("title", title);
		fis.setVariable("headings", headings);
		fis.setVariable("body", body);

		// Evaluate
		fis.evaluate();

		int fuzzyScore = (int) fis.getVariable("score").getValue();

		return fuzzyScore;
	}
}
