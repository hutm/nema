package org.imirsel.nema.model;

/**
 * Model class representing a distance to a specified track. This model is 
 * expected to be held in a list which refers to a track from which the 
 * distances are specified.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class NemaTrackDistance implements Comparable<NemaTrackDistance>{
	NemaTrack track;
	float distance;
	
	public NemaTrackDistance(NemaTrack track, float distance){
		this.track = track;
		this.distance = distance;
	}
	
	public NemaTrackDistance(String trackId, float distance){
		this.track = new NemaTrack(trackId);
		this.distance = distance;
	}

	public NemaTrack getTrack() {
		return track;
	}
	
	public String getTrackId(){
		return track.getId();
	}

	public void setTrack(NemaTrack track) {
		this.track = track;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	@Override
	public int hashCode() {
		return track.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NemaTrackDistance other = (NemaTrackDistance) obj;
		if (track == null) {
			if (other.track != null)
				return false;
		} else if (!track.equals(other.track))
			return false;
		return true;
	}

	public int compareTo(NemaTrackDistance o) {
		if (distance<o.distance){
			return -1;
		}else if(distance>o.distance){
			return 1;
		}else{
			return 0;
		}
	}

	
}
