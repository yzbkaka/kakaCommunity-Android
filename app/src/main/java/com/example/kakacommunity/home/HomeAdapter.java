package com.example.kakacommunity.home;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.HomeArticle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private MyDataBaseHelper dataBaseHelper;

    private List<HomeArticle> homeArticleList;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView fresh;
        TextView author;
        TextView time;
        TextView title;
        TextView chapter;
        TextView tag;
        ImageView collect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.home_item_layout);
            fresh = (TextView) itemView.findViewById(R.id.home_item_fresh);
            author = (TextView) itemView.findViewById(R.id.home_item_author);
            time = (TextView) itemView.findViewById(R.id.home_item_time);
            title = (TextView) itemView.findViewById(R.id.home_item_title);
            chapter = (TextView) itemView.findViewById(R.id.home_item_chapter);
            tag = (TextView) itemView.findViewById(R.id.home_item_tag);
            collect = (ImageView) itemView.findViewById(R.id.home_item_collect);
        }
    }

    public HomeAdapter(List<HomeArticle> homeArticleList) {
        dataBaseHelper = MyDataBaseHelper.getInstance();
        this.homeArticleList = homeArticleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        HomeArticle homeArticle = homeArticleList.get(position);
        holder.author.setText(homeArticle.getAuthor());
        holder.time.setText(homeArticle.getNiceDate());
        holder.title.setText(homeArticle.getTitle());
        holder.chapter.setText(homeArticle.getChapterName());
        boolean fresh = homeArticle.isFresh();
        if (fresh) {
            holder.fresh.setVisibility(View.VISIBLE);
        } else {
            holder.fresh.setVisibility(View.GONE);
        }
        String tag = homeArticle.getTag();
        if (!(tag == null || tag.length() == 0)) {
            holder.tag.setText(tag);
            holder.tag.setVisibility(View.VISIBLE);
        } else {
            holder.tag.setVisibility(View.GONE);
        }
        if (homeArticle.isCollect()) {
            holder.collect.setImageResource(R.drawable.iscollect);
        }
        holder.collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!homeArticle.isCollect()) {
                    String only = homeArticle.getLink();
                    if (queryOnly(only)) {
                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("type", TYPE_ARTICLE);
                        contentValues.put("author", homeArticle.getAuthor());
                        contentValues.put("title", homeArticle.getTitle());
                        contentValues.put("link", homeArticle.getLink());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = new Date();
                        contentValues.put("save_date", dateFormat.format(date));
                        contentValues.put("chapter_name", homeArticle.getChapterName());
                        db.insert("Collect", null, contentValues);
                        Toast.makeText(MyApplication.getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MyApplication.getContext(), "已经收藏过啦", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    onItemClickListener.onItemCollectClick(position);
                }
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return homeArticleList.size();
    }

    private boolean queryOnly(String only) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, "link = ?", new String[]{only}, null, null, null);
        if (cursor.getCount() == 0) {  //没有收藏过
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;  //收藏过
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemCollectClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}