package org.imirsel.nema.model;

import java.io.Serializable;

/**
 * Class representing a collection of tracks in the NEMA repository. A collection is merely an 
 * organisational unit, although permissions may be applied at the collection level at some point.
 * 
 * @author kris.west@gmail.com
 */
public class NemaCollection implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String description;

    /**
     * Constructor. Sets the ID but not the name or description.
     * @param id The ID to create the Object with.
     */
    public NemaCollection(int id){
        this.id = id;
    }

    /**
     * Constructor. Sets all variables (id, name, description).
     * @param id The ID to create the Object with.
     * @param name The name to create the Object with.
     * @param description The description to create the Object with.
     */
    public NemaCollection(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Return the ID of the collection.
     * @return the ID of the collection.
     */
    public int getId(){
        return id;
    }

    /**
     * Set the ID of the collection.
     * @param id The ID to set.
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Return the colleciton name.
     * @return the collection name.
     */
    public String getName(){
        return name;
    }

    /**
     * Set the collection name.
     * @param name The collection name to set.
     */
    public void setName(String name){
        this.name = name;
    }

    /** 
     * Return the collection description.
     * @return the collection description.
     */
    public String getDescription(){
        return description;
    }

    /** 
     * Set the collection description.
     * @param description the collection description to set.
     */
    public void setDescription(String description){
        this.description = description;
    }

    @Override
    /**
     * Hashcodes are purely based on the integer collection ID.
     * @return The hashcode.
     */
    public int hashCode(){
        return id;
    }

    @Override
    /**
     * Return true if the other Object is an instance of <code>NemaCollection</code> with the same ID,
     * false otherwise.
     * @param object The Object to compare to.
     * @return a boolean indeicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaCollection)){
            return false;
        }
        NemaCollection other = (NemaCollection)object;
        if (id == other.id){
            return true;
        }
        return false;
    }

	@Override
	public String toString() {
		return "NemaCollection [description=" + description + ", id=" + id
				+ ", name=" + name + "]";
	}

    

}
