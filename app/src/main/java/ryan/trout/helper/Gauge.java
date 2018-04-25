package ryan.trout.helper;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ryan on 3/27/2018.
 */

public class Gauge {
    private String id, name, flow_rate, height, time, h_unit, r_unit, h_desc, r_desc;
    private double lat,lon;
    private LatLng latlon;

    public Gauge(String iid, String iname, String ilat, String ilon, String iTime){
        this.id =iid;
        this.name =iname;
        this.lat=Double.parseDouble(ilat);
        this.lon=Double.parseDouble(ilon);
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
    public String getH_unit(){return this.h_unit;}
    public String getH_desc(){return this.h_desc;}
    public String getR_unit(){return this.r_unit;}
    public String getR_desc(){return this.r_desc;}

    public void setHeight(String height){ this.height=height; }
    public void setH_unit(String H_unit){this.h_unit=h_unit;}
    public void setH_desc(String h_desc){this.h_desc =h_desc;}
    public void setFlow_rate(String rate){this.flow_rate=rate;}
    public void setR_unit(String r_unit){this.r_unit=r_unit;}
    public void setR_desc(String r_desc){this.r_desc=r_desc;}



}
