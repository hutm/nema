package org.imirsel.nema.model.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaMetadataEntry;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.fileTypes.BeatTextFile;
import org.imirsel.nema.model.fileTypes.ChordIntervalTextFile;
import org.imirsel.nema.model.fileTypes.ChordNumberTextFile;
import org.imirsel.nema.model.fileTypes.ChordShortHandTextFile;
import org.imirsel.nema.model.fileTypes.ClassificationTextFile;
import org.imirsel.nema.model.fileTypes.KeyTextFile;
import org.imirsel.nema.model.fileTypes.MelodyTextFile;
import org.imirsel.nema.model.fileTypes.MultiF0EstTextFile;
import org.imirsel.nema.model.fileTypes.MultiF0NtTextFile;
import org.imirsel.nema.model.fileTypes.MultipleTrackEvalFileType;
import org.imirsel.nema.model.fileTypes.NemaFileType;
import org.imirsel.nema.model.fileTypes.OnsetTextFile;
import org.imirsel.nema.model.fileTypes.OpaqueDirectoryFormat;
import org.imirsel.nema.model.fileTypes.OpaqueFileFormat;
import org.imirsel.nema.model.fileTypes.RawAudioFile;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;
import org.imirsel.nema.model.fileTypes.StructureTextFile;
import org.imirsel.nema.model.fileTypes.TagAffinityTextFile;
import org.imirsel.nema.model.fileTypes.TagClassificationTextFile;
import org.imirsel.nema.model.fileTypes.TempoTextFile;
import org.imirsel.nema.model.fileTypes.TrackListTextFile;
import org.imirsel.nema.repositoryservice.RepositoryClientInterface;

