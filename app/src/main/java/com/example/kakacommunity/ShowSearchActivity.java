package com.example.kakacommunity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;

public class ShowSearchActivity extends AppCompatActivity {

    private ImageView back;

    private TextView title;

    private String keyWord;

    private RecyclerView recyclerView;

    private List<HomeArticle> articleList = new ArrayList<>();

    private HomeAdapter homeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_search);
        initView();
        getQueryJSON();
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = articleList.get(position).getLink();
                String title = articleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        back = (ImageView)findViewById(R.id.query_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = (TextView)findViewById(R.id.query_title);
        recyclerView = (RecyclerView)findViewById(R.id.query_recycler_view);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        homeAdapter = new HomeAdapter(articleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
    }

    private void getQueryJSON() {
        Intent intent = getIntent();
        keyWord = intent.getStringExtra("keyword");
        title.setText(keyWord);
        RequestBody requestBody = new FormBody.Builder()
                .add("k", keyWord)
                .build();
        HttpUtil.OkHttpPOST(ANDROID_ADDRESS + "/article" + "/query"  + "/0" + "/json", requestBody, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseQueryJSON(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void parseQueryJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for(int i = 0;i < datas.length();i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                HomeArticle homeArticle = new HomeArticle();
                homeArticle.setAuthor(jsonObject.getString("author"));
                homeArticle.setTitle(jsonObject.getString("title"));
                homeArticle.setLink(jsonObject.getString("link"));
                homeArticle.setNiceDate(jsonObject.getString("niceDate"));
                homeArticle.setChapterName(jsonObject.getString("chapterName"));
                JSONArray tags = jsonObject.getJSONArray("tags");
                if(tags.length() != 0) homeArticle.setTag(tags.getJSONObject(0).getString("name"));
                articleList.add(homeArticle);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
