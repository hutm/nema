package org.imirsel.nema.model.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.imirsel.nema.model.util.ChordConversionUtil;
import org.junit.Test;

public class ChordConversionUtilTest {

//	@Test
//	public void testConvertIntervalsToNotenumbers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertNotenumbersToIntervals() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertShorthandToNotenumbers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertNoteNumbersToShorthand() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertChordNumbersToNoteNumbers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertNotenumbersToChordnumbers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testReadChordDictionaryString() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testInitUtil() throws Exception{
		ChordConversionUtil util = ChordConversionUtil.getInstance();
		assertTrue(util!=null);
	}
	
	//@Test
	public void testReadChordDictionaryFile() throws IOException{
		File dictFile = new File("src/main/resources/org/imirsel/nema/model/util/ShorthandDictionary.txt");
		Map<String,int[]> map = ChordConversionUtil.readChordDictionary(dictFile);
		assertTrue(map!=null);
	}
	

	
	
	

}
