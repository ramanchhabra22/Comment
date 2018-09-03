package com.android.comment;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  ArrayList<CommentModel> commentModels = new ArrayList<>();
  private PopupWindow popupWindow;
  private String accessToken;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onFacebookClick(View view) {
    Intent intent = new Intent(MainActivity.this, FacebookComment.class);
    startActivity(intent);
  }

  public void onFacebookAllClick(View view) {
    getAccessToken("Facebook_all_comments.xls", "Facebook", false);
  }

  public void onFacebookDailyClick(View view) {
    getAccessToken("Facebook_" + Utils.getCurrentDate() + ".xls", "Facebook", true);
  }

  public void onYouTubeAllClick(View view) {
    fetchComments("", "YouTube_all_comments.xls", "YouTube", false);
  }

  public void onYouTubeClick(View view) {
    Intent intent = new Intent(MainActivity.this, YouTubeComment.class);
    startActivity(intent);
  }

  public void onYouTubeDailyClick(View view) {
    fetchComments("", "YouTube_" + Utils.getCurrentDate() + ".xls", "YouTube", true);
  }


  public void fetchComments(String pageToken, final String fileName, final String sheetName, final boolean isDaily) {
    showPd();
    String url = "https://www.googleapis.com/youtube/v3/commentThreads?key=" + Utils.DEVELOPER_KEY + "&textFormat=plainText&part=snippet,replies&videoId=cGcYiP1XZBY&maxResults=25&&pageToken=" + pageToken;
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          if (!jsonObject.isNull("items")) {
            JSONArray array = jsonObject.optJSONArray("items");
            int len = array.length();
            for (int i = 0; i < len; i++) {
              JSONObject object = array.optJSONObject(i);
              CommentModel commentModel = new CommentModel(object, 2);
              if (isDaily) {
                if (Utils.compareTime(commentModel.createTime)) {
                  commentModels.add(commentModel);
                } else {
                  Utils.makeDailyData(MainActivity.this, commentModels, fileName, sheetName);
                  return;
                }
              } else {
                commentModels.add(commentModel);
              }
            }
          }

          if (!jsonObject.isNull("nextPageToken")) {
            String nextToken = jsonObject.optString("nextPageToken");
            fetchComments(nextToken, fileName, sheetName, isDaily);
          } else {
            Utils.makeDailyData(MainActivity.this, commentModels, fileName, sheetName);
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


  private void getAccessToken(final String fileName, final String sheetName, final boolean isDaily) {
    showPd();
    String url = "https://graph.facebook.com/oauth/access_token?client_id=1589621004486239&client_secret=ec96a38703be57bdeca98f35d78f5402&grant_type=client_credentials";
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          accessToken = jsonObject.optString("access_token");
          String url = "https://graph.facebook.com/v2.12/489081634454102_2020225241339726/comments?access_token=" + accessToken;
          if (isDaily){
            url = url + "&since=" + Utils.getSinceTime() + "&until=" + Utils.getCurrentTime();
          }
          fetchFacebookComments(url, fileName, sheetName, isDaily);
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

  public void fetchFacebookComments(String url, final String fileName, final String sheetName, final boolean isDaily) {
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          if (!jsonObject.isNull("data")) {
            JSONArray array = jsonObject.optJSONArray("data");
            int len = array.length();
            for (int i = 0; i < len; i++) {
              JSONObject object = array.optJSONObject(i);
              CommentModel commentModel = new CommentModel(object, 1);
              if (!commentModel.message.isEmpty())
                fetchCommentReplies(commentModel);
            }
          }


          if (!jsonObject.isNull("paging")) {
            JSONObject pagingObj = jsonObject.optJSONObject("paging");
            String nextUrl = pagingObj.optString("next");
            if (nextUrl.isEmpty()){
              Utils.makeDailyData(MainActivity.this, commentModels, fileName, sheetName);
            }else{
              fetchFacebookComments(nextUrl, fileName, sheetName, isDaily);
            }
          }else{
            Utils.makeDailyData(MainActivity.this, commentModels, fileName, sheetName);
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

  private void fetchCommentReplies(final CommentModel commentModel) {
    String url = "https://graph.facebook.com/v2.12/" + commentModel.id + "/comments?access_token=" + accessToken;
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          if (!jsonObject.isNull("data")) {
            JSONArray array = jsonObject.optJSONArray("data");
            int len = array.length();
            ArrayList<CommentReplyModel> replyModels = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
              JSONObject object = array.optJSONObject(i);
              CommentReplyModel replyModel = new CommentReplyModel(object);
              replyModels.add(replyModel);
            }
            commentModel.replyModels = replyModels;
            commentModels.add(commentModel);
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

  public void showPd() {
    try {
      if (popupWindow == null) {
        View progressBar = LayoutInflater.from(getApplicationContext()).inflate(R.layout.progress, null);
        ProgressBar bar = (ProgressBar) progressBar.findViewById(R.id.progress);
        if (bar.getProgressDrawable() != null)
          bar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        popupWindow = new PopupWindow(progressBar, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.grey));
      }
      if (popupWindow != null && !popupWindow.isShowing())
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void dismissPd() {
    try {
      if (getApplicationContext() != null && popupWindow != null && popupWindow.isShowing())
        popupWindow.dismiss();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
