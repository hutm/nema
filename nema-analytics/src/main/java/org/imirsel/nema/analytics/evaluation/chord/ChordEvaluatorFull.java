package org.imirsel.nema.analytics.evaluation.chord;

/**
 * @version 1.0 5/5/11 3:05 PM
 * @author: Hut
 */
public class ChordEvaluatorFull extends ChordEvaluator {


    protected int calcOverlap(int[] gt, int[] sys) {
        if (gt == null || sys == null || gt.length != sys.length) {
            return 0;
        } else {
            for (int i = 0; i < gt.length; i++) {
                if (gt[i] != sys[i]) {
                    return 0;
                }
            }
            return 1;
        }
    }


}
