package ryan.trout.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ryan.trout.R;
import ryan.trout.app.AppConfig;
import ryan.trout.app.AppController;
import ryan.trout.helper.Gauge;
import ryan.trout.helper.Stream;
import ryan.trout.helper.PopupAdapter;
import ryan.trout.helper.Stream_Gauge_Data;


public class MapPage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private ProgressDialog pDialog;
    private static final String TAG = MapPage.class.getSimpleName();
    private List<Stream> AStreams = null;
    private static List<Gauge> lGauges = null;
    private static GoogleMap map;
    private Button plot_astream_btn, plot_guages_btn, plot_wild_stream_btn,plot_approved_streams, plot_nat_btn,legend_btn;
    private FloatingActionButton change_base_layer_btn;
    private static List<Marker> guageMarkers, aStreamMarkers;
    private boolean aStreamsDisplayed = false, guagesDisplayed = false;
    private List<Double> lon, lat;
    private VisibleRegion visRegion;
    private LatLng usr_location;
    SupportMapFragment mapFragment;
    private KmlLayer[][] a_stream_arr;
    private KmlLayer layer_a_stream = null, layer_wild_stream = null, layer_natrepro_stream, layer_approved_stream;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    Location lastLocation;
    Marker currLocationMarker;
    public static Context mContext;
    private Stream_Gauge_Data gauge_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_page);
        mContext= getApplicationContext();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        plot_astream_btn = (Button) findViewById(R.id.button1);
        plot_guages_btn = (Button) findViewById(R.id.button2);
        plot_wild_stream_btn = (Button) findViewById(R.id.button3);
        plot_approved_streams = (Button) findViewById(R.id.button4);
        plot_nat_btn = (Button) findViewById(R.id.button5);
        legend_btn = (Button) findViewById(R.id.button6);
        change_base_layer_btn = (FloatingActionButton) findViewById(R.id.change_btn);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        gauge_data = new Stream_Gauge_Data();
        gauge_data.connect();
        lat = new ArrayList<Double>() {
            {
                add(40.20472);
                add(40.6825);
                add(41.16722);
                add(41.64528);
            }
        };
        lon = new ArrayList<Double>() {
            {
                add(-79.35222);
                add(-78.18639);
                add(-77.62028);
                add(-75.85361);
            }
        };

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        FragmentManager myFragmentManager = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        //bottomSheetBehavior.setPeekHeight(80);

        legend_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapPage.this, Legend.class);
                startActivity(intent);
            }
        });

        plot_guages_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guageMarkers != null) {
                    if (!isGuagesOn()) {
                        showMarkers(guageMarkers);
                        plot_guages_btn.setText("Hide Guages");
                        setGuageDisplay();
                        //map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
                        //map.setOnInfoWindowClickListener(MapPage.this);
                    } else {
                        //hide guages
                        plot_guages_btn.setText("Show Guages");
                        hideMarkers(guageMarkers);
                        setGuageDisplay();
                    }
                }
                else{Log.e(TAG, "did not display nul....................");};
            }
        });
        plot_astream_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer_a_stream != null && !layer_a_stream.isLayerOnMap()) {
                    //showMarkers(aStreamMarkers);
                    plot_astream_btn.setText("Hide Streams");
                    add_layer(layer_a_stream);
                    Log.e("layer add:", "is layer added");
                    /*layer_a_stream.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {

                        @Override
                        public void onFeatureClick(Feature feature) {
                            Log.i("KmlClick", "Feature clicked: " + feature.getId());
                            feature.getProperty("description");
                            Log.i("KML description", "desc is: " + feature.getProperty("description"));
                            Log.i("KML Name", "Name is: " + feature.getProperty("name"));
                            final Dialog d = new Dialog(MapPage.this);
                            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            //d.setTitle("Select");
                            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                            d.setContentView(R.layout.popup);
                            TextView tvName = (TextView) d.findViewById(R.id.snippet);
                            Spanned htmlAsSpanned = Html.fromHtml(feature.getProperty("description"));
                            tvName.setText(htmlAsSpanned);
                            d.show();
                        }
                    });*/
                    Log.e("after:", "1");

                } else if (layer_a_stream != null) {
                    plot_astream_btn.setText("Show Streams");
                    //hideMarkers(aStreamMarkers);
                    try {
                        layer_a_stream.removeLayerFromMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        plot_wild_stream_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer_wild_stream != null && !layer_wild_stream.isLayerOnMap()) {
                    plot_wild_stream_btn.setText("Hide Wild Streams");

                    add_layer(layer_wild_stream);
                    Log.e("layer add:", "is wild layer added");

                } else if (layer_wild_stream != null) {
                    plot_wild_stream_btn.setText("Show Wild Streams");
                    try {
                        layer_wild_stream.removeLayerFromMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //not loaded yet
                }
            }
        });
        plot_approved_streams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer_approved_stream != null && !layer_approved_stream.isLayerOnMap()) {
                    plot_approved_streams.setText("Hide Wild Streams");

                    add_layer(layer_approved_stream);
                    Log.e("layer add:", "is wild layer added");

                } else if (layer_approved_stream != null) {
                    plot_approved_streams.setText("Show Wild Streams");
                    try {
                        layer_approved_stream.removeLayerFromMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //not loaded yet
                }
            }
        });
        plot_nat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer_natrepro_stream != null && !layer_natrepro_stream.isLayerOnMap()) {
                    plot_nat_btn.setText("Hide Wild Streams");

                    add_layer(layer_natrepro_stream);
                    Log.e("layer add:", "is wild layer added");

                } else if (layer_natrepro_stream != null) {
                    plot_nat_btn.setText("Show Wild Streams");
                    try {
                        layer_natrepro_stream.removeLayerFromMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //not loaded yet
                }
            }
        });
        change_base_layer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (map.getMapType()) {
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 4:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
    }

    public void showMarkers(List<Marker> list) {
        for (Marker m : list) {
            m.setVisible(true);
        }
    }

    public void hideMarkers(List<Marker> list) {
        for (Marker m : list) {
            m.setVisible(false);
        }
    }

    /**
     * Implements onMapReady callback sets GoogleMap variable
     * @param googleMap
     * creates KML layers
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.map = googleMap;
        //done in background to increase speed of start up
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //layer = new KmlLayer(map, R.raw.classa_str_2018, getApplicationContext());
                    layer_a_stream = new KmlLayer(map, R.raw.classa_str_2018, getApplicationContext());
                    layer_wild_stream = new KmlLayer(map, R.raw.wilderness_trout_streams2015, getApplicationContext());
                    layer_natrepro_stream = new KmlLayer(map, R.raw.trout_natural_repro2018, getApplicationContext());
                    layer_approved_stream = new KmlLayer(map, R.raw.approved_trout_waters2018, getApplicationContext());

                    /*a_stream_arr = new KmlLayer[][]{{new KmlLayer(map, R.raw.classa_2018_00, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_01, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_02, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_03, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_04, getApplicationContext())},
                            {new KmlLayer(map, R.raw.classa_2018_10, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_11, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_12, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_13, getApplicationContext()), null},
                            {new KmlLayer(map, R.raw.classa_2018_20, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_21, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_22, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_23, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_24, getApplicationContext())},
                            {null, new KmlLayer(map, R.raw.classa_2018_31, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_32, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_33, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_34, getApplicationContext())},
                            {new KmlLayer(map, R.raw.classa_2018_40, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_41, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_42, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_43, getApplicationContext()), new KmlLayer(map, R.raw.classa_2018_44, getApplicationContext())}};
                    //(Get-Content c:\temp\test.txt).replace('<MultiGeometry>', '') | Set-Content c:\temp\test.txt*/
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //getAStreams();
        //UpdateStreams();
        //getGuages();

        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);

        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(120000); // two minute interval
        locationRequest.setFastestInterval(120000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Feature feature;
                //feature = layer_a_stream.getContainerFeature(polyline);


                if ((layer_a_stream.isLayerOnMap())
                        &&(((feature = layer_a_stream.getFeature(polyline)) != null)
                        || ((feature = layer_a_stream.getContainerFeature(polyline)) != null))) {
                        Log.i("get feature container", "feature not null");

                        feature.getProperty("description");
                        //Log.i("KML description", "desc is: " + feature.getProperty("description"));
                        Log.i("KML Name", "Name is: " + feature.getProperty("name"));
                        final Dialog d = new Dialog(MapPage.this);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        //d.setTitle("Select");
                        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                        d.setContentView(R.layout.popup);
                        TextView tvName = (TextView) d.findViewById(R.id.snippet);
                        Spanned htmlAsSpanned = Html.fromHtml(feature.getProperty("description"));
                        tvName.setText(htmlAsSpanned);
                        d.show();

                } else if ((layer_wild_stream.isLayerOnMap())&& (((feature = layer_wild_stream.getFeature(polyline)) != null)
                            || ((feature = layer_wild_stream.getContainerFeature(polyline)) != null))) {
                         Log.i("get feature container", "feature not null");


                         feature.getProperty("description");
                         //Log.i("KML description", "desc is: " + feature.getProperty("description"));
                         Log.i("KML Name", "Name is: " + feature.getProperty("name"));
                         final Dialog d = new Dialog(MapPage.this);
                         d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                         //d.setTitle("Select");
                         d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                         d.setContentView(R.layout.popup);
                         TextView tvName = (TextView) d.findViewById(R.id.snippet);
                         Spanned htmlAsSpanned = Html.fromHtml(feature.getProperty("description"));
                         tvName.setText(htmlAsSpanned);
                         d.show();

                }else if ((layer_approved_stream.isLayerOnMap())
                        &&(((feature = layer_approved_stream.getFeature(polyline)) != null)
                            || ((feature = layer_approved_stream.getContainerFeature(polyline)) != null))) {
                        Log.i("get feature container", "feature not null");


                        feature.getProperty("description");
                        //Log.i("KML description", "desc is: " + feature.getProperty("description"));
                        Log.i("KML Name", "Name is: " + feature.getProperty("name"));
                        final Dialog d = new Dialog(MapPage.this);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        //d.setTitle("Select");
                        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                        d.setContentView(R.layout.popup);
                        TextView tvName = (TextView) d.findViewById(R.id.snippet);
                        Spanned htmlAsSpanned = Html.fromHtml(feature.getProperty("description"));
                        tvName.setText(htmlAsSpanned);
                        d.show();
                }else if ((layer_natrepro_stream.isLayerOnMap())
                        &&(((feature = layer_natrepro_stream.getFeature(polyline)) != null)
                        || ((feature = layer_natrepro_stream.getContainerFeature(polyline)) != null))) {
                    Log.i("get feature container", "feature not null");


                    feature.getProperty("description");
                    //Log.i("KML description", "desc is: " + feature.getProperty("description"));
                    Log.i("KML Name", "Name is: " + feature.getProperty("name"));
                    final Dialog d = new Dialog(MapPage.this);
                    d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    //d.setTitle("Select");
                    d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    d.setContentView(R.layout.popup);
                    TextView tvName = (TextView) d.findViewById(R.id.snippet);
                    Spanned htmlAsSpanned = Html.fromHtml(feature.getProperty("description"));
                    tvName.setText(htmlAsSpanned);
                    d.show();
                }
            }
        });

        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                Log.e("check loc","checking loc0");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                Log.e("check loc","check permission");

                checkLocationPermission();
            }
       /* }
        else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            map.setMyLocationEnabled(true);
        }*/

    }

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                lastLocation = location;
                if (currLocationMarker != null) {
                    currLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currLocationMarker = map.addMarker(markerOptions);

                //move map camera
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        };

    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("check loc","Alert dialog");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapPage.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                Log.e("check loc","No alert");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.e("check loc","checking loc1");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        map.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
        int ind = getGuageIndex(marker.getTitle());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://waterdata.usgs.gov/nwis/uv?site_no="+ lGauges.get(ind).getId()));
        startActivity(browserIntent);
    }

    private void add_layer(KmlLayer layers){
        //determine what coordinates are within the map frame
        //limit to a certain zoom level?
        try {
            layers.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        visRegion=map.getProjection().getVisibleRegion();
        //visRegion.latLngBounds.northeast.longitude;
        //visRegion.latLngBounds.southwest.latitude;
       /* if(visRegion.latLngBounds.southwest.latitude>=lat.get(3)){
            if(longi<=lon.get(0)){
                //00
                mainlist.get(0).get(0).add(pm);
            }else if(longi<=lon.get(1) && longi>lon.get(0)){
                //01
                mainlist.get(0).get(1).add(pm);
            }else if(longi<=lon.get(2) && longi>lon.get(1)){
                //02
                mainlist.get(0).get(2).add(pm);
            }else if(longi<=lon.get(3) && longi>lon.get(2)){
                //03
                mainlist.get(0).get(3).add(pm);
            }else if(longi>lon.get(3)){
                //04
                mainlist.get(0).get(4).add(pm);
            }else{

            }


        }else if(lati>=lat.get(2)&&lati<lat.get(3)){
            if(longi<=lon.get(0)){
                //10
                mainlist.get(1).get(0).add(pm);
            }else if(longi<=lon.get(1) && longi>lon.get(0)){
                //11
                mainlist.get(1).get(1).add(pm);
            }else if(longi<=lon.get(2) && longi>lon.get(1)){
                //12
                mainlist.get(1).get(2).add(pm);
            }else if(longi<=lon.get(3) && longi>lon.get(2)){
                //13
                mainlist.get(1).get(3).add(pm);
            }else if(longi<lon.get(3)){
                //14
                mainlist.get(1).get(4).add(pm);
            }else{

            }

        }else if(lati>=lat.get(1)&&lati<lat.get(2)){
            if(longi<=lon.get(0)){
                //20
                mainlist.get(2).get(0).add(pm);
            }else if(longi<=lon.get(1) && longi>lon.get(0)){
                //21
                mainlist.get(2).get(1).add(pm);
            }else if(longi<=lon.get(2) && longi>lon.get(1)){
                //22
                mainlist.get(2).get(2).add(pm);
            }else if(longi<=lon.get(3) && longi>lon.get(2)){
                //23
                mainlist.get(2).get(3).add(pm);
            }else if(longi>lon.get(3)){
                //24
                mainlist.get(2).get(4).add(pm);
            }else{

            }

        }else if(lati>=lat.get(0)&&lati<lat.get(1)){

            if(longi<=lon.get(0)){
                //30
                mainlist.get(3).get(0).add(pm);
            }else if(longi<=lon.get(1) && longi>lon.get(0)){
                //31
                mainlist.get(3).get(1).add(pm);
            }else if(longi<=lon.get(2) && longi>lon.get(1)){
                //32
                mainlist.get(3).get(2).add(pm);
            }else if(longi<=lon.get(3) && longi>lon.get(2)){
                //33
                mainlist.get(3).get(3).add(pm);
            }else if(longi>lon.get(3)){
                //34
                mainlist.get(3).get(4).add(pm);
            }else{

            }
        }else if(lati<lat.get(0)){
            if(longi<=lon.get(0)){
                //40
                mainlist.get(4).get(0).add(pm);
            }else if(longi<=lon.get(1) && longi>lon.get(0)){
                //41
                mainlist.get(4).get(1).add(pm);
            }else if(longi<=lon.get(2) && longi>lon.get(1)){
                //42
                mainlist.get(4).get(2).add(pm);
            }else if(longi<=lon.get(3) && longi>lon.get(2)){
                //43
                mainlist.get(4).get(3).add(pm);
            }else if(longi>lon.get(3)){
                //44
                mainlist.get(4).get(4).add(pm);
            }else{

            }
        }else {
            // out of bounds

        }*/

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
     * Sets array list guageMarkers to Gauge Markers recieved
     * Sets visibility of all markers to false
     */
    public static void addGuages(){
        if(lGauges != null) {
            guageMarkers = new ArrayList<Marker>();

            for (Gauge gauge : lGauges) {
                guageMarkers.add(map.addMarker(new MarkerOptions().position(gauge.getLatlon())
                        .title(gauge.getName())
                        .snippet("Flow Rate: " + gauge.getFlow_rate() + "\nHeight: " +
                                gauge.getHeight() + "\nTime: " + gauge.getTime())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .visible(false)));

            }
            //map.moveCamera(CameraUpdateFactory.newLatLng(lGauges.get(0).getLatlon()));
        }
    }

    public int getGuageIndex(String gName){
        int index=0;
        for(Gauge g: lGauges){
            if(g.getName().equals(gName)) {return index;}
            index++;
        }
        return index;
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

    public void  UpdateStreams(){
        String tag_string_req = "req_update_streams";

        pDialog.setMessage("Retrieving data ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_STREAMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Update Stream Response: " + response.toString());
                hideDialog();
                //getGuages();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Stream update Error: " + error.getMessage());
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
     * @param g is a List of Gauge sets private class var lGauges
     */
    public static void setGuages(List<Gauge> g){lGauges = g;}

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

    public void initializeMap(){

    }

    @Override
    public void onResume() {
        Log.e("In on resume","on resume");
        super.onResume();
        //initializeMap();
        if(mapFragment!=null)
            mapFragment.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        //mapFragment.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    @Override
    public void onDestroy() {
        //if(mapFragment!=null)
            //mapFragment.onDestroy();

        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //if(mapFragment!=null)
          //  mapFragment.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }



}
