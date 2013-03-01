/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.repositoryservice;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.imirsel.nema.model.NemaCollection;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaFile;
import org.imirsel.nema.model.NemaMetadataEntry;
import org.imirsel.nema.model.NemaSubmission;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaPublishedResult;
import org.imirsel.nema.model.fileTypes.NemaFileType;

/**
 * An interface defining the methods of a query client for the temporary NEMA metadata
 * repository. Additionally, result publication methods are defined.
 *
 * @author kris.west@gmail.com
 */
public interface RepositoryClientInterface {
	//TODO update DB and Client to support Tasks
    /**
     * Close any DB connections held.
     */
    public void close();

    /**
     * Check if the client is connected and valid.
     * @return Boolean flag indicating whether the client is still valid.
     */
    public boolean isValid();
    
    /**
     * Retrieves a list of NemaCollection Objects describing the available
     * collections.
     *
     * @return a list of NemaCollection Objects describing the available
     * collections.
     *
     * @throws SQLException
     */
    public List<NemaCollection> getCollections() throws SQLException;

    /**
     * Retrieves a Set containing Sets of NemaMetadataEntry Objects which define
     * the different file types that files in the collection are available in.
     * The inner set defines a unique combination of different metadata values
     * that appears in the collection. If one of more files with a particular
     * combination of metadata values appears in the specified collection that
     * combination will be returned by this method.
     *
     * @param collection The collection to retrieve the Set of versions for.
     *
     * @return A Set containing Sets of NemaMetadataEntry Objects which define
     * the different file types that files in the collection are available in.
     *
     * @throws SQLException
     */
    public List<List<NemaMetadataEntry>> getCollectionVersions(NemaCollection collection) throws SQLException;
    /**
     * Retrieves a Set containing Sets of NemaMetadataEntry Objects which define
     * the different file types that files in the collection are available in.
     * The inner set defines a unique combination of different metadata values
     * that appears in the collection. If one of more files with a particular
     * combination of metadata values appears in the specified collection that
     * combination will be returned by this method.
     *
     * @param collectionId The collection id to retrieve the Set of versions for.
     *
     * @return A Set containing Sets of NemaMetadataEntry Objects which define
     * the different file types that files in the collection are available in.
     *
     * @throws SQLException
     */
    public List<List<NemaMetadataEntry>> getCollectionVersions(int collectionId) throws SQLException;


    /**
     * Retrieves a List of NemaDataset Objects describing the datasets that are
     * available. No guarantee is given that the
     * datasets described have complete audio file sets in any particular
     * file version.
     *
     * @return a List of NemaDataset Objects describing the datasets that are
     * available.
     *
     * @throws SQLException
     */
    public List<NemaDataset> getDatasets() throws SQLException;

    /**
     * Retrieves a NemaDataset Object describing the requested dataset. No 
     * guarantee is given that the datasets described has a complete audio file 
     * set in any particular file version.
     * @param dataset_id The dataset id to retrieve.
     *
     * @return a List of NemaDataset Objects describing the datasets that are
     * available.
     *
     * @throws SQLException
     */
    public NemaDataset getDataset(int dataset_id) throws SQLException;
    
    /**
     * Retrieves a List of NemaTask Objects describing the tasks that are
     * available. No guarantee is given that the tasks described have 
     * complete audio file sets in any particular file version.
     *
     * @return a List of NemaTask Objects describing the tasks that are
     * available.
     *
     * @throws SQLException
     */
    public List<NemaTask> getTasks() throws SQLException;
    
    /**
     * Retrieves a List of NemaTask Objects describing the tasks that are
     * available for the specified track metadata id. No guarantee is given 
     * that the tasks described have complete audio file sets in any particular 
     * file version.
     *
     * @return a List of NemaTask Objects describing the tasks that are
     * available.
     * @param id The track metadata id to retrieve tasks for.
     * @throws SQLException
     */
    public List<NemaTask> getTasks(int id) throws SQLException;

