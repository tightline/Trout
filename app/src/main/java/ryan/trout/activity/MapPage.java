package ryan.trout.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.google.maps.android.data.Layer;
import com.google.maps.android.data.kml.KmlLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ryan.trout.R;
import ryan.trout.app.AppConfig;
import ryan.trout.app.AppController;
import ryan.trout.helper.Guage;
import ryan.trout.helper.Stream;
import ryan.trout.helper.PopupAdapter;

public class MapPage extends FragmentActivity  implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private ProgressDialog pDialog;
    private static final String TAG = MapPage.class.getSimpleName();
    private List<Stream> AStreams=null;
    private List<Guage> lGuages =null;
    private GoogleMap map;
    private Button plot_stream_btn, plot_guages_btn;
    private List<Marker> guageMarkers, aStreamMarkers;
    private boolean aStreamsDisplayed=false, guagesDisplayed=false;
    SupportMapFragment mapFragment;
    private KmlLayer layer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_page);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        plot_stream_btn = (Button)findViewById(R.id.button1);
        plot_guages_btn = (Button)findViewById(R.id.button2);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        plot_guages_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGuagesOn()) {
                    showMarkers(guageMarkers);
                    plot_guages_btn.setText("Hide Guages");
                    setGuageDisplay();
                }else{
                    //hide guages
                    plot_guages_btn.setText("Show Guages");
                    hideMarkers(guageMarkers);
                    setGuageDisplay();
                }
            }
        });
        plot_stream_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!layer.isLayerOnMap()) {
                    //showMarkers(aStreamMarkers);
                    plot_stream_btn.setText("Hide Streams");
                    try {
                        layer.addLayerToMap();
                        Log.e("layer add:","is layer added");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }else{
                    plot_stream_btn.setText("Show Streams");
                    //hideMarkers(aStreamMarkers);
                    try {
                        layer.removeLayerFromMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void showMarkers(List<Marker> list){
        for(Marker m : list){
            m.setVisible(true);
        }
    }
    public void hideMarkers(List<Marker> list){
        for(Marker m : list){
            m.setVisible(false);
        }
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
            Log.e("waiting","yes waiting ");
            this.map=googleMap;
        getGuages();
                getAStreams();
        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //layer = new KmlLayer(map, R.raw.classa_str_2018, getApplicationContext());
                    layer = new KmlLayer(map, R.raw.classa_str_2018, getApplicationContext());
                    //(Get-Content c:\temp\test.txt).replace('<MultiGeometry>', '') | Set-Content c:\temp\test.txt
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    /**
     * Sets array list aStreamMarkers to Class A Stream Markers recieved
     * Sets visibility of all markers to false
     */
    private void addStreams(){
        if(AStreams != null) {
            aStreamMarkers = new ArrayList<Marker>();

            for (Stream stream : AStreams) {
                //LatLng sydney = new LatLng(-33.852, 151.211);
                aStreamMarkers.add(map.addMarker(new MarkerOptions().position(stream.getLatlon())
                        .title(stream.getName())
                        .visible(false)));

            }
            //map.moveCamera(CameraUpdateFactory.newLatLng(AStreams.get(0).getLatlon()));
        }
    }

    /**
     * Sets array list guageMarkers to Guage Markers recieved
     * Sets visibility of all markers to false
     */
    private void addGuages(){
        if(lGuages != null) {
            guageMarkers = new ArrayList<Marker>();

            for (Guage guage : lGuages) {
                guageMarkers.add(map.addMarker(new MarkerOptions().position(guage.getLatlon())
                        .title(guage.getName())
                        .snippet("Flow Rate: " + guage.getFlow_rate() + "\nHeight: " +
                                guage.getHeight() + "\nTime: " + guage.getTime())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .visible(false)));

            }
            //map.moveCamera(CameraUpdateFactory.newLatLng(lGuages.get(0).getLatlon()));
        }
    }


    /**
     * Volley call to server to retrieve Class A Stream Data
     * Creates list of object Stream called AStreams
     * Calls setAStreams() when data recieved
     * Calls addStreams()
     */
    public void  getAStreams(){
        String tag_string_req = "req_get_AStreams";

        pDialog.setMessage("Retrieving data ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_STREAMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Stream Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Log.e(TAG, response.toString());
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // String result = sb.toString();
                        //callback.onSuccess(response);

                        try {
                            //parse data here
                            //JSONObject jObj = new JSONObject(result);
                            JSONArray jArray = jObj.getJSONArray("streams");
                            ArrayList<Stream> aStreams = new ArrayList<Stream>();
                            if (jArray != null) {
                                for (int i = 0; i < jArray.length(); i++) {
                                    jArray.getJSONObject(i).optString("");

                                    aStreams.add(new Stream(Double.parseDouble(jArray.getJSONObject(i).optString("WtrLatDD")),
                                            Double.parseDouble(jArray.getJSONObject(i).optString("WtrLonDD")),
                                            jArray.getJSONObject(i).optString("WtrName"),
                                            jArray.getJSONObject(i).optString("TroutBioma"),
                                            jArray.getJSONObject(i).optString("COUNTY_NAM"),
                                            Double.parseDouble(jArray.getJSONObject(i).optString("Length_MI_")),
                                            jArray.getJSONObject(i).optString("Fishery"),
                                            Double.parseDouble(jArray.getJSONObject(i).optString("Percent_Pu")),
                                            Double.parseDouble(jArray.getJSONObject(i).optString("LAT")),
                                            Double.parseDouble(jArray.getJSONObject(i).optString("LNG"))));
                                }
                                setAStreams(aStreams);
                                addStreams();
                    }
                }catch(Exception e){
                    e.printStackTrace();

                }
                        //Toast.makeText(getApplicationContext(), "custs recieved!", Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Stream retrieval Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Volley call to server to retrieve Stream Guage data
     * Creates list of object Guages
     * Calls setGuages() when recieved data
     * Calls addGuages()
     */
    public void  getGuages(){
        String tag_string_req = "req_get_guages";

        pDialog.setMessage("Retrieving data ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_GUAGES, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Guages Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Log.e(TAG, response.toString());
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        try {
                            //parse data here
                            JSONArray jArray = jObj.getJSONArray("guages");
                            ArrayList<Guage> aGuages = new ArrayList<Guage>();
                            if (jArray != null) {
                                for (int i = 0; i < jArray.length(); i++) {
                                    jArray.getJSONObject(i).optString("");

                                    aGuages.add(new Guage(jArray.getJSONObject(i).optString("id"),
                                            jArray.getJSONObject(i).optString("name"),
                                            jArray.getJSONObject(i).optString("lat"),
                                            jArray.getJSONObject(i).optString("lon"),
                                            jArray.getJSONObject(i).optString("rate"),
                                            jArray.getJSONObject(i).optString("height"),
                                            jArray.getJSONObject(i).optString("time")));
                                }
                                setGuages(aGuages);
                                addGuages();
                            }
                        }catch(Exception e){
                            e.printStackTrace();

                        }
                        //Toast.makeText(getApplicationContext(), "custs recieved!", Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Guage retrieval Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     *
     * @param st is a List of Stream sets private class var AStreams
     */
    private void setAStreams(List<Stream> st){
        this.AStreams=st;
    }

    /**
     *
     * @param g is a List of Guage sets private class var lGuages
     */
    private void setGuages(List<Guage> g){this.lGuages = g;}

    public boolean isStreamsOn(){
        return aStreamsDisplayed;
    }
    public boolean isGuagesOn(){
        return guagesDisplayed;
    }

    public void setGuageDisplay(){
        if(guagesDisplayed){
            guagesDisplayed=false;
        }else{
            guagesDisplayed=true;
        }
    }
    public void setaStreamDisplay(){
        if(aStreamsDisplayed){
            aStreamsDisplayed=false;
        }else{
            aStreamsDisplayed=true;
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    @Override
    public void onResume() {
        Log.e("In on resume","on resume");
        super.onResume();
        mapFragment.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapFragment.onSaveInstanceState(outState);
    }

}
