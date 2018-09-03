package com.android.comment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by raman.chhabra on 3/21/18.
 */

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private Context context;
  private ArrayList<CommentModel> commentModels;
  private FacebookComment facebookComment;
  private YouTubeComment youTubeComment;
  private int count;
  private String nextURl;
  private String nextToken;


  public CommentAdapter(Context context, ArrayList<CommentModel> commentModels, int type) {
    this.context = context;
    this.commentModels = commentModels;
    setActivity(type);
  }

  private void setActivity(int type) {
    switch (type) {
      case 1:
        facebookComment = (FacebookComment) context;
        break;
      case 2:
        youTubeComment = (YouTubeComment) context;
        break;

    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_tile, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (position == count - 10 ) {
      if (nextURl != null && facebookComment!= null){
        facebookComment.fetchComments(nextURl);
      }else if (nextToken != null && youTubeComment != null){
        youTubeComment.fetchComments(nextToken);
      }
    }
    CommentModel commentModel = commentModels.get(position);
    final ViewHolder viewHolder = (ViewHolder) holder;
    viewHolder.messageView.setText(commentModel.message);

    if (commentModel.replyModels != null) {
      ArrayList<CommentReplyModel> replyModels = commentModel.replyModels;
      int len = replyModels.size();
      if (len > 0) {
        viewHolder.layout.setVisibility(View.VISIBLE);
        viewHolder.layout.removeAllViews();
        for (int i = len-1; i >= 0; i--) {
          CommentReplyModel replyModel = replyModels.get(i);
          View view = LayoutInflater.from(context).inflate(R.layout.reply_tile, null);
          TextView messageView = view.findViewById(R.id.ct_message);
          if (replyModel.displayName != null && !replyModel.displayName.isEmpty()){
            messageView.setText(Html.fromHtml("<b>"+replyModel.displayName+"</b> "+ replyModel.message));
          }else{
            messageView.setText(replyModel.message);

          }

          if (i == 0) {
            view.findViewById(R.id.rt_line).setVisibility(View.GONE);
          }

          viewHolder.layout.addView(view);
        }
      } else {
        viewHolder.layout.setVisibility(View.GONE);
      }
    } else {
      viewHolder.layout.setVisibility(View.GONE);
    }
  }

  public void setNextUrl(String url) {
    nextURl = url;
  }


  public void setNextToken(String token) {
      nextToken= token;
  }

  @Override
  public int getItemCount() {
    count = commentModels.size();
    return count;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    private TextView messageView;
    private LinearLayout layout;

    public ViewHolder(View itemView) {
      super(itemView);
      messageView = itemView.findViewById(R.id.ct_message);
      layout = itemView.findViewById(R.id.ct_layout);
    }
  }
}
