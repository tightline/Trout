package ryan.trout.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ryan.trout.app.AppConfig;
import ryan.trout.app.AppController;

/**
 * Created by Ryan on 3/16/2018.
 */

public class Stream {
    private double lat, lon, lenm,lat2,lon2,perPub;
    private String name, county, biomass, fishery;
    private LatLng latlon;
    private List<Marker> aMarkers;

    public Stream(double lat, double lon, String name, String biomass, String county,
                  double lenm, String fishery, double perPub, double lat2, double lon2){
        this.lat = lat;
        this.lon = lon;
        this.lat2 =lat2;
        this.lon2=lon2;
        this.biomass=biomass;
        this.perPub =perPub;
        this.lenm=lenm;
        this.fishery=fishery;
        this.name=name;
        this.county=county;
        this.latlon= new LatLng(this.lat,this.lon);
    }

    public double getLat(){
        return lat;
    }
    public double getLon2(){
        return lon2;
    }
    public double getLat2(){
        return lat2;
    }
    public double getLon(){
        return lon;
    }
    public double getLenm(){
        return lenm;
    }
    public double getPerPub(){return perPub;}
    public String getBiomass(){return biomass;}
    public String getFishery(){return fishery;}
    public String getName(){
        return name;
    }
    public String getCounty(){
        return county;
    }
    public LatLng getLatlon(){
        return latlon;
    }
    //public List<Marker> getaMarkers(){return aMarkers;}
   /* public void setaMarkers(List<Stream> aStreamlist){
        //
        for (Stream stream : aStreamlist) {
            //LatLng sydney = new LatLng(-33.852, 151.211);
            this.aMarkers.add( new Marker().position(stream.getLatlon())
                    .title(stream.getName()));

        }
    }*/


}