    /**
     * Retrieves a NemaTask Object describing the requested task. No guarantee 
     * is given that the task described has a complete audio file set in any 
     * particular file version.
     * 
     * @param task_id the task id to retrieve.
     *
     * @return a List of NemaDataset Objects describing the datasets that are
     * available for the specified Collection.
     *
     * @throws SQLException
     */
    public NemaTask getTask(int task_id) throws SQLException;

    /**
     * Retrieves a NemaTrackList Object describing the set of tracks that
     * is relevant to a dataset. This may be resolved to a list of tracks using
     * <code>getTracks(NemaTrackList set)</code> and that to a list of files using
     * <code>getFiles(List<NemaTrack> trackList, Set<NemaMetadataEntry> constraint)</code>.
     * Finally, the file type versions (each defined by a set of metadata
     * entries) that the dataset is available in may be retrieved using
     * <code>getTrackListVersions(NemaTrackList set)</code>.
     *
     * @param dataset The dataset to retrieve the collection subset for.
     *
     * @return A NemaTrackList Object describing the collection subset.
     *
     * @throws SQLException
     */
    public NemaTrackList getDatasetSubset(NemaDataset dataset) throws SQLException;
    /**
     * Retrieves a NemaTrackList Object describing the subset of a Collection that
     * is relevant to a dataset. This may be resolved to a list of tracks using
     * <code>getTracks(NemaTrackList set)</code> and that to a list of files using
     * <code>getFiles(List<NemaTrack> trackList, Set<NemaMetadataEntry> constraint)</code>.
     * Finally, the file type versions (each defined by a set of metadata
     * entries) that the dataset is available in may be retrieved using
     * <code>getTrackListVersions(NemaTrackList set)</code>.
     *
     * @param datasetId The dataset ID to retrieve the collection subset for.
     *
     * @return A NemaTrackList Object describing the collection subset.
     *
     * @throws SQLException
     */
    public NemaTrackList getDatasetSubset(int datasetId) throws SQLException;

    /**
     * Retrieves a NemaTrackList Object by Id.
     * 
     * @param setId The id of the track list to be retrieved.
     * @return A NemaTrackList Object describing the track list.
     * @throws SQLException
     */
    public NemaTrackList getTrackList(int setId) throws SQLException;

    /**
     * Retrieves a List containing Lists of NemaMetadataEntry Objects where
     * each List defines the different file types that the <emph>complete</emph> 
     * set of tracks, corresponding to the NemaTrackList, are available in.
     * The inner List defines a unique combination of different metadata values
     * that appears in the NemaTrackList. To be returned by this method a file
     * with that combination of metadata values must exist for all tracks
     * in the NemaTrackList.
     *
     * Can be used in conjunction with <code>getCollectionSubset(int datasetId)</code>
     * to get the different versions that a complete dataset is available in.
     *
     * @param set The NemaTrackList to find complete file type version lists for.
     *
     * @return a List containing Lists of NemaMetadataEntry Objects which define
     * the different file types that the <emph>complete</emph> set of tracks
     * for the NemaTrackList are available in.
     *
     * @throws SQLException
     */
    public List<List<NemaMetadataEntry>> getTrackListVersions(NemaTrackList trackList) throws SQLException;
    /**
     * Retrieves a List containing Lists of NemaMetadataEntry Objects which define
     * the different file types that the <emph>complete</emph> set of tracks,
     * corresponding to the NemaTrackList, are available in.
     * The inner List defines a unique combination of different metadata values
     * that appears in the NemaTrackList. To be returned by this method a file
     * with that combination of metadata values must exist for all tracks
     * in the NemaTrackList.
     *
     * Can be used in conjunction with <code>getCollectionSubset(int datasetId)</code>
     * to get the different versions that a complete dataset is available in.
     * 
     * @param setId The NemaTrackList ID to find complete file type version lists for.
     *
     * @return a Set containing Sets of NemaMetadataEntry Objects which define
     * the different file types that the <emph>complete</emph> set of tracks
     * for the NemaTrackList are available in.
     *
     * @throws SQLException
     */
    public List<List<NemaMetadataEntry>> getTrackListVersions(int trackListId) throws SQLException;


