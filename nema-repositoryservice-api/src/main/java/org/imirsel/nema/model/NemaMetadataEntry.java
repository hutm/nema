/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;

/**
 * A class representing a typed metadata value.
 * 
 * @author kriswest
 */
public class NemaMetadataEntry implements Serializable{
    private static final long serialVersionUID = 1L;

    private String type;
    private String value;

    /**
     * Constructor. Both a string specifying the type of the value and the value itself must be passed.
     * 
     * @param type The type of the value.
     * @param value The value.
     */
    public NemaMetadataEntry(String type, String value){
        this.type = type;
        this.value = value;
    }

    /**
     * @return the type String.
     */
    public String getType(){
        return type;
    }

    /**
     * @param type the type String to set.
     * @param value the value String to set.
     * @param value 
     */
    public void setType(String type, String value){
        this.type = type;
        this.value = value;
    }

    /**
     * @return the value String.
     */
    public String getValue(){
        return value;
    }

    /**
     * @param value the value String to set.
     */
    public void setValue(String value){
        this.value = value;
    }

    @Override
    /**
     * Hashcodes are based on the the calculation of a String hashcode for the combination of the
     * type and value Strings.
     * @return the HashCode.
     */
    public int hashCode(){
        return (type + "--" + value).hashCode();
    }

    @Override
    /**
     * Returns true if the other Object is an instance of <code>NemaMetadataEntry</code> and carries
     * identical type and value Strings. Note this comparison is case-sensitive.
     * @param object the Object to compare to.
     * @return a boolean indicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaMetadataEntry)){
            return false;
        }
        NemaMetadataEntry other = (NemaMetadataEntry)object;
        if (type.equals(other.type) && value.equals(other.value)){
            return true;
        }
        return false;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaMetadataEntry [type=" + type + ", value=" + value + "]";
	}
    
    

}
