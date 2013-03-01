package org.imirsel.nema.model.fileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;


/**
 * Melody (single F0) text file type.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 *
 */
public class MelodyTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final DecimalFormat TIMESTAMP_DEC = new DecimalFormat("0.0000");
	public static final DecimalFormat F0_DEC = new DecimalFormat("0.00");
	public static final String TYPE_NAME = "Melody (single F0) text file";
	
	/**
	 * Constructor
	 */
	public MelodyTextFile() {
		super(TYPE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {

		/* Read a space-delimited melody text file as a 2D string array */
		String[][] melodyDataStrArray = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);

		/* Convert the 2D string data to a 2D double array */
		int nrows = melodyDataStrArray.length;
		int ncols = 2;
		double[][] melodyDataRaw = new double[nrows][ncols];
		for(int r = 0; r < nrows; r++) {
			try{
				for(int c = 0; c < ncols; c++) {
					melodyDataRaw[r][c] = Double.valueOf(melodyDataStrArray[r][c]);
				}
			}catch(Exception e){
				String msg = "Failed to parse line " + r + " of file " + theFile.getAbsolutePath() + "\n" +
				"Content: \n";
				for (int i = 0; i < melodyDataStrArray[r].length; i++) {
					msg += "'" + melodyDataStrArray[r][i] + "'";
					if (i<melodyDataStrArray[r].length-1){
						msg += ",";
					}
					
				}
				msg += "\n";
				throw new IllegalArgumentException(msg,e);
			}
		}
		
		/* 
		 * Set up the 0th-order interpolation to convert to the 
		 * MIREX-spec 10ms time-grid 
		 */
		List<Double> melodyInterpTimeStamp = new ArrayList<Double>();
		List<Double> melodyInterpF0 = new ArrayList<Double>();
		melodyInterpF0.add(0, new Double(0.0));
    	melodyInterpTimeStamp.add(0, new Double(0.0));
		
    	/* Indices into the new, interpolated data array-list */
        int index = 0;
        int oldindex = 0;

        /*
         *  minDiff and currDiff represent time-stamp differences to make 
         *  sure the f0 value we use in the original data is the one 
         *  closest-in-time to the MIREX-spec desired time-stamp
         */
        double minDiff = 10000000.0;
        
        double currDiff = 0.0;
        
        /* Loop through original arbitrary time-stamped data */
        for (int i = 0; i < nrows; i++) {
            index = (int)Math.round(melodyDataRaw[i][0]/NemaDataConstants.MELODY_TIME_INC);
            
            /* Case where the file's time-step is less than 10ms */
            if (index == oldindex) {
                currDiff = Math.abs(melodyDataRaw[i][0] - NemaDataConstants.MELODY_TIME_INC*(double)index);
                if (currDiff < minDiff) {	
                	melodyInterpF0.set(index, new Double(melodyDataRaw[i][1]));
                	melodyInterpTimeStamp.set(index, new Double(NemaDataConstants.MELODY_TIME_INC*(double)index));
                    minDiff = currDiff;
                }
            }
            
         	/*
         	 *  Case where the file's time-step is 10ms or has 'caught up' if 
         	 *  less than 10ms and gone on to the next index in the 10ms grid
         	 */
            else if (index == oldindex + 1) {
            	melodyInterpF0.add(new Double(melodyDataRaw[i][1]));
            	melodyInterpTimeStamp.add(new Double(NemaDataConstants.MELODY_TIME_INC*(double)index));
                minDiff = Math.abs(melodyDataRaw[i][0] - NemaDataConstants.MELODY_TIME_INC*(double)index);
            }
            
            /* 
             * Case where the file's time-step is greater than 10ms, and
             * the sample-hold takes place, i.e. repeat f0's multiple times
             */
            else if (index > oldindex + 1) {
                int indDiff = index - oldindex;
                for (int j = 0; j < indDiff-1; j++) {
                	melodyInterpF0.add(melodyInterpF0.get(oldindex));
                	melodyInterpTimeStamp.add(new Double(NemaDataConstants.MELODY_TIME_INC*(double)oldindex + (double)(j+1)*NemaDataConstants.MELODY_TIME_INC));
                }
                melodyInterpF0.add(new Double(melodyDataRaw[i][1]));
                melodyInterpTimeStamp.add(new Double(NemaDataConstants.MELODY_TIME_INC*(double)index));
                minDiff = Math.abs(melodyDataRaw[i][0] - NemaDataConstants.MELODY_TIME_INC*(double)index);
            }
            oldindex = index;                                
        }   

        /*
         *  Put the contents of the Time-stamp and F0 array-lists into a 
         *  single 2 column 2d-double array 
         */
        double[][] melodyDataInterpolated = new double[melodyInterpF0.size()][2];
        for (int i = 0; i < melodyDataInterpolated.length; i++) {
        	melodyDataInterpolated[i][0] = (melodyInterpTimeStamp.get(i)).doubleValue();
        	melodyDataInterpolated[i][1] = (melodyInterpF0.get(i)).doubleValue();
        }
		
		/* Form the NemaData Object for this file and return it */
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.MELODY_EXTRACTION_DATA, melodyDataInterpolated);
		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		double[][] melodyData = data.get2dDoubleArrayMetadata(NemaDataConstants.MELODY_EXTRACTION_DATA);
		
		/* Convert the data to a 2D double array */
		int nrows = melodyData.length;

		String[][] melodyDataStrArray = new String[nrows][2];
		try{
			for(int r = 0; r < nrows; r++) {		
				melodyDataStrArray[r][0] = TIMESTAMP_DEC.format(melodyData[r][0]);
				melodyDataStrArray[r][1] = F0_DEC.format(melodyData[r][1]);
			}
		}catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Track " + data.getId() + " should have a double[N][2] array for metadata" +
					" type '" + NemaDataConstants.MELODY_EXTRACTION_DATA + "', number of columns is wrong" ,e);
		}
		DeliminatedTextFileUtilities.writeStringDataToDelimTextFile(theFile, WRITE_DELIMITER, melodyDataStrArray);
	}

}
