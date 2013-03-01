/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.tagsClassification;

import java.io.Serializable;

/**
 *
 * @author kris
 */
public class AffinityDataPoint implements Comparable, Serializable{
        boolean tagApplies;
        double affinity;

        public AffinityDataPoint(boolean tagApplies, double affinity) {
            this.tagApplies = tagApplies;
            this.affinity = affinity;
        }

        public int compareTo(Object o) {
            //sort into descending order
            return -Double.compare(affinity, ((AffinityDataPoint)o).affinity);
        }
    }