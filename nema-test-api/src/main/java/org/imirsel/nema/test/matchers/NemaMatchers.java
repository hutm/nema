package org.imirsel.nema.test.matchers;

import java.io.File;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**Allows static imports of the the matchers.
 * 
 * @author kumaramit01
 * @since 0.1.0
 * 
 */
public class NemaMatchers {

		  @Factory
		  public static <T> Matcher<Object> between(double start, double end) {
		    return new Between(start, end);
		  }
		  
		  @Factory
		  public static <T> Matcher<Object> fileContentContains(String content) {
			    return new FileContentContains(content);
		  }
		
		  @Factory
		  public static <T> Matcher<Object> fileContentContainsIgnoreCase(String content) {
			    return new FileContentContainsIgnoreCase(content);
		  }
		
		  @Factory
		  public static <T> Matcher<File> fileContentEquals(File file) {
			    return new FileContentEquals<File>(file);
		  }
		  
		  @Factory
		  public static <T> Matcher<Object> fileLineCountEquals(long l) {
			    return new FileLineCount(l);
		  }
		  
		  
		
}