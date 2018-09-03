package com.android.comment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class YouTubeComment extends AppCompatActivity {

  private ArrayList<CommentModel> commentModels;
  private RecyclerView recyclerView;
  private CommentAdapter adapter;
  private String pageToken = "";

  String fileName = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_you_tube_comment);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    commentModels = new ArrayList<>();
    recyclerView = findViewById(R.id.cfc_comments);
    adapter = new CommentAdapter(this, commentModels, 2);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(false);
    recyclerView.setAdapter(adapter);
    fetchComments(pageToken);
//    fetchComments("");
  }

//  private void fetchComments(String videoId) {
//    List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
//
//    try {
//      // Authorize the request.
//      Credential credential = Auth.authorize(scopes, "commentthreads");
//
//      // This object is used to make YouTube Data API requests.
//      youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
//          .setApplicationName("youtube-cmdline-commentthreads-sample").build();
//
//
//
//      // Call the YouTube Data API's commentThreads.list method to
//      // retrieve video comment threads.
//      CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
//          .list("snippet").setVideoId(videoId).setTextFormat("plainText").execute();
//      List<CommentThread> videoComments = videoCommentsListResponse.getItems();
//
//      if (videoComments.isEmpty()) {
//        System.out.println("Can't get video comments.");
//      } else {
//        // Print information from the API response.
//        System.out
//            .println("\n================== Returned Video Comments ==================\n");
//        for (CommentThread videoComment : videoComments) {
//          CommentSnippet snippet = videoComment.getSnippet().getTopLevelComment()
//              .getSnippet();
//          System.out.println("  - Author: " + snippet.getAuthorDisplayName());
//          System.out.println("  - Comment: " + snippet.getTextDisplay());
//          System.out
//              .println("\n-------------------------------------------------------------\n");
//        }
//        CommentThread firstComment = videoComments.get(0);
//
//
//
//
//        // Call the YouTube Data API's comments.list method to retrieve
//        // existing comment
//        // replies.
//        CommentListResponse commentsListResponse = youtube.comments().list("snippet")
//            .setParentId(firstComment.getId()).setTextFormat("plainText").execute();
//        List<Comment> comments = commentsListResponse.getItems();
//
//        if (comments.isEmpty()) {
//          System.out.println("Can't get comment replies.");
//        } else {
//          // Print information from the API response.
//          System.out
//              .println("\n================== Returned Comment Replies ==================\n");
//          for (Comment commentReply : comments) {
//            CommentSnippet snippet = commentReply.getSnippet();
//            System.out.println("  - Author: " + snippet.getAuthorDisplayName());
//            System.out.println("  - Comment: " + snippet.getTextDisplay());
//            System.out
//                .println("\n-------------------------------------------------------------\n");
//          }
//          Comment firstCommentReply = comments.get(0);
//          firstCommentReply.getSnippet().setTextOriginal("updated");
//          Comment commentUpdateResponse = youtube.comments()
//              .update("snippet", firstCommentReply).execute();
//          // Print information from the API response.
//          System.out
//              .println("\n================== Updated Video Comment ==================\n");
//          CommentSnippet snippet = commentUpdateResponse.getSnippet();
//          System.out.println("  - Author: " + snippet.getAuthorDisplayName());
//          System.out.println("  - Comment: " + snippet.getTextDisplay());
//          System.out
//              .println("\n-------------------------------------------------------------\n");
//        }
//      }
//    }catch (GoogleJsonResponseException e) {
//      System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
//          + " : " + e.getDetails().getMessage());
//      e.printStackTrace();
//
//    } catch (IOException e) {
//      System.err.println("IOException: " + e.getMessage());
//      e.printStackTrace();
//    } catch (Throwable t) {
//      System.err.println("Throwable: " + t.getMessage());
//      t.printStackTrace();
//    }
//  }

  public void fetchComments(String pageToken) {
    String url = "https://www.googleapis.com/youtube/v3/commentThreads?key=" + Utils.DEVELOPER_KEY + "&textFormat=plainText&part=snippet,replies&videoId=cGcYiP1XZBY&maxResults=25&&pageToken=" + pageToken;
    adapter.setNextToken(null);
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          if (!jsonObject.isNull("items")) {
            JSONArray array = jsonObject.optJSONArray("items");
            int len = array.length();
            for (int i = 0; i < len; i++) {
              JSONObject object= array.optJSONObject(i);
              CommentModel commentModel = new CommentModel(object,2);
              commentModels.add(commentModel);
            }
            adapter.notifyDataSetChanged();
          }

          if (!jsonObject.isNull("nextPageToken")){
            String nextToken = jsonObject.optString("nextPageToken");
            adapter.setNextToken(nextToken);
          }
        }

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError volleyError) {
        System.out.println(volleyError.toString());
      }
    });
    int socketTimeout = 50000;
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    request.setRetryPolicy(policy);
    CommentApplication.getInstance().getRequestQueue().add(request);
  }




}
