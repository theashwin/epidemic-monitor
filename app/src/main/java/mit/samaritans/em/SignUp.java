package mit.samaritans.em;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SignUp extends AppCompatActivity {

    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 9001;

    private static GoogleSignInClient mGoogleSignInClient;

    private Spinner typeSpinner;
    private Spinner genderSpinner;
    private EditText email;
    private EditText name;
    private EditText phone;
    private Button signup;
    private Button logout;
    private FloatingActionButton floatingActionButton;
    private GoogleSignInAccount acc;
    private TextView tv;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup = findViewById(R.id.signup);
        logout = findViewById(R.id.logout);
        typeSpinner = findViewById(R.id.typeSpinner);
        genderSpinner = findViewById(R.id.genderSpinner);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        tv = findViewById(R.id.aadhaarmsg);

        String[] type = {"Citizen","Media Person","Hospital / Clinic"};
        ArrayAdapter typeArrayAdapter = new ArrayAdapter(this,R.layout.spinner, type);
        typeArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeArrayAdapter);

        String[] sex = {"Male","Female"};
        ArrayAdapter genderArrayAdapter = new ArrayAdapter(this,R.layout.spinner, sex);
        typeArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderArrayAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch((int) adapterView.getItemIdAtPosition(i)) {
                    case 0: //CITIZEN
                        tv.setText(R.string.aadhaar_optional);
                        tv.setTextColor(getResources().getColor(R.color.blue));
                        break;
                    case 1: //MEDIA
                    case 2: //HOSPITAL / CLINIC
                        tv.setText(R.string.aadhaar_mandatory);
                        tv.setTextColor(getResources().getColor(R.color.red));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        googleSignIn();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //VALIDATE DATA
                try {
                    if(phone.getText().length() != 10 || !TextUtils.isDigitsOnly(phone.getText())) {
                        phone.setBackgroundColor(R.color.red);
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

    }

    //GOOGLE LOGIN FUNCTIONS
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            acc = account;
            mongoFetch();
        } catch (ApiException e) {
            Toast.makeText(getApplicationContext(),"Login Failed!",Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private  void updateUI(GoogleSignInAccount a) {
        if(a == null ) {
            name.setText("");
            email.setText("");
            phone.setInputType(InputType.TYPE_NULL);
            phone.setText("");
            genderSpinner.setEnabled(false);
            typeSpinner.setEnabled(false);
            signup.setText("SIGN UP");
            signup.setEnabled(true);
        }
        else {
            name.setText(a.getDisplayName());
            email.setText(a.getEmail());
            phone.setInputType(InputType.TYPE_CLASS_NUMBER);
            genderSpinner.setEnabled(true);
            typeSpinner.setEnabled(true);
            signup.setText("ALREADY SIGNED IN");
            signup.setEnabled(false);
        }
    }

    private void googleSignIn() {
        Intent rootIntent = new Intent(SignUp.this, Root.class);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            acc = account;
            mongoFetch();
            rootIntent = new Intent(SignUp.this, Root.class);
            rootIntent.putExtra("EMAIL", account.getEmail());
            rootIntent.putExtra("NAME", account.getDisplayName());
            rootIntent.putExtra("USER_ID", account.getId());
            Toast.makeText(this,account.getEmail(),Toast.LENGTH_SHORT).show();
            //startActivity(rootIntent);

        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(SignUp.this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(SignUp.this,"Logged Out!",Toast.LENGTH_SHORT).show();
                        GoogleSignInAccount account = null;
                        updateUI(null);
                    }
                });
    }

    private void mongoFetch(){
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
                        updateUI(acc);
                    }
                    else { //UPDATE PROFILE
                        Log.d("Stitch",items.toString());
                        phone.setInputType(InputType.TYPE_CLASS_NUMBER);
                        if(items.get(0).get("gender").toString().equals("Male")) {
                            genderSpinner.setSelection(0);
                        } else {
                            genderSpinner.setSelection(1);
                        }
                        genderSpinner.setEnabled(false);
                        name.setText(items.get(0).get("name").toString());
                        phone.setText(items.get(0).get("phone").toString());
                        email.setText(items.get(0).get("email").toString());
                        if(items.get(0).get("type").toString().equals("Citizen")) {
                            typeSpinner.setSelection(0);
                        } else if(items.get(0).get("type").toString().equals("Media")) {
                            typeSpinner.setSelection(1);
                        } else {
                            typeSpinner.setSelection(2);
                        }
                        typeSpinner.setEnabled(true);
                        signup.setText("ALREADY SIGNED IN");
                        tv.setText("ONLY ACCOUNT TYPE AND PHONE NUMBER CAN BE CHANGED.");
                        tv.setTextColor(getResources().getColor(R.color.red));
                    }
                } else {
                    Log.e("app", "failed to find documents with: ", task.getException());
                }
            }
        });
    }

    void mongoPush() {
        Document doc = new Document()
                .append("email",acc.getEmail().toString())
                .append("name",acc.getDisplayName().toString())
                .append("phone",phone.getText().toString())
                .append("gender",genderSpinner.getSelectedItem().toString())
                .append("type",typeSpinner.getSelectedItem().toString())
                .append("aadhaar","0")
                .append("verified","false")
                .append("suspended","false");

        Log.d("Local",doc.toString());

        final StitchAppClient client = Stitch.getDefaultAppClient();
        client.getAuth().loginWithCredential(new AnonymousCredential());
        final RemoteMongoClient mongoClient =
                client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        final RemoteMongoCollection<Document> coll =
                mongoClient.getDatabase("em").getCollection("users");

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);
        Document filterDoc = new Document().append("email",acc.getEmail());

        final Task <RemoteUpdateResult> updateTask =
                coll.updateOne(filterDoc, doc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
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
}
