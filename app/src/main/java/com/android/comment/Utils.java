package com.android.comment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by raman.chhabra on 3/22/18.
 */

public class Utils {

  public static String DEVELOPER_KEY = "AIzaSyAn9Q4Wj051JZEErRa90HksmwtKfdAwwRQ";

  public static void makeDailyData(Context context, ArrayList<CommentModel> commentModels, String csvFile, String sheetName) {

    File sd = Environment.getExternalStorageDirectory();

    File directory = new File(sd.getAbsolutePath());
    //create directory if not exist
    if (!directory.isDirectory()) {
      directory.mkdirs();
    }
    try {

      //file path
      File file = new File(directory, csvFile);

      WorkbookSettings wbSettings = new WorkbookSettings();
      wbSettings.setLocale(new Locale("en", "EN"));
      WritableWorkbook workbook;
      workbook = Workbook.createWorkbook(file, wbSettings);
      WritableSheet sheet = workbook.createSheet(sheetName, 0);

      sheet.addCell(new Label(0, 0, "CreateDate"));
      sheet.addCell(new Label(1, 0, "Comment"));
      sheet.addCell(new Label(2, 0, "Replies"));

      int len = commentModels.size();
      for (int i = 1; i <= len; i++) {
        CommentModel commentModel = commentModels.get(i - 1);
        sheet.addCell(new Label(0, i, commentModel.createTime));
        sheet.addCell(new Label(1, i, commentModel.message));
        ArrayList<CommentReplyModel> replyModels = commentModel.replyModels;
        if (replyModels != null) {
          int replyLen = replyModels.size();
          int k = 2;
          for (int j = 0; j < replyLen; j++) {
            CommentReplyModel replyModel = replyModels.get(j);
            sheet.addCell(new Label(k + j, i, replyModel.message));
          }
        }
      }

      workbook.write();
      workbook.close();
      String path = file.getAbsolutePath();
      openFile(context, path);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (RowsExceededException e) {
      e.printStackTrace();
    } catch (WriteException e) {
      e.printStackTrace();
    } finally {
      ((MainActivity) context).dismissPd();
    }
  }

  private static void openFile(final Context context, final String path) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      uri = FileProvider.getUriForFile(context, context.getPackageName() + ".my.comment.provider", new File(path));
      List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
      for (ResolveInfo resolveInfo : resInfoList) {
        String packageName = resolveInfo.activityInfo.packageName;
        context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }
    } else {
      uri = Uri.fromFile(new File(path));
    }
//    intent.setDataAndType(uri, "application/vnd.ms-excel");
//    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    try {
//      context.startActivity(intent);
//    } catch (ActivityNotFoundException e) {
//      Toast.makeText(context, "No Application Available to View Excel",
//          Toast.LENGTH_SHORT).show();
//    }

    Intent emailIntent = new Intent(Intent.ACTION_SEND);
    emailIntent.setType("text/plain");
    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"raman.chhabra@brightlifecare.com"});
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Comment File");
    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
    context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));

  }


  public static Date formatData(String time) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      return formatter.parse(time);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getCurrentDate() {
    Calendar calendar = Calendar.getInstance();
    int month = calendar.get(Calendar.MONTH) + 1;
    if (month < 10)
      return calendar.get(Calendar.YEAR) + "-" + "0" + month + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    else
      return calendar.get(Calendar.YEAR) + "-" + month + "-" + calendar.get(Calendar.DAY_OF_MONTH);

  }

  private static long getTimeInMilliSec(String givenDateString) {
    if (givenDateString.contains("Z")) {
      givenDateString = givenDateString.replace(".000Z", "");
    } else if (givenDateString.contains("+")) {
      givenDateString = givenDateString.replace("+0000", "");
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      Date mDate = sdf.parse(givenDateString);
      return mDate.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  public static boolean compareTime(String givenDateString) {
    Calendar calendar = Calendar.getInstance();
    long currentTimeInMilliSec = calendar.getTimeInMillis();
    long time = getTimeInMilliSec(givenDateString);
    return (time > currentTimeInMilliSec - TimeUnit.HOURS.toMillis(24));

  }

  public static long getSinceTime() {
    Calendar calendar = Calendar.getInstance();
    long currentTimeInMilliSec = calendar.getTimeInMillis();
    return (currentTimeInMilliSec - TimeUnit.HOURS.toMillis(24)) / 1000;
  }

  public static long getCurrentTime() {
    Calendar calendar = Calendar.getInstance();
    return calendar.getTimeInMillis() / 1000;
  }

}