    /**
     * Retrieves a List of Lists of NemaTrackList Objects that describe the
     * experimental track lists for each iteration of the experiment defined by
     * the NemaDataset. The outer list enumerates the folds. Hence, for a 3-fold
     * cross-validated classification experiment the outer list would
     * contain 3 lists each containing two list, one defining the test and the
     * other the training set.
     *
     * @param dataset The dataset to retrieve the experiment lists for.
     *
     * @return a List of Lists of NemaTrackList Objects that describe the
     * experimental track lists for each iteration of the experiment defined by
     * the NemaDataset.
     *
     * @throws SQLException
     */
    public List<List<NemaTrackList>> getExperimentTrackLists(NemaDataset dataset) throws SQLException;
    /**
     * Retrieves a List of Lists of NemaTrackList Objects that describe the
     * experimental track lists for each iteration of the experiment defined by
     * the NemaDataset corresponding to the datasetID passed. The outer list
     * enumerates the folds. Hence, for a 3-fold cross-validated classification
     * experiment the outer list would contain 3 lists each containing two track lists
     * defining the test and training list.
     *
     * @param datasetID The dataset ID to retrieve the experiment sets for.
     *
     * @return a List of Lists of NemaTrackList Objects that describe the
     * experimental track sets for each iteration of the experiment defined by
     * the NemaDataset corresponding to the datasetID passed..
     *
     * @throws SQLException
     */
    public List<List<NemaTrackList>> getExperimentTrackLists(int datasetID) throws SQLException;


    /**
     * Retrieves a List of NemaTrack Objects defining the tracks corresponding
     * to a NemaTrackList Object.
     *
     * @param trackList The NemaTrackList to retreve a track list for.
     *
     * @return a List of NemaTrack Objects.
     *
     * @throws SQLException
     */
    public List<NemaTrack> getTracks(NemaTrackList trackList) throws SQLException;
    /**
     * Retrieves a List of NemaTrack Objects defining the tracks corresponding
     * to a NemaTrackList Object.
     *
     * @param trackListId The NemaTrackList to retrieve a track list for.
     *
     * @return a List of NemaTrack Objects.
     *
     * @throws SQLException
     */
    public List<NemaTrack> getTracks(int trackListId) throws SQLException;

    /**
     * Retrieves a List of NemaTrack Objects defining the tracks corresponding
     * to a NemaTrackList Object.
     *
     * @param trackList The NemaTrackList to retrieve a track list for.
     *
     * @return a List of NemaTrack Objects.
     *
     * @throws SQLException
     */
    public List<String> getTrackIDs(NemaTrackList trackList) throws SQLException;
    /**
     * Retrieves a List of NemaTrack Objects defining the tracks corresponding
     * to a NemaTrackList Object.
     *
     * @param trackListId The NemaTrackList to retrieve a track list for.
     *
     * @return a List of NemaTrack Objects.
     *
     * @throws SQLException
     */
    public List<String> getTrackIDs(int trackListId) throws SQLException;

    /**
     * Returns a NemaFile matching the the NemaTrack specified and having
     * the metadata values specified. If more than one NemaFile matches the
     * NemaTrack and constraint then no guarantee is provided as to which is
     * returned. If no NemaFile matches then null is returned.
     *
     * @param track The NemaTrack to retrieve a File for.
     * @param constraint The File metadata based constraint used to select the
     * NemaFile.
     *
     * @return A NemaFile matching the NemaTrack and File metadata constraint.
     *
     * @throws SQLException
     */
    public NemaFile getFile(NemaTrack track, Set<NemaMetadataEntry> constraint) throws SQLException;
    /**
     * Returns a NemaFile matching the NemaTrack specified and having
     * the metadata values specified. If more than one NemaFile matches the
     * NemaTrack and constraint then no guarantee is provided as to which is
     * returned. If no NemaFile matches then null is returned.
     *
     * @param trackId The ID of the NemaTrack to retrieve a File for.
     * @param constraint The File metadata based cpnstraint used to select the
     * NemaFile.
     *
     * @return A NemaFile matching the NemaTrack and File metadata constraint.
     *
     * @throws SQLException
     */
    public NemaFile getFile(String trackId, Set<NemaMetadataEntry> constraint) throws SQLException;

