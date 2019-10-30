package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class Settings extends Activity {

    String UID = "";
    EditText newPassword,confirmPassword;

    Button submitButton;
    private final String baseURL = "http://testnscore.com/api/index.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);

        newPassword = (EditText)findViewById(R.id.txt_newpwd);
        confirmPassword = (EditText)findViewById(R.id.txt_ConfirmPwd);

        submitButton = (Button)findViewById(R.id.btn_ChangePwd);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(newPassword.getText().toString().equals("")){

                    Toast.makeText(getApplicationContext(),"New password is empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(confirmPassword.getText().toString().equals("")){

                    Toast.makeText(getApplicationContext(),"Confirm password is empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                String pwda = newPassword.getText().toString();
                String pwdb = confirmPassword.getText().toString();


                if(pwda.equals(pwdb)){

                    PostData p = new PostData();
                    p.execute(UID,pwdb);

                }else{

                    Toast.makeText(getApplicationContext(),"Confirm password does not match!",Toast.LENGTH_LONG).show();
                    return;
                }




            }
        });


     }

    private  class PostData extends AsyncTask<String, Void, Void> {

        boolean userAccessStatus = false;

        protected Void doInBackground(String ...params){

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(baseURL);

            try{

                String uid = params[0];
                String pwd = params[1];


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", "CHANGE_PWD"));
                nameValuePairs.add(new BasicNameValuePair("pwd", pwd));

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

                            Toast.makeText(getApplicationContext(), "Unable to process request!", Toast.LENGTH_SHORT).show();

                        }
                    });

                }else {


                    if (status.equals("200")) {

                        runOnUiThread(new Runnable() {

                            public void run() {

                                Toast.makeText(getApplicationContext(), "Password has been Changed!", Toast.LENGTH_SHORT).show();

                            }
                        });

                        Intent dbIntent = new Intent(Settings.this, Dashboard.class);
                        startActivity(dbIntent);
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
