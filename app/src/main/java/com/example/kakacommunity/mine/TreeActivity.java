package com.example.kakacommunity.mine;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.Tree;
import com.example.kakacommunity.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

public class TreeActivity extends AppCompatActivity {

    private MyDataBaseHelper dataBaseHelper = MyDataBaseHelper.getInstance();

    private Toolbar toolbar;

    private ListView listView;

    private List<Tree> treeList = new ArrayList<>();

    private List<String> trees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);
        queryTree();
        initView();
    }

    private void queryTree() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Tree", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                trees.add(name);
                Tree tree = new Tree();
                tree.setId(id);
                tree.setName(name);
                treeList.add(tree);

            } while (cursor.moveToNext());
        }
        cursor.close();
        if (trees.size() == 0) {
            getTreeJSON();
        }
    }

    private void getTreeJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/tree" + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseTreeJSON(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    private void parseTreeJSON(String responseData) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray datas = jsonData.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                ContentValues contentValues = new ContentValues();
                JSONObject jsonObject = datas.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                contentValues.put("id",id);
                contentValues.put("name", name);
                db.insert("Tree", null, contentValues);
                trees.add(name);
                Tree tree = new Tree();
                tree.setId(id);
                tree.setName(name);
                treeList.add(tree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.tree_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        listView = (ListView)findViewById(R.id.tree_list_view);
        initListView();
    }

    private void initListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(TreeActivity.this,
                android.R.layout.simple_list_item_1, trees);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TreeActivity.this,TreeArticleActivity.class);
                intent.putExtra("id",treeList.get(position).getId());
                intent.putExtra("name",treeList.get(position).getName());
                startActivity(intent);
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
