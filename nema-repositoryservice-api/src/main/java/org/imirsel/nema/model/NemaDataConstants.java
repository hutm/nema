package org.imirsel.nema.model;

import java.util.HashMap;
import java.util.List;


public class NemaDataConstants {

    //Metadata key constants
    /** Constant definition for metadata key: column label     */
    public final static String PROP_COLUMN_LABELS = "columnLabels";
    /** Constant definition for metadata key: file location     */
    public final static String PROP_ID  = "id";
    /** Constant definition for metadata key: file location     */
    public final static String PROP_FILE_LOCATION  = "file location";
    /** Constant definition for metadata key: file site     */
    public final static String PROP_FILE_SITE  = "file site";
    
    /** Constant definition for metadata key: Raw audio file data. */
    public static final String FILE_DATA = "Raw file byte data";
    

//    /** Constant definition for performance metadata.     */
//    public final static String PROP_PERF = "Performance";
//    /** Constant definition for algorithm name (used as an identifier in evaluations).     */
//    public final static String PROP_ALG_NAME = "Algorithm name";
//    /** Constant definition for      */
//    public final static String PATH_TO_ARTIST_MAP = "Path to artist map";
//    
//    
//    //Evaluation results constants
//    /** Constant definition for metadata key: Evaluation report*/
//    public static final String SYSTEM_RESULTS_REPORT = "Single system rsult evaluation report";
//    
    
    //File format constants
    /** Constant definition for section divider used in ASCII file     */
    public final static String DIVIDER = "-===-";
    /** Constant definition for SEPARATOR used in ASCII file     */
    public final static String SEPARATOR = "\t";
    /** Constant definition for header used in ASCII file     */
    public final static String fileHeader = "nema-analytics NemaData (8th Feb 2010)";
    
    //Similarity evaluator constants
    public final static String SEARCH_TRACK_DISTANCE_LIST = "Similar track list";
    public final static String SEARCH_DISTANCE_MATRIX_NAME = "Distance matrix name";
    
    public final static String SEARCH_ARTIST_FILTERED_GENRE_NEIGHBOURHOOD = "Search Artist-Filtered Genre Neighbourhood clustering";
    public final static String SEARCH_TEST_LEVELS = "Number of results search statisitcs calculated at";
    public final static String SEARCH_GENRE_NEIGHBOURHOOD = "Search Genre Neighbourhood clustering";
    public final static String SEARCH_ARTIST_NEIGHBOURHOOD = "Search Artist Neighbourhood clustering";
    public final static String SEARCH_ALBUM_NEIGHBOURHOOD = "Search Album Neighbourhood clustering";
    public final static String SEARCH_TRIANGULAR_INEQUALITY = "Search Triangular inequality holds";
    public final static String SEARCH_PEAK_SIMILAR_TRACKS = "Peak number of tracks a single track similar to";
    public final static String SEARCH_TRACKS_NEVER_SIMILAR = "Percantage of tracks never similar to another track";
    
    
    //Test/Train classification evaluator constants
    public final static String CLASSIFICATION_EXPERIMENT_CLASSNAMES = "Classification Experiment Classnames";
    public final static String CLASSIFICATION_CONFUSION_MATRIX_RAW = "Classification Confusion Matrix - raw";
    public final static String CLASSIFICATION_CONFUSION_MATRIX_PERCENT = "Classification Confusion Matrix - percent";
    public final static String CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW = "Classification Discounted Confusion Matrix - raw";
    public final static String CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT = "Classification Discounted Confusion Matrix - percent";
    public final static String CLASSIFICATION_ACCURACY = "Classification Accuracy";
    public final static String CLASSIFICATION_DISCOUNTED_ACCURACY = "Classification Discounted Accuracy";
    public final static String CLASSIFICATION_NORMALISED_ACCURACY = "Normalised Classification Accuracy";
    public final static String CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY = "Normalised Classification Discounted Accuracy";
    
