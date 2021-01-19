package com.example.kakacommunity.mine.collect;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.community.CommunityAdapter;
import com.example.kakacommunity.community.CommunityDetailActivity;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

public class CollectCommunityFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;

    private CommunityAdapter adapter;

    private List<HomeArticle> collectCommunityList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_community, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        recyclerView = (RecyclerView) view.findViewById(R.id.collect_community_recycler_view);
        queryCollectCommunity();
        initRecyclerView();
        return view;
    }

    private void queryCollectCommunity() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, "type = ?", new String[]{TYPE_COMMUNITY}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HomeArticle homeArticle = new HomeArticle();
                String author = cursor.getString(cursor.getColumnIndex("author"));
                homeArticle.setAuthor(author);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                homeArticle.setTitle(title);
                String discussPostId = cursor.getString(cursor.getColumnIndex("link"));
                homeArticle.setDiscussPostId(discussPostId);
                String niceDate = cursor.getString(cursor.getColumnIndex("save_date"));
                homeArticle.setNiceDate(niceDate);
                String chapterName = cursor.getString(cursor.getColumnIndex("chapter_name"));
                homeArticle.setChapterName(chapterName);
                homeArticle.setCollect(true);
                collectCommunityList.add(homeArticle);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new CommunityAdapter(collectCommunityList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setOnItemCLickListener(new CommunityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String discussPostId = collectCommunityList.get(position).getDiscussPostId();
                Intent intent = new Intent(MyApplication.getContext(), CommunityDetailActivity.class);
                intent.putExtra("discussPostId", discussPostId);
                startActivity(intent);
            }

            @Override
            public void onItemCollectClick(int position) {
                String only = collectCommunityList.get(position).getDiscussPostId();
                collectCommunityList.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(MyApplication.getContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                deleteCollectCommunity(only);
            }
        });
    }

    private void deleteCollectCommunity(String only) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete("Collect", "link = ?", new String[]{only});
    }
}
