/**
 * 
 */
package org.imirsel.nema.repositoryservice;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * An interface defining the methods of an update client for the temporary NEMA metadata
 * repository. This interface extends <code>RepositoryUpdateCLientInterface</code> with methods
 * to add data to or change data within the repository DB (except for result publication which 
 * is handled by <code>RepositoryUpdateCLientInterface</code>).
 * 
 * @author kriswest
 *
 */
public interface RepositoryUpdateClientInterface extends
		RepositoryClientInterface {

	/**
	 * Starts a DB transaction by setting autocommit to false,
	 * @throws SQLException
	 */
	public void startTransation() throws SQLException;
    
	/**
	 * Commits the transaction and sets autocommit back to true.
	 * @throws SQLException
	 */
    public void endTransation() throws SQLException;
        
    /**
     * Rolls back a transaction and sets autocommit back to true.
     * @throws SQLException
     */
    public void rollback() throws SQLException;
	
	/**
	 * Add a new track metadata type definition to the repository DB.
	 * @param name Name for the new type definition.
	 * @throws SQLException Thrown if a problem with the update occurs, including duplicate values of the name parameter in the DB.
	 */
	public void insertTrackMetaDef(String name) throws SQLException;

	/**
	 * Inserts a track metadata value for a particular track metadata type. An id generated for the value
	 * is returned or if the value already exists in the DB the id of the existing value is returned.
	 * @param metadata_type_id The int type ID to add a value for.
	 * @param value The value String.
	 * @return a unique int ID for the value.
	 * @throws SQLException Thrown if a problem with the update occurs.
	 */
    public int insertTrackMeta(int metadata_type_id, String value) throws SQLException;

    /**
     * Links a track metadata value to a particular track ID.
     * @param track_id The track ID to link to the value.
     * @param track_metadata_id The track metadata value ID to link to the track.
     * @throws SQLException Thrown if a problem with the update occurs.
     */
    public void insertTrackMetaLink(String track_id, int track_metadata_id) throws SQLException;
    
    /**
     * Inserts a track ID in to the repository DB.
     * @param id The track ID to insert.
     * @throws SQLException Thrown if a problem with the update occurs.
     */
    public void insertTrack(String id) throws SQLException;
    
    /** 
     * Links a collection ID to a track ID.
     * @param collection_id The collection ID to link to the track ID.
     * @param track_id The trackID to link to the collection ID.
     * @throws SQLException Thrown if a problem with the update occurs.
     */
    public void insertTrackCollectionLink(int collection_id, String track_id) throws SQLException;

    /**
     * Inserts a file path that points to a legacy location for a particular 
     * file. As files are moved and renamed on ingestion, this provides a link
     * back to the original file location.
     * @param file_id The file id to link to the path.
     * @param legacyFilePath The legacy file path.
     * @throws SQLException Thrown if a problem with the update occurs.
     * @since 0.2.0
     */
    public void insertLegacyFilePath(int file_id, String legacyFilePath) throws SQLException;
    
    /**
     * Inserts a new file path, relating to a track ID, into the repository DB.
     * @param track_id The track ID to insert the file path against.
     * @param path The filesystem path to insert against the track ID.
     * @param site The NEMA site ID at which the path is valid.
     * @return The int ID generated for the inserted file.
     * @throws SQLException Thrown if a problem with the update occurs.
     */
    public int insertFile(String track_id, String path, int site) throws SQLException;

    /**
	 * Add a new file metadata type definition to the repository DB.
	 * @param name Name for the new type definition.
	 * @throws SQLException Thrown if a problem with the update occurs, including duplicate values of the name parameter in the DB.
	 */
    public void insertFileMetaDef(String name) throws SQLException;

    /**
	 * Inserts a file metadata value for a particular file metadata type. An id generated for the value
	 * is returned or if the value already exists in the DB the id of the existing value is returned.
	 * @param metadata_type_id The int type ID to add a value for.
	 * @param value The value String.
	 * @return the id generated for the file metadata value.
	 * @throws SQLException Thrown if a problem with the update occurs.
	 */
    public int insertFileMeta(int metadata_type_id, String value) throws SQLException;

    /**
     * Links a file metadata value to a particular file ID.
     * @param file_id The file ID to link to the value.
     * @param file_metadata_id The file metadata value ID to link to the file.
     * @throws SQLException Thrown if a problem with the update occurs.
     */
    public void insertFileMetaLink(int file_id, int file_metadata_id) throws SQLException;
    
    /**
     * Adds a dataset to the repository DB that contains only test sets e.g. an 
     * analysis collection for onset detection or multi-F0 estimation. 
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param subsetList A list of all the track IDs appearing in the dataset.
     * @param testLists An (ordered) list of the track IDs in each of the test sets.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     */
    public int insertTestOnlyDataset(String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            List<String> subsetList,
            List<List<String>> testLists) throws SQLException;
    
    /**
     * Adds a dataset to the repository DB that contains only a single split and single set for that split
     * (i.e. this dataset is just a list with divisions into sets or experiment iterations, e.g. an analysis 
     * collection for onset detection or multi-F0 estimation). 
     * 
     * The trackID list is read from a UTF-8 file on disk with one trackID per line. Other data on each line
     * (delimited by whitespace) is ignored. TrackIDs maybe specified as full file paths, from which IMIRSEL 
     * trackIDs (e.g. a002341) will be extracted.
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param dataset_subset_file A file listing the track IDs in the dataset with one track ID per line.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     * @throws IOException
     */
    public int insertTestOnlyDataset(String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            File dataset_subset_file) throws SQLException, IOException;
    
    /**
     * Adds a dataset to the repository DB that contains only a single split and single set for that split
     * (i.e. this dataset is just a list with divisions into sets or experiment iterations, e.g. an analysis 
     * collection for onset detection or multi-F0 estimation). 
     * 
     * The trackID list is read from a UTF-8 file on disk with one trackID per line. Other data on each line
     * (delimited by whitespace) is ignored. TrackIDs maybe specified as full file paths, from which IMIRSEL 
     * trackIDs (e.g. a002341) will be extracted.
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param dataset_subset_file A file listing the track IDs in the dataset with one track ID per line.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     * @throws IOException
     */
    public int insertTestOnlyDatasetFromDir(String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            File dataset_dir) throws SQLException, IOException;
    
    /**
     * Adds a dataset to the repository DB that contains one or more splits of the dataset into
     * training and test sets (e.g. a 3 fold cross-validated genre classification set, having
     * three pairs of training and test sets).
     * 
     * Training sets are computed by subtracting the test set from the set of all tracks in the 
     * dataset (subsetList).
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param subsetList A list of all the track IDs appearing in the dataset.
     * @param testLists An (ordered) list of the track IDs in each of the test sets.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     */
    public int insertTestTrainDataset(String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            List<String> subsetList,
            List<List<String>> testLists) throws SQLException;

    /**
     * Adds a dataset to the repository DB that contains one or more splits of the dataset into
     * training and test sets (e.g. a 3 fold cross-validated genre classification set, having
     * three pairs of training and test sets).
     * 
     * Training sets are computed by subtracting the test set from the set of all tracks in the 
     * dataset (subsetList).
     * 
     * The set of all tracks in the dataset and the set of track IDs in each test set are read 
     * from list files with one track ID per line. Other data on each line (delimited by whitespace) 
     * is ignored. TrackIDs maybe specified as full file paths, from which IMIRSEL trackIDs 
     * (e.g. a002341) will be extracted.
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param dataset_subset_file  A file listing the track IDs in the dataset with one track ID per line.
     * @param testset_files A list of files containing the track IDs for each test set in the dataset
     * with one track ID per line.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     * @throws IOException
     */
    public int insertTestTrainDataset(String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            File dataset_subset_file,
            List<File> testset_files) throws SQLException, IOException;

    /**
     * Adds a dataset to the repository DB. No splits are added, hence, at least one Set description 
     * must be manually added to the dataset and tracks linked to it.
     * 
     * @param name The dataset name.
     * @param description The dataset description.
     * @param subject_track_metadata_type_id The subject track metadata or -1 if none.
     * @param filter_track_metadata_type_id The filter track metadata or -1 if none.
     * @param subsetList A list of all the track IDs appearing in the dataset.
     * @return The ID of the inserted dataset in the repository DB.
     * @throws SQLException
     */
    public int insertDataset(
            String name,
            String description,
            int subject_track_metadata_type_id,
            int filter_track_metadata_type_id,
            List<String> subsetList) throws SQLException;

    /**
     * Adds a track list to the repository DB. The track list must be linked to a dataset (if it is to retrievable)
     * and a track list type specified. A fold number must also be specified if this is a test or training set.
     * For analysis datasets (unsplit datasets) please enter a fold number of 1. For dataset subsets, please
     * enter -1 (as they don't belong to a fold number).
     * 
     * @param datasetId The dataset ID to link the Set to.
     * @param trackListTypeId The set type ID.
     * @param foldNumber The split number.
     * @return The ID of the Set inserted.
     * @throws SQLException
     */
    public int insertTrackListDescription(int datasetId, int trackListTypeId, int foldNumber) throws SQLException;

    /**
     * Links the specified track IDs to the specified track list in the repository DB. The track IDs must already
     * exist in the DB and tracks must also appear in the subset
     * for the dataset that they are relevant to (N.B. this is not checked). 
     * 
     * @param trackListId The track list to link the tracks to.
     * @param tracks The list of track IDs to link to the track list.
     * @throws SQLException
     */
    public void insertTrackListTracks(int trackListId, List<String> tracks) throws SQLException;
	
    /**
     * Inserts a new task definition into the database.
     * 
     * @param name The name of the task.
     * @param description A text description of the task.
     * @param subjectTrackMetadataName The name of the metadata processed or 
     * operated on by the task (which must already be defined in the DB).
     * @param datasetId The id of the dataset which the task will operate on.
     * @return the ID of the task inserted into the database.
     * @throws SQLException
     */
    public int insertTask(String name, String description, String subjectTrackMetadataName, int datasetId)  throws SQLException;
}
