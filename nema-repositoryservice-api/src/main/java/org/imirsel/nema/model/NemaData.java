/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * A multi-purpose data-structure so be used to store, index and retrieve data and metadata
 * about tracks and evaluations. 
 * 
 * Note: in the current implementation string constants are declared in this class for representing
 * different items of data. The value of these constants is likely to change in future, which will 
 * break files written to disk at that time. Further no typing is provided at present and hence,
 * retrieval methods currently require the user to know the type of the data they are retrieving
 * or setting and therefore to not mix types for the same key. 
 * 
 * @author kris.west@gmail.com
 */
public class NemaData implements Serializable, Comparable<NemaData>{

    public static final long serialVersionUID = -1234567894463456789L;
    
    /**
     * The metadata hashmap.
     */
    private HashMap<String,Object> metadata;
    
    /** Creates a new instance of NemaData */
    public NemaData() {
        metadata = new HashMap<String,Object>();
    }
    
    /** Creates a new instance of NemaData with the file location as metadata
     *  @param fileLocation Original location of evaluation file, used as an identifier
     */
    public NemaData(String id) {
        metadata = new HashMap<String,Object>();
        metadata.put(NemaDataConstants.PROP_ID, id);
    }
    
    /**
     * Creates a new instance of NemaData which is a shallow copy of 
     * the NemaData passed as a parameter
     * @param oldObj The NemaData to copy
     */
    public NemaData(NemaData oldObj) {
        //copy metadata
        metadata = new HashMap<String,Object>();
        Set<String> keys = oldObj.metadata.keySet();
        String[] keysArray = keys.toArray(new String[keys.size()]);
        for (int i=0;i<keysArray.length;i++) {
            metadata.put(new String(keysArray[i]), oldObj.metadata.get(keysArray[i]));
        }
        //this is only copying/cloning keys and just mapping to original value instance
    }
    
    /**
     * Returns a flag indicating whether there is an entry in the metadata map
     * for the specified key.
     * @param key The metadata key to check.
     * @return a flag indicating whether there is an entry in the metadata map
     * for the specified key.
     */
    public boolean hasMetadata(String key){
        return metadata.containsKey(key);
    }
    
    /**
     *  Compares two NemaData Objects for equality based on their 
     *  filelocation metadata.
     *  @param otherObj The NemaData to compare this Object with.
     *  @return A boolean indicating equality.
     */
    public boolean equals(Object otherObj) {
        try {
            return this.getStringMetadata(NemaDataConstants.PROP_ID).equals(((NemaData)otherObj).getStringMetadata(NemaDataConstants.PROP_ID));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unable to compare NemaData Objects with filelocation metadata",ex);
        }
    }
    
    /**
     * Clones the NemaData object including the metadata HashMap keys. However, values are not cloned.
     * 
     * @return A clone of this NemaData object.
     */
    public Object clone() throws java.lang.CloneNotSupportedException {
        return (new NemaData(this));
    }
    
    /** Returns a File Object for the path specified by the file location metadata.
     *  @return a File Object for the path specified by the file location metadata.
     */
    public String getId() throws IllegalArgumentException {
        return this.getStringMetadata(NemaDataConstants.PROP_ID);
    }
    
    /**
     * Adds the <code>value</code> to the metadata <code>HashMap</code> with 
     * the specified <code>key</code>. If there is already metadata 
     * corresponding to the supplied key it is replaced. Primitive datatypes 
     * must be wrapped in Object based datatypes, e.g. int is wrapped
     * in the Integer class, double is wrapped in the Double class.
     * @param key The key to be added to the <code>metadata</code> 
     * <code>HashMap</code>.
     * @param value The value to be added to the <code>metadata</code>
     * <code>HashMap</code>.
     * @throws IllegalArgumentException Thrown if either key and value are null.
     */
    public void setMetadata(String key, Object value) throws IllegalArgumentException {
    	if(key == null)
        {
        	throw new IllegalArgumentException("The key must not be null!");
        }
    	if(value == null)
        {
        	throw new IllegalArgumentException("The value must not be null! key=" + key);
        }
        metadata.put(key, value);
    }
    
