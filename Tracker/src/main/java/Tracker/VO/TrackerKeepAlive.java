package Tracker.VO;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class TrackerKeepAlive implements Comparator<Object> {

    private String id;
    private Date lastKeepAlive;
    private boolean master;
    private boolean iAm;

    private HashMap<String, Estado> confirmacionActualizacion = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setLastKeepAlive(Date lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public Estado getConfirmacionActualizacion(String id) {
        return confirmacionActualizacion.get(id);
    }

    public void setConfirmacionActualizacion(String id, Estado confirmacionActualizacion) {
        this.confirmacionActualizacion.put(id ,confirmacionActualizacion);
    }

    @Override
    public String toString() {
        return "Id: " + id + " Master: " + master;
    }

    @Override
    public int compare(Object o1, Object o2) {
        TrackerKeepAlive a = (TrackerKeepAlive) o1;
        TrackerKeepAlive b = (TrackerKeepAlive) o2;
        if (Integer.parseInt(a.getId()) > Integer.parseInt(b.getId())) {
            return 1;
        } else {
            if (Integer.parseInt(a.getId()) < Integer.parseInt(b.getId())) {
                return -1;
            } else
                return 0;
        }
    }

    public boolean isiAm() {
        return iAm;
    }

    public void setiAm(boolean iAm) {
        this.iAm = iAm;
    }

    public void removeConfirmacionActualizacion(String idDatabase) {
        confirmacionActualizacion.remove(idDatabase);
    }
}
