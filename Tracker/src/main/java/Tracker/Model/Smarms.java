package Tracker.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.HashSet;
import java.util.Set;

public class Smarms implements java.io.Serializable, Observable {

	private String smarmsId;
	private String hexInfoHash;
	private Integer tamanoEnBytes;
	private Integer numeroPares;
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

	public String getSmarmsId() {
		return this.smarmsId;
	}

	public void setSmarmsId(String smarmsId) {
		this.smarmsId = smarmsId;
	}

	public String getHexInfoHash() {
		return this.hexInfoHash;
	}

	public void setHexInfoHash(String hexInfoHash) {
		this.hexInfoHash = hexInfoHash;
	}

	public Integer getTamanoEnBytes() {
		return this.tamanoEnBytes;
	}

	public void setTamanoEnBytes(Integer tamanoEnBytes) {
		this.tamanoEnBytes = tamanoEnBytes;
	}

	public Set<PeerSmarms> getPeerSmarmses() {
		return this.peerSmarmses;
	}

	public void setPeerSmarmses(Set<PeerSmarms> peerSmarmses) {
		this.peerSmarmses = peerSmarmses;
	}

	public void addListener(InvalidationListener listener) {

	}
	public Integer getNumeroPares() {
		return numeroPares;
	}

	public void setNumeroPares(Integer numeroPares) {
		this.numeroPares = numeroPares;
	}

	public void removeListener(InvalidationListener listener) {

	}
}