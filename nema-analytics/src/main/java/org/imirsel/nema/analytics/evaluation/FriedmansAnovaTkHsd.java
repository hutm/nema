package org.imirsel.nema.analytics.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.imirsel.nema.analytics.util.io.CopyFileFromClassPathToDisk;
import org.imirsel.nema.analytics.util.process.MatlabExecutorImpl;

/**
 * Utility class for computing Friedman's ANOVA with Tukey-Kramer Honestly-Significant-Difference multiple
 * comparisons. THis is a non-parametric statistical significance test that performs statistically valid
 * pair-wise comparisons - in contrast to procedures based on multiply applied T-tests, which both assume
 * the normal distribution of the underlying data and fail to acknowledge the accumulation of error
 * likelihoods over multiple comparisons.
 * 
 * This implementation is currently based on Matlab but maybe re-implemented in pure java in the future.
 * 
 * @author kris.west@gmail.com
 *
 */
public class FriedmansAnovaTkHsd {

	/** Constant definition for the default matlab path 'matlab', which assumes matlab is on the user's
	 * PATH.
	 */
	public static final String DEFAULT_MATLAB_COMMAND = "matlab";
	
	/**
	 * Uses a CSV File and set of system name labels to perform a statisitcal significance test in Matlab.
	 * The test performed is Friedman's ANOVA with Tukey-Kramer Honestly-Significant-Difference multiple
	 * comparisons. Hence the test is both non-parametric and performs valid pairwise comparisons between
	 * the performance scores of a set of results from multiple systems.
	 * 
	 * @param outputDir The directory to write the results of the test to (a plot and test table).
	 * @param CSVResultFile The CSV file to base the test on. The data should be organised into columns
	 * for different systems and provide one or more rows of data. 
	 * @param systemNamesRow The (zero-based) row number that the system names are on.
	 * @param startDataCol The (zero-based) column number of the first data column (i.e. the first column 
	 * of performance scores for a system).
	 * @param startDataRow The (zero-based) row number of the first data row (i.e. the first row 
	 * of performance scores for systems). Must be greater than systemNamesRow.
	 * @param numSystems The number of columns of data to use.
	 * @return Returns a a File of length 2 containing File Objects pointing to the Friedman's plot
	 * and Friedman's test table.
	 */
	public static File[] performFriedman(File outputDir, File CSVResultFile, int systemNamesRow, 
			int startDataCol, int startDataRow, int numSystems, File matlabPath) 
			throws IOException, IllegalArgumentException{
        //make sure readtext.m is in the working directory for Matlab
        File readtextMFile = new File(outputDir.getAbsolutePath() + File.separator + "readtext.m");
        CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/util/resources/readtext.m", readtextMFile);
        
        //create an m-file to run the test
        String name = CSVResultFile.getName().replaceAll(".csv", "");
        String evalCommand = name.replaceAll("[\\W]", "_") + "_friedmanTKHSD";
        File tempMFile = new File(outputDir.getAbsolutePath() + File.separator + evalCommand + ".m");
        String plotFileName = name + ".friedmanTKHSD.png";
        String matlabPlotPath = outputDir.getAbsolutePath() + File.separator + plotFileName;
        String matlabPlotRelPath = "." + File.separator + plotFileName;
        String friedmanTablePath = outputDir.getAbsolutePath() + File.separator + name + ".friedmanTKHSD.csv";
        try {
            BufferedWriter textOut = new BufferedWriter(new FileWriter(tempMFile));
            
            int matlabSysNamesRow = systemNamesRow+1;
            int matlabStartCol = startDataCol+1;
            int matlabStartRow = startDataRow+1;
            textOut.write("[data, result] = readtext('" + CSVResultFile.getAbsolutePath() + "', ',');");
            textOut.newLine();
            textOut.write("algNames = data(" + matlabSysNamesRow + "," + matlabStartCol + ":" + (numSystems + (matlabStartCol-1)) + ")';");
            textOut.newLine();
            textOut.write("[length,width] = size(data);");
            textOut.newLine();
            textOut.write("Acc_Scores = cell2mat(data(" + matlabStartRow + ":length," + matlabStartCol + ":" + (numSystems + (matlabStartCol-1)) + "));");
            textOut.newLine();
            textOut.write("[val sort_idx] = sort(mean(Acc_Scores));");
            textOut.newLine();
            textOut.write("[P,friedmanTable,friedmanStats] = friedman(Acc_Scores(:,fliplr(sort_idx)),1,'on'); close(gcf)");
            textOut.newLine();
            textOut.write("[c,m,h,gnames] = multcompare(friedmanStats, 'ctype', 'tukey-kramer','estimate', 'friedman', 'alpha', 0.05,'display','off');");
            textOut.newLine();
            textOut.write("fig = figure;");
            textOut.newLine();
            textOut.write("width = (-c(1,3)+c(1,5))/4;");
            textOut.newLine();
            textOut.write("set(fig,'paperunit','points')");
            textOut.newLine();
            textOut.write("set(fig,'paperposition',[1 500 1200 500])");
            textOut.newLine();
            textOut.write("set(fig,'papersize',[1200 500])");
            textOut.newLine();
            textOut.write("set(fig,'position',[1 500 1200 500])");
            textOut.newLine();
            textOut.write("plot(friedmanStats.meanranks,'ro'); hold on");
            textOut.newLine();
            textOut.write("for i=1:" + numSystems + ",");
            textOut.newLine();
            textOut.write("    plot([i i],[-width width]+friedmanStats.meanranks(i));");
            textOut.newLine();
            textOut.write("    plot([-0.1 .1]+i,[-width -width]+friedmanStats.meanranks(i))");
            textOut.newLine();
            textOut.write("    plot([-0.1 .1]+i,[+width +width]+friedmanStats.meanranks(i))");
            textOut.newLine();
            textOut.write("end");
            textOut.newLine();
            textOut.write("set(gca,'xtick',1:" + numSystems + ",'xlim',[0.5 " + numSystems + "+0.5])");
            textOut.newLine();
            textOut.write("sortedAlgNames = algNames(fliplr(sort_idx));");
            textOut.newLine();
            textOut.write("set(gca,'xticklabel',sortedAlgNames)");
            textOut.newLine();
            textOut.write("ylabel('Mean Column Ranks')");
            textOut.newLine();
            textOut.write("h = title('Friedman TKHSD: " + CSVResultFile.getName() + "')");
            textOut.newLine();
            textOut.write("set(h,'interpreter','none')");
            textOut.newLine();
            textOut.write("outerpos = get(gca,'outerposition');");
            textOut.newLine();
            textOut.write("tightinset = get(gca,'tightinset');");
            textOut.newLine();
            textOut.write("newpos = [tightinset(1) tightinset(2) outerpos(3)-(tightinset(1) + tightinset(3)) outerpos(4)-(tightinset(2) + tightinset(4))];");
            textOut.newLine();
            textOut.write("set(gca,'position',newpos);");
            textOut.newLine();
            textOut.write("saveas(fig,'" + matlabPlotRelPath + "');");
            textOut.newLine();
            textOut.write("fidFriedman=fopen('" + friedmanTablePath + "','w+');");
            textOut.newLine();
            textOut.write("fprintf(fidFriedman,'%s,%s,%s,%s,%s,%s\\n','*TeamID','TeamID','Lowerbound','Mean','Upperbound','Significance');");
            textOut.newLine();
            textOut.write("for i=1:size(c,1)");
            textOut.newLine();
            textOut.write("        if sign(c(i,3))*sign(c(i,5)) > 0");
            textOut.newLine();
            textOut.write("            tf='TRUE';");
            textOut.newLine();
            textOut.write("        else");
            textOut.newLine();
            textOut.write("            tf='FALSE';");
            textOut.newLine();
            textOut.write("        end");
            textOut.newLine();
            textOut.write("         fprintf(fidFriedman,'%s,%s,%6.4f,%6.4f,%6.4f,%s\\n',sortedAlgNames{c(i,1)},sortedAlgNames{c(i,2)},c(i,3),c(i,4),c(i,5),tf);");
            textOut.newLine();
            textOut.write("end");
            textOut.newLine();
            textOut.write("fclose(fidFriedman);");
            textOut.newLine();
            textOut.write("exit;");
            textOut.newLine(); 


            textOut.close();
        } catch (IOException ex) {
            Logger.getLogger(FriedmansAnovaTkHsd.class.getName()).log(Level.SEVERE, null, ex);
        }

        MatlabExecutorImpl matlabIntegrator = new MatlabExecutorImpl(outputDir,true,outputDir,outputDir,outputDir,"",evalCommand,null);
        matlabIntegrator.setMatlabBin(matlabPath);
        matlabIntegrator.runCommand(null);
        
        return new File[]{new File(matlabPlotPath),new File(friedmanTablePath)};
    }
}
