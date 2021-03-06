package com.example.kakacommunity.community;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
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
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.model.HomeArticle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

/**
 * 社区适配器
 */
public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    private MyDataBaseHelper dataBaseHelper;

    private List<HomeArticle> communityArticleList;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView fresh;
        TextView author;
        TextView time;
        TextView title;
        TextView content;
        TextView chapter;
        TextView tag;
        ImageView collect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.community_item_layout);
            fresh = (TextView) itemView.findViewById(R.id.community_item_fresh);
            author = (TextView) itemView.findViewById(R.id.community_item_author);
            time = (TextView) itemView.findViewById(R.id.community_item_time);
            title = (TextView) itemView.findViewById(R.id.community_item_title);
            content = (TextView) itemView.findViewById(R.id.community_item_content);
            chapter = (TextView) itemView.findViewById(R.id.community_item_chapter);
            tag = (TextView) itemView.findViewById(R.id.community_item_tag);
            collect = (ImageView) itemView.findViewById(R.id.community_item_collect);
        }
    }

    public CommunityAdapter(List<HomeArticle> communityArticleList) {
        dataBaseHelper = MyDataBaseHelper.getInstance();
        this.communityArticleList = communityArticleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        HomeArticle homeArticle = communityArticleList.get(position);
        holder.author.setText(homeArticle.getAuthor());
        holder.time.setText(homeArticle.getNiceDate());
        holder.title.setText(String.valueOf(Html.fromHtml(homeArticle.getTitle())));
        if (homeArticle.getContent() != null) {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(homeArticle.getContent());
        } else {
            holder.content.setVisibility(View.GONE);
            holder.content.setText(homeArticle.getContent());
        }
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
                    String only = homeArticle.getDiscussPostId();
                    if (queryOnly(only)) {
                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("type", TYPE_COMMUNITY);
                        contentValues.put("author", homeArticle.getAuthor());
                        contentValues.put("title", homeArticle.getTitle());
                        contentValues.put("link", homeArticle.getDiscussPostId());
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
                saveReadHistory(communityArticleList.get(position));
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return communityArticleList.size();
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

    private void saveReadHistory(HomeArticle homeArticle) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String link = homeArticle.getDiscussPostId();
        Cursor cursor = db.query("History", null, "link = ?", new String[]{link}, null, null, null);
        if (cursor.getCount() == 0) {
            contentValues.put("type", TYPE_COMMUNITY);
            contentValues.put("author", homeArticle.getAuthor());
            contentValues.put("title", homeArticle.getTitle());
            contentValues.put("link", homeArticle.getDiscussPostId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date();
            contentValues.put("read_date", dateFormat.format(date));
            contentValues.put("chapter_name", homeArticle.getChapterName());
            db.insert("History", null, contentValues);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date();
            contentValues.put("read_date", dateFormat.format(date));
            db.update("History", contentValues, "link = ?", new String[]{link});
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
