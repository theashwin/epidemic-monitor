package mit.samaritans.em;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import mit.samaritans.em.utils.BottomSheetFragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.heatmaps.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class RealTimeMonitor extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button mDo;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private BottomAppBar bottomAppBar;
    private NavigationView navigationView;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtm);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyCeOmvalttgRogXN1BiW1wq-3HvVmSogEE");

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                addMarker(place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("1", "An error occurred: " + status);
            }

        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //FIND VIEW BY IDS
        mDo = findViewById(R.id.mDo);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        navigationView = findViewById(R.id.navigation_view);
        floatingActionButton = findViewById(R.id.fab);
        setSupportActionBar(bottomAppBar);
        bottomAppBar.replaceMenu(R.menu.map);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent onIntent = new Intent(RealTimeMonitor.this, Crowdsource.class);
                RealTimeMonitor.this.startActivity(onIntent);
            }
        });

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.next_btn:
                        Toast.makeText(RealTimeMonitor.this, "Notification clicked.", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottomSheet(BottomSheetFragment.class.getName());
            }
        });
    }

    //Inflate menu to bottom bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);


        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                return;
            }
        }
        catch(Exception e) {
            Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
        }



        //BLUE DABA DE.
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map));

            if (!success) {
                Log.e("1", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("1", "Can't find style. Error: ", e);
        }


        // Add a marker in Pune and move the camera
        LatLng pune = new LatLng(18.5204, 73.8567);
        LatLng pimpri = new LatLng(18.5204, 73.9567);
        //mMap.addMarker(new MarkerOptions().position(new LatLng()).title("Marker in Pune"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pune));
        mMap.setMaxZoomPreference(11.5f);
        mMap.setMinZoomPreference(4.0f);

        mMap.addCircle(new CircleOptions()
                .center(pune)
                .radius(20000)
                .strokeColor(Color.parseColor("#502196f3"))
                .fillColor(Color.parseColor("#501565c0"))
                .strokeWidth(3)
                .clickable(true));

        mMap.addCircle(new CircleOptions()
                .center(pimpri)
                .radius(20000)
                .strokeColor(Color.parseColor("#504caf50"))
                .fillColor(Color.parseColor("#502e7d32"))
                .strokeWidth(3)
                .clickable(true));



        //HEATMAPS
        addHeatMap();


        //Default variables for translation
        String textToBeTranslated = "Hello world, yeah I know it is stereotype.";
        String languagePair = "en-en"; //English to French ("<source_language>-<target_language>")
        //Executing the translation function
        Translate(textToBeTranslated,languagePair);


    }

    private void addHeatMap() {
        List<LatLng> list = null;

        // Get the data: latitude/longitude positions of police stations.
        try {
            list = readItems(R.raw.heatmap);
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        mProvider.setOpacity(0.7);
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            private float currentZoom;
            @Override
            public void onCameraMove() {
                float zoom = mMap.getCameraPosition().zoom;
                LatLng x = mMap.getCameraPosition().target;
                if(zoom < 4 && zoom >= 12)
                    return;
                if(Math.abs(currentZoom - zoom) >= 1) {
                    currentZoom = zoom;

                    double someLatValue = x.latitude;
                    float desiredRadiusInMeters = 15000;
                    double metersPerPx = (156543.03392 * Math.cos(someLatValue * Math.PI / 180)) / Math.pow(2,zoom);

                    mProvider.setRadius((int)Math.ceil(desiredRadiusInMeters / metersPerPx));
                    mOverlay.clearTileCache();
                    //mDo.setText(metersPerPx+"-"+currentZoom+"+"+someLatValue);
                    mDo.setVisibility(View.GONE);
                }
            }
        });



        // Create the gradient.
        int[] colors = {
                Color.rgb(255, 152, 0),    // orange
                Color.rgb(255, 10, 10)    // red
        };

        float[] startPoints = {
                0.2f, 0.75f
        };

        Gradient gradient = new Gradient(colors, startPoints);
        mProvider.setGradient(gradient);


        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    String Translate(String textToBeTranslated,String languagePair){
        TranslatorBackgroundTask translatorBackgroundTask= new TranslatorBackgroundTask(getApplicationContext());
        try {
            String translationResult = translatorBackgroundTask.execute(textToBeTranslated,languagePair).get(); // Returns the translated text as a String
            Log.d("Translation Result",translationResult); // Logs the result in Android Monitor
            return translationResult;
        }
        catch(Exception e) { }
        return "";
    }

    public void openBottomSheet(String tag) {
        BottomSheetDialogFragment bottomSheet = null;
        if (tag.equals(BottomSheetFragment.class.getName())) {
            bottomSheet = new BottomSheetFragment();
            bottomSheet.setAllowEnterTransitionOverlap(true);
        }
        if (bottomSheet != null) {
            bottomSheet.show(getSupportFragmentManager(), tag);
        }
    }

    private void gotoZoom(LatLng loc) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.getUiSettings();
    }


    public void addMarker(String loc){
        List<Address> addressList = new ArrayList<>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addressList = geocoder.getFromLocationName(loc, 1);
            double newLattitude = addressList.get(0).getLatitude();
            double newLongitude = addressList.get(0).getLongitude();
            Log.d("MyActivityTag", newLattitude + " : "+ newLongitude);
            LatLng latLng = new LatLng(newLattitude, newLongitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(options);
        }
        catch (Exception e)   {

        }



    }
}
