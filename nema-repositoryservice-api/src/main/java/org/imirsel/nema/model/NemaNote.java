package org.imirsel.nema.model;

import java.util.Arrays;

import org.imirsel.nema.model.NemaSegment;

/**
 * Model representing a single Note.
 * 
 * @author mert.bay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class NemaNote extends NemaSegment{
	
	double onset;
	double offset;
	Double f0;
	
	public NemaNote() {
		super();
		f0 = null;
	}
	
	public NemaNote(double onset, double offset, final double fZero){
		super(onset,offset);
		this.f0 = fZero;
	}
	


	@Override
	public String toString() {
		return onset + "\t" + offset + "\t" + f0.toString();
	}
	
	public static NemaNote fromString(String noteString){
		String[] comps = noteString.split("\\s+");
		if (comps.length < 3){
			throw new IllegalArgumentException("Couldn't parse NemaNote from String: " + noteString);
		}
		double onset = Double.valueOf(comps[0]);
		double offset = Double.valueOf(comps[1]);
		Double f0 = Double.valueOf(comps[2]);
		if (f0 == null){
			throw new IllegalArgumentException("Couldn't parse NemaNote from String: " + noteString);
		}
		return new NemaNote(onset,offset,f0);
	}


	
	/**
	 * Compare notes. If the otherNote's onset value is within the onsetThreshold(default 50ms) of the this.onset's value and offset value
	 * is within the offfsetThreshold (default 20% of this note's length or 50ms whichever is smaller )
	 * and  the f0 value is within a semi-tone range of the this.f0's value  then they are equal.
	 * @return flag indicating equality
	 */
	public boolean isEqualOnsetOffset(NemaNote otherNote,double onsetThreshold, double  offsetThreshold, double f0ThresholdLower, double f0ThresholdHigher){
		
		if (!(Math.abs(this.onset-otherNote.getF0()) < onsetThreshold) )
			return false;
		if (!(Math.abs(this.offset-otherNote.getOffset()) < offsetThreshold ))
			return false;
		if ( (this.f0/otherNote.getF0() > f0ThresholdLower) && (this.f0/otherNote.getF0() < f0ThresholdHigher)  );
		return true;
	}
	

	public double getF0() {
		return f0;
	}

	public void setF0(double fZero) {
		this.f0 = fZero;
	}
	
	public double getOnset() {
		return onset;
	}
	
	public double getOffset(){
		return offset;
	}
	
}
