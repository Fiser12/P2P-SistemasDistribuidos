package Tracker.VO;

import java.util.Comparator;
import java.util.Date;

public class TrackerKeepAlive implements Comparator<Object> {

    private String id;
    private Date lastKeepAlive;
    private boolean master;
    private boolean iAm;

    private Estado confirmacionActualizacion;

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

    public Estado getConfirmacionActualizacion() {
        return confirmacionActualizacion;
    }

    public void setConfirmacionActualizacion(Estado confirmacionActualizacion) {
        this.confirmacionActualizacion = confirmacionActualizacion;
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
}
