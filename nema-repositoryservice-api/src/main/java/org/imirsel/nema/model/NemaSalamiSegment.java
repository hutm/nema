package org.imirsel.nema.model;

public class NemaSalamiSegment extends NemaSegment {

	String function_label;
	
	public NemaSalamiSegment(double onset, double offset, String label, String functionLabel){
		super(onset, offset, label);
		if(onset == -1){
			throw new RuntimeException("Received uninitialized onset!");
		}
		if(offset == -1){
			throw new RuntimeException("Received uninitialized onset!");
		}
		this.function_label = functionLabel;
		if(functionLabel != null){
			this.function_label = functionLabel.replaceAll("\"", "");
		}
		
	}
	
	@Override
	public String toString() {
		return onset + "\t" + offset + "\t" + label;
	}
	
	public static NemaSegment fromString(String segmentString){
		throw new UnsupportedOperationException("Use SalamiStructureTextFile to parse as segments are not fully encoded on a line (no offset)");
	}
	
	public void setFunctionLabel(String functionLabel) {
		this.function_label = functionLabel;
		this.function_label.replaceAll("\"", "");
	}
	
}
