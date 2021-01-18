package com.example.kakacommunity.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.model.HomeArticle;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.community_item_layout);
            fresh = (TextView) itemView.findViewById(R.id.community_item_fresh);
            author = (TextView) itemView.findViewById(R.id.community_item_author);
            time = (TextView) itemView.findViewById(R.id.community_item_time);
            title = (TextView) itemView.findViewById(R.id.community_item_title);
            content = (TextView)itemView.findViewById(R.id.community_item_content);
            chapter = (TextView) itemView.findViewById(R.id.community_item_chapter);
            tag = (TextView) itemView.findViewById(R.id.community_item_tag);
        }
    }

    public CommunityAdapter(List<HomeArticle> communityArticleList) {
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
        holder.title.setText(homeArticle.getTitle());
        holder.content.setText(homeArticle.getContent());
        holder.chapter.setText(homeArticle.getChapterName());
        boolean fresh = homeArticle.isFresh();
        if (fresh) {
            holder.fresh.setVisibility(View.VISIBLE);
        }else {
            holder.fresh.setVisibility(View.GONE);
        }
        String tag = homeArticle.getTag();
        if (!(tag == null || tag.length() == 0)) {
            holder.tag.setText(tag);
            holder.tag.setVisibility(View.VISIBLE);
        }else{
            holder.tag.setVisibility(View.GONE);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return communityArticleList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
