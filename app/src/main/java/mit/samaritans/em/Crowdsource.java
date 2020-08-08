package mit.samaritans.em;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Crowdsource extends AppCompatActivity implements OnMapReadyCallback{
    private Spinner spinner;
    private Spinner ageSpinner;
    private EditText date;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private FloatingActionButton floatingActionButton;
    private MapView mapView;
    private OnMapReadyCallback onMapReadyCallback;
    private String provider;

    private ArrayList<String> diseases;
    boolean isEnabled = true;
    private GoogleSignInAccount acc;

    private RadioButton male;

    private double latitude;
    private double longitude;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);


        Button currentBT = findViewById(R.id.currentBT);
        final Button saveBT = findViewById(R.id.saveBT);

        currentBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToCurrentLocation(googleMap);
            }
        });

        saveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEnabled = !isEnabled;
                googleMap.getUiSettings().setAllGesturesEnabled(isEnabled);

                if(isEnabled) {
                    saveBT.setText(R.string.save_location);
                }
                else {
                    saveBT.setText(R.string.edit_location);
                }
            }
        });

        // Use the location manager through GPS
        try {
            moveToCurrentLocation(googleMap);
            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    TextView tv = findViewById(R.id.select);
                    latitude = googleMap.getCameraPosition().target.latitude;
                    longitude = googleMap.getCameraPosition().target.longitude;
                  }
            });
        }
        catch(Exception e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowdsource);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }

        mapView = findViewById(R.id.customMap);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        googleSignIn();
        mongoFetch();

        String[] ageGroups = {"<1","1 - 4","5 - 9","10 - 14","15 - 24","25 - 34","35 - 44","45 - 54","55 - 64","65+"};
        spinner = findViewById(R.id.diseaseSpinner);
        ageSpinner = findViewById(R.id.ageSpinner);
        date = findViewById(R.id.date);
        floatingActionButton = findViewById(R.id.fab);
        diseases = new ArrayList<String>();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton male = findViewById(R.id.male);
                RadioButton female = findViewById(R.id.female);
                RadioGroup gender = findViewById(R.id.gender);
                //VALIDATE DATA
                try {
                    //- DATE
                    Calendar cal = Calendar.getInstance();
                    final int day = cal.get(Calendar.DAY_OF_MONTH);
                    final int month = cal.get(Calendar.MONTH);
                    final int year = cal.get(Calendar.YEAR);
                    int mt = month + 1;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy");
                    Date current = sdf.parse(day + " / " + mt + " / " + year);
                    Date user = sdf.parse(date.getText().toString());

                    if(current.before(user)) {
                        date.setBackgroundColor(R.color.red);
                        return;
                    }
                    mongoPush();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),"Data is Valid",Toast.LENGTH_SHORT).show();
            }
        });


        ArrayAdapter ageArrayAdapter = new ArrayAdapter(this,R.layout.spinner,ageGroups);
        ageArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        ageSpinner.setAdapter(ageArrayAdapter);

        Calendar cal = Calendar.getInstance();
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        final int month = cal.get(Calendar.MONTH);
        final int year = cal.get(Calendar.YEAR);
        int mt = month + 1;
        date.setText(day + " / " + mt + " / " + year);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(
                        Crowdsource.this,
                        AlertDialog.THEME_HOLO_LIGHT,
                        dateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                date.setText(day + " / " + month + " / " + year);
            }
        };
    }

    private void mongoFetch(){
        final StitchAppClient client = Stitch.getDefaultAppClient();
        client.getAuth().loginWithCredential(new AnonymousCredential());
        final RemoteMongoClient mongoClient =
                client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

        final RemoteMongoCollection<Document> coll =
                mongoClient.getDatabase("em").getCollection("diseases");

        Document filterDoc = new Document();

        RemoteFindIterable findResults = coll
                .find(filterDoc)
                .projection(new Document()
                        .append("_id", 0)
                        .append("symptoms",0)
                        .append("diagnosis",0)
                        .append("complications",0)
                        .append("transmissions",0)
                        .append("causes",0)
                        .append("deaths",0)
                        .append("onset",0)
                        .append("medication",0)
                        .append("links",0))
                .sort(new Document("name",1));

        Task <List<Document>> itemsTask = findResults.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    List<Document> items = task.getResult();
                    Log.d("app", String.format("successfully found %d documents", items.size()));

                    for (Document item: items) {
                        diseases.add((String) item.get("name"));
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),R.layout.spinner, diseases);
                    arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdapter);
                } else {
                    Log.e("app", "failed to find documents with: ", task.getException());
                }
            }
        });
    }

    private void moveToCurrentLocation(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                CameraPosition.Builder camBuilder = CameraPosition.builder();
                camBuilder.target(new LatLng(location.getLatitude(), location.getLongitude()));
                camBuilder.zoom(14);
                CameraPosition cp = camBuilder.build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            }
        }
    }

    private void mongoPull(){
        final StitchAppClient client = Stitch.getDefaultAppClient();
        client.getAuth().loginWithCredential(new AnonymousCredential());
        final RemoteMongoClient mongoClient =
                client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

        final RemoteMongoCollection<Document> coll =
                mongoClient.getDatabase("em").getCollection("users");

        Document filterDoc = new Document().append("email",acc.getEmail());

        RemoteFindIterable findResults = coll
                .find(filterDoc);

        Task <List<Document>> itemsTask = findResults.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@com.mongodb.lang.NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    List<Document> items = task.getResult();
                    Log.d("app", String.format("successfully found %d documents", items.size()));
                    if(items.size() == 0) {
                        floatingActionButton.setEnabled(false);
                    }
                } else {
                    Log.e("app", "failed to find documents with: ", task.getException());
                }
            }
        });
    }

    void mongoPush() {
        mongoPull();

        CheckBox check = findViewById(R.id.check);
        LinearLayout checkwrap = findViewById(R.id.checkwrap);
        if(!check.isChecked()) {
            checkwrap.setBackground(getApplicationContext().getResources().getDrawable(R.color.design_default_color_error));
            return;
        }

        male = findViewById(R.id.male);
        Document doc = new Document()
                .append("user",acc.getEmail().toString())
                .append("disease",spinner.getSelectedItem().toString())
                .append("gender",(male.isChecked()?"Male":"Female"))
                .append("age-group",ageSpinner.getSelectedItem().toString())
                .append("date",date.getText().toString())
                .append("location",
                        new Document()
                    .append("latitude",latitude)
                    .append("longitude",longitude)
                    .append("address","Pune, Maharashtra"));

        Log.d("Local",doc.toString());

        final StitchAppClient client = Stitch.getDefaultAppClient();
        client.getAuth().loginWithCredential(new AnonymousCredential());
        final RemoteMongoClient mongoClient =
                client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        final RemoteMongoCollection<Document> coll =
                mongoClient.getDatabase("em").getCollection("reports");

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);
        Document filterDoc = new Document();

        final Task <RemoteUpdateResult> updateTask =
                coll.updateOne(filterDoc, doc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getUpsertedId() != null) {
                        String upsertedId = task.getResult().getUpsertedId().toString();
                        Log.d("app", String.format("successfully upserted document with id: %s",
                                upsertedId));
                    } else {
                        long numMatched = task.getResult().getMatchedCount();
                        long numModified = task.getResult().getModifiedCount();
                        Log.d("app", String.format("successfully matched %d and modified %d documents",
                                numMatched, numModified));
                    }
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                }
            }
        });
    }

    private void googleSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            acc = account;
            Toast.makeText(this,account.getEmail(),Toast.LENGTH_SHORT).show();

        } else {
            LinearLayout info = findViewById(R.id.info);
            info.setVisibility(View.VISIBLE);
            acc = null;
            Toast.makeText(getApplicationContext(),"Login Please",Toast.LENGTH_SHORT).show();
        }
        updateUI(acc);
    }

    private void updateUI(GoogleSignInAccount acc) {
        if(acc == null) {
            LinearLayout crowd = findViewById(R.id.crowd);
            crowd.setVisibility(View.GONE);
        }
    }
}
