package org.imirsel.nema.test.matchers;



import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;


/**This matcher is used by assertThat to check if line count in the file
 * matches the one being sent
 * 
 * @author kumaramit01
 * @since 0.2.0  
 * @param <T>
 */
public class FileLineCount <T> extends BaseMatcher<T>  {
	
	private long count =0;
	
	public  FileLineCount(long count){
		this.count = count;
	}
	

	public boolean matches(Object object) {
	File file = (File)object;

	if(!file.exists() || !file.canRead()){
		return false;
	}
	long thisCount=0;
	try {
		LineIterator lit=FileUtils.lineIterator(file);
		while(lit.hasNext()){
			thisCount++;
			lit.next();
		}
	} catch (IOException e) {
	}
	
	if(thisCount==count){
		return true;
	}
	
	return false;
	}


	public void describeTo(Description description) {
		description.appendText(" File line count should be: " + count);
	}

}
