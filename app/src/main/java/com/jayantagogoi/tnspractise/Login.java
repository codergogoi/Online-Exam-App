package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {

    EditText txtUID,txtPWD;
    boolean loginSuccess = false;
    private final String baseURL = "http://testnscore.com/api/index.php";
    LinearLayout pnlLogin,pnlContinue;
    String uid,pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.txtUID = (EditText)findViewById(R.id.txtUID);
        this.txtPWD = (EditText)findViewById(R.id.txtPWD);
        pnlContinue = (LinearLayout)findViewById(R.id.pnl_ContinueLogin);
        pnlLogin = (LinearLayout)findViewById(R.id.pnl_FirstLogin);
        TextView txtName = (TextView)findViewById(R.id.txt_continue);

        SharedPreferences pref = getSharedPreferences("USER_INFO", MODE_PRIVATE);
        uid =  pref.getString("UID", "");
        String uname =  pref.getString("NAME","");
        pwd = pref.getString("PWD","");

        if(uid.equals("")){

            pnlContinue.setVisibility(View.GONE);
            pnlLogin.setVisibility(View.VISIBLE);
        }else{
            pnlLogin.setVisibility(View.GONE);
            pnlContinue.setVisibility(View.VISIBLE);
            txtName.setText("Hi "+ uname);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }


    public void onContinue(View v){


        if(isNetworkAvailable()) {
            PostData p = new PostData();
            p.execute(uid, pwd);
        }else{

            Toast.makeText(getApplicationContext(),"Internet Connection Not Available",Toast.LENGTH_SHORT).show();
            return;
        }


    }

    public void onNotYou(View v){

        pnlLogin.setVisibility(View.VISIBLE);
        pnlContinue.setVisibility(View.INVISIBLE);

    }

    private boolean isNetworkAvailable(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public void registerMe(View v){

        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.testnscore.com/register.html"));
        startActivity(browser);

    }

    public void onLoginPerform(View view){


        //this.txtUID.setText("ABC01MEGHAXA1");
       // this.txtPWD.setText("jay");

        if(this.txtUID.getText().toString().equals("")){

            Toast.makeText(getApplicationContext(),"User Name Empty",Toast.LENGTH_SHORT).show();
            return;
        }

        if(this.txtPWD.getText().toString().equals("")){

            Toast.makeText(getApplicationContext(),"Password is Empty", Toast.LENGTH_SHORT).show();
            return;
        }


        //Log.d("LOGIN","Yes it is working..");

        // send request for authentication

        String std_uid,std_pwd;
        std_uid = this.txtUID.getText().toString();
        std_pwd = this.txtPWD.getText().toString();

        if(isNetworkAvailable()) {
            Log.d("LOG", "Executing..."+std_uid+""+std_pwd);

            PostData p = new PostData();
            p.execute(std_uid, std_pwd);
        }else{

            Toast.makeText(getApplicationContext(),"Internet Connection Not Available",Toast.LENGTH_SHORT).show();
            return;
        }



    }

    public  void PerformLogin(){

        Intent dbIntent = new Intent(this, Dashboard.class);

        dbIntent.putExtra("UID",this.txtUID.getText().toString());
        dbIntent.putExtra("PWD",this.txtPWD.getText().toString());

        startActivity(dbIntent);

    }

    public void wrongUidPwd(){

        Toast.makeText(getApplicationContext(),"Password is Empty", Toast.LENGTH_SHORT).show();


    }

    public void  forgotPwd(View view){

        Intent forgotIntent = new Intent(this,ForgotPassword.class);
        startActivity(forgotIntent);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {


        finish();


    }


    private  class PostData extends AsyncTask<String, Void, Void>{


        boolean userAccessStatus = false;

        protected Void doInBackground(String ...params){

            Log.d("STEP WOrking..","DOINBACKGROUND");
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(baseURL);

            try{

                String uid = params[0];
                String pwd = params[1];
                Log.d("Goin in Try...",uid+" "+ pwd);


                List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("pwd", pwd));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", "LOGIN"));

                Log.d("HI",nameValuePairs.toString());


                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                Log.d("Log1", "response"+ response);

                String responseText = null;

                responseText = EntityUtils.toString(response.getEntity());

               // Log.d("LOG", responseText.toString());

                JSONObject json = new JSONObject(responseText);

                String status = json.getString("status");

                if(status.equals("404")){

                    runOnUiThread(new Runnable() {

                        public void run() {

                            Toast.makeText(getApplicationContext(), "User ID and Password Does not Match!", Toast.LENGTH_SHORT).show();

                        }
                    });

                }else {

                    JSONObject json_root = json.getJSONObject("data");

                    Log.d("UID=>", json_root.toString());

                    if (status.equals("200")) {

                        String s_uid = json_root.getString("uid");
                        String s_name = json_root.getString("name");
                        String subscribe = json_root.getString("subscribe");
                        String isAuthor = json_root.getString("isauthor");
                        String isExternal = json_root.getString("isexternal");
                        String std = json_root.getString("std");
                        String testCount = json_root.getString("test_count");

                        SharedPreferences.Editor editor = getSharedPreferences("USER_INFO",MODE_PRIVATE).edit();
                        editor.putString("UID",s_uid);
                        editor.putString("PWD",pwd);
                        editor.putString("NAME",s_name);
                        editor.putString("AUTHOR",isAuthor);
                        editor.putString("ISEX", isExternal);
                        editor.putString("ISSUB", subscribe);
                        editor.putString("STD", std);
                        editor.putString("TEST_COUNT",testCount);

                        editor.commit();

                        Intent dbIntent = new Intent(Login.this, Dashboard.class);
                        startActivity(dbIntent);
                    } else {

                        runOnUiThread(new Runnable() {

                            public void run() {

                                Toast.makeText(getApplicationContext(), "User ID and Password Does not Match!", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }

            }catch(ClientProtocolException e){

                e.printStackTrace();;
            }catch(IOException e){

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return  null;

        }


    }
}
