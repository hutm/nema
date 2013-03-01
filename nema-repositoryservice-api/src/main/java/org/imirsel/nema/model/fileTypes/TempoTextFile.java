package org.imirsel.nema.model.fileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Tempo estimation text file type.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class TempoTextFile extends SingleTrackEvalFileTypeImpl {
	
	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final DecimalFormat TEMPO_DEC = new DecimalFormat("0.000");
	
	public static final String TYPE_NAME = "Tempo estimation text file";
	
	/**
	 * Constructor
	 */
	public TempoTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public NemaData readFile(File theFile) throws IllegalArgumentException,
			FileNotFoundException, IOException {
		
		/* Read a space-delimited key text file as a 2D string array (should have just 1 row, 2 columns)*/
		String[][] tempoDataStrArray = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);

		/* Check that the text file is of proper format: <tempo1>\t<tempo2>\t<salience>\n<EOF> */
		if (tempoDataStrArray.length != 1) {
			String msg = "This file has more than a single line! Format should be <tempo1>\t<tempo2>\t<salience>\n<EOF>";
			throw new IllegalArgumentException(msg);
		}
		
		if (!(tempoDataStrArray[0].length == 3 || tempoDataStrArray[0].length == 2)) {
			String msg = "This file could not be parsed into separate tempo1, tempo2, and tempo salience! " +
					"Format should be <tempo1>\t<tempo2>\t<salience>\n<EOF> OR <tempo1>\t<tempo2>\n<EOF>. Content: \n";
			for (int i = 0; i < tempoDataStrArray[0].length; i++) {
				msg += "'" + tempoDataStrArray[0][i] + "'";
				if (i<tempoDataStrArray[0].length-1){
					msg += ",";
				}		
			}
			throw new IllegalArgumentException(msg);
		}
		
		/* Fill the NemaData object with the proper data and return it*/
		double[] tempoData = new double[tempoDataStrArray[0].length];
		for (int i =0; i< tempoData.length; i++) {
			if(tempoDataStrArray[0][i].equalsIgnoreCase("nan")){
				tempoDataStrArray[0][i]= "NaN";
			}
			tempoData[i] = Double.valueOf(tempoDataStrArray[0][i]);
		}
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_DATA, tempoData);
		return obj;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		/* Pull out musical tempo data from NemaData object */
		double[] tempoData = data.getDoubleArrayMetadata(NemaDataConstants.TEMPO_EXTRACTION_DATA);
		
		/* Construct output 2d String array for writing */
		String[][] tempoDataStrArray = new String[1][tempoData.length];
		try{		
			tempoDataStrArray[0][0] = TEMPO_DEC.format(tempoData[0]);
			tempoDataStrArray[0][1] = TEMPO_DEC.format(tempoData[1]);
			if (tempoData.length == 3) {
				tempoDataStrArray[0][2] = TEMPO_DEC.format(tempoData[2]);
			}
		}catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Track " + data.getId() + " should have a double[2] or double[3] array for metadata" +
					" type '" + NemaDataConstants.TEMPO_EXTRACTION_DATA + "', number of columns is wrong" ,e);
		}
		
		/* Write out the file */
		DeliminatedTextFileUtilities.writeStringDataToDelimTextFile(theFile, WRITE_DELIMITER, tempoDataStrArray);

	}

}
