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
 * MultiF0Est  text file type.
 * 
 * @author mertbay@gmail.com
 * @since 0.4.0
 *
 */
public class MultiF0EstTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final DecimalFormat FORMAT_DEC = new DecimalFormat("0.0000");
	public static final String TYPE_NAME = "MultiF0 Estimation text file";
	
	/**
	 * Constructor
	 */
	public MultiF0EstTextFile() {
		super(TYPE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {

		/* Read a space-delimited multiF0 text file as a 2D string array */
		String[][] multiF0EstDataStrArray = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);
		int nrows = multiF0EstDataStrArray.length;
		int ncols = multiF0EstDataStrArray[0].length;

        /*
         *  Put the contents of the Time-stamp and multi-F0s into a 
         *  single variable column 2d-double array 
         */
		double[][] multiF0EstData = new double[nrows][];
		for(int r = 0; r < nrows; r++) {
			try{
				int ctr = 0;
				while ((ctr < ncols )&&(multiF0EstDataStrArray[r][ctr]!=null)){
					ctr++;
				}
				double[] tmpRow = new double[ctr];
				for(int c = 0; c < ctr; c++) {
					tmpRow[c] = Double.valueOf(multiF0EstDataStrArray[r][c]);
				}
				multiF0EstData[r]=tmpRow;				
			}catch(Exception e){
				String msg = "Failed to parse line " + r + " of file " + theFile.getAbsolutePath() + "\n" +
				"Content: \n";
				for (int i = 0; i < multiF0EstDataStrArray[r].length; i++) {
					msg += "'" + multiF0EstDataStrArray[r][i] + "'";
					if (i<multiF0EstDataStrArray[r].length-1){
						msg += ",";
					}					
				}
				msg += "\n";
				throw new IllegalArgumentException(msg,e);
			}
		}
		
		/* Form the NemaData Object for this file and return it */
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.MULTI_F0_EST_DATA, multiF0EstData);
		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		double[][] multiF0EstData = data.get2dDoubleArrayMetadata(NemaDataConstants.MULTI_F0_EST_DATA);
		
		/* Convert the data to a 2D double array */
		int nrows = multiF0EstData.length;

		String[][] multiF0EstDataStrArray = new String[nrows][];
		try{
			for(int r = 0; r < nrows; r++) {						
				int ncols = multiF0EstData[r].length;
				String[] row = new String[ncols];
				for (int c = 0; c < ncols; c++){
					row[c] = FORMAT_DEC.format(multiF0EstData[r][c]);
				}
				multiF0EstDataStrArray[r] = row;
			}
		}catch(Exception e){
			throw new IllegalArgumentException("Failed to write F0s for Track " + data.getId()  +
					" type '" + NemaDataConstants.MULTI_F0_EST_DATA + "'," ,e);
		}
		DeliminatedTextFileUtilities.writeStringDataToDelimTextFile(theFile, WRITE_DELIMITER, multiF0EstDataStrArray);
	}

}