    /**
     * Returns the legacy file path for the specified NemaFile id. If the file
     * id does not exist or has no legacy path, null is returned.
     * 
     * @param fileId The file id to retrieve the legacy path for.
     * @return String representing the legacy path to the file.
     * 
     * @throws SQLException
     */
    public String getLegacyFilePath(int fileId) throws SQLException;
    
    /**
     * Returns a NemaFile matching the legacy file path passed. Null is returned
     * if no file record matches the legacy path.
     * @param legacyPath The legacy path to resolve to a NemaFile record.
     * @return a NemaFile matching the legacy file path passed.
     * @throws SQLException
     */
    public NemaFile getFileByLegacyPath(String legacyPath) throws SQLException;
    
    
    
    /**
     * Returns a map linking NemaFile Objects (keys) to Sets of
     * NemaMetadataEntry Objects (values) which define the metadata of that NemaFile
     * Object. An entry in the map will be made for each file that matches
     * the constraint passed. If the constrant is null or empty then entries
     * will be given for each file that corresponds to the NemaTrack.
     *
     * @param track The NemaTrack to retrieve NemaFile's for.
     * @param constraint The File metadata based cpnstraint used to select the
     * NemaFile.
     * @return A map where the keys are NemaFile Objects matching the constraint
     * and the specified NemaTrack, the values are Sets of NemaMetadataEntry
     * Objects defining describing the File type.
     * @throws SQLException
     */
    public Map<NemaFile,List<NemaMetadataEntry>> getFileFuzzy(NemaTrack track, Set<NemaMetadataEntry> constraint) throws SQLException;
    /**
     * Returns a map linking NemaFile Objects (keys) to Sets of
     * NemaMetadataEntry Objects (values) which define the metadata of that NemaFile
     * Object. An entry in the map will be made for each file that matches
     * the constraint passed. If the constrant is null or empty then entries
     * will be given for each file that corresponds to the NemaTrack.
     *
     * @param trackId The ID of the NemaTrack to retrieve NemaFile's for.
     * @param constraint The File metadata based cpnstraint used to select the
     * NemaFile.
     * @return A map where the keys are NemaFile Objects matching the constraint
     * and the specified NemaTrack, the values are Sets of NemaMetadataEntry
     * Objects defining describing the File type.
     * @throws SQLException
     */
    public Map<NemaFile,List<NemaMetadataEntry>> getFileFuzzy(String trackId, Set<NemaMetadataEntry> constraint) throws SQLException;

    /**
     * Returns a NemaFile matching the the NemaTrack specified and having
     * the metadata values specified in the list passed.
     * If more than one NemaFile matches each NemaTrack and constraint then no
     * guarantee is  provided as to which is returned. If no NemaFile matches
     * then null is returned (The ArrayList clas supports null entries).
     *
     * @param trackList The list of NemaTrack Objects to retrieve NEMAFiles for.
     * @param constraint The file metadata constraint to use in selecting the
     * files.
     *
     * @return A list of NemaFile Objects corresponding to the NemaTrack Objects
     * with null entires where no NemaFile could be found that matched the
     * constraint.
     *
     * @throws SQLException
     */
    public List<NemaFile> getFiles(List<NemaTrack> trackList, Set<NemaMetadataEntry> constraint) throws SQLException;
    /**
     * Returns a NemaFile matching the the NemaTrack with the specified ID and
     * having the metadata values specified in the list passed.
     * If more than one NemaFile matches each NemaTrack and constraint then no
     * guarantee is  provided as to which is returned. If no NemaFile matches
     * then null is returned (The ArrayList clas supports null entries).
     *
     * @param trackIDList The list of IDs to retrieve NEMAFiles for.
     * @param constraint The file metadata constraint to use in selecting the
     * files.
     *
     * @return A list of NemaFile Objects corresponding to the track IDs
     * with null entries where no NemaFile could be found that matched the
     * constraint.
     *
     * @throws SQLException
     */
    public List<NemaFile> getFilesByID(List<String> trackIDList, Set<NemaMetadataEntry> constraint) throws SQLException;

