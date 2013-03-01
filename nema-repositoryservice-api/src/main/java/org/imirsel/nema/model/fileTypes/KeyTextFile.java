package org.imirsel.nema.model.fileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Key text file type.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 *
 */
public class KeyTextFile extends SingleTrackEvalFileTypeImpl {
	
	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final String TYPE_NAME = "Key text file";
	
	/**
	 * Constructor
	 */
	public KeyTextFile() {
		super(TYPE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NemaData readFile(File theFile) throws IllegalArgumentException,
			FileNotFoundException, IOException {
		
		/* Read a space-delimited key text file as a 2D string array (should have just 1 row, 2 columns)*/
		String[][] keyDataStrArray = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);

		/* Check that the text file is of proper format: <tonic>\t<mode>\n<EOF> */
		if (keyDataStrArray.length != 1) {
			String msg = "This file has more than a single line! Format should be <tonic>\t<mode>\n<EOF>";
			throw new IllegalArgumentException(msg);
		}
		
		if (keyDataStrArray[0].length != 2) {
			String msg = "This file could not be parsed into separate tonic and mode fields! " +
					"Format should be <tonic>\t<mode>\n<EOF>. Content: \n";
			for (int i = 0; i < keyDataStrArray[0].length; i++) {
				msg += "'" + keyDataStrArray[0][i] + "'";
				if (i<keyDataStrArray[0].length-1){
					msg += ",";
				}		
			}
			throw new IllegalArgumentException(msg);
		}
		
		/* Fill the NemaData object with the proper data and return it*/
		String[] keyData = keyDataStrArray[0];
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.KEY_DETECTION_DATA, keyData);
		return obj;
	}

	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {		

		/* Pull out musical key data from NemaData object */
		String[] keyData = data.getStringArrayMetadata(NemaDataConstants.KEY_DETECTION_DATA);
		
		/* Construct output 2d String array for writing */
		String[][] keyDataStrArray = new String[1][2];
		try{		
			keyDataStrArray[0][0] = keyData[0];
			keyDataStrArray[0][1] = keyData[1];
		}catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Track " + data.getId() + " should have a String[1][2] array for metadata" +
					" type '" + NemaDataConstants.KEY_DETECTION_DATA + "', number of columns is wrong" ,e);
		}

		/* Write out the file */
		DeliminatedTextFileUtilities.writeStringDataToDelimTextFile(theFile, WRITE_DELIMITER, keyDataStrArray);

	}

}
