/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author kris.west@gmail.com
 */
public class IOUtil {

    private static final DecimalFormat MEMORY_FORMAT = new DecimalFormat("###,###,###,###.#");
    private static final double MEGABYTE_DIVISOR = 1024 * 1024;

    
    /**
     * Copies one file into another.
     * 
     * @param in File to copy.
     * @param out Location to copy it to.
     * @throws IOException
     */
	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
    
    /** 
     * Read bytes from a File into a byte[].
     * 
     * @param file The File to read.
     * @return A byte[] containing the contents of the File.
     * @throws IOException Thrown if the File is too long to read or couldn't be
     * read fully.
     */
    public static byte[] readBytesFromFile(File file) throws IOException {
    	InputStream is = new FileInputStream(file);
    	
	    // Get the size of the file
	    long length = file.length();
	
	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	    	throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
	    }
	
	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];
	
	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	
	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file " + file.getName());
	    }
	
	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}
    
    /**
     * Writes the specified byte[] to the specified File path.
     * 
     * @param theFile File Object representing the path to write to.
     * @param bytes The byte[] of data to write to the File.
     * @throws IOException Thrown if there is problem creating or writing the 
     * File.
     */
    public static void writeBytesToFile(File theFile, byte[] bytes) throws IOException {
    	BufferedOutputStream bos = null;
    	
		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos); 
			bos.write(bytes);
		}finally {
			if(bos != null) {
				try	{
					//flush and close the BufferedOutputStream
					bos.flush();
					bos.close();
				} catch(Exception e){}
			}
		}
    }
	    
    private static long addTarEntry(File toTar, String name,
                                    TarArchiveOutputStream tarOut) throws IOException{
        TarArchiveEntry entry = new TarArchiveEntry(name);
        long len = toTar.length();
        entry.setSize(len);
        tarOut.putArchiveEntry(entry);

        if (toTar.isDirectory()){
            //do something with dirs?
//            System.out.println("Created entry for directory: " + toTar.getAbsolutePath());
        }else{
//            System.out.println("Reading " + len + " bytes from file: " + toTar.getAbsolutePath());
            byte[] bytes = getBytesFromFile(toTar);
            tarOut.write(bytes);
            len = bytes.length;
//            System.out.println("got " + len + " bytes from file: " + toTar.getAbsolutePath());
        }
        tarOut.closeArchiveEntry();
        
        return len;
    }

    private static byte[] getBytesFromFile(File file) throws IOException{
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0){
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length){
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Returns the relative path from the first File to the second File.
     * @param toModify The FIle path to make relative.
     * @param base The base path the returned path should be relative to.
     * @return The relative path.
     */
    public static String makeRelative(File toModify, File base) {
    	String out = toModify.getAbsolutePath();
    	String baseStr = base.getAbsolutePath();
    	if (out.startsWith(baseStr)){
    		out = out.substring(baseStr.length());
    		if (out.startsWith("/") || out.startsWith("\\")){
    			out = out.substring(1);
    		}
    	}
    	return out;

//            String relative = base.toURI().relativize(toModify.toURI()).getPath();
//            return relative;
    }
    
    /**
     * Creates a zipped tarball from the File or Directory indicated and writes
     * it out to a file name created from the source file with .tar.gz added.
     *
     * @param toTar The File or Directory to compress.
     * @return Returns the location the tarball was written to.
     */
    public static File tarAndGzip(File toTar){
        File out = new File(toTar.getAbsolutePath() + ".tar.gz");
        tarAndGzip(toTar,out,null);
        return out;
    }

    /**
     * Creates a zipped tarball from the File or Directory indicated and writes
     * it out to a file name created from the source file with .tar.gz added. 
     * Files containing the specified keywords are ignored.
     * @param toTar The File or Directory to compress.
     * @param keywords Ignore files containing any of the specified strings.
     * @return Returns the location the tarball was written to.
     */
    public static File tarAndGzip(File toTar, String[] keywords){
        File out = new File(toTar.getAbsolutePath() + ".tar.gz");
        tarAndGzip(toTar,out,keywords);
        return out;
    }

    private static boolean checkName(String[] keywords, String name){
        for (int i = 0; i < keywords.length; i++){
            if (name.indexOf(keywords[i]) != -1){
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a zipped tarball from the File or Directory indicated and writes
     * it out to the specified file. Files containing the specified keywords
     * are ignored.
     * @param toTar The File or Directory to compress.
     * @param outfile The location to write the output to.
     * @param keywords Ignore files containing any of the specified strings.
     */
    public static void tarAndGzip(File toTar, File outfile, String[] keywords){
        TarArchiveOutputStream tarOut = null;
        long uncompressedSize = 0L;

        FileOutputStream tarFout = null;
        File tempTar = null;
        try{
            tempTar = File.createTempFile(outfile.getName() + "_" + toTar.getName(), ".tar");
            tempTar.deleteOnExit();
            tarFout = new FileOutputStream(tempTar);
            tarOut = new TarArchiveOutputStream(tarFout);

            //String base = toTar.getAbsolutePath();
            if (toTar.isDirectory()){
                LinkedList<File> todo = new LinkedList<File>();
                todo.add(toTar);
                while(!todo.isEmpty()){
                    File aFile = todo.removeFirst();
                    String name = makeRelative(aFile,toTar);

                    if (aFile.isDirectory()){
//                        name += "/";
//                        uncompressedSize += addTarEntry(aFile, name, tarOut);
                        File[] files = aFile.listFiles();
                        for (int i = 0; i < files.length; i++){
                            todo.add(files[i]);
                        }

                    }else{
                        if(keywords==null||!checkName(keywords, name)){
                            uncompressedSize += addTarEntry(aFile, name, tarOut);
                        }
                    }

                }
            }else{
                uncompressedSize += addTarEntry(toTar, toTar.getName(), tarOut);
            }

            tarOut.finish();
            tarOut.flush();
            

        }catch (FileNotFoundException ex){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, "Exception occured while attempting to compress " + toTar.getAbsolutePath() + " to a temp tar file", ex);
        }catch (IOException e){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE,"Exception occured while attempting to compress " + toTar.getAbsolutePath() + " to a temp tar file", e);
        }finally{
            try{
                if (tarOut!=null){
                    tarOut.close();
                }
            }catch (IOException ex){}
        }

        long tarSize = tempTar.length();

        FileOutputStream gzFout = null;
        GZIPOutputStream gzOut = null;
        try{
            gzFout = new FileOutputStream(outfile);
            gzOut = new GZIPOutputStream(gzFout);
            gzOut.write(getBytesFromFile(tempTar));
            gzOut.finish();
            gzOut.flush();
        }catch (FileNotFoundException ex){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, "Exception occured while attempting to Gzip the temp tar file: " + tempTar.getAbsolutePath(), ex);
        }catch (IOException ex){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, "Exception occured while attempting to Gzip the Document tar file: " + tempTar.getAbsolutePath(), ex);
        }finally{
            try{
                gzOut.close();
            }catch (IOException ex){}
        }

        long gzSize = outfile.length();

        Logger.getLogger(IOUtil.class.getName()).log(Level.INFO, "Created zipped tarball from: " + toTar.getAbsolutePath() + ", archive: " + outfile.getAbsolutePath() + "\n" +
                "Original file size:     " + uncompressedSize + " bytes\n" +
                "Tar file size:          " + tarSize + " bytes\n" +
                ".tar.gz file size:      " + gzSize + " bytes");
    }

    /**
     * Serializes an Object to file and returns true if successful, false
     * otherwise.
     * @param file Path to write the serialized file out to.
     * @param object The Object to serialize.
     * @return A flag indicating whether the opertaion was successful.
     */
    public static boolean writeObject(File file, Object object) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel channel = fileOutputStream.getChannel();
                // Use the file channel to create a lock on the file.
            FileLock lock = null;
            long start = System.currentTimeMillis();
            while (lock == null){
                // Try acquiring the lock without blocking. This method returns
                // null or throws an exception if the file is already locked.
                try {
                    lock = channel.tryLock();
                } catch (OverlappingFileLockException e) {
                    // File is already locked in this thread or virtual machine
                }
                if (lock == null){
                    if (System.currentTimeMillis() - start > 120000){
                        Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE,"Failed to acquire write lock (2 min timeout) for: " + file.getAbsolutePath());
                        return false;
                    }
                    Thread.sleep((int)(Math.random() * 60));
                }
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            try{
                lock.release();
            } catch (Exception e) {}
            try{
                channel.close();
            } catch (Exception e) {}
            try{
                fileOutputStream.close();
            } catch (Exception e) {}
        } catch (Exception e) {
            //Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE,"Exception occured while attempting to serialize an Object to: " + file.getAbsolutePath(), e);
            //return false;
            throw new RuntimeException("Exception occured while attempting to serialize an Object to: " + file.getAbsolutePath(), e);

        }
        return true;
    }

    /**
     * Reads a serialized <code>Object</code> from the path given.
     * @param file A <code>File</code> <code>Objec</code>t representing the
     * path to read from.
     * @return The <code>Object</code> read or <code>null</code> if the
     * <code>Object</code> could not be read.
     */
    public static Object readObject(File file) {
        FileInputStream istream = null;
        FileChannel channel = null;
        FileLock lock = null;
        Object object = null;
        try {

            istream = new FileInputStream(file);
            channel = istream.getChannel();
                // Use the file channel to create a lock on the file.
            lock = null;
            long start = System.currentTimeMillis();
            while (lock == null){
                // Try acquiring the lock without blocking. This method returns
                // null or throws an exception if the file is already locked.
                try {
                    lock = channel.tryLock(0,channel.size(),true);
                } catch (OverlappingFileLockException e) {
                    // File is already locked in this thread or virtual machine
                }
                if (lock == null){
                    if (System.currentTimeMillis() - start > 120000){
                        Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE,"Failed to acquire write lock (2 min timeout) for: " + file.getAbsolutePath());
                        return null;
                    }
                    Thread.sleep((int)(Math.random() * 60));
                }
            }


            ObjectInputStream p = new ObjectInputStream(istream);
            object = p.readObject();

        } catch (Exception e) {
//            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE,"Exception occured while attempting to deserialize an Object from: " + file.getAbsolutePath(), e);
            throw new RuntimeException("Exception occured while attempting to deserialize an Object from: " + file.getAbsolutePath(), e);
        }
        finally{
            try{
                lock.release();
            } catch (Exception e) {}
            try{
                channel.close();
            } catch (Exception e) {}
            try{
                istream.close();
            } catch (Exception e) {}

        }

        return object;
    }
    
    /**
     * 
     * @param searchDir
     * @param extension
     * @deprecated
     * @return List of files
     */
    public static List<File> getFilteredPathStrings(File searchDir, String extension) {
        System.out.println("Getting list of files in " + searchDir.getAbsolutePath() + " with extension " + extension);
        if (!searchDir.exists()) {
            throw new IllegalArgumentException("Search directory did not exist!");
        }

        LinkedList<File> resultFiles = new LinkedList<File>();
        LinkedList<File> todo = new LinkedList<File>();
        todo.add(searchDir);

        while (true) {
            File directory = todo.removeFirst();

            if (directory.isDirectory()) {
                System.out.println("\tgetting file list for: " + directory.getAbsolutePath());
                File[] files = directory.listFiles();
                int numFilesInDirectory = files.length;

                for (int i = 0; i < numFilesInDirectory; i++) {
                    File file = files[i];

                    if (file.isDirectory()) {
                        todo.add(file);
                    }else if (file.getName().endsWith(extension)) {
                        resultFiles.add(file);
                    }
                }
            }

            if (todo.isEmpty()) {
                break;
            }
        }
        System.out.println("\treturning " + resultFiles.size());
        return resultFiles;
    }


    @SuppressWarnings("unchecked")
	public static void listFiles(File dir, File outputFile, String extension){
        Collection<File> paths = (Collection<File>)FileUtils.listFiles(dir, new String[]{extension}, true);//getFilteredPathStrings(dir, extension);
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new FileWriter(outputFile));
            for (Iterator<File> it = paths.iterator(); it.hasNext();){
                File file = it.next();
                out.write(file.getAbsolutePath());
                out.newLine();
            }
            out.flush();
        }catch (FileNotFoundException ex){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex){
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(out != null){
                try{
                    out.close();
                }catch (IOException ex){
                    Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static void printMemStats(){
        Runtime runtime = Runtime.getRuntime();

        long max = runtime.maxMemory();
        long allocated = runtime.totalMemory();
        long free = runtime.freeMemory();
        
        System.out.println("Max memory:        " + MEMORY_FORMAT.format(max / MEGABYTE_DIVISOR) + " mb");
        System.out.println("Allocated memory:  " + MEMORY_FORMAT.format(allocated / MEGABYTE_DIVISOR) + " mb");
        System.out.println("Free memory:       " + MEMORY_FORMAT.format(free/ MEGABYTE_DIVISOR) + " mb");
        System.out.println("Total free memory: " + MEMORY_FORMAT.format((free + (max - allocated)) / MEGABYTE_DIVISOR) + " mb");
        System.out.println("Total used memory: " + MEMORY_FORMAT.format((allocated - free) / MEGABYTE_DIVISOR) + " mb");
    }
}
