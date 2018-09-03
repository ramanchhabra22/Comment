package com.android.comment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class FacebookComment extends AppCompatActivity {

  private ArrayList<CommentModel> commentModels;
  private RecyclerView recyclerView;
  private CommentAdapter adapter;
  private String accessToken;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_facebook_comment);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    commentModels = new ArrayList<>();
    recyclerView = findViewById(R.id.cfc_comments);
    adapter = new CommentAdapter(this, commentModels,1);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(false);
    recyclerView.setAdapter(adapter);
    getAccessToken();
  }

//  public void fbLogin() {
//    callbackManager = CallbackManager.Factory.create();
//    LoginManager.getInstance().logOut();
//    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//      @Override
//      public void onSuccess(final LoginResult loginResult) {
//        loginResult.getAccessToken();
//
//      }
//
//      @Override
//      public void onCancel() {
//        LoginManager.getInstance().logOut();
//      }
//
//      @Override
//      public void onError(FacebookException exception) {
//        System.out.println(exception);
//      }
//    });
//
//    AccessToken token = AccessToken.getCurrentAccessToken();
//    if (token != null) {
//      accessToken = token.getToken();
//    } else {
//      LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
//    }
//  }

  private void getAccessToken() {
    String url = "https://graph.facebook.com/oauth/access_token?client_id=1589621004486239&client_secret=ec96a38703be57bdeca98f35d78f5402&grant_type=client_credentials";
//    String url = "https://graph.facebook.com/oauth/access_token?client_id=1472859052935932&client_secret=1f56ce83887422a642c9690a9ccd15d3&grant_type=client_credentials";
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          accessToken = jsonObject.optString("access_token");
          String url = "https://graph.facebook.com/v2.12/489081634454102_2020225241339726/comments?access_token=" + accessToken;
          fetchComments(url);
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

  public void fetchComments(String url) {
    adapter.setNextUrl(null);
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null) {
          if (!jsonObject.isNull("data")) {
            JSONArray array = jsonObject.optJSONArray("data");
            int len = array.length();
            for (int i = 0; i < len; i++) {
              JSONObject object = array.optJSONObject(i);
              CommentModel commentModel = new CommentModel(object,1);
              if (!commentModel.message.isEmpty())
                fetchCommentReplies(commentModel);
            }
          }


          if (!jsonObject.isNull("paging")) {
            JSONObject pagingObj = jsonObject.optJSONObject("paging");
            String nextUrl = pagingObj.optString("next");
            adapter.setNextUrl(nextUrl);
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
            adapter.notifyDataSetChanged();
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
