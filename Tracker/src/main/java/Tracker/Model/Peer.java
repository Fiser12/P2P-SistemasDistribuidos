package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.HashSet;
import java.util.Set;

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

	public Long getIdPeer() {
		return this.idPeer;
	}

	public void setIdPeer(Long idPeer) {
		this.idPeer = idPeer;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

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