    /**
     * Lists all metadata keys for this NemaData Object.
     * @return An array of Strings representing the available metadata.
     */
    public String[] metadataKeys() {
        return (String[])metadata.keySet().toArray(new String[metadata.size()]);
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key.
     * @param key The key to return the value for.
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The value corresponding to the supplied key.
     */
    public Object getMetadata(String key) throws IllegalArgumentException {
        if (metadata.containsKey(key) == false) {
            Object[] keys = metadata.keySet().toArray();
            String keyString = "";
            for (int i=0;i<keys.length;i++) {
                keyString += (String)keys[i] + "\n";
            }
            throw new IllegalArgumentException("There is no metadata corresponding to the supplied key for NemaData id: " + this.getId() + "!\n" +
                    "Key supplied = " + key + "\n" +
                    "Keys available:\n" +
                    keyString);
        }
        return metadata.get(key);
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as an Integer and returns the int value.
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The integer value corresponding to the supplied key
     */
    public int getIntMetadata(String key) throws IllegalArgumentException {
        return ((Integer)this.getMetadata(key)).intValue();
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as an int[].
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The int[] corresponding to the supplied key
     */
    public int[] getIntArrayMetadata(String key) throws IllegalArgumentException {
        return ((int[])this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as an int[][].
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The int[][] corresponding to the supplied key
     */
    public int[][] get2dIntArrayMetadata(String key) throws IllegalArgumentException {
        return ((int[][])this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a String
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The String value corresponding to the supplied key
     */
    public String getStringMetadata(String key) throws IllegalArgumentException {
        return ((String)this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a String[]
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The String[] of values corresponding to the supplied key
     */
    public String[] getStringArrayMetadata(String key) throws IllegalArgumentException {
        return ((String[])this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a String[][]
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The String[][] of values corresponding to the supplied key
     */
    public String[][] get2dStringArrayMetadata(String key) throws IllegalArgumentException {
        return ((String[][])this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a Double and returns the double value
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The double value corresponding to the supplied key
     */
    public double getDoubleMetadata(String key) throws IllegalArgumentException {
        return ((Double)this.getMetadata(key)).doubleValue();
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a double[]
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The double[] of values corresponding to the supplied key
     */
    public double[] getDoubleArrayMetadata(String key) throws IllegalArgumentException {
        return ((double[])this.getMetadata(key));
    }
    
    /**
     * Returns the metadata value corresponding to the supplied key and casts it
     * as a double[][]
     * @param key The key to return the value for
     * @throws IllegalArgumentException Thrown if the key does not exist.
     * @return The double[][] of values corresponding to the supplied key
     */
    public double[][] get2dDoubleArrayMetadata(String key) throws IllegalArgumentException {
        return ((double[][])this.getMetadata(key));
    }
    
    /**
     * Reads a NemaData Object from an ASCII file in the format 
     * produced by the <code>write</code> method.
     * @param theFile The File object to load the NemaData from.
     * @throws java.io.IOException Thrown if an IOException occurs.
     * @throws java.lang.ClassNotFoundException Thrown if an attempt load an 
     * unknown class is made.
     * @return The loaded NemaData.
     */
    public static NemaData read(File theFile) throws java.io.IOException, ClassNotFoundException, IllegalArgumentException {
        //Check readLine() behaviour is valid... could be more robust?
        NemaData dataObject = new NemaData();
        
        if (!theFile.exists()) {
            throw new FileNotFoundException("NemaData.read(): The specified file does not exist!\n File: " + theFile.getPath());
        }
        if (theFile.isDirectory()) {
            throw new IllegalArgumentException("NemaData.read(): The specified file is a directory and therefore cannot be read!\n Path: " + theFile.getPath());
        }
        if (!theFile.canRead()) {
            throw new IllegalArgumentException("NemaData.read(): The specified file exists but cannot be read!\n File: " + theFile.getPath());
        }
        
        BufferedReader textBuffer;
        try {
            textBuffer = new BufferedReader( new FileReader(theFile) );
        } catch(java.io.FileNotFoundException fnfe) {
            throw new IllegalArgumentException("NemaData.read(): The specified file does not exist, this exception should never be thrown and indicates a serious bug.\n File: " + theFile.getPath());
        }
        String line = null;
        try {
            //check headers
            line = textBuffer.readLine();
            if (!line.equals(NemaDataConstants.fileHeader)) {
                System.out.println("WARNING: NemaData.read(): Doesn't match the current format specification\nFile: " + theFile.getPath() + "\nCurrent spec: " + NemaDataConstants.fileHeader + "\nFile spec: " + line);
            }
            line = textBuffer.readLine();
            if (!line.equals(NemaDataConstants.DIVIDER)) {
                throw new IllegalArgumentException("NemaData.read(): The file being read is not in the correct format!\n File: " + theFile.getPath());
            }
            
//            String[] theColumnLabels = null;
            
            //read metadata
            line = textBuffer.readLine();
            if (!line.equals("null")) {
                //read metadata
                while(!line.equals(NemaDataConstants.DIVIDER)) {//Format: key className length (data1 + SEPARATOR ... datalength)
                    String[] comps = line.split(NemaDataConstants.SEPARATOR);
                    if (comps[1].equals("null")){
                        //ignore line
                    } else if (comps[2].equals("-1")) {
                        if (comps.length > 3){
                            if (comps[1].equals("java.lang.Integer")) {
                                dataObject.setMetadata(comps[0], new Integer(comps[3]));
                            } else if (comps[1].equals("java.lang.String")) {
                                dataObject.setMetadata(comps[0], comps[3]);
                            } else if (comps[1].equals("java.lang.Double")) {
                                dataObject.setMetadata(comps[0], new Double(comps[3]));
                            }
                        }
                    } else {
                        if (comps.length != (3 + Integer.parseInt(comps[2]))) {
                            throw new IllegalArgumentException("NemaData.read(): The file being read is not in the correct format (wrong number of items in metadata array)!\n File: " + theFile.getPath() + "\nLine: " + line);
                        }
                        //init array
                        Object anArray = null;
                        if (comps[1].equals("int")) {
                            anArray = java.lang.reflect.Array.newInstance(java.lang.Integer.TYPE,Integer.parseInt(comps[2]));
                        } else if(comps[1].equals("double")) {
                            anArray = java.lang.reflect.Array.newInstance(java.lang.Double.TYPE,Integer.parseInt(comps[2]));
                        } else {
                            anArray = java.lang.reflect.Array.newInstance(Class.forName(comps[1]),Integer.parseInt(comps[2]));
                        }
                        
                        //populate array
                        for (int m=0; m<Integer.parseInt(comps[2]); m++) {
                            if (comps[1].equals("int")) {
                                ((int[])anArray)[m] = Integer.parseInt(comps[3+m]);
                            } else if (comps[1].equals("java.lang.String")) {
                                ((String[])anArray)[m] = comps[3+m];
                            } else if (comps[1].equals("double")) {
                                ((double[])anArray)[m] = Double.parseDouble(comps[3+m]);
                            }
                        }
                        dataObject.setMetadata(comps[0], anArray);
                    }
                    
                    line = textBuffer.readLine();
                }
            } else {
                line = textBuffer.readLine();
                if (!line.equals(NemaDataConstants.DIVIDER)) {
                    throw new IllegalArgumentException("NemaData.read(): The file being read is not in the correct format!\n File: " + theFile.getPath());
                }
            }
            
            textBuffer.close();
        } catch (java.io.IOException ioe) {
            throw new java.io.IOException("NemaData.read(): An IOException occured while reading file: " + theFile.getPath() + "\n" + ioe);
        } catch (java.lang.NullPointerException npe) {
            npe.printStackTrace();
            throw new IllegalArgumentException("NullPointerException caused by: " + theFile.getCanonicalPath());
        } catch (java.lang.ArrayIndexOutOfBoundsException idxex){
            idxex.printStackTrace();
            throw new IllegalArgumentException("ArrayIndexOutOfBoundsException caused by: " + theFile.getCanonicalPath());
        }
        
        return dataObject;
    }
    
    /**
     * Creates a String representation of a NemaData Object
     * @return a String representation of a NemaData Object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(NemaDataConstants.fileHeader + "\n" + NemaDataConstants.DIVIDER + "\n");
        
        if (metadata == null) {
            buffer.append("null\n");
        } else {
            Set<String> keys = this.metadata.keySet();
            String[] keysArray = keys.toArray(new String[keys.size()]);
            // sort the keys so items are always output
            // in the same order
            Arrays.sort(keysArray);
            for (int i=0;i<keysArray.length;i++) {
                buffer.append((String)keysArray[i] + NemaDataConstants.SEPARATOR);
                int length = 0;
                if (metadata.get(keysArray[i]) == null) {
                    buffer.append("null\n");
                } else if (metadata.get(keysArray[i]).getClass().isArray()) {
                    //Supports only int array, String array and double array types
                    String compName = metadata.get(keysArray[i]).getClass().getComponentType().getName();
                    if ((!compName.equals("int"))&&(!compName.equals("double"))&&(!compName.equals("java.lang.String"))) {
                        throw new IllegalArgumentException("NemaData.write(): Only intger, double and String array types are supported at present, contact developers.");
                    }
                    
                    if (compName.equals("int")) {
                        length = ((int[])metadata.get(keysArray[i])).length;
                    } else if (compName.equals("java.lang.String")) {
                        length = ((String[])metadata.get(keysArray[i])).length;
                    } else if (compName.equals("double")) {
                        length = ((double[])metadata.get(keysArray[i])).length;
                    }
                    buffer.append(compName + NemaDataConstants.SEPARATOR + length  + NemaDataConstants.SEPARATOR);
                    
                    for (int j=0;j<length;j++) {
                        if (compName.equals("int")) {
                            buffer.append(((int[])metadata.get(keysArray[i]))[j] + NemaDataConstants.SEPARATOR);
                        } else if (compName.equals("java.lang.String")) {
                            buffer.append(((String[])metadata.get(keysArray[i]))[j] + NemaDataConstants.SEPARATOR);
                        } else if (compName.equals("double")) {
                            buffer.append(((double[])metadata.get(keysArray[i]))[j] + NemaDataConstants.SEPARATOR);
                        }
                    }
                    buffer.append("\n");
                } else {
                    //Supports only Integer, String and Double data types
                    String className = metadata.get(keysArray[i]).getClass().getName();
                    length = -1;
                    buffer.append(className + NemaDataConstants.SEPARATOR + length  + NemaDataConstants.SEPARATOR);
                    if (className.equals("java.lang.Integer")) {
                        buffer.append(((Integer)metadata.get(keysArray[i])).intValue() + "\n");
                    } else if (className.equals("java.lang.String")) {
                        buffer.append(((String)metadata.get(keysArray[i])) + "\n");
                    } else if (className.equals("java.lang.Double")) {
                        buffer.append(((Double)metadata.get(keysArray[i])).doubleValue() + "\n");
                    }
                } //TODO add support for collections and other objects that can be marshalled to a String
            }
        }
        buffer.append(NemaDataConstants.DIVIDER + "\n");
        return buffer.toString();
    }
    
    /**
     * Writes a NemaData Object to an ASCII file.
     * @param theFile The file to write the Object to.
     * @throws java.io.IOException Thrown if an IO error occurs, such as being 
     * unable to create the File or being unable to write to it.
     */
    public void write(File theFile) throws java.io.IOException {
        File theDir = theFile.getParentFile();
        theDir.mkdirs();
        BufferedWriter textBuffer;
        try {
            textBuffer = new BufferedWriter( new FileWriter(theFile, false) );
        } catch (java.io.IOException ioe) {
            throw new java.io.IOException("NemaData.write(): An IOException occured while opening file: " + theFile.getPath() + " for writing\n" + ioe);
        }
        
        textBuffer.write(this.toString());
        textBuffer.flush();
        textBuffer.close();
    }

    /**
     *  Compares two NemaData Objects for equality and sorting based on their 
     *  file location metadata.
     *  @param o The NemaData to compare this Object with.
     *  @return An integer indicating equality or ordering.
     */
	public int compareTo(NemaData o) {
		return this.getId().compareTo(o.getId());
	}
}