    //classification types
    public final static String CLASSIFICATION_DUMMY = "Classification";
    public final static String CLASSIFICATION_ALBUM = "Album";
    public final static String CLASSIFICATION_ARTIST = "Artist";
    public final static String CLASSIFICATION_COMPOSER = "Composer";
    public final static String CLASSIFICATION_MIREX_COMPOSER = "MIREX_composer";
    public final static String CLASSIFICATION_GENRE = "Genre";
    public final static String CLASSIFICATION_MIREX_GENRE = "MIREX_genre";
    public final static String CLASSIFICATION_TITLE = "Title";
    public final static String CLASSIFICATION_MOOD = "Mood";
    
    
    
    
    
    //Tag classification evaluator constants
    
    public final static String TAG_CLASSIFICATIONS = "Tag classifications";
    
    //constants for different tag types we have in repository
    public final static String TAG_MAJORMINER = "Tag_MajorMiner";
    public final static String TAG_MOOD = "Tag_Mood";
    public final static String TAG_TAGATUNE = "Tag_Tagatune";
    
    
    
    /** Constant definition for tag classification data in the form of a 
     * {@code HashMap<String,HashMap<String,Double>>} - mapping paths
     * to a map linking tags to their affinity values. */
    public static final String TAG_AFFINITY_MAP = "Tag classification affinity map";
    
    /** Constant definition for the list of tag names appearing in the data in 
     * the form of a {@code HashSet<String>}. */
    public static final String TAG_EXPERIMENT_CLASSNAMES = "Tag Classification Experiment Classnames";
    
    //track metrics
    //double
    public static final String TAG_ACCURACY = "Tag classification accuracy";
    public static final String TAG_POS_ACCURACY = "Tag classification positive example accuracy";
    public static final String TAG_NEG_ACCURACY = "Tag classification negative example accuracy";
    public static final String TAG_PRECISION = "Tag classification precision";
    public static final String TAG_RECALL = "Tag classification recall";
    public static final String TAG_FMEASURE = "Tag classification fMeasure";
    
    //fold and overall metrics
    //Tag name -> score (Map<String,Double>)
    public static final String TAG_ACCURACY_TAG_MAP = "Tag classification per-tag accuracy";
    public static final String TAG_POS_ACCURACY_TAG_MAP = "Tag classification positive example per-tag accuracy";
    public static final String TAG_NEG_ACCURACY_TAG_MAP = "Tag classification negative example per-tag accuracy";
    public static final String TAG_PRECISION_TAG_MAP = "Tag classification per-tag precision";
    public static final String TAG_RECALL_TAG_MAP = "Tag classification per-tag recall";
    public static final String TAG_FMEASURE_TAG_MAP = "Tag classification per-tag fMeasure";
    
    
    /** double[] - Precision at N scores for tags predicted */
    public static final String TAG_AFFINITY_PRECISION_AT_N = "Tag affinity precision at N tags";
    /** int[] - number of results precision at N scores were calculated at */
    public static final String TAG_AFFINITY_PRECISION_AT_N_LEVELS = "Tag affinity precision at N test levels";
    /** double - Area Under Curve - Receiver Operating Characteristic Curve */
    public static final String TAG_AFFINITY_AUC_ROC = "Tag affinity AUC-ROC";
    /** List<double[]> - Receiver Operating Characteristic Curve data*/
    public static final String TAG_AFFINITY_ROC_DATA = "Tag affinity ROC data points";
    /** Map<String, Double>() - tag name to Area Under Curve - Receiver Operating Characteristic Curve */
    public static final String TAG_AFFINITY_AUC_ROC_MAP = "Tag affinity per-tag AUC-ROC map";
    /** Map<String, List<double[]>> - tag name to Receiver Operating Characteristic Curve data*/
    public static final String TAG_AFFINITY_ROC_DATA_MAP = "Tag affinity per-tag ROC data points map";
    
    
    
    
    //fold and overall statistics
    public static final String TAG_NUM_POSITIVE_EXAMPLES_MAP = "Tag classification number of positive examples map";
    public static final String TAG_NUM_NEGATIVE_EXAMPLES_MAP = "Tag classification number of negative examples map";
    public static final String TAG_NUM_POSITIVE_EXAMPLES = "Tag classification number of positive examples overall";
    public static final String TAG_NUM_NEGATIVE_EXAMPLES = "Tag classification number of negative examples overall";
    
    
    // Melody Extraction Evaluator Constants
    public static final String MELODY_EXTRACTION_DATA = "Raw melody transcription data with timestamps and F0 in Hz";
    public static final String MELODY_RAW_PITCH_ACCURACY = "Raw Pitch Accuracy";
    public static final String MELODY_RAW_CHROMA_ACCURACY = "Raw Chroma Accuracy";
    public static final String MELODY_VOICING_RECALL = "Voicing Recall Rate";
    public static final String MELODY_VOICING_FALSE_ALARM = "Voicing False-Alarm Rate";
    public static final String MELODY_OVERALL_ACCURACY = "Overall Accuracy";
    public static final double MELODY_TIME_INC = 0.01;		// MIREX-spec 10ms time-increment
    
