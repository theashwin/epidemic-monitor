package mit.samaritans.em;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import mit.samaritans.em.utils.MainActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Root extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        Button btc = findViewById(R.id.mapcrowd);
        btc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent oncIntent = new Intent(Root.this, Crowdsource.class);
                Root.this.startActivity(oncIntent);
            }
        });


        Button bt3 = findViewById(R.id.home);
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent on3Intent = new Intent(Root.this, Home.class);
                Root.this.startActivity(on3Intent);
            }
        });

        Button bt4 = findViewById(R.id.logout);
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        Button bt = findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent onbIntent = new Intent(Root.this, MainActivity.class);
                Root.this.startActivity(onbIntent);
            }
        });

        Button bt2 = findViewById(R.id.mapbtn);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent onIntent = new Intent(Root.this, RealTimeMonitor.class);
                Root.this.startActivity(onIntent);

                /*
                String url = "https://www.webmd.com/a-to-z-guides/malaria-symptoms";
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(url));
                */
            }
        });
    }

    public void logout() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Root.this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(Root.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent homeIntent = new Intent(Root.this, Onboarding.class);
                        startActivity(homeIntent);
                        Toast.makeText(Root.this,"Logged Out!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
