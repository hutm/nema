package org.imirsel.nema.analytics.evaluation;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.beat.BeatEvaluator;
import org.imirsel.nema.analytics.evaluation.chord.ChordEvaluator;
import org.imirsel.nema.analytics.evaluation.chord.ChordEvaluatorFull;
import org.imirsel.nema.analytics.evaluation.classification.ClassificationEvaluator;
import org.imirsel.nema.analytics.evaluation.key.KeyEvaluator;
import org.imirsel.nema.analytics.evaluation.melody.MelodyEvaluator;
import org.imirsel.nema.analytics.evaluation.multif0.MultiF0EstEvaluator;
import org.imirsel.nema.analytics.evaluation.onset.OnsetEvaluator;
import org.imirsel.nema.analytics.evaluation.structure.StructureEvaluator;
import org.imirsel.nema.analytics.evaluation.tagsClassification.TagAffinityEvaluator;
import org.imirsel.nema.analytics.evaluation.tagsClassification.TagClassificationEvaluator;
import org.imirsel.nema.analytics.evaluation.tempo.TempoEvaluator;
import org.imirsel.nema.analytics.evaluation.tempo.TempoResultRenderer;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;

/**
 * Factory class that can setup known evaluator types keyed on the String
 * metadata id.
 *  
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class EvaluatorFactory {
	
	private static final Map<String,Class<? extends Evaluator>> EVALUATOR_REGISTRY = new HashMap<String, Class<? extends Evaluator>>();
	static{
		//register all the known evaluators for known metadata keys
		EVALUATOR_REGISTRY.put(NemaDataConstants.CHORD_LABEL_SEQUENCE, ChordEvaluator.class);
        EVALUATOR_REGISTRY.put(NemaDataConstants.CHORD_LABEL_SEQUENCE_FULL, ChordEvaluatorFull.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.MELODY_EXTRACTION_DATA, MelodyEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.KEY_DETECTION_DATA, KeyEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.TEMPO_EXTRACTION_DATA, TempoEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA, StructureEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.ONSET_DETECTION_DATA, OnsetEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.BEAT_TRACKING_DATA, BeatEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.MULTI_F0_EST_DATA, MultiF0EstEvaluator.class);
//		EVALUATOR_REGISTRY.put(NemaDataConstants.MULTI_F0_NT_DATA, MultiF0NTEvaluator.class);
		
		//tag tasks
		EVALUATOR_REGISTRY.put(NemaDataConstants.TAG_CLASSIFICATIONS, TagClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.TAG_AFFINITY_MAP, TagAffinityEvaluator.class);
		
		
			//classification tasks
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ALBUM, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_ARTIST, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_TITLE, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_GENRE, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_COMPOSER, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MOOD, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_COMPOSER, ClassificationEvaluator.class);
		EVALUATOR_REGISTRY.put(NemaDataConstants.CLASSIFICATION_MIREX_GENRE, ClassificationEvaluator.class);
	}
	
	public static Evaluator getEvaluator(String metadataKey,
			NemaTask task,
            NemaDataset dataset,
            List<NemaTrackList> trainingSets,
			List<NemaTrackList> testSets
            ) throws InstantiationException, IllegalAccessException, FileNotFoundException{
		
		Class<? extends Evaluator> evalClass = EVALUATOR_REGISTRY.get(metadataKey);
		Evaluator out = evalClass.newInstance();
		out.setTask(task);
		out.setDataset(dataset);
		out.setTrainingSets(trainingSets);
		out.setTestSets(testSets);
		
		return out;
	}
}
