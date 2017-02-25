package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import javax.persistence.*;

@Entity
@Table(name = "peer_smarmses")
@AssociationOverrides({
		@AssociationOverride(name = "pk.smarms", joinColumns = @JoinColumn(name = "SMARMS_ID")),
		@AssociationOverride(name = "pk.peer", joinColumns = @JoinColumn(name = "PEER_ID")) })
public class PeerSmarms implements java.io.Serializable, Observable {

	private PeerSmarmsId pk = new PeerSmarmsId();
	private Long bytesDescargados;

	public PeerSmarms() {
	}

	@EmbeddedId
	public PeerSmarmsId getPk() {
		return pk;
	}

	public void setPk(PeerSmarmsId pk) {
		this.pk = pk;
	}

	@Transient
	public Peer getPeer() {
		return getPk().getPeer();
	}

	public void setPeer(Peer peer) {
		getPk().setPeer(peer);
	}

	@Transient
	public Smarms getSmarms() {
		return getPk().getSmarms();
	}

	public void setSmarms(Smarms smarms) {
		getPk().setSmarms(smarms);
	}

	@Column(name = "BYTES_DESCARGADOS", nullable = false)
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