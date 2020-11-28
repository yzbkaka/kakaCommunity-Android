package com.example.kakacommunity.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kakacommunity.R;
import com.just.agentweb.AgentWeb;

public class WebActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    private ImageView back;

    private TextView webTitle;

    private AgentWeb agentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        webTitle.setText(title);
        String url = intent.getStringExtra("url");
        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(linearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(R.color.black)
                .createAgentWeb()
                .ready()
                .go(url);
    }

    private void initView() {
        back = (ImageView)findViewById(R.id.web_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webTitle = (TextView)findViewById(R.id.web_title);
        linearLayout = (LinearLayout)findViewById(R.id.web_layout);
    }

}
