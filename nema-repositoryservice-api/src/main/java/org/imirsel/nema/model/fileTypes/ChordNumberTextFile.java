/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model.fileTypes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imirsel.nema.model.NemaChord;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.ChordConversionUtil;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;


/**
 * Chord file type where chords are specified in the number format.
 * 
 * @author mert.bay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class ChordNumberTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "Chord Number text file";
	
	public ChordNumberTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		String[][] chordStringsData = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);
		
		// Convert the data to a 2D double array
		int nrows = chordStringsData.length;
		List<NemaChord> chords = new ArrayList<NemaChord>(nrows);
		
		double onset,offset;
		int[] notes;
		for(int r = 0; r < nrows-1; r++) {
			onset = Double.parseDouble(chordStringsData[r][0]);
			offset = Double.parseDouble(chordStringsData[r][1]);
			try{
				notes = ChordConversionUtil.getInstance().convertChordNumbersToNoteNumbers(chordStringsData[r][2]);
			}catch(IllegalArgumentException e){
				Logger.getLogger(ChordShortHandTextFile.class.getName()).log(Level.SEVERE, "Failed to convert chord format in file: " + theFile.getAbsolutePath(), e);
				throw e;
			}
			chords.add(new NemaChord(onset, offset, notes));
		}
		Collections.sort(chords);
		
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE, chords);
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		BufferedWriter writer = null;
		try{
			List<NemaChord> chords = null;
			try{
				Object obj = data.getMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE);
				chords = (List<NemaChord>)obj;
			}catch(Exception e){
				throw new IllegalArgumentException("Failed to retrieve chords from: " + data.getId()); 
			}
			writer = new BufferedWriter(new FileWriter(theFile));
			
			NemaChord nemaChord;
			for (Iterator<NemaChord> it = chords.iterator(); it.hasNext();) {
				nemaChord = it.next();
				writer.write(nemaChord.getOnset() + WRITE_DELIMITER + nemaChord.getOffset() + WRITE_DELIMITER + ChordConversionUtil.getInstance().convertNotenumbersToChordnumbers(nemaChord.getNotes()) + "\n");
			}
			getLogger().info(NemaDataConstants.CHORD_LABEL_SEQUENCE + " metadata for " + data.getId() + " written to file: " + theFile.getAbsolutePath());
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
			}
		}
	}    
}
