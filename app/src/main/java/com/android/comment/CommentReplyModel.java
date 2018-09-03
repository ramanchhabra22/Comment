package com.android.comment;



import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by raman.chhabra on 3/21/18.
 */

public class CommentReplyModel implements Serializable {
  public String message;
  public String id;
  public String createTime;
  public String displayName;

  public CommentReplyModel(JSONObject object){
    message = object.optString("message");
    id = object.optString("id");
    createTime = object.optString("created_time");
  }

  public CommentReplyModel(){

  }
}
