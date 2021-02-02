package com.example.kakacommunity.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.utils.HttpUtil;
import com.example.kakacommunity.utils.StringUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ezy.ui.view.RoundButton;
import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

/**
 * 搜索
 */
public class SearchActivity extends AppCompatActivity {

    private MyDataBaseHelper dataBaseHelper = MyDataBaseHelper.getInstance();

    private ImageView back;

    private TextInputEditText searchText;

    private ImageView search;

    private TagFlowLayout tagFlowLayout;

    private TagFlowLayout historyFlowLayout;

    private LayoutInflater inflater;

    private List<String> tagList = new ArrayList<>();

    private List<String> historyList = new ArrayList<>();

    private TextView clear;

    private String keyWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        queryTag();
        queryHistory();
        initView();
    }

    private void queryTag() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Tag", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                tagList.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (tagList.size() == 0) {
            getTagJSON();
        }
    }

    private void queryHistory() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("SearchHistory", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (!historyList.contains(name)) {
                    historyList.add(0, name);
                } else {
                    historyList.remove(name);
                    historyList.add(0, name);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void getTagJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/hotkey" + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseTagJSON(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTag();
                    }
                });
            }
        });
    }

    private void parseTagJSON(String responseData) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray datas = jsonData.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                ContentValues contentValues = new ContentValues();
                JSONObject jsonObject = datas.getJSONObject(i);
                String name = jsonObject.getString("name");
                contentValues.put("name", name);
                tagList.add(name);
                db.insert("Tag", null, contentValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.search_back);
        searchText = (TextInputEditText) findViewById(R.id.search_text);
        search = (ImageView) findViewById(R.id.search_search);
        tagFlowLayout = (TagFlowLayout) findViewById(R.id.tag_flow_layout);
        clear = (TextView) findViewById(R.id.clear_history);
        historyFlowLayout = (TagFlowLayout) findViewById(R.id.history_flow_layout);
        inflater = LayoutInflater.from(SearchActivity.this);
        tagFlowLayout.setAdapter(new TagAdapter<String>(tagList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                RoundButton roundButton = (RoundButton) inflater.inflate(R.layout.tag, tagFlowLayout, false);
                roundButton.setText(tagList.get(position));
                return roundButton;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryHistory();
        updateHistory();
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                db.delete("SearchHistory", null, null);
                historyList.clear();
                updateHistory();
            }
        });
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
                    saveHistory(keyWord);
                    startActivity(intent);
                }
            }
        });
        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    keyWord = searchText.getText().toString();
                    if (!StringUtil.isBlank(keyWord)) {
                        Intent intent = new Intent(SearchActivity.this, ShowSearchActivity.class);
                        intent.putExtra("keyword", keyWord);
                        saveHistory(keyWord);
                        startActivity(intent);
                        return true;
                    }
                }
                return false;
            }
        });
        tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                keyWord = tagList.get(position);
                Intent intent = new Intent(SearchActivity.this, ShowSearchActivity.class);
                intent.putExtra("keyword", keyWord);
                saveHistory(keyWord);
                startActivity(intent);
                return true;
            }
        });
        historyFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                keyWord = historyList.get(position);
                Intent intent = new Intent(SearchActivity.this, ShowSearchActivity.class);
                intent.putExtra("keyword", keyWord);
                saveHistory(keyWord);
                startActivity(intent);
                return true;
            }
        });
    }

    private void saveHistory(String name) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        db.insert("SearchHistory", null, contentValues);
    }

    private void updateTag() {
        tagFlowLayout.setAdapter(new TagAdapter<String>(tagList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                RoundButton roundButton = (RoundButton) inflater.inflate(R.layout.tag, tagFlowLayout, false);
                roundButton.setText(tagList.get(position));
                return roundButton;
            }
        });
    }

    private void updateHistory() {
        historyFlowLayout.setAdapter(new TagAdapter<String>(historyList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                RoundButton roundButton = (RoundButton) inflater.inflate(R.layout.tag, historyFlowLayout, false);
                roundButton.setText(historyList.get(position));
                return roundButton;
            }
        });
    }
}
