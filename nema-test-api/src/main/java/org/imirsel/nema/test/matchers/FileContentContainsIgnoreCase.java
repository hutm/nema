package org.imirsel.nema.test.matchers;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;


/**This matcher is used by assertThat to check if a file
 * has a particular string -case insensitive search
 * 
 * @author kumaramit01
 * @since 0.2.0  
 * @param <T>
 */
public class FileContentContainsIgnoreCase <T> extends BaseMatcher<T>  {
	
	private String content;
	
	public  FileContentContainsIgnoreCase(String content){
		this.content = content;
	}
	

	public boolean matches(Object object) {
	File file = (File)object;
	if(content==null){
		return false;
	}
	if(!file.exists() || !file.canRead()){
		return false;
	}
	try {
		String fileContent=FileUtils.readFileToString(file);
		if(fileContent.toLowerCase().indexOf(content.toLowerCase())!=-1){
			return true;
		}
	} catch (IOException e) {
	}
	return false;
	}


	public void describeTo(Description description) {
		description.appendText(" file contains " + content);
	}

}
