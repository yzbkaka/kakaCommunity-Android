package com.example.kakacommunity.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommentReply;

import java.util.ArrayList;
import java.util.List;

public class ReplyDetailAdapter extends RecyclerView.Adapter<ReplyDetailAdapter.ViewHolder> {

    private List<CommentReply> communityReplyList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView replyName;
        TextView replyTime;
        TextView replyContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.comment_reply_detail_author_image);
            replyName = (TextView)itemView.findViewById(R.id.comment_reply_detail_author);
            replyTime = (TextView)itemView.findViewById(R.id.comment_reply_detail_time);
            replyContent = (TextView)itemView.findViewById(R.id.comment_reply_detail_content);
        }
    }

    public ReplyDetailAdapter(List<CommentReply> communityReplyList) {
        this.communityReplyList = communityReplyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_detail_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentReply commentReply = communityReplyList.get(position);
        holder.replyName.setText(commentReply.getName());
        holder.replyTime.setText(commentReply.getTime());
        holder.replyContent.setText(commentReply.getContent());
        Glide.with(MyApplication.getContext())
                .load(commentReply.getImageUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return communityReplyList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