    // Multi-F0 Estimation Evaluator Constants
    public static final double MULTI_F0_EST_TIME_INC = 0.01; // 10ms time-increment
    public static final String MULTI_F0_EST_DATA = "Raw multi-F0 Estimation data with timestamps and F0 in Hz";
    public static final String MULTI_F0_EST_ACCURACY = "Multi-F0 Estimation Accuracy";
    public static final String MULTI_F0_EST_CHROMA_ACCURACY= "Multi-F0 Estimation Chroma Accuracy";
    public static final String MULTI_F0_EST_PRECISION = "Multi-F0 Estimation Precision";
    public static final String MULTI_F0_EST_E_TOT = "Multi-F0 Estimation Total Error";
    public static final String MULTI_F0_EST_E_MISS = "Multi-F0 Estimation Missed Error";
    public static final String MULTI_F0_EST_E_FA = "Multi-F0 Estimation False Alarm Error";
    
 // Multi-F0 Note Tracking Evaluator Constants
    public static final String MULTI_F0_NT_NOTE_SEQUENCE = "Multi-F0 Note Tracking note sequence";
    public static final String MULTI_F0_NT_DATA = "Multi-F0 Note Tracking data with onset, offset and F0 values";
    public static final String MULTI_F0_NT_PRECISION = "Multi-F0 Note Tracking precision";
    public static final String MULTI_F0_NT_RECALL =  "Multi-F0 Note Tracking recall";
    public static final String MULTI_F0_NT_OVERLAP = "Multi-F0 Note Tracking overlap";
    public static final String MULTI_F0_AVE_NT_OVERLAP = "Multi-F0 Note Tracking average overlap";	    
    public static final String MULTI_F0_NT_F_MEASURE = "Multi-F0 Note Tracking F-measure";
    public static final String MULTI_F0_NT_AVE_F_MEASURE = "Multi-F0 Note Tracking average F-measure";
    	        	
    	    	
    
    // Key Detection Evaluator Constants
    public static final String KEY_DETECTION_DATA = "The musical key (tonic/mode)";
    public static final String KEY_DETECTION_WEIGHTED_SCORE ="Weighted Key Score";
    public static final String KEY_DETECTION_CORRECT = "Correct Key(s)";
    public static final String KEY_DETECTION_PERFECT_FIFTH_ERROR = "Perfect Fifth Error(s)";
    public static final String KEY_DETECTION_RELATIVE_ERROR = "Relative Major/Minor Error(s)";
    public static final String KEY_DETECTION_PARALLEL_ERROR = "Parallel Major/Minor Error(s)";
    public static final String KEY_DETECTION_ERROR = "Pure Non-discounted Error(s)";
    
    // Tempo Extraction Evaluator Constants
    public static final String TEMPO_EXTRACTION_DATA = "Two predominant tempi, and the salience of the first reported tempo";
    public static final String TEMPO_EXTRACTION_P_SCORE ="Tempo P-Score";
    public static final String TEMPO_EXTRACTION_ONE_CORRECT = "At least One Tempo Correct";
    public static final String TEMPO_EXTRACTION_TWO_CORRECT = "Both Tempi Correct";    
    
    // Chord Estimation Evaluator Constants
    public static final String CHORD_LABEL_SEQUENCE = "Chord label sequence";
    public static final String CHORD_LABEL_SEQUENCE_FULL = "Chord label sequence full dictionary";
    public static final String CHORD_OVERLAP_RATIO = "Chord Overlap ratio";
    public static final String CHORD_WEIGHTED_AVERAGE_OVERLAP_RATIO = "Chord weighted average overlap ratio";
	