    /**
     * Uses a file encoding constraint set to resolve and add file paths to the
     * resources whose IDs are given in the input List of NemaData Objects. The
     * input list is modified and returned.
     * 
     * @param trackDataList
     * @param constraint
     * @return the updated list of NemaData Objects.
     * @throws SQLException Thrown if a problem occurs when querying the 
     * database.
     * @throws IllegalArgumentException Thrown if the IDs cannot be resolved to 
     * files using the supplied constraint.
     */
    public List<NemaData> resolveTracksToFiles(List<NemaData> trackDataList, Set<NemaMetadataEntry> constraint) throws SQLException, IllegalArgumentException;
    
    /**
     * Uses a file encoding constraint set to resolve and add file paths to the
     * resources whose IDs are given in the input Map of Lists of NemaData 
     * Objects. The data in the input list is modified and returned.
     * 
     * @param trackDataMap
     * @param constraint
     * @return the updated Map of NemaData Object Lists.
     * @throws SQLException Thrown if a problem occurs when querying the 
     * database.
     * @throws IllegalArgumentException Thrown if the IDs cannot be resolved to 
     * files using the supplied constraint.
     */
    public Map<NemaTrackList,List<NemaData>> resolveTracksToFiles(Map<NemaTrackList,List<NemaData>> trackDataMap, Set<NemaMetadataEntry> constraint) throws SQLException, IllegalArgumentException;
    
    /**
     * Logs a result directory path or identifier and system name against a 
     * specific dataset to facilitate group evaluation and comparison of all
     * published results for a dataset.
     * 
     * @param dataset_id The id of the dataset.
     * @param systemName The system name, will be used to identify the system in
     * evaluations.
     * @param username The user name, will be used to list the published
     * results for a user.
     * @param result_path The path or identifier that will be used to retrieve
     * the result directory.
     * @param fileType The NemaFileType class associated with the outputs 
     * (whether the path refers to a directory or such files or a single file).
     * @throws SQLException
     */
    public void publishResultForTask(int task_id, int set_id, String submissionCode, String systemName,
            String result_path, Class<NemaFileType> fileType) throws SQLException;

    /**Returns all the published results for a submission code over all tasks. 
     * 
     * @param submissionCode The submission code to retrieve results for.
     * @return List<NemaPublishedResult> list of published results
     * @throws SQLException
     */
    public List<NemaPublishedResult> getPublishedResultsForSubmissionCode(String submissionCode) throws SQLException;;
    
    /**
     * Returns a list of the published results for a task.
     *
     * @param task_id The id of the task
     * @return a list of the published results for a task.
     * @throws SQLException
     */
    public List<NemaPublishedResult> getPublishedResultsForTask(int task_id) throws SQLException;
    
    /**
     * Returns a list of the published results for a track list.
     *
     * @param set_id The id of the track list
     * @return a list of the published results for a track list.
     * @throws SQLException
     */
    public List<NemaPublishedResult> getPublishedResultsForTrackList(int track_list_id) throws SQLException;

    /**
     * Returns a list of the published results for a task and submission code.
     *
     * @param task_id The id of the task
     * @param submissionCode  The submission code to retrieve results for.
     * @return a list of the published results for a task.
     * @throws SQLException
     */
    public List<NemaPublishedResult> getPublishedResultsForTaskAndSubmissionCode(int task_id, String submissionCode) throws SQLException;

    /**
     * Deletes a published result record from the DB.
     *
     * @param result_id The id of teh result to delete.
     * @throws SQLException
     */
    public void deletePublishedResult(int result_id) throws SQLException;
    
