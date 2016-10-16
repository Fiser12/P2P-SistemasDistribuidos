package Tracker.Model;

import com.turn.ttorrent.common.Torrent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class Tracker {
	public static final String ANNOUNCE_URL = "/announce";
	/**
	 * Puerto por defecto del tracker es 6969 (Standart BitTorrent)
	 */
	public static final int DEFAULT_TRACKER_PORT = 6969;
	/**
	 * Nombre por defecto del servidor y su version anunciada por el Tracker
	 */
	public static final String DEFAULT_VERSION_STRING =
			"BitTorrent Tracker (ttorrent)";
	private final Connection connection;
	private final InetSocketAddress address;
	private final ConcurrentMap<String, TrackedTorrent> torrents;

	private Thread tracker;
	private Thread collector;
	private boolean stop;
	
	/**
	 * Crea un nuevo tracker que escucha la direccion que se le envia en el puerto por defecto
	 * @param address
	 * @throws IOException
	 */
	public Tracker(InetAddress address) throws IOException {
		this(new InetSocketAddress(address, DEFAULT_TRACKER_PORT),
			DEFAULT_VERSION_STRING);
	}

	/**
	 * Crea un nuevo tracker que escucha la direccion que se le envia
	 *
	 * @param address 
	 * @throws IOException 
	 */
	public Tracker(InetSocketAddress address) throws IOException {
		this(address, DEFAULT_VERSION_STRING);
	}

	/**
	 * Create un nuevo tracker que escucha la direccion que se le envia. Tambien recive la version
	 *
	 * @param address
	 * @param version 
	 * @throws IOException 
	 */
	public Tracker(InetSocketAddress address, String version)
		throws IOException {
		this.address = address;

		this.torrents = new ConcurrentHashMap<String, TrackedTorrent>();
		this.connection = new SocketConnection(
				new TrackerService(version, this.torrents));
	}
}
