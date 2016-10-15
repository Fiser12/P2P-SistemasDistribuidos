package Tracker.Model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "smarms")
public class Smarms implements java.io.Serializable {

	private String smarmsId;
	private String name;
	private Integer tamanoEnBytes;
	private Set<PeerSmarms> peerSmarmses = new HashSet<PeerSmarms>(0);

	public Smarms() {
	}

	public Smarms(String name, Integer tamanoEnBytes) {
		this.name = name;
		this.tamanoEnBytes = tamanoEnBytes;
	}

	public Smarms(String name, String desc, Set<PeerSmarms> peerSmarmses) {
		this.name = name;
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

	@Column(name = "NAME", nullable = false, length = 10)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

}