    /**
     * Deletes a published result records from the DB with specific task and 
     * submission code values..
     *
     * @param task_id The task id of the results to delete.
     * @param submissionCode The submission code of the results to delete.
     * @throws SQLException
     */
    public void deletePublishedResultsForTaskAndSubmission(int task_id, String submissionCode) throws SQLException;
    
    /**
     * Deletes a published result records from the DB with specific set_id and 
     * submission code values. Used to ensure that ffeatures aren't deleted
     * when classificaiton results are published for the same submission code.
     *
     * @param set_id The set (tracklist) id of the results to delete.
     * @param submissionCode The submission code of the results to delete.
     * @throws SQLException
     */
    public void deletePublishedResultsForSetAndSubmission(int set_id, String submissionCode) throws SQLException;
    
    /**
     * Deletes a published result record from the DB.
     *
     * @param result The result to delete.
     * @throws SQLException
     */
    public void deletePublishedResult(NemaPublishedResult result) throws SQLException;



    /**
     * Retrieve File metadata values for a specified file id.
     * @param fileId The ID of the file to get metadata values for.
     * @return A List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the file id.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getFileMetadataByID(int fileId) throws SQLException;
    /**
     * Retrieve File metadata values for a specified <code>NemaFile</code> Object.
     * @param file The <code>NemaFile</code> to retrieve metadata values for.
     * @return A List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the file id.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getFileMetadata(NemaFile file) throws SQLException;
    
    /**
     * Retrieve File metadata values for a specified List of file IDs.
     * @param fileIDs A List of the IDs to get metadata values for.
     * @return A Map from file ID to a List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the file id.
     * @throws SQLException
     */
    public Map<Integer,List<NemaMetadataEntry>> getFileMetadataByID(List<Integer> fileIDs) throws SQLException;
    /**
     * Retrieve File metadata values for a specified List of <code>NemaFile</code> Object.
     * @param files A List of the <code>NemaFile</code> Objects to retrieve metadata values for.
     * @return A Map from file ID to a List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the file id.
     * @throws SQLException
     */
    public Map<Integer,List<NemaMetadataEntry>> getFileMetadata(List<NemaFile> files) throws SQLException;

    /**
     * Retrieve the id of specified value of a particular metadata type or -1
     * if it does not exist.
     * @param metadataTypeId The metadata type id to query.
     * @param value The value to query.
     * @return The id of the metadata value for specified type or -1 if it does
     * not exist.
     * @throws SQLException
     */
    public int getTrackMetadataIdForValue(int metadataTypeId, String value) throws SQLException;
    
    /**
     * Retrieve Track metadata values for a specified Track id.
     * @param trackId The ID of the track to get metadata values for.
     * @return A List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the track id.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getTrackMetadataByID(String trackId) throws SQLException;
    /**
     * Retrieve File metadata values for a specified <code>NemaTrack</code> Object.
     * @param track The <code>NemaTrack</code> to retrieve metadata values for.
     * @return A List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the track id.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getTrackMetadata(NemaTrack track) throws SQLException;
    
    /**
     * Retrieve Track metadata values for a specified List of track IDs.
     * @param tracks A List of the IDs to get metadata values for.
     * @return A Map from track ID to a List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the track id.
     * @throws SQLException
     */
    public Map<String,List<NemaMetadataEntry>> getTrackMetadataByID(List<String> tracks) throws SQLException;
    /**
     * Retrieve Track metadata values for a specified List of <code>NemaTrack</code> Object.
     * @param tracks A List of the <code>NemaTrack</code> Objects to retrieve metadata values for.
     * @return A Map from track ID to a List of the metdata values, represented as <code><NemaMetadataEntry/code> Objects, for the track id.
     * @throws SQLException
     */
    public Map<String,List<NemaMetadataEntry>> getTrackMetadata(List<NemaTrack> tracks) throws SQLException;

