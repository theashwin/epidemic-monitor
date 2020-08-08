package mit.samaritans.em;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.transition.TransitionManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {
    CardView cardViewOne;
    CardView cardViewTwo;
    CardView cardViewThree;
    LinearLayout linearLayoutOne;
    LinearLayout linearLayoutTwo;
    LinearLayout linearLayoutThree;
    TextView diseaseMappingTV;
    ImageView scaleOne;
    ImageView scaleTwo;
    ImageView scaleThree;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final float scale = getResources().getDisplayMetrics().density;

        cardViewOne = findViewById(R.id.cardViewOne);
        cardViewTwo = findViewById(R.id.cardViewTwo);
        cardViewThree = findViewById(R.id.cardViewThree);

        linearLayoutOne = findViewById(R.id.llone);
        linearLayoutTwo = findViewById(R.id.lltwo);
        linearLayoutThree = findViewById(R.id.llthree);

        scaleOne = findViewById(R.id.scaleone);
        scaleTwo = findViewById(R.id.scaletwo);
        scaleThree = findViewById(R.id.scalethree);

        diseaseMappingTV = findViewById(R.id.diseaseMappingTV);
        diseaseMappingTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"Hey",Toast.LENGTH_SHORT).show();
                Intent realTimeMapping = new Intent(Home.this,SignUp.class);
                Home.this.startActivity(realTimeMapping);
            }
        });

        cardViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linearLayoutOne.getVisibility() == View.GONE) {
                    linearLayoutOne.setVisibility(View.VISIBLE);
                    scaleOne.getLayoutParams().height = (int) (305*scale);
                    scaleOne.setAlpha((float)0.5);
                    scaleOne.invalidate();

                }
                else {
                    scaleOne.getLayoutParams().height = (int) (150*scale);
                    scaleOne.setAlpha((float)0.75);
                    scaleOne.requestLayout();
                    linearLayoutOne.setVisibility(View.GONE);
                }
            }
        });

        cardViewTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linearLayoutTwo.getVisibility() == View.GONE) {
                    linearLayoutTwo.setVisibility(View.VISIBLE);
                    scaleTwo.getLayoutParams().height = (int) (305*scale);
                    scaleTwo.setAlpha((float)0.5);
                    scaleTwo.invalidate();

                }
                else {
                    scaleTwo.getLayoutParams().height = (int) (150*scale);
                    scaleTwo.setAlpha((float)0.75);
                    scaleTwo.requestLayout();
                    linearLayoutTwo.setVisibility(View.GONE);
                }
            }
        });

        cardViewThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linearLayoutThree.getVisibility() == View.GONE) {
                    linearLayoutThree.setVisibility(View.VISIBLE);
                    scaleThree.getLayoutParams().height = (int) (305*scale);
                    scaleThree.setAlpha((float)0.5);
                    scaleThree.invalidate();

                }
                else {
                    scaleThree.getLayoutParams().height = (int) (150*scale);
                    scaleThree.setAlpha((float)0.75);
                    scaleThree.requestLayout();
                    linearLayoutThree.setVisibility(View.GONE);
                }
            }
        });

        onClickListeners();
    }

    private void onClickListeners() {
        TextView threeone = findViewById(R.id.threeone);
        TextView threetwo = findViewById(R.id.threetwo);
        TextView threethree = findViewById(R.id.threethree);

        threeone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this,Profile.class);
                Home.this.startActivity(intent);
            }
        });
    }
}
