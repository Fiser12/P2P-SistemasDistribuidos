package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "peer")
public class Peer implements java.io.Serializable, Observable {

	private Integer idPeer;
	private String ip;
	private Integer port;
	private Set<PeerSmarms> peerSmarmses = new HashSet<PeerSmarms>(0);

	public Peer() {
	}

	public Peer(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}

	public Peer(String ip, Integer port,
				Set<PeerSmarms> peerSmarmses) {
		this.ip = ip;
		this.port = port;
		this.peerSmarmses = peerSmarmses;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PEER_ID", nullable = false)
	public Integer getIdPeer() {
		return this.idPeer;
	}

	public void setIdPeer(Integer idPeer) {
		this.idPeer = idPeer;
	}

	@Column(name = "PEER_IP", nullable = false, length = 15)
	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column(name = "PEER_PORT", nullable = false, length = 5)
	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.smarms", cascade=CascadeType.ALL)
	public Set<PeerSmarms> getPeerSmarmses() {
		return this.peerSmarmses;
	}

	public void setPeerSmarmses(Set<PeerSmarms> peerSmarmses) {
		this.peerSmarmses = peerSmarmses;
	}

	@Override
	public void addListener(InvalidationListener listener) {

	}

	@Override
	public void removeListener(InvalidationListener listener) {

	}
}