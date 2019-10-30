package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPassword extends Activity {

    EditText txtEmailId;
    Button btn_ForgotPwd;
    private final String baseURL = "http://testnscore.com/api/index.php";
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        this.txtEmailId = (EditText)findViewById(R.id.txtEmailForgot);
        this.btn_ForgotPwd = (Button)findViewById(R.id.btn_resetpwd);

    }


    private boolean isNetworkAvailable(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public void resetPassword(View view){


        if(isNetworkAvailable()) {

            if (this.txtEmailId.getText().toString().equals("")) {
                Toast.makeText(this, "Please enter your Email ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emailValidator(this.txtEmailId.getText().toString())) {

                PostData p = new PostData();
                p.execute(this.txtEmailId.getText().toString());
            } else {

                Toast.makeText(this, "Please enter A valid Email ID", Toast.LENGTH_SHORT).show();
            }

        }else{

            Toast.makeText(this, "Internet Connection not available", Toast.LENGTH_LONG).show();
        }

    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private  class PostData extends AsyncTask<String, Void, Void> {

        boolean userAccessStatus = false;


        protected Void doInBackground(String ...params){

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(baseURL);

            try{

                runOnUiThread(new Runnable() {

                    public void run() {

                        Toast.makeText(getApplicationContext(), "Please wait for a moment we are checking.. !", Toast.LENGTH_LONG).show();
                        btn_ForgotPwd.setEnabled(false);

                    }
                });

                String email = params[0];


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email",email));
                 nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", "FORGOT_PWD"));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                String responseText = null;

                responseText = EntityUtils.toString(response.getEntity());

                JSONObject json = new JSONObject(responseText);

                String status = json.getString("status");



                if(status.equals("404")){

                    runOnUiThread(new Runnable() {

                        public void run() {
                            btn_ForgotPwd.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Your email Address does not Exist in Our Database !", Toast.LENGTH_SHORT).show();

                        }
                    });

                }else {


                    if (status.equals("200")) {

                        runOnUiThread(new Runnable() {

                            public void run() {
                                btn_ForgotPwd.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Please check your email address for your password!", Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        });

                    }
                }


            }catch(ClientProtocolException e){


            }catch(IOException e){

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return  null;

        }


    }

}