/**
 * File type conversion utility. Can be used to list the available (registered)
 * file types for each metadata key, convert NemaData Objects to the specified
 * file format (to be written to a specified folder and File handle returned) or
 * to read a specified File and return NemaData Objects representing the 
 * contents.
 * 
 * These facilities are to be used to prepare data for use in the execution of 
 * external binary processes and to harvest the output from the same, whilst
 * preserving as much information as possible.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class FileConversionUtil {

	/**
	 * Registry map for file types available to use as input to binaries 
	 * extracting or predicting the specified metadata key (i.e. doesn't contain 
	 * ground-truth metadata, just identify files to process).
	 */
	public static final Map<String,List<Class<? extends NemaFileType>>> INPUT_FILE_TYPE_REGISTRY = new HashMap<String, List<Class<? extends NemaFileType>>>();
	
	/**
	 * Registry map for file types available to use as output from binaries 
	 * extracting or predicting the specified metadata key and as input training
	 * files (i.e. does contain predicted/ground-truth metadata for the task).
	 */
	public static final Map<String,List<Class<? extends NemaFileType>>> OUTPUT_FILE_TYPE_REGISTRY = new HashMap<String, List<Class<? extends NemaFileType>>>();
	
	/**
	 * Registry map for the file type that each metadata is inserted 
	 * into/retrieved from the repository database in.
	 */
	public static final Map<String,Class<? extends SingleTrackEvalFileType>> REPOSITORY_METADATA_FILE_TYPE_REGISTRY = new HashMap<String, Class<? extends SingleTrackEvalFileType>>();	
	static {
		//register known list file types for known metadata keys
		{
			//setup raw audio file input type
			List<Class<? extends NemaFileType>> rawAudioTypeList = new ArrayList<Class<? extends NemaFileType>>(2);
			rawAudioTypeList.add(RawAudioFile.class);
			rawAudioTypeList.add(TrackListTextFile.class);
			
			//these are tasks where individual audio files are used as input... i.e. there is no list file

			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MELODY_EXTRACTION_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_EST_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_NT_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.KEY_DETECTION_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TEMPO_EXTRACTION_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.ONSET_DETECTION_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.BEAT_TRACKING_DATA, rawAudioTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA, rawAudioTypeList);
			
			//chord systems can have a training stage so may need to handle opaque files
			List<Class<? extends NemaFileType>> chordTypeList = new ArrayList<Class<? extends NemaFileType>>(3);
			chordTypeList.add(RawAudioFile.class);
			chordTypeList.add(TrackListTextFile.class);
			chordTypeList.add(OpaqueFileFormat.class);
			chordTypeList.add(OpaqueDirectoryFormat.class);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CHORD_LABEL_SEQUENCE, chordTypeList);
			
			//classification tasks
			//has a training stage so may need to handle opaque files
			List<Class<? extends NemaFileType>> classificationTypeList = new ArrayList<Class<? extends NemaFileType>>(4);
			classificationTypeList.add(TrackListTextFile.class);
			classificationTypeList.add(ClassificationTextFile.class);
			classificationTypeList.add(OpaqueFileFormat.class);
			classificationTypeList.add(OpaqueDirectoryFormat.class);
			
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ALBUM, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ARTIST, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_TITLE, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_GENRE, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_COMPOSER, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_GENRE, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_COMPOSER, classificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MOOD,classificationTypeList);
			
			//tag classification tasks
			//has a training stage so may need to handle opaque files
			List<Class<? extends NemaFileType>> tagClassificationTypeList = new ArrayList<Class<? extends NemaFileType>>(4);
			tagClassificationTypeList.add(TrackListTextFile.class);
			tagClassificationTypeList.add(TagClassificationTextFile.class);
			tagClassificationTypeList.add(OpaqueFileFormat.class);
			tagClassificationTypeList.add(OpaqueDirectoryFormat.class);
			
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_CLASSIFICATIONS, tagClassificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_MAJORMINER, tagClassificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_MOOD, tagClassificationTypeList);
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_TAGATUNE, tagClassificationTypeList);
			
			//arguments that take opaque file formats (files we don't know how to read but can move around - e.g. model files)
			List<Class<? extends NemaFileType>> opaqueTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			opaqueTypeList.add(OpaqueFileFormat.class); 
			INPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.FILE_DATA, opaqueTypeList);
		}
		
		//register known GT and prediction file types for known metadata keys
		{
			//Chord
			//has a training stage so may need to handle opaque files
			List<Class<? extends NemaFileType>> outputTypeList = new ArrayList<Class<? extends NemaFileType>>(3);
			outputTypeList.add(ChordIntervalTextFile.class);
			outputTypeList.add(ChordNumberTextFile.class);
			outputTypeList.add(ChordShortHandTextFile.class);
			outputTypeList.add(OpaqueFileFormat.class);
			outputTypeList.add(OpaqueDirectoryFormat.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CHORD_LABEL_SEQUENCE, outputTypeList);
			
			//Melody
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(MelodyTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MELODY_EXTRACTION_DATA, outputTypeList);

			//Key
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(KeyTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.KEY_DETECTION_DATA, outputTypeList);
			
			//classification tasks
			//has a training stage so may need to handle opaque files
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(5);
			outputTypeList.add(ClassificationTextFile.class);
			outputTypeList.add(OpaqueFileFormat.class);
			outputTypeList.add(OpaqueDirectoryFormat.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ALBUM, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ARTIST, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_TITLE, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_GENRE, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_COMPOSER, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_GENRE, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_COMPOSER, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MOOD, outputTypeList);
			
			//tag classification tasks
			//has a training stage so may need to handle opaque files
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(3);
			outputTypeList.add(TagClassificationTextFile.class);
			outputTypeList.add(TagAffinityTextFile.class);
			outputTypeList.add(OpaqueFileFormat.class);
			outputTypeList.add(OpaqueDirectoryFormat.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_CLASSIFICATIONS, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_MAJORMINER, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_MOOD, outputTypeList);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TAG_TAGATUNE, outputTypeList);
			
			//Structure
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(StructureTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA, outputTypeList);
			
			//Onset
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(OnsetTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.ONSET_DETECTION_DATA, outputTypeList);
			
			//Beat
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(BeatTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.BEAT_TRACKING_DATA, outputTypeList);
			
			//Multi-F0 Est
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(MultiF0EstTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_EST_DATA, outputTypeList);
			
			//Multi-F0 NT
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(MultiF0NtTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_NT_DATA, outputTypeList);
			
			//Tempo
			outputTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			outputTypeList.add(TempoTextFile.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.TEMPO_EXTRACTION_DATA, outputTypeList);
			
			
			//arguments that produce opaque file formats (files we don't know how to read but can move around - e.g. model files)
			List<Class<? extends NemaFileType>> opaqueTypeList = new ArrayList<Class<? extends NemaFileType>>(1);
			opaqueTypeList.add(OpaqueFileFormat.class);
			opaqueTypeList.add(OpaqueDirectoryFormat.class);
			OUTPUT_FILE_TYPE_REGISTRY.put(NemaDataConstants.FILE_DATA, opaqueTypeList);
		}
		
		//register known repository file types (what type was used to populate repository) for known metadata keys
		{
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.CHORD_LABEL_SEQUENCE, ChordShortHandTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.MELODY_EXTRACTION_DATA, MelodyTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.KEY_DETECTION_DATA, KeyTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA, StructureTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.ONSET_DETECTION_DATA, OnsetTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.BEAT_TRACKING_DATA, BeatTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_EST_DATA, MultiF0EstTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.MULTI_F0_NT_DATA, MultiF0NtTextFile.class);
			REPOSITORY_METADATA_FILE_TYPE_REGISTRY.put(NemaDataConstants.TEMPO_EXTRACTION_DATA, TempoTextFile.class);
			
			//none needed for classification tasks just insert metadata into the field named by the key as a string
		}
		
		
		
	}
	
	/**
	 * Returns the list of file types that may be used as input to
	 * binaries related to a particular task.
	 * 
	 * @param taskId The task ID to retrieve.
	 * @param client The repository client to use to retrieve the task.
	 * @return a List of NemaFileType implementations.
	 * @throws SQLException
	 */
	public static List<Class<? extends NemaFileType>> getInputFileTypesForTask(int taskId, RepositoryClientInterface client) throws SQLException{
		NemaTask task = client.getTask(taskId);
		
		return getTestInputFileTypes(task.getSubjectTrackMetadataName());
	}
	
	/**
	 * Returns the list of file types that may be used as output from
	 * binaries related to a particular task.
	 * 
	 * @param taskId The task ID to retrieve.
	 * @param client The repository client to use to retrieve the task.
	 * @return a List of NemaFileType implementations.
	 * @throws SQLException
	 */
	public static List<Class<? extends NemaFileType>> getOutputFileTypesForTask(int taskId, RepositoryClientInterface client) throws SQLException{
		NemaTask task = client.getTask(taskId);
		return getGTAndPredictionFileTypes(task.getSubjectTrackMetadataName());
	}
	
	/**
	 * Returns a List of NemaFileType implementations that may be used to read
	 * and write test input files for the specified metadata key. The file type
	 * <code>RawAudioFile</code> indicates that the raw audio file should be 
	 * used as an input, which does not need to be processed by the actual 
	 * <code>RawAudioFile</code> file type class.
	 * @param metadataKey The metadata type to retrieve file types for.
	 * @return a List of NemaFileType implementations.
	 */
	public static List<Class<? extends NemaFileType>> getTestInputFileTypes(String metadataKey){
		List<Class<? extends NemaFileType>> types = INPUT_FILE_TYPE_REGISTRY.get(metadataKey);
		if (types == null){
			Logger.getLogger(FileConversionUtil.class.getName()).severe("Failed to retrieve input types for metadata key '" + metadataKey + "', which is not registered!");
		}else if (types.size()==0){
			Logger.getLogger(FileConversionUtil.class.getName()).warning("Retrieved list of 0 input types for metadata key '" + metadataKey + "'!");
		}
		return types; 
	}
	
	/**
	 * Returns a list of NemaFileType implementations that may be used to read
	 * and write ground-truth or prediction files for the specified metadata 
	 * key.
	 * @param metadataKey The metadata type to retrieve file types for.
	 * @return a List of NemaFileType implementations.
	 */
	public static List<Class<? extends NemaFileType>> getGTAndPredictionFileTypes(String metadataKey){
		List<Class<? extends NemaFileType>> types = OUTPUT_FILE_TYPE_REGISTRY.get(metadataKey);
		if (types == null){
			Logger.getLogger(FileConversionUtil.class.getName()).severe("Failed to retrieve input types for metadata key '" + metadataKey + "', which is not registered!");
		}else if (types.size()==0){
			Logger.getLogger(FileConversionUtil.class.getName()).warning("Retrieved list of 0 input types for metadata key '" + metadataKey + "'!");
		}
		return types;
	}
	
	/**
	 * Given a task file format, output directory path, file type and a Map of 
	 * NemaTrackList to a List of NemaData Objects encoding the data relating
	 * to each fold of an experiment this method will prepare files necessary
	 * to encode the input data and return a List of File Objects to required 
	 * to perform each fold of the execution of an external process.
	 * 
	 * Note: NemaData Objects must have been resolved to audio files prior to
	 * executing this method on them. This can be done using a
	 * <code>org.imirsel.nema.repository.RepositoryClientImpl</code> instance
	 * (from the nema-repository project) and the 
	 * <code>resolveTracksToFiles(List<NemaData> trackDataList, Set<NemaMetadataEntry> constraint)</code>
	 * method.
	 * 
	 * @param outputDirectory Path to write any files created to.
	 * @param task The NemaTask to be performed on the data.
	 * @param executionData Map of NemaTrackList to a List of NemaData Objects
	 * encoding the data to be worked on.
	 * @param fileType The file type to be used to prepare the data. Note that 
	 * the RawAudioFile type will cause the list of audio files to be written
	 * @return Map of NemaTrackList to a list of File Objects representing the 
	 * files to be used as inputs to the process.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file writer can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found or created.
	 * @throws IOException Thrown if there is a problem writing the files to
	 * disk.
	 */
	public static Map<NemaTrackList,List<String>> prepareProcessInput(
			File outputDirectory, 
			NemaTask task,
			Map<NemaTrackList,List<NemaData>> executionData, 
			Class<? extends NemaFileType> fileType
			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
		
		Map<NemaTrackList,List<String>> out = null;
		if (SingleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			if(fileType.equals(RawAudioFile.class)) {
				//use raw audio files
				out = getResourceFilesList(executionData,task,outputDirectory);
				return out;
			}else {
				//write directory of metadata files - ignore isTrainingRun flag 
				  //as there is nothing else we can other than write the 
				  //specified file (e.g. QBT writes out timestamps of users 
				  //tap times which are used as queries - this may eventually 
				  //happen in other tasks)
				out = new HashMap<NemaTrackList,List<String>>(executionData.size());
				for (Iterator<NemaTrackList> iterator = executionData.keySet().iterator(); iterator
						.hasNext();) {
					NemaTrackList testSet = iterator.next();
					File dir = writeGroundTruthDataFileOrDirectory(testSet,executionData.get(testSet),task, fileType, outputDirectory);
					List<File> files = Arrays.asList(dir.listFiles());
					List<String> paths = new ArrayList<String>(files.size());
					for(File file:files){
						paths.add(file.getPath());
					}
					out.put(testSet, paths);
				}
			}
		}else if(MultipleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			out = new HashMap<NemaTrackList,List<String>>(executionData.size());
			for (Iterator<NemaTrackList> iterator = executionData.keySet().iterator(); iterator
			.hasNext();) {
				NemaTrackList testSet = iterator.next();
				File file = writeGroundTruthDataFileOrDirectory(testSet,executionData.get(testSet),task, fileType, outputDirectory);
				List<String> paths = new ArrayList<String>(1);
				paths.add(file.getPath());
				out.put(testSet, paths);
			}
		}
		return out;
	}
	
	/**
	 * Given a task file format, output directory path, file type and a Map of 
	 * NemaTrackList to a List of NemaData Objects encoding the data relating
	 * to each fold of an experiment this method will prepare files necessary
	 * to encode the input data and return a List of File Objects required 
	 * to perform each fold of the execution of an external process.
	 * 
     *
	 * Note: NemaData Objects must have been resolved to audio files prior to
	 * executing this method on them. This can be done using a
	 * <code>org.imirsel.nema.repository.RepositoryClientImpl</code> instance
	 * (from the nema-repository project) and the 
	 * <code>resolveTracksToFiles(List<NemaData> trackDataList, 
	 * Set<NemaMetadataEntry> constraint)</code> method. This resolution will
	 * append the file path and site name to the Object.
	 * 
	 * @param outputDirectory Path to write any files created to.
	 * @param task The NemaTask to be performed on the data.
	 * @param executionData Map of NemaTrackList to a List of NemaData Objects
	 * encoding the data to be worked on.
	 * @param fileType The file type to be used to prepare the data. Note that 
	 * the RawAudioFile type will cause the list of audio files to be written.
	 * @param defaultSite Where files are created from the DB, mark them as
	 * available at this default site.
	 * @return Map of site name to a Map of NemaTrackList to a list of paths 
	 * representing the files to be used as inputs to the process.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file writer can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found or created.
	 * @throws IOException Thrown if there is a problem writing the files to
	 * disk.
	 */
	public static Map<String,Map<NemaTrackList,List<String>>> prepareOmenProcessInput(
			File outputDirectory, 
			NemaTask task,
			Map<NemaTrackList,List<NemaData>> executionData, 
			Class<? extends NemaFileType> fileType,
			String defaultSite
			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
		
		Map<String,Map<NemaTrackList,List<String>>> out = null;
		
	
		if(fileType.equals(RawAudioFile.class)) {
			//use raw audio files
			out = getOmenResourceFilesList(executionData,task,outputDirectory);
			return out;
		}else {
			
//			//Nothing else supported currently as the file created would have to be marshalled over to the remote site
//			throw new IllegalArgumentException("Files of type '" + fileType.getName() + "' are not currently supported for remote execution. " +
//					"Only processes taking '" + RawAudioFile.class.getName() + "' as input are supported.");
			boolean isSingleTrackType = false;
			if (SingleTrackEvalFileType.class.isAssignableFrom(fileType)) {
				isSingleTrackType = true;
			}
			
			//write directory of metadata files
			out = new HashMap<String,Map<NemaTrackList,List<String>>>();
			for (Iterator<NemaTrackList> iterator = executionData.keySet().iterator(); iterator.hasNext();) {
				NemaTrackList testSet = iterator.next();
				List<NemaData> data = executionData.get(testSet);
				
				//filter by site
				Map<String,List<NemaData>> siteDataMap = partitionBySite(data,defaultSite);
				
				//create data files separately for each site
				for (Iterator<String> it2 = siteDataMap.keySet().iterator(); it2.hasNext();) {
					String site = it2.next();
					
					//NB: will need to send these files to remote site...
					File fileOrDir = writeGroundTruthDataFileOrDirectory(testSet, data, task, fileType, outputDirectory);
					List<String> filePaths;
					if(isSingleTrackType){
						File[] fileArr = fileOrDir.listFiles();
						filePaths = new ArrayList<String>(fileArr.length);
						for (int i = 0; i < fileArr.length; i++) {
							filePaths.add(fileArr[i].getAbsolutePath());
						}
					}else{ //MultipleTrackEvalFileType.class.isAssignableFrom(fileType) == true
						filePaths = new ArrayList<String>(1);
						filePaths.add(fileOrDir.getAbsolutePath());
					}
					Map<NemaTrackList,List<String>> siteOut = out.get(site);
					if(siteOut == null){
						siteOut = new HashMap<NemaTrackList,List<String>>();
						out.put(site,siteOut);
					}
					siteOut.put(testSet, filePaths);
				}
			}
		}
		
		return out;
	}
	
	private static Map<String,List<NemaData>> partitionBySite(List<NemaData> list, String defaultSite){
		Map<String,List<NemaData>> out = new HashMap<String,List<NemaData>>();
		for (Iterator<NemaData> iterator = list.iterator(); iterator.hasNext();) {
			NemaData nemaData = iterator.next();
			String site = nemaData.getStringMetadata(NemaDataConstants.PROP_FILE_SITE);
			if (site == null){
				site = defaultSite;
			}
			List<NemaData> siteList = out.get(site);
			if (siteList == null){
				siteList = new ArrayList<NemaData>();
				out.put(site, siteList);
			}
			siteList.add(nemaData);
		}
		
		return out;
	}
	
	/**
	 * Based on a set of inputs that will be used to execute a process, a chosen
	 * output file format, an output directory and a file name extension to use,
	 * this method constructs output file or directory names to use to execute 
	 * the process.
	 * 
	 * The data-structure returned is the same as that used by the 
	 * <code>readProcessOutput</code> method and hence after being used to 
	 * execute the process, this structure maybe used to read the data files
	 * produced back in.
	 * 
	 * @param executionData The inputs that will be sent to the process (e.g.
	 * audio files to process, IDs of the test sets etc.).
	 * @param outputFileTypeInstance The file type to use to read the files or 
	 * directories.
	 * @param outputFileExt The extension to append to filenames created.
	 * @param outputDirectory The directory to create the output files in.
	 * @return Map of NemaTrackList to a list of File Objects representing the 
	 * files to be created.
	 */
	public static Map<NemaTrackList,List<File>> createOutputFileNames(
			Map<NemaTrackList,List<NemaData>> executionData, 
			Class<? extends NemaFileType> inputType,
			NemaFileType outputFileTypeInstance,
			String outputDirectory
			) {
		
		Map<NemaTrackList,List<File>> out = new HashMap<NemaTrackList,List<File>>(executionData.size());
		
		for (Iterator<NemaTrackList> iterator = executionData.keySet().iterator(); iterator.hasNext();) {
			
			List<File> list = new ArrayList<File>();
			NemaTrackList testSet = iterator.next();
			
			/*If the input has data on multiple tracks per file and the output 
			 * output file type contains data on a single track per file, we are 
			 * probably going to receive a directory of output files with 
			 * unspecified names, otherwise its one in one out).
			*/
			if(OpaqueDirectoryFormat.class.isAssignableFrom(outputFileTypeInstance.getClass())){
				//directory
				File foldDir = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out");
				foldDir.mkdirs();
				list.add(foldDir);
				
			}else if(OpaqueFileFormat.class.isAssignableFrom(outputFileTypeInstance.getClass())){
				//create directory of metadata or new raw audio files
				File foldDir = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out");
				foldDir.mkdirs();
				List<NemaData> data = executionData.get(testSet);
				for (Iterator<NemaData> nemaDataIt = data.iterator(); nemaDataIt
						.hasNext();) {
					File fileLoc = new File(nemaDataIt.next().getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
					String name = fileLoc.getName();
					File newPath = new File(foldDir.getAbsolutePath() + File.separator + name + outputFileTypeInstance.getFilenameExtension());
					list.add(newPath);
				}
				
			}else if(!MultipleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass()) && 
					MultipleTrackEvalFileType.class.isAssignableFrom(inputType)){
				//directory
				File foldDir = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out");
				foldDir.mkdirs();
				list.add(foldDir);
				
			}else{
				//files
				List<NemaData> data = executionData.get(testSet);
				
				if (SingleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass())) {
					//create directory of metadata or new raw audio files
					File foldDir = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out");
					foldDir.mkdirs();
					
					
					for (Iterator<NemaData> nemaDataIt = data.iterator(); nemaDataIt
							.hasNext();) {
						File fileLoc = new File(nemaDataIt.next().getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
						String name = fileLoc.getName();
						File newPath = new File(foldDir.getAbsolutePath() + File.separator + name + outputFileTypeInstance.getFilenameExtension());
						list.add(newPath);
					}
				}else if(MultipleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass())) {
					//create one output file per fold
					list = new ArrayList<File>(1);
					File newPath = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out" + outputFileTypeInstance.getFilenameExtension());
					list.add(newPath);
				}
			}
			
			
			
			out.put(testSet, list);
		}
		
		return out;
	}
	
	/**
	 * Based on a set of inputs that will be used to execute a process, a chosen
	 * output file format, an output directory and a file name extension to use,
	 * this method constructs output file or directory names to use to execute 
	 * the process. The paths a divided into maps according to the site that
	 * the execution will be performed at.
	 * 
	 * The data-structure returned is the same as that used by the 
	 * <code>readProcessOutput</code> method and hence after being used to 
	 * execute the process, this structure maybe used to read the data files
	 * produced back in.
	 * 
	 * @param executionData The inputs that will be sent to the process (e.g.
	 * audio files to process, IDs of the test sets etc.).
	 * @param outputFileTypeInstance The file type to use to read the files or 
	 * directories.
	 * @param outputFileExt The extension to append to filenames created.
	 * @param outputDirectory The directory to create the output files in.
	 * @return Map of site name to a Map of NemaTrackList to a list of File
	 * Objects representing the files to be created.
	 */
	public static Map<String,Map<NemaTrackList,List<File>>> createOMENOutputFileNames(
			Map<NemaTrackList,List<NemaData>> executionData, 
			Class<? extends NemaFileType> inputType,
			NemaFileType outputFileTypeInstance,
			String outputDirectory
			) {		 
		
		Map<String,Map<NemaTrackList,List<File>>> out = new HashMap<String,Map<NemaTrackList,List<File>>>(5);
		
		//iterate through folds
		for (Iterator<NemaTrackList> iterator = executionData.keySet().iterator(); iterator.hasNext();) {
			NemaTrackList testSet = iterator.next();
			Map<NemaTrackList,List<File>> aSiteMap;
			List<File> fileList;
			
			//files
			List<NemaData> data = executionData.get(testSet);
			
			//create directory of metadata or new raw audio files
			File foldDir = new File(outputDirectory + File.separator +"set-" + testSet.getId() + "-out");
			foldDir.mkdirs();
			
			//divide data according to sites
			Map<String,List<NemaData>> siteToData = new HashMap<String, List<NemaData>>();
			for (Iterator<NemaData> nemaDataIt = data.iterator(); nemaDataIt.hasNext();) {
				NemaData anItem = nemaDataIt.next();
				String site = anItem.getStringMetadata(NemaDataConstants.PROP_FILE_SITE);
				List<NemaData> siteList = siteToData.get(site); 
				if(siteList == null){
					siteList = new ArrayList<NemaData>();
					siteToData.put(site,siteList);
				}
				siteList.add(anItem);
			}
			
			//iterate through sites
			for (Iterator<String> siteIt = siteToData.keySet().iterator(); siteIt.hasNext();) {
				String site = siteIt.next();
				List<NemaData> siteList = siteToData.get(site); 
				aSiteMap = out.get(site);
				if(aSiteMap == null){
					aSiteMap = new HashMap<NemaTrackList, List<File>>();
					out.put(site, aSiteMap);
				}
				fileList = aSiteMap.get(testSet);
				if (fileList == null){
					fileList = new ArrayList<File>();
					aSiteMap.put(testSet, fileList);
				}
				
				/*If the input has data on multiple tracks per file and the output 
				 * output file type contains data on a single track per file, we are 
				 * probably going to receive a directory of output files with 
				 * unspecified names, otherwise its one in one out).
				*/
				if(OpaqueDirectoryFormat.class.isAssignableFrom(outputFileTypeInstance.getClass())){
					//directory
					fileList.add(foldDir);
				}else if(OpaqueFileFormat.class.isAssignableFrom(outputFileTypeInstance.getClass())){
					//create directory of metadata or new raw audio files
					for (Iterator<NemaData> nemaDataIt = siteList.iterator(); nemaDataIt.hasNext();) {
						File fileLoc = new File(nemaDataIt.next().getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
						String name = fileLoc.getName();
						File newPath = new File(foldDir.getPath() + File.separator + name + outputFileTypeInstance.getFilenameExtension());
						fileList.add(newPath);
					}
				}else if(!MultipleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass()) && 
						MultipleTrackEvalFileType.class.isAssignableFrom(inputType)){
					//directory
					fileList.add(foldDir);
				}else{
					//files
					if (SingleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass())) {
						//create directory of metadata or new raw audio files
						for (Iterator<NemaData> nemaDataIt = siteList.iterator(); nemaDataIt.hasNext();) {
							File fileLoc = new File(nemaDataIt.next().getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
							String name = fileLoc.getName();
							File newPath = new File(foldDir.getPath() + File.separator + name + outputFileTypeInstance.getFilenameExtension());
							fileList.add(newPath);
						}
					}else if(MultipleTrackEvalFileType.class.isAssignableFrom(outputFileTypeInstance.getClass())) {
						//create one output file per fold
						File newPath = new File(foldDir.getPath() + File.separator +"set-" + testSet.getId() + outputFileTypeInstance.getFilenameExtension());
						fileList.add(newPath);
					}
				}
				
			}
		}
		return out;
	}

	
	/**
	 * Converts metadata entries retrieved from the repository into a list of
	 * NemaData Object models by interpretting the data through a file type
	 * registered for the repository on the task metadata.
	 * 
	 * @param trackToMeta Map of track ID to the List of NemaMetadataEntry
	 * Objects retrieved from the repository for it.
	 * @param task The task that the data relates to ().
	 * @return a List of NemaData Objects encoding the desired data for each 
	 * track.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file reader can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found.
	 * @throws IOException Thrown if there is a problem reading a file.
	 */
	public static List<NemaData> convertMetadataToGroundtruthModel(
			Map<String,List<NemaMetadataEntry>> trackToMeta, 
			NemaTask task) 
			throws IllegalArgumentException, InstantiationException, 
				IllegalAccessException, FileNotFoundException, IOException{
	
		List<NemaData> out = new ArrayList<NemaData>(trackToMeta.size());
	
		//get and instantiate reader file type
		String metadataType = task.getSubjectTrackMetadataName();
		Class<? extends SingleTrackEvalFileType> readerClass = REPOSITORY_METADATA_FILE_TYPE_REGISTRY.get(metadataType);
		if (readerClass == null){
			//a classification task has no repository file type
			for (Iterator<String> it = trackToMeta.keySet().iterator(); it.hasNext();){
	            String id = it.next();
	            NemaData aTrack = new NemaData(id);
	            List<NemaMetadataEntry> meta_list = trackToMeta.get(id);
	            if (meta_list.size() > 1){
	            	List<String> vals = new ArrayList<String>(meta_list.size());
	            	for (Iterator<NemaMetadataEntry> iterator = meta_list.iterator(); iterator.hasNext();) {
	            		vals.add(iterator.next().getValue());
					}
	            	aTrack.setMetadata(metadataType, vals);
	            }else if(meta_list.size() == 1){
	            	aTrack.setMetadata(metadataType, meta_list.get(0).getValue());
	            }else{
	            	Logger.getLogger(FileConversionUtil.class.getName()).severe("No metadata " + metadataType + " found for id '" + id + "', it is being dropped!");
	            }
	            
	            out.add(aTrack);
			}
		}else{
			SingleTrackEvalFileType reader = readerClass.newInstance();
			for (Iterator<String> it = trackToMeta.keySet().iterator(); it.hasNext();){
	            String id = it.next();
	            
	            List<NemaMetadataEntry> meta_list = trackToMeta.get(id);
	            NemaData aTrack;
	            if (meta_list.size() > 0){
	            	NemaMetadataEntry metadata = meta_list.iterator().next();
	            	
	            	File temp = File.createTempFile(id + ".", ".txt");
	        		BufferedWriter tmpOut = new BufferedWriter(new FileWriter(temp));
	        		tmpOut.write(metadata.getValue());
	        		tmpOut.flush();
	        		tmpOut.close();
	        		
	        		aTrack = reader.readFile(temp);
	        		aTrack.setMetadata(NemaDataConstants.PROP_ID, id);
	            	
	            	if(meta_list.size() > 1) {
	            		//use only first value and print warning
	            		Logger.getLogger(FileConversionUtil.class.getName()).warning("Found " + meta_list.size() + " " + metadataType + " metadata records for id '" + id + "', using only first value");
	            	}
	            }else {
	            	aTrack = new NemaData(id);
	            	Logger.getLogger(FileConversionUtil.class.getName()).severe("No metadata " + metadataType + " found for id '" + id + "', it is being dropped!");
	            }
	            out.add(aTrack);
			}
		}
		
		return out;
	}
	
	
	
	/**
	 * Reads the files output by a process using the specified file type. The
	 * first parameter is a Map of NemaTrackList (each representing
	 * a fold of the experiment) to a list of File Objects representing files or 
	 * directories on disk in the specified file format, which encode data about 
	 * a track or tracks (according to the file type). This is the same 
	 * data-structure as that returned by the <code>createOutputFileNames</code> 
	 * method and hence this may be passed unmodified to read back in the data 
	 * after a process has been run to generate data files at the specified 
	 * paths.
	 * 
	 * @param filesOrDirectoriesToRead Map of NemaTrackList to a list of File 
	 * Objects representing the files to be read in.
	 * @param task The task that the data relates (only required for 
	 * classification file types).
	 * @param fileType The file type to use to read the files or directories.
	 * @return A Map of NemaTrackList to a List of NemaData Objects encoding the 
	 * data read from each file or directory.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file reader can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found.
	 * @throws IOException Thrown if there is a problem reading a file.
	 */
	public static Map<NemaTrackList,List<NemaData>> readProcessOutput(
			Map<NemaTrackList,List<File>> filesOrDirectoriesToRead, 
			NemaTask task, 
			Class<? extends NemaFileType> fileType) 
			throws IllegalArgumentException, InstantiationException, 
				IllegalAccessException, FileNotFoundException, IOException{
		Map<NemaTrackList,List<NemaData>> out = new HashMap<NemaTrackList,List<NemaData>>(filesOrDirectoriesToRead.size());
		for (Iterator<NemaTrackList> iterator = filesOrDirectoriesToRead.keySet().iterator(); iterator.hasNext();) {
			NemaTrackList testSet = iterator.next();
			List<File> files = filesOrDirectoriesToRead.get(testSet);
			//read all files relevant to the fold and merge into single list for fold
			List<NemaData> dataRead = new ArrayList<NemaData>();
			for (Iterator<File> fileIt = files.iterator(); fileIt.hasNext();) {
				File file = fileIt.next();
				List<NemaData> data = readData(file, task, fileType);
				dataRead.addAll(data);
			}
			out.put(testSet, dataRead);
		}
		return out;
	}
	
	/**
	 * Reads data from either a file (for file formats that encode data about
	 * multiple files) or a directory (for file formats that encode data about
	 * a single track per file).
	 * 
	 * @param fileOrDirectoryToRead The file or directory to read.
	 * @param task The task that the data relates (only required for 
	 * classification evaluators).
	 * @param fileType The file type to use to read the file or directory.
	 * @return A List of NemaData Objects encoding the data read.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file reader can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if the file or directory cannot be 
	 * found.
	 * @throws IOException Thrown if there is a problem reading the file.
	 */
	public static List<NemaData> readData(
			File fileOrDirectoryToRead, 
			NemaTask task, 
			Class<? extends NemaFileType> fileType
			) throws IllegalArgumentException, InstantiationException, IllegalAccessException, FileNotFoundException, IOException{
		if (SingleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			SingleTrackEvalFileType typeInstance = (SingleTrackEvalFileType)fileType.newInstance();
			return typeInstance.readDirectory(fileOrDirectoryToRead, null);
		}else if(MultipleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			MultipleTrackEvalFileType typeInstance = (MultipleTrackEvalFileType)fileType.newInstance();
			//set type for classification evaluator
			if (ClassificationTextFile.class.equals(fileType)) {
				((ClassificationTextFile)typeInstance).setMetadataType(task.getSubjectTrackMetadataName());
			}
			return typeInstance.readFile(fileOrDirectoryToRead);
		}else {
			throw new IllegalArgumentException("Unrecognized file type: " + fileType.getName());
		}
	}
	
	/**
	 * Writes the list of ground-truth NemaData Objects to either a single file or 
	 * a directory of files (as appropriate to the file type). I.e. if the file
	 * type encodes data about multiple NemaData Objects in a single file the 
	 * path to the file written is returned. If the file type encodes data about 
	 * a single NemaData Object per file then a directory is created, files are 
	 * named using the the ID of each NemaData Object, written to the directory
	 * created and the path to the directory returned.
	 * 
	 * Note: if multiple files are written then it is likely that any extraction
	 * or estimation run on these files should be executed once per File as no 
	 * list file is written.
	 * 
	 * @param fold The track list being written out, used to name the output 
	 * file or directory.
	 * @param data The List of NemaData Objects to write.
	 * @param task The task that the data relates to (primarily used to type 
	 * ClassificationEvaluator.
	 * @param fileType The file type to use to write the ground-truth files.
	 * @param outputDirectory The directory to create the output in.
	 * @return A File representing the file or directory written out.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file writer can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found or created.
	 * @throws IOException Thrown if there is a problem writing the files to
	 * disk.
	 */
	public static File writeGroundTruthDataFileOrDirectory(
			NemaTrackList fold, 
			List<NemaData> data, 
			NemaTask task, 
			Class<? extends NemaFileType> fileType, 
			File outputDirectory
			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
		//mint a file or directory path
		File outputLocation = new File(outputDirectory.getAbsolutePath() + File.separator + "set-" + fold.getId());
		if (SingleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			SingleTrackEvalFileType typeInstance = (SingleTrackEvalFileType)fileType.newInstance();
			//create dir to store files
			outputLocation.mkdirs();
			//write each file using id as name
			for (Iterator<NemaData> iterator = data.iterator(); iterator.hasNext();) {
				NemaData nemaData = iterator.next();
				File file = new File(outputLocation.getAbsolutePath() + File.separator + nemaData.getId() + typeInstance.getFilenameExtension());
				typeInstance.writeFile(file, nemaData);
			}
			//return dir created
			return outputLocation;
		}else if(MultipleTrackEvalFileType.class.isAssignableFrom(fileType)) {
			//write a single file
			MultipleTrackEvalFileType typeInstance = (MultipleTrackEvalFileType)fileType.newInstance();
			outputLocation = new File(outputLocation.getAbsolutePath() + typeInstance.getFilenameExtension());
			//set type for classification evaluator
			if (ClassificationTextFile.class.equals(fileType)) {
				((ClassificationTextFile)typeInstance).setMetadataType(task.getSubjectTrackMetadataName());
			}
			//write single file and return
			typeInstance.writeFile(outputLocation, data);
			return outputLocation;
		}else {
			throw new IllegalArgumentException("Unrecognized file type: " + fileType.getName());
		}
	}
	
	/**
	 * Retrieves a A Map of NemaTrackList to a List of String paths representing
	 * the resources referred to in the lists of NemaData Objects in the input 
	 * data-structure.
	 * 
	 * Note: most, perhaps all, tasks which use file formats encoding data about
	 * a single track take a file at a time as input (i.e. they don't use a list 
	 * file). 
	 * 
	 * @param testData A Map of NemaTrackList to a List of NemaData Objects 
	 * encoding the data to write to each file or directory.
	 * @param task The task that the data relates (only required for 
	 * classification file types).
	 * @param outputDirectory The directory to write the data files to.
	 * @return A Map of NemaTrackList to a List of String paths representing
	 * the resources.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file writer can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found or created.
	 */
	public static Map<NemaTrackList,List<String>> getResourceFilesList(
			Map<NemaTrackList,List<NemaData>> testData, 
			NemaTask task, 
			File outputDirectory
			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
		Map<NemaTrackList,List<String>> out = new HashMap<NemaTrackList,List<String>>();
		
		for (Iterator<NemaTrackList> iterator = testData.keySet().iterator(); iterator.hasNext();) {
			NemaTrackList testSet = iterator.next();
			List<NemaData> data = testData.get(testSet);
			List<String> paths = new ArrayList<String>(data.size());
			for (Iterator<NemaData> iterator2 = data.iterator(); iterator2
					.hasNext();) {
				paths.add(iterator2.next().getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
			}
			out.put(testSet,paths);
		}
	
		return out;
	}

	/**
	 * Retrieves a Map of site name to a Map of NemaTrackList to a List of File 
	 * Objects representing the resources referred to in the lists of NemaData 
	 * Objects in the input data-structure.
	 * 
	 * Note: most, perhaps all, tasks which use file formats encoding data about
	 * a single track take a file at a time as input (i.e. they don't use a list 
	 * file). 
	 * 
	 * @param testData A Map of NemaTrackList to a List of NemaData Objects 
	 * encoding the data to write to each file or directory.
	 * @param task The task that the data relates (only required for 
	 * classification file types).
	 * @param outputDirectory The directory to write the data files to.
	 * @return a Map of site name to a Map of NemaTrackList to a List of File 
	 * Objects representing the resources.
	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
	 * NemaFileType is received.
	 * @throws InstantiationException Thrown if the file writer can't be 
	 * instantiated (for example if there is no zero-arg constructor).
	 * @throws IllegalAccessException Thrown if we do not have access to the 
	 * definition of the specified file type class.
	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
	 * found or created.
	 */
	public static Map<String,Map<NemaTrackList,List<String>>> getOmenResourceFilesList(
			Map<NemaTrackList,List<NemaData>> testData, 
			NemaTask task, 
			File outputDirectory
			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
		
		Map<String,Map<NemaTrackList,List<String>>> out = new HashMap<String,Map<NemaTrackList,List<String>>>();
		
		for (Iterator<NemaTrackList> iterator = testData.keySet().iterator(); iterator.hasNext();) {
			NemaTrackList testSet = iterator.next();
			List<NemaData> data = testData.get(testSet);
			
			for (Iterator<NemaData> iterator2 = data.iterator(); iterator2
					.hasNext();) {
				NemaData anItem = iterator2.next();
				String site = anItem.getStringMetadata(NemaDataConstants.PROP_FILE_SITE);
				Map<NemaTrackList,List<String>> siteMap = out.get(site);
				if(siteMap == null){
					siteMap = new HashMap<NemaTrackList, List<String>>();
					out.put(site, siteMap);
				}
				
				List<String> paths = siteMap.get(testSet);
				if (paths == null){
					paths = new ArrayList<String>();
					siteMap.put(testSet, paths);
				}
				paths.add(anItem.getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
			}
		}
	
		return out;
	}
	
//		
//	/**
//	 * Writes out a list of test files (one per fold of the experiment) that 
//	 * encodes paths for more than one track . 
//	 * 
//	 * @param testData A Map of NemaTrackList to a List of NemaData Objects 
//	 * encoding the data to write to each file or directory.
//	 * @param task The task that the data relates (only required for 
//	 * classification file types).
//	 * @param fileType The file type to use to write the list files.
//	 * @param outputDirectory The directory to write the data files to.
//	 * @return 
//	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
//	 * NemaFileType is received.
//	 * @throws InstantiationException Thrown if the file writer can't be 
//	 * instantiated (for example if there is no zero-arg constructor).
//	 * @throws IllegalAccessException Thrown if we do not have access to the 
//	 * definition of the specified file type class.
//	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
//	 * found or created.
//	 * @throws IOException Thrown if there is a problem writing the files to
//	 * disk.
//	 */
//	public static Map<NemaTrackList,File> writeTestListFiles(
//			Map<NemaTrackList,List<NemaData>> testData, 
//			NemaTask task, 
//			Class<? extends MultipleTrackEvalFileType> fileType, 
//			File outputDirectory
//			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
//		Map<NemaTrackList,File> out = new HashMap<NemaTrackList,File>();
//		
//		for (Iterator<NemaTrackList> iterator = testData.keySet().iterator(); iterator.hasNext();) {
//			NemaTrackList testSet = iterator.next();
//			List<NemaData> data = testData.get(testSet);
//			File written = writeTestListFile(testSet,data,task,fileType,outputDirectory);
//			out.put(testSet,written);
//		}
//	
//		return out;
//	}
//	
//	/**
//	 * Writes out a single list
//	 * @param testData a List of NemaData Objects 
//	 * encoding the data to write to the file or directory.
//	 * @param task The task that the data relates (only required for 
//	 * classification file types).
//	 * @param fileType The file type to use to write the list files.
//	 * @param outputDirectory The directory to write the data files to.
//	 * @return 
//	 * @throws IllegalArgumentException Thrown if an unknown sub-interface of 
//	 * NemaFileType is received.
//	 * @throws InstantiationException Thrown if the file writer can't be 
//	 * instantiated (for example if there is no zero-arg constructor).
//	 * @throws IllegalAccessException Thrown if we do not have access to the 
//	 * definition of the specified file type class.
//	 * @throws FileNotFoundException Thrown if a file or directory cannot be 
//	 * found or created.
//	 * @throws IOException Thrown if there is a problem writing the files to
//	 * disk.
//	 */
//	public static File writeTestListFile(
//			NemaTrackList testSet, 
//			List<NemaData> testData, 
//			NemaTask task, 
//			Class<? extends MultipleTrackEvalFileType> fileType, 
//			File outputDirectory
//			) throws IllegalArgumentException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
//		if(MultipleTrackEvalFileType.class.isAssignableFrom(fileType)) {
//			MultipleTrackEvalFileType typeInstance = (MultipleTrackEvalFileType)fileType.newInstance();
//			//set type for classification evaluator
//			if (ClassificationTextFile.class.equals(fileType)) {
//				((ClassificationTextFile)typeInstance).setMetadataType(task.getSubjectTrackMetadataName());
//			}
//			
//			//mint some sort of unique file name
//			File toWriteTo = new File(outputDirectory.getAbsolutePath() + File.separator + "test-set-" + testSet.getId());
//			typeInstance.writeFile(toWriteTo, testData);
//			return toWriteTo;
//		}else {
//			throw new IllegalArgumentException("Unrecognized file type for test data files: " + fileType.getName());
//		}
//		
//	}
//
//		
	
	
}