    // Structure Segmentation Evaluator Constants
    public static final String STRUCTURE_SEGMENTATION_DATA = "Structural segmentation";
    public static final String STRUCTURE_SEGMENTATION_OVERSEGSCORE = "Normalised conditional entropy based over-segmentation score";
    public static final String STRUCTURE_SEGMENTATION_UNDERSEGSCORE = "Normalised conditional entropy based under-segmentation score";
    public static final String STRUCTURE_SEGMENTATION_PWF = "Frame pair clustering F-measure";
    public static final String STRUCTURE_SEGMENTATION_PWPRECISION = "Frame pair clustering precision rate";
    public static final String STRUCTURE_SEGMENTATION_PWRECALL = "Frame pair clustering recall rate";
    public static final String STRUCTURE_SEGMENTATION_R = "Random clustering index";
    public static final String STRUCTURE_SEGMENTATION_FMEASUREATPOINTFIVE = "Segment boundary recovery evaluation measure @ 0.5sec";
    public static final String STRUCTURE_SEGMENTATION_PRECRATEATPOINTFIVE = "Segment boundary recovery precision rate @ 0.5sec";
    public static final String STRUCTURE_SEGMENTATION_RECRATEATPOINTFIVE = "Segment boundary recovery recall rate @ 0.5sec";
    public static final String STRUCTURE_SEGMENTATION_FMEASUREATTHREE = "Segment boundary recovery evaluation measure @ 3sec";
    public static final String STRUCTURE_SEGMENTATION_PRECRATEATTHREE  = "Segment boundary recovery precision rate @ 3sec";
    public static final String STRUCTURE_SEGMENTATION_RECRATEATTHREE  = "Segment boundary recovery recall rate @ 3sec";
    public static final String STRUCTURE_SEGMENTATION_MEDCLAIM2TRUE = "Median distance from an annotated segment boundary to the closest found boundary";
    public static final String STRUCTURE_SEGMENTATION_MEDTRUE2CLAIM = "Median distance from a found segment boundary to the closest annotated one";
    

    public static final String SALAMI_STRUCTURE_SEGMENTATION_DATA = "SALAMI Structural segmentation";
    
    //Onset Detection Evaluator Constants
    public static final String ONSET_DETECTION_DATA = "List of onset times";
    public static final String ONSET_DETECTION_CLASS = "Instrumentation class for single file";
    public static final String ONSET_DETECTION_CLASSES = "Instrumentation class list for overall evaluation";
    public static final String ONSET_DETECTION_ANNOTATORS = "Annotators";
    public static final String ONSET_DETECTION_AVG_PRECISION = "Average precision";
    public static final String ONSET_DETECTION_AVG_RECALL = "Average recall";
    public static final String ONSET_DETECTION_AVG_FMEASURE = "Average F-measure";
    public static final String ONSET_DETECTION_AVG_PRECISION_BY_CLASS ="Average precision by class";
    public static final String ONSET_DETECTION_AVG_RECALL_BY_CLASS = "Average recall by class";
    public static final String ONSET_DETECTION_AVG_FMEASURE_BY_CLASS = "Average F-measure by class";
    
    //Beat Tracking Evaluator Constants
    public static final String BEAT_TRACKING_DATA = "List of beat times";
    public static final String BEAT_TRACKING_ANNOTATORS = "Annotators";
    public static final String BEAT_TRACKING_FMEASURE ="F-Measure";
    public static final String BEAT_TRACKING_CEMGIL ="Cemgil";
    public static final String BEAT_TRACKING_GOTO = "Goto";
    public static final String BEAT_TRACKING_MCKINNEY = "McKinney P-score";
    public static final String BEAT_TRACKING_CMLC = "CMLc";
    public static final String BEAT_TRACKING_CMLT ="CMLt";
    public static final String BEAT_TRACKING_AMLC = "AMLc";
    public static final String BEAT_TRACKING_AMLT = "AMLt";
    public static final String BEAT_TRACKING_D = "D (bits)";
    public static final String BEAT_TRACKING_DG = "Dg (bits)";
}
