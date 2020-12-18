package com.example.kakacommunity.mine;

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

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;

public class HistoryArticleFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;

    private HomeAdapter historyArticleAdapter;

    private List<HomeArticle> historyArticleList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_article, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        recyclerView = (RecyclerView) view.findViewById(R.id.history_article_recycler_view);
        queryHistoryArticle();
        initRecyclerView();
        return view;
    }

    private void queryHistoryArticle() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("History", null, "type = ?", new String[]{TYPE_ARTICLE}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HomeArticle homeArticle = new HomeArticle();
                String author = cursor.getString(cursor.getColumnIndex("author"));
                homeArticle.setAuthor(author);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                homeArticle.setTitle(title);
                String link = cursor.getString(cursor.getColumnIndex("link"));
                homeArticle.setLink(link);
                String niceDate = cursor.getString(cursor.getColumnIndex("read_date"));
                homeArticle.setNiceDate(niceDate);
                String chapterName = cursor.getString(cursor.getColumnIndex("chapter_name"));
                homeArticle.setChapterName(chapterName);
                historyArticleList.add(homeArticle);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        historyArticleAdapter = new HomeAdapter(historyArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(historyArticleAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        historyArticleAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = historyArticleList.get(position).getLink();
                String title = historyArticleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
    }
}
