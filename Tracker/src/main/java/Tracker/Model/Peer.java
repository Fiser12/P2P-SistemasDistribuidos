package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "peer")
public class Peer implements java.io.Serializable, Observable {

	private Long idPeer;
	private String ip;
	private Integer port;
	private Long connectionId;

	public Long getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}

	private Set<PeerSmarms> peerSmarmses = new HashSet<PeerSmarms>(0);

	public Peer() {
	}


	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PEER_ID", nullable = false)
	public Long getIdPeer() {
		return this.idPeer;
	}

	public void setIdPeer(Long idPeer) {
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