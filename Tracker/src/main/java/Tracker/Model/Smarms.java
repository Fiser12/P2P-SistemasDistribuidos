package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "smarms")
public class Smarms implements java.io.Serializable, Observable {

	private String smarmsId;
	private String hexInfoHash;
	private Integer tamanoEnBytes;
	private Set<PeerSmarms> peerSmarmses = new HashSet<PeerSmarms>(0);

	public Smarms() {
	}

	public Smarms(String hexInfoHash, Integer tamanoEnBytes) {
		this.hexInfoHash = hexInfoHash;
		this.tamanoEnBytes = tamanoEnBytes;
	}

	public Smarms(String hexInfoHash, Integer tamanoEnBytes, Set<PeerSmarms> peerSmarmses) {
		this.hexInfoHash = hexInfoHash;
		this.tamanoEnBytes = tamanoEnBytes;
		this.peerSmarmses = peerSmarmses;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "SMARMS_ID", unique = true, nullable = false)
	public String getSmarmsId() {
		return this.smarmsId;
	}

	public void setSmarmsId(String smarmsId) {
		this.smarmsId = smarmsId;
	}

	@Column(name = "hexInfoHash", nullable = false, length = 10)
	public String getHexInfoHash() {
		return this.hexInfoHash;
	}

	public void setHexInfoHash(String hexInfoHash) {
		this.hexInfoHash = hexInfoHash;
	}

	@Column(name = "SMARMS_TAMANO", nullable = false)
	public Integer getTamanoEnBytes() {
		return this.tamanoEnBytes;
	}

	public void setTamanoEnBytes(Integer tamanoEnBytes) {
		this.tamanoEnBytes = tamanoEnBytes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.peer")
	public Set<PeerSmarms> getPeerSmarmses() {
		return this.peerSmarmses;
	}

	public void setPeerSmarmses(Set<PeerSmarms> peerSmarmses) {
		this.peerSmarmses = peerSmarmses;
	}

	public void addListener(InvalidationListener listener) {

	}

	public void removeListener(InvalidationListener listener) {

	}
}