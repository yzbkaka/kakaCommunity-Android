package com.example.kakacommunity.mine.history;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

public class HistoryCommunityFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;

    private CommunityAdapter adapter;

    private List<HomeArticle> historyCommunityList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_community, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        recyclerView = (RecyclerView) view.findViewById(R.id.history_community_recycler_view);
        queryHistoryCommunity();
        initRecyclerView();
        return view;
    }

    private void queryHistoryCommunity() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("History", null, "type = ?", new String[]{TYPE_COMMUNITY}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HomeArticle homeArticle = new HomeArticle();
                String author = cursor.getString(cursor.getColumnIndex("author"));
                homeArticle.setAuthor(author);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                homeArticle.setTitle(title);
                String discussPostId = cursor.getString(cursor.getColumnIndex("link"));
                homeArticle.setDiscussPostId(discussPostId);
                String niceDate = cursor.getString(cursor.getColumnIndex("read_date"));
                homeArticle.setNiceDate(niceDate);
                String chapterName = cursor.getString(cursor.getColumnIndex("chapter_name"));
                homeArticle.setChapterName(chapterName);
                historyCommunityList.add(homeArticle);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new CommunityAdapter(historyCommunityList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setOnItemCLickListener(new CommunityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String discussPostId = historyCommunityList.get(position).getDiscussPostId();
                Intent intent = new Intent(MyApplication.getContext(), CommunityDetailActivity.class);
                intent.putExtra("discussPostId", discussPostId);
                startActivity(intent);
            }
        });
    }
}
