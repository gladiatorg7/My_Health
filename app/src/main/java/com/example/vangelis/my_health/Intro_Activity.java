package com.example.vangelis.my_health;

import android.content.Intent;
import android.icu.util.TimeUnit;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Intro_Activity extends AppCompatActivity {
    public static final String MY_TAG = "the_custom_message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_);

        TextView textView2=(TextView) findViewById(R.id.textView2);
        TextView textView3=(TextView) findViewById(R.id.textView3);
        Animation anim= AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {                       // molis teleiwsei to animation kalw tin MainActivity
                Intent intent =new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        textView2.setAnimation(anim);
        textView3.setAnimation(anim);
    }
}
