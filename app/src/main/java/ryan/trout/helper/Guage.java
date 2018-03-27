package ryan.trout.helper;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ryan on 3/27/2018.
 */

public class Guage {
    private String id, name, flow_rate, height, time;
    private double lat,lon;
    private LatLng latlon;

    public Guage(String iid, String iname, String ilat, String ilon, String iflow, String iheight, String iTime){
        this.id =iid;
        this.name=iname;
        this.lat=Double.parseDouble(ilat);
        this.lon=Double.parseDouble(ilon);
        this.flow_rate=iflow;
        this.height=iheight;
        this.time=iTime;
        this.latlon= new LatLng(this.lat,this.lon);    }

    public String getId(){return id;}
    public String getName(){return name;}
    public double getLon(){return lon;}
    public double getLat() {return lat;}
    public String getFlow_rate(){return flow_rate;}
    public String getHeight(){return height;}
    public String getTime(){return time;}
    public LatLng getLatlon(){return latlon;}
}