    /**
     * Retrieve metadata of a particular type for a track.
     * @param trackId The track ID to retrieve data for.
     * @param metadataId The metadata type ID to retrieve.
     * @return The list of metadata values of the specified type represented as a <code>NemaMetadataEntry</code> Object.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getTrackMetadataByID(String trackId, int metadataId) throws SQLException;
    
    /**
     * Retrieve the first metadata of a particular type for a track.
     * @param track the <code>NemaTrack</code> Object to retreive metadata for.
     * @param metadataId The metadata type ID to retrieve.
     * @return The metadata value represented as a <code>NemaMetadataEntry</code> Object.
     * @throws SQLException
     */
    public List<NemaMetadataEntry> getTrackMetadata(NemaTrack track, int metadataId) throws SQLException;
    
    /**
     * Retrieve the first metadata of a particular type for each of a list of tracks.
     * @param tracks The list of track IDs to retrieve data for.
     * @param metadataId The metadata type ID to retrieve.
     * @return A map of track ID to the metadata value represented as a <code>NemaMetadataEntry</code> Object.
     * @throws SQLException
     */
    public Map<String,List<NemaMetadataEntry>> getTrackMetadataByID(List<String> tracks, int metadataId) throws SQLException;
    /**
     * Retrieve the first metadata of a particular type for each of a list of tracks.
     * @param tracks The list of <code>NemaTrack</code> Objects to retrieve data for.
     * @param metadataId The metadata type ID to retrieve.
     * @return A map of track ID to the metadata value represented as a <code>NemaMetadataEntry</code> Object.
     * @throws SQLException
     */
    public Map<String,List<NemaMetadataEntry>> getTrackMetadata(List<NemaTrack> tracks, int metadataId) throws SQLException;

    /**
     * Returns an unmodifiable map linking track metadata type names to their IDs.
     * @return an unmodifiable map.
     */
    public Map<String,Integer> getTrackMetadataNameMap();
    
    /**
     * Returns an unmodifiable map linking file metadata type names to their IDs.
     * @return an unmodifiable map.
     */
    public Map<String,Integer> getFileMetadataNameMap();
    
    /**
     * Returns an unmodifiable map linking set type names to their IDs.
     * @return an unmodifiable map.
     */
    public Map<String,Integer> getTrackListTypeMap();
    
    /**
     * Returns the name for a track metadata type ID.
     * @param typeId metadata type ID to retrieve name for.
     * @return metadata type name.
     */
    public String getTrackMetadataName(int typeId);

    /**
     * Returns the name for a file metadata type ID.
     * @param typeId metadata type ID to retrieve name for.
     * @return metadata type name.
     */
    public String getFileMetadataName(int typeId);

    /**
     * Returns the name for a set metadata type ID.
     * @param typeId metadata type ID to retrieve name for.
     * @return metadata type name.
     */
    public String getTrackListTypeName(int typeId);

    /**
     * Returns the name for a site ID.
     * @param siteId site ID to retrieve name for.
     * @return site name.
     */
    public String getSiteName(int siteId);

    /**
     * Returns the integer ID for the specified Track metadata type name.
     * @param typeName metadata type name to retrieve ID for.
     * @return metadata type ID.
     */
    public int getTrackMetadataID(String typeName);

    /**
     * Returns the integer ID for the specified File metadata type name.
     * @param typeName metadata type name to retrieve ID for.
     * @return metadata type ID.
     */
    public int getFileMetadataID(String typeName);

    /**
     * Returns the integer ID for the specified Set metadata type name.
     * @param typeName metadata type name to retrieve ID for.
     * @return metadata type ID.
     */
    public int getTrackListTypeID(String typeName);

    /**
     * Returns the integer ID for the specified site name.
     * @param siteName siet name to retrieve ID for.
     * @return site ID.
     */
    public int getSiteId(String siteName);

    /** 
     * Returns details on a submission code from the MIREX submissions DB.
     * 
     * @param submissionCode The submission code to retrieve details for.
     * @return NemaSubmission model encoding data on the submission.
     * @throws SQLException
     */
    public NemaSubmission getSubmissionDetails(String submissionCode) throws SQLException;
}
