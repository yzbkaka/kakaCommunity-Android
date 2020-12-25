package com.example.kakacommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.example.kakacommunity.base.KakaCommunityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class StartActivity extends AppCompatActivity {

    private AlphaAnimation alphaAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);  //设置activity切场动画
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        alphaAnimation = new AlphaAnimation(0.3F, 1.0F);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {  //设置动画监听
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        View splashView = (View) findViewById(R.id.layout_start);
        splashView.startAnimation(alphaAnimation);  //开始动画

    }

}