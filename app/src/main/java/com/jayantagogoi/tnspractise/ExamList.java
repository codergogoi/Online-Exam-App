package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Collections;
import java.util.List;

public class ExamList extends Activity {

    ListView lst_TimeList;
    String UID = "";
    String STDNAME = "";
    private final String baseURL = "http://testnscore.com/api/index.php";

    private  boolean isDataLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_list);
        this.lst_TimeList = (ListView)findViewById(R.id.lst_timelist);
        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);
        getExamList();
     }





    public void getExamList(){

        PostData p = new PostData();
        p.execute("GET_EXAM_LIST",this.UID);
    }

    private  class PostData extends AsyncTask<String, Void, Void> {


        protected Void doInBackground(String ...params){

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(baseURL);

            try{

                String cmd,uid,subject,subType ,pid = "";

                cmd = params[0];
                uid = params[1];


                int cmdIndex = 0;

                final ArrayList<String> current_TimeList = new ArrayList<String>();



                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                // Generic
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", cmd));




                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                String responseText = null;

                responseText = EntityUtils.toString(response.getEntity());

                Log.d("API", response.toString());

                JSONObject json = new JSONObject(responseText);

                Log.d("RESPONS", ""+json.toString());

                String status = json.getString("status");


                if(status.equals("200")){


                    JSONObject json_root = json.getJSONObject("data");

                   // Log.d("UID=>", json_root.toString());

                    for(int i = 0; i < json_root.length(); i++){
                       // Log.e("JS", "key =>"+ json_root.names().getString(i));
                        current_TimeList.add(json_root.get(json_root.names().getString(i)).toString());

                    }

                    Collections.sort(current_TimeList,String.CASE_INSENSITIVE_ORDER);

                    runOnUiThread(new Runnable() {

                        public void run() {

                            ArrayAdapter<String> adopter = new ArrayAdapter<String>(ExamList.this, android.R.layout.simple_list_item_1, current_TimeList);

                            lst_TimeList.setAdapter(adopter);
                            lst_TimeList.setEnabled(true);
                            isDataLoaded = true;

                        }
                    });


                }else{

                    isDataLoaded = true;

                    runOnUiThread(new Runnable() {

                        public void run() {

                           // Toast.makeText(getApplicationContext(), "No any exam scheduled for a while!", Toast.LENGTH_LONG).show();

                            dismissExamList();
                        }
                    });

                }



            }catch(ClientProtocolException e){

                isDataLoaded = true;

            }catch(IOException e){

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return  null;

        }


    }

    public void dismissExamList(){

        if(isDataLoaded) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exam Not Scheduled!");
            builder.setMessage("Exam is not scheduled for a while!");
            builder.setCancelable(false);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    finish();

                    }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void onBackPressed() {

        if(isDataLoaded){

            finish();
        }else{

            return;
            //finish();
        }


    }
}
