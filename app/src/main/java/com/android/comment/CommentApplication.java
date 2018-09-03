package com.android.comment;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
//import com.facebook.FacebookSdk;

/**
 * Created by raman.chhabra on 3/21/18.
 */

public class CommentApplication extends Application {

  private static CommentApplication mInstance;
  private RequestQueue mRequestQueue;


  public static synchronized CommentApplication getInstance() {
    return mInstance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mInstance = this;
//    FacebookSdk.sdkInitialize(getApplicationContext());
  }

  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }
    return mRequestQueue;
  }
}
