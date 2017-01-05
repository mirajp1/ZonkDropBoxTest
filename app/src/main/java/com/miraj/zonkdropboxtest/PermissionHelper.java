package com.miraj.zonkdropboxtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by miraj on 5/1/17.
 */

public class PermissionHelper {

    final static public int PERM_REQUEST_CODE=6;

    public static void askPermission(Activity activity, Context context){

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(context,"Storage Permission Needed to store dropbox login data",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERM_REQUEST_CODE);
        }

    }

    public static boolean checkPermission(Context context){
        int result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result1 == PackageManager.PERMISSION_GRANTED ){

            return true;

        } else {

            return false;

        }
    }


}
