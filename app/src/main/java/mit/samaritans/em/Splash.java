package mit.samaritans.em;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.util.Locale;
import java.util.Random;

import mit.samaritans.em.utils.MainActivity;

public class Splash extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        createNotificationChannel();

        progressBar = findViewById(R.id.progressBar);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus < 100) {
                    progressStatus += new Random().nextInt(15);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(50);    //SET TO ARBITRARY NUMBER
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                setLanguage("en");
                Intent onbIntent = new Intent(Splash.this, Root.class);
                Splash.this.startActivity(onbIntent);
                finish();
            }
        }).start();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //MIN
            int importanceLow = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channelLow = new NotificationChannel("default", "Epidemic Monitor", importanceLow);
            channelLow.setDescription("Automator Notification Channel");

            //HIGH PRIORITY
            int importanceHigh = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channelHigh = new NotificationChannel("high", "Epidemic Monitor", importanceHigh);
            channelHigh.setDescription("Automator Notification Channel");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channelLow);
            notificationManager.createNotificationChannel(channelHigh);
        }
    }

    public void setLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Locale.getAvailableLocales();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}

