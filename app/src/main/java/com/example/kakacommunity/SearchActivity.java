package com.example.kakacommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.kakacommunity.utils.HttpUtil;
import com.example.kakacommunity.utils.StringUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;


public class SearchActivity extends AppCompatActivity {

    private ImageView back;

    private TextInputEditText searchText;

    private ImageView search;

    private String keyWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.search_back);
        searchText = (TextInputEditText) findViewById(R.id.search_text);
        search = (ImageView) findViewById(R.id.search_search);
    }

    @Override
    protected void onResume() {
        super.onResume();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyWord = searchText.getText().toString();
                if (!StringUtil.isBlank(keyWord)) {
                    Intent intent = new Intent(SearchActivity.this, ShowSearchActivity.class);
                    intent.putExtra("keyword", keyWord);
                    startActivity(intent);
                }
            }
        });
    }
}
