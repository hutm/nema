package org.imirsel.nema.analytics.util.process;


import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandLineFormatParserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleMelodyFormatParsing() throws Exception {
		String configString1 = "$i1{org.imirsel.nema.model.fileTypes.RawAudioFile} $o1{org.imirsel.nema.model.fileTypes.MelodyTextFile}";
		String path1 = "/dummy/path1";
		String path2 = "/dummy/path2";
		String formatString1 = "/dummy/path1 /dummy/path2";
		
		CommandLineFormatParser parser = new CommandLineFormatParser(configString1);
		
		assert(parser.getArguments() != null);
		System.out.println("Number of command argument parts: " + parser.getArguments().size());
		int count = 0;
		for (Iterator<CommandArgument> iterator = parser.getArguments().iterator(); iterator.hasNext();) {
			CommandArgument arg = iterator.next();
			System.out.println("\t" + count++ + ": " + arg.toConfigString()); 
		}
		
		//check IO types
		assert(parser.getInputType(1).equals(org.imirsel.nema.model.fileTypes.RawAudioFile.class));
		assert(parser.getOutputType(1).equals(org.imirsel.nema.model.fileTypes.MelodyTextFile.class));
		
		//check properties
		//TODO implement properties check
		
		//check config string
		System.out.println("Original config string  : " + configString1);
		String configString2 = parser.toConfigString();
		System.out.println("Reproduced config string: " + configString2);
		assert(configString1.equals(configString2));
		
		//check formatted string
		parser.setPreparedPathForInput(1, path1);
		parser.setPreparedPathForOutput(1, path2);
		System.out.println("Correct formatted string:  " + formatString1);
		String formatString2 = parser.toFormattedString();
		System.out.println("Computed formatted string: " + formatString2);
		assert(formatString1.equals(formatString2));
		
		
	}
	
	@Test
	public void testClassificationFormatParsing() throws Exception {
		String configString1 = "-v -x 1234 $i1{org.imirsel.nema.model.fileTypes.TrackListTextFile(bitrate=96k,sample-rate=22050)} " +
				"-laln -o=$o1{org.imirsel.nema.model.fileTypes.ClassificationTextFile} asdioajds";
		String path1 = "/dummy/path1";
		String path2 = "/dummy/path2";
		String formatString1 = "-v -x 1234 /dummy/path1 -laln -o=/dummy/path2 asdioajds";
		
		CommandLineFormatParser parser = new CommandLineFormatParser(configString1);
		
		assert(parser.getArguments() != null);
		System.out.println("Number of command argument parts: " + parser.getArguments().size());
		int count = 0;
		for (Iterator<CommandArgument> iterator = parser.getArguments().iterator(); iterator.hasNext();) {
			CommandArgument arg = iterator.next();
			System.out.println("\t" + count++ + ": " + arg.toConfigString()); 
		}
		
		//check IO types
		assert(parser.getInputType(1).equals(org.imirsel.nema.model.fileTypes.TrackListTextFile.class));
		assert(parser.getOutputType(1).equals(org.imirsel.nema.model.fileTypes.ClassificationTextFile.class));
		
		//check properties
		//TODO implement properties check
		
		//check config string
		System.out.println("Original config string  : " + configString1);
		String configString2 = parser.toConfigString();
		System.out.println("Reproduced config string: " + configString2);
		//can't do this as it doesn't matter what order properties come out in
		//assert(configString1.equals(configString2));
		
		//check formatted string
		parser.setPreparedPathForInput(1, path1);
		parser.setPreparedPathForOutput(1, path2);
		System.out.println("Correct formatted string:  " + formatString1);
		String formatString2 = parser.toFormattedString().trim();
		System.out.println("Computed formatted string: " + formatString2);
		assert(formatString1.equals(formatString2));	
	}
}
