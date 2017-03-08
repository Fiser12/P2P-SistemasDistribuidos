package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class PeerSmarms implements java.io.Serializable, Observable {

	private PeerSmarmsId pk = new PeerSmarmsId();
	private Long bytesDescargados;

	public PeerSmarms() {
	}

	public PeerSmarmsId getPk() {
		return pk;
	}

	public void setPk(PeerSmarmsId pk) {
		this.pk = pk;
	}

	public Peer getPeer() {
		return getPk().getPeer();
	}

	public void setPeer(Peer peer) {
		getPk().setPeer(peer);
	}

	public Smarms getSmarms() {
		return getPk().getSmarms();
	}

	public void setSmarms(Smarms smarms) {
		getPk().setSmarms(smarms);
	}

	public Long getBytesDescargados() {
		return this.bytesDescargados;
	}

	public void setBytesDescargados(Long bytesDescargados) {
		this.bytesDescargados = bytesDescargados;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PeerSmarms that = (PeerSmarms) o;

		if (getPk() != null ? !getPk().equals(that.getPk())
				: that.getPk() != null)
			return false;

		return true;
	}

	public int hashCode() {
		return (getPk() != null ? getPk().hashCode() : 0);
	}

	public void addListener(InvalidationListener listener) {

	}

	public void removeListener(InvalidationListener listener) {

	}
}