package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class PeerSmarmsId implements java.io.Serializable, Observable {

	private Peer peer;
    private Smarms smarms;

	@ManyToOne
	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	@ManyToOne
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
        if (peer != null ? !peer.equals(that.peer) : that.peer != null)
            return false;

        return true;
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