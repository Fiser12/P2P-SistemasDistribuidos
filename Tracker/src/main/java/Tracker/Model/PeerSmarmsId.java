package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class PeerSmarmsId implements java.io.Serializable, Observable {

	private Peer peer;
    private Smarms smarms;

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public Smarms getSmarms() {
		return smarms;
	}

	public void setSmarms(Smarms smarms) {
		this.smarms = smarms;
	}

	public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerSmarmsId that = (PeerSmarmsId) o;
        if (smarms != null ? !smarms.equals(that.smarms) : that.smarms != null) return false;
		return peer != null ? peer.equals(that.peer) : that.peer == null;
	}
    public int hashCode() {
        int result;
        result = (smarms != null ? smarms.hashCode() : 0);
        result = 31 * result + (peer != null ? peer.hashCode() : 0);
        return result;
    }

	public void addListener(InvalidationListener listener) {

	}

	public void removeListener(InvalidationListener listener) {

	}
}