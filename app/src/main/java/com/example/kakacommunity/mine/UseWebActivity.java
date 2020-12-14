package com.example.kakacommunity.mine;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.UseWeb;
import com.example.kakacommunity.utils.HttpUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import ezy.ui.view.RoundButton;
import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

public class UseWebActivity extends AppCompatActivity {

    private MyDataBaseHelper dataBaseHelper = MyDataBaseHelper.getInstance();

    private Toolbar toolbar;

    private TagFlowLayout tagFlowLayout;

    private List<String> tagList = new ArrayList<>();

    private List<UseWeb> useWebList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_web);
        queryWeb();
        initView();
    }

    private void queryWeb() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Useweb", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String link = cursor.getString(cursor.getColumnIndex("link"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                tagList.add(name);
                UseWeb useWeb = new UseWeb();
                useWeb.setLink(link);
                useWeb.setName(name);
                useWebList.add(useWeb);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (tagList.size() == 0) {
            getUseWebJSON();
        }
    }

    private void getUseWebJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/friend" + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseUseWebJSON(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    private void parseUseWebJSON(String responseData) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray datas = jsonData.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                ContentValues contentValues = new ContentValues();
                JSONObject jsonObject = datas.getJSONObject(i);
                String name = jsonObject.getString("name");
                String link = jsonObject.getString("link");
                contentValues.put("name", name);
                contentValues.put("link",link);
                db.insert("Useweb", null, contentValues);
                tagList.add(name);
                UseWeb useWeb = new UseWeb();
                useWeb.setName(name);
                useWeb.setLink(link);
                useWebList.add(useWeb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.use_web__toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tagFlowLayout = (TagFlowLayout)findViewById(R.id.use_web_layout);
        LayoutInflater inflater = LayoutInflater.from(UseWebActivity.this);
        tagFlowLayout.setAdapter(new TagAdapter<String>(tagList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                RoundButton roundButton = (RoundButton) inflater.inflate(R.layout.use_web, tagFlowLayout, false);
                roundButton.setText(tagList.get(position));
                return roundButton;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                Intent intent = new Intent(UseWebActivity.this, WebActivity.class);
                intent.putExtra("url",useWebList.get(position).getLink());
                intent.putExtra("title",useWebList.get(position).getName());
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:  //默认id
                finish();
                break;
        }
        return true;
    }
}
