package Tracker.Util.bittorrent.tracker.protocol.udp;

/**
 * Size				Name
 * 32-bit integer  	seeders
 * 32-bit integer  	completed
 * 32-bit integer  	leechers
 */
public class ScrapeInfo {

    /*
        complete = número de seeders del swarm
        downloaded = número de veces que se ha descargado el contenido.
        incomplete = número de leechers del swarm
     */
    private int leechers;
    private int completed;
    private int seeders;

    public int getLeechers() {
        return leechers;
    }

    public void setLeechers(int leechers) {
        this.leechers = leechers;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getSeeders() {
        return seeders;
    }

    public void setSeeders(int seeders) {
        this.seeders = seeders;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("leechers: ");
        buffer.append(this.leechers);
        buffer.append(" - completed: ");
        buffer.append(this.completed);
        buffer.append(" - seeders: ");
        buffer.append(this.seeders);

        return buffer.toString();
    }
}