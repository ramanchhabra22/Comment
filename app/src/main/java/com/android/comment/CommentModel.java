package com.android.comment;



import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by raman.chhabra on 3/21/18.
 */

public class CommentModel implements Serializable {
  public String message;
  public String id;
  public String createTime;
  public ArrayList<CommentReplyModel> replyModels;

  public CommentModel(JSONObject object, int type) {
    switch (type) {
      case 1:
        message = object.optString("message");
        id = object.optString("id");
        createTime = object.optString("created_time");
        break;

      case 2:
        id = object.optString("id");
        if (!object.isNull("snippet")){
          JSONObject jsonObject = object.optJSONObject("snippet");
          if (!jsonObject.isNull("topLevelComment")){
            JSONObject commentObj = jsonObject.optJSONObject("topLevelComment");
            if (!commentObj.isNull("snippet")){
              JSONObject snippetObj = commentObj.optJSONObject("snippet");
              message = snippetObj.optString("textDisplay");
              createTime = snippetObj.optString("updatedAt");
            }
          }
        }
        if (!object.isNull("replies")){
          JSONObject jsonObject = object.optJSONObject("replies");
          if (!jsonObject.isNull("comments")){
            JSONArray array = jsonObject.optJSONArray("comments");
            int len = array.length();
            replyModels = new ArrayList<>();
            for (int i=0;i<len;i++){
              JSONObject commentObj = array.optJSONObject(i);
              CommentReplyModel commentReplyModel = new CommentReplyModel();
              commentReplyModel.id= commentObj.optString("id");
              if (!commentObj.isNull("snippet")){
                JSONObject snippetObj = commentObj.optJSONObject("snippet");
                commentReplyModel.message = snippetObj.optString("textDisplay");
                commentReplyModel.displayName = snippetObj.optString("authorDisplayName");
              }
              replyModels.add(commentReplyModel);
            }
          }
        }
        break;
    }
  }
}
