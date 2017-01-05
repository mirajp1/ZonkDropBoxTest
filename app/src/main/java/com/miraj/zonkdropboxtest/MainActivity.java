package com.miraj.zonkdropboxtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final static private String DB_PREFS="db_prefs";
    final static private String DB_ACCESS_TOKEN="access_token";
    final static private int FILE_REQUEST_CODE=5;

    private Button loginButton;
    private Button uploadButton;

    private boolean mLoggedIn=false;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button)findViewById(R.id.loginButton);
        uploadButton = (Button)findViewById(R.id.uploadButton);

        PermissionHelper.askPermission(this,getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isLoggedIn()) {
                    if(!PermissionHelper.checkPermission(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(),"Can't continue without permissions",Toast.LENGTH_SHORT).show();
                    }
                    else if(!ConnectivityHelper.isConnected()){
                        Toast.makeText(getApplicationContext(),"Check internet connection",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        startLogin();
                    }
                }
                else{
                    logout();
                }

            }
        });

        //init();

    }


    @Override
    protected void onResume() {
        super.onResume();

        login();

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!PermissionHelper.checkPermission(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(),"Can't continue without Storage permissions",Toast.LENGTH_SHORT).show();
                }
                else if(!ConnectivityHelper.isConnected()) {
                    Toast.makeText(getApplicationContext(),"Check Internet Connection",Toast.LENGTH_SHORT).show();
                }
                else{
                    upload();
                }

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isLoggedIn(){
        return mLoggedIn;
    }





    private void login(){

        SharedPreferences prefs = getSharedPreferences(DB_PREFS, MODE_PRIVATE);
        accessToken=prefs.getString(DB_ACCESS_TOKEN,null);

        if(accessToken==null){
           accessToken = Auth.getOAuth2Token(); //generate Access Token
        }

        if (accessToken != null) {
            prefs.edit().putString(DB_ACCESS_TOKEN, accessToken).apply();

            Toast.makeText(this,"Logged In",Toast.LENGTH_LONG).show();
            mLoggedIn=true;
            uploadButton.setEnabled(true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginButton.setText("Logout");
                }
            });
        }


    }

    private void startLogin(){
        Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DB_APP_KEY));
    }
    private void logout(){

        mLoggedIn=false;
        uploadButton.setEnabled(false);

        SharedPreferences prefs= getSharedPreferences(DB_PREFS,0);
        prefs.edit().clear().apply();
        accessToken=null;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginButton.setText("DropBox Login");
                uploadButton.setEnabled(false);
            }
        });

        Toast.makeText(this,"LoggedOut",Toast.LENGTH_LONG).show();

    }

    public static DbxClientV2 getClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("zonk_test/1.0").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);
        return client;
    }

    private void upload(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, FILE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null)
            return;
        if (requestCode == FILE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                File file = new File(FileHelper.getPath(getApplicationContext(),data.getData()));
                Toast.makeText(getApplicationContext(),"Starting upload",Toast.LENGTH_SHORT).show();
                new UploadFileAsyncTask(
                        getApplicationContext(),
                        getClient(accessToken),
                        file)
                        .execute();


            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.PERM_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Can't proceed without Storage permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
