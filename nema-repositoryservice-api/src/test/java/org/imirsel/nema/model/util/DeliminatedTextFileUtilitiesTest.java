package org.imirsel.nema.model.util;
import static org.junit.Assert.*;

import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.Test;


public class DeliminatedTextFileUtilitiesTest extends BaseManagerTestCase{

	

	@Test
	public void testParseCommaDelimTextLine() {

		String delim = ",";
		String[] valsShouldBe = new String[]{"col_a","col_b","  col_c"};
		String testLine = "\"" + valsShouldBe[0] + "\"" + delim + valsShouldBe[1] + delim + valsShouldBe[2];
		
		String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
		
		for (int i = 0; i < out.length; i++) {
			if (!out[i].equals(valsShouldBe[i])){
				fail("Values produced by parser don't match true values.\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
			}
		}
	}
	
	@Test
	public void testParseCommaDelimTextLineWithQuotes() {

		String delim = ",";
		String quote = "\"";
		String[] valsShouldBe = new String[]{"col_a","col_b","col_c"};
		String testLine = quote + valsShouldBe[0] + quote + delim + quote + valsShouldBe[1] + quote + delim + quote + valsShouldBe[2] + quote;
		
		
		
		String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
		
		for (int i = 0; i < out.length; i++) {
			if (!out[i].equals(valsShouldBe[i])){
				fail("Values produced by parser don't match true values.\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
			}
		}
	}
	
	
	@Test
	public void testParseTabDelimTextLineWithSpaces() {
		String delim = "\t";
		String[] valsShouldBe = new String[]{"col_a","col_b","  col_c"};
		String testLine = "   \"" + valsShouldBe[0] + "\"" + delim + valsShouldBe[1] + delim + valsShouldBe[2];
		
		String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
		
		for (int i = 0; i < out.length; i++) {
			if (!out[i].equals(valsShouldBe[i])){
				fail("Values produced by parser don't match true values.\nTest line: '" + testLine + "'\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
			}
		}
	}
	

	@Test
	public void testParseTabDelimTextLine() {
		String delim = "\t";
		String[] valsShouldBe = new String[]{"col_a","col_b","  col_c"};
		String testLine = "\"" + valsShouldBe[0] + "\"" + delim + valsShouldBe[1] + delim + valsShouldBe[2];
		
		String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
		
		for (int i = 0; i < out.length; i++) {
			if (!out[i].equals(valsShouldBe[i])){
				fail("Values produced by parser don't match true values.\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
			}
		}
	}
	
	@Test
	public void testParseWhitespaceDelimTextLine() {
		String delim = "\\s+";
		String whitespace1 = "   ";
		String whitespace2 = " ";
		String tab = "\t";
		String[] valsShouldBe = new String[]{"col_a","col_b","col_c","col_d"};
		String testLine = "\"" + valsShouldBe[0] + "\"" + whitespace1 + valsShouldBe[1] + whitespace2 + valsShouldBe[2] + tab + valsShouldBe[3];
		
		try{
			String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
			
			for (int i = 0; i < out.length; i++) {
				if (!out[i].equals(valsShouldBe[i])){
					fail("Values produced by parser don't match true values.\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}


	@Test
	public void testParseLongTabLine() {

		String delim = "\t";
		String[] valsShouldBe = new String[]{
				"Studies, Allegretto in E",
				"The Segovia Collection Vol. 7: Guitar Etudes",
				"Andrs Segovia",
				"Fernando Sor",
				"Classical - Romantic",
				"1990",
				"3C010B625C783D64",
				"10833",
				"192",
				"mp3",
				"44100",
				"2",
				"/mnt/hgfs/MacintoshHD2/Codaich/Classical/Andr%C3%A9s%20Segovia/The%20Segovia%20Collection%20Vol.%207_%20Guitar%20Et/35%20Studies,%20Allegretto%20in%20E.mp3"};
		
		String testLine = "\"Studies, Allegretto in E\"	\"The Segovia Collection Vol. 7: Guitar Etudes\"	\"Andrs Segovia\"	\"Fernando Sor\"	\"Classical - Romantic\"	1990	\"3C010B625C783D64\"	10833	192	\"mp3\"	44100	2	\"/mnt/hgfs/MacintoshHD2/Codaich/Classical/Andr%C3%A9s%20Segovia/The%20Segovia%20Collection%20Vol.%207_%20Guitar%20Et/35%20Studies,%20Allegretto%20in%20E.mp3\"";
		
		String[] out = DeliminatedTextFileUtilities.parseDelimTextLine(testLine, delim);
		
		for (int i = 0; i < out.length; i++) {
			if (!out[i].equals(valsShouldBe[i])){
				fail("Values produced by parser don't match true values.\nParsed: '" + out[i] + "'" + "\nActual: '" + valsShouldBe[i] + "'");
			}
		}
	}
	

}
