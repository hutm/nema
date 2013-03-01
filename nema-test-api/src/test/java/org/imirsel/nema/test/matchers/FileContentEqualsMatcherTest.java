package org.imirsel.nema.test.matchers;

import java.io.File;
import java.io.FileWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.imirsel.nema.test.matchers.*;

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.Each.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.imirsel.nema.test.matchers.NemaMatchers.*;


/**
 * 
 * @author kumaramit01
 * Unit test for matchers
 */
public class FileContentEqualsMatcherTest {
	
	private File file1;
	private File file2;
	private final String EOL=System.getProperty("line.separator");

	@Before
	public void setUp() throws Exception {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		file1 = new File(tmpFolder,"one");
		FileWriter fwrite1 = new FileWriter(file1);
		fwrite1.write("Hello World"+EOL);
		fwrite1.write("Hello World"+EOL);
		fwrite1.flush();
		file2 = new File(tmpFolder,"two");
		FileWriter fwrite2 = new FileWriter(file2);
		fwrite2.write("Hello World"+EOL);
		fwrite2.write("Hello World"+EOL);
		fwrite2.flush();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public  void testMatcher(){
		  assertThat(file1, fileContentEquals(file2));
		  assertThat(file1,fileContentContains("Hello"));
		  assertThat(file1,fileContentContainsIgnoreCase("hello"));
		  assertThat(10,between(5,15));
		  assertThat("amit is here", containsString("amit"));
		  assertThat(file1,fileLineCountEquals(2));
	}
	
	
}
