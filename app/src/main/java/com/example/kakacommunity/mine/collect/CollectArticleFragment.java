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

import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;

/**
 * 收藏文章
 */
public class CollectArticleFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;

    private HomeAdapter collectArticleAdapter;

    private List<HomeArticle> collectArticleList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_article, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        recyclerView = (RecyclerView) view.findViewById(R.id.collect_article_recycler_view);
        queryCollectArticle();
        initRecyclerView();
        return view;
    }

    private void queryCollectArticle() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, "type = ?", new String[]{TYPE_ARTICLE}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HomeArticle homeArticle = new HomeArticle();
                String author = cursor.getString(cursor.getColumnIndex("author"));
                homeArticle.setAuthor(author);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                homeArticle.setTitle(title);
                String link = cursor.getString(cursor.getColumnIndex("link"));
                homeArticle.setLink(link);
                String niceDate = cursor.getString(cursor.getColumnIndex("save_date"));
                homeArticle.setNiceDate(niceDate);
                String chapterName = cursor.getString(cursor.getColumnIndex("chapter_name"));
                homeArticle.setChapterName(chapterName);
                homeArticle.setCollect(true);
                collectArticleList.add(homeArticle);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        collectArticleAdapter = new HomeAdapter(collectArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(collectArticleAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        collectArticleAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = collectArticleList.get(position).getLink();
                String title = collectArticleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }

            @Override
            public void onItemCollectClick(int position) {
                String only = collectArticleList.get(position).getLink();
                collectArticleList.remove(position);
                collectArticleAdapter.notifyDataSetChanged();
                Toast.makeText(MyApplication.getContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                deleteCollectArticle(only);
            }
        });
    }

    private void deleteCollectArticle(String only) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete("Collect", "link = ?", new String[]{only});
    }
}
