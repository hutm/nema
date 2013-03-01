package org.imirsel.nema.model;

import java.util.Arrays;

import org.imirsel.nema.model.util.ChordConversionUtil;
import org.imirsel.nema.model.NemaSegment;

/**
 * Model representing a single Chord.
 * 
 * @author mert.bay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class NemaChord extends NemaSegment{
	
	protected int[] notes;
	
	public NemaChord() {
		super();
		notes = null;
	}
	
	public NemaChord(double onset, double offset, final int[] notes){
		super(onset,offset);
		this.notes = notes;
	}
	
	public NemaChord(double onset, double offset, final int[] notes, String label){
		super(onset,offset);
		this.notes = notes;
		this.label = label;
	}
	
	/**
	 * Compute hashcode using only the notes (not onset/offest times).
	 * @return hashCode
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(notes);
	}

	@Override
	public String toString() {
		String out = onset + "\t" + offset + "\t";
		if (label == null){
			out += ChordConversionUtil.getInstance().convertNoteNumbersToShorthand(notes);;
		}else{
			out += label;
		}
		return out;
	}
	
	public static NemaChord fromString(String chordString){
		String[] comps = chordString.split("\\s+");
		if (comps.length < 3){
			throw new IllegalArgumentException("Couldn't parse NemaChord from String: " + chordString);
		}
		double onset = Double.valueOf(comps[0]);
		double offset = Double.valueOf(comps[1]);
		int[] notes = ChordConversionUtil.getInstance().convertShorthandToNotenumbers(comps[2]);
		if (notes == null){
			throw new IllegalArgumentException("Couldn't parse NemaChord from String: " + chordString);
		}
		return new NemaChord(onset,offset,notes);
	}

	/**
	 * Compare for equality using only the notes (not onset/offest times).
	 * @return flag inidcating equality
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NemaChord other = (NemaChord) obj;
		if (!Arrays.equals(notes, other.notes))
			return false;
		return true;
	}

	public int[] getNotes() {
		return notes;
	}

	public void setNotes(int[] notes) {
		this.notes = notes;
	}

	public String getLabel() {
		if (label == null){
			return ChordConversionUtil.getInstance().convertNoteNumbersToShorthand(notes);
		}else{
			return label;
		}
	}
	
	public void setLabel(String label){
		this.label = label; 
	}
}
