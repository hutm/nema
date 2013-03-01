package org.imirsel.nema.model;



/**
 * Model representing a single segment.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class NemaSegment implements Comparable<NemaSegment>{
	
	protected double onset;
	protected double offset;
	protected String label;
	
	public NemaSegment() {
		onset = -1;
		offset = -1;
		label = null;
	}
	
	public NemaSegment(double onset, double offset, String label){
		this.onset = onset;
		if(this.onset < 0){
			this.onset = 0;
		}
		this.offset = offset;
		if(this.offset < 0){
			this.offset = 0;
		}
		this.label = label.replaceAll("\"", "");
	}
	
	public NemaSegment(double onset, double offset){
		this.onset = onset;
		if(this.onset < 0){
			this.onset = 0;
		}
		this.offset = offset;
		if(this.offset < 0){
			this.offset = 0;
		}
		this.label = null;
	}
	

	@Override
	public String toString() {
		return onset + "\t" + offset + "\t" + label;
	}
	
	public static NemaSegment fromString(String segmentString){
		String[] comps = segmentString.split("\\s+");
		if (comps.length < 3){
			throw new IllegalArgumentException("Couldn't parse NemaSegment from String: " + segmentString);
		}
		double onset = Double.valueOf(comps[0]);
		double offset = Double.valueOf(comps[1]);
		String label = comps[2];
		if (label == null){
			throw new IllegalArgumentException("Couldn't parse NemaSegment from String: " + segmentString);
		}
		return new NemaSegment(onset,offset,label);
	}

	public double getOnset() {
		return onset;
	}

	public void setOnset(double onset) {
		this.onset = onset;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label.replaceAll("\"", "");
	}
	
	public int compareTo(NemaSegment o) {
		double cmp = this.offset - o.offset;
		if (cmp > 0){
			return 1;
		}
		if(cmp < 0){
			return -1;
		}
		return 0;
	}
}
