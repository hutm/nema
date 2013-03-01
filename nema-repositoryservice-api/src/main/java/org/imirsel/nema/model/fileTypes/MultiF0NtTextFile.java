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
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileTypeImpl; 
import org.imirsel.nema.model.util.PathAndTagCleaner;
import org.imirsel.nema.model.NemaNote;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;


/**
 * MultiF0 Note tracking  file type consists of onsets, offsets and F0 values.
 * 
 * @author mert.bay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.4.0
 */
public class MultiF0NtTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "MultiF0 Note-Tracking text file";
	
	public MultiF0NtTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		String[][] mf0NtStringsData = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);
		
		// Convert the data to a 2D double array
		int nrows = mf0NtStringsData.length;
		List<NemaNote> notes = new ArrayList<NemaNote>(nrows);
		
		double onset,offset;
		Double f0;
		for(int r = 0; r < nrows-1; r++) {
			onset = Double.parseDouble(mf0NtStringsData[r][0]);
			offset = Double.parseDouble(mf0NtStringsData[r][1]);
			f0 = Double.parseDouble(mf0NtStringsData[r][2]);
			notes.add(new NemaNote(onset, offset, f0));
		}
		Collections.sort(notes);
		
		NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		obj.setMetadata(NemaDataConstants.MULTI_F0_NT_NOTE_SEQUENCE, notes);
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		//TODO implement me
		BufferedWriter writer = null;
		try{
			List<NemaNote> notes = null;
			try{
				Object obj = data.getMetadata(NemaDataConstants.MULTI_F0_NT_NOTE_SEQUENCE);
				notes = (List<NemaNote>)obj;
			}catch(Exception e){
				throw new IllegalArgumentException("Failed to retrieve notes from: " + data.getId()); 
			}
			writer = new BufferedWriter(new FileWriter(theFile));
			
			NemaNote nemaNote;
			for (Iterator<NemaNote> it = notes.iterator(); it.hasNext();) {
				nemaNote = it.next();
				writer.write(nemaNote.getOnset() + WRITE_DELIMITER + nemaNote.getOffset() + WRITE_DELIMITER + nemaNote.getF0() + "\n");
			}
			getLogger().info(NemaDataConstants.MULTI_F0_NT_NOTE_SEQUENCE + " metadata for " + data.getId() + " written to file: " + theFile.getAbsolutePath());
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
