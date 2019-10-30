package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.List;

public class SchedulePaper extends Activity {

    String UID = "";
    String STDNAME = "";

    Spinner spn_SubjectList;
    Spinner spn_PaperList;
    Button btn_StartExam;
    TextView userName;
    private final String baseURL = "http://testnscore.com/api/index.php";
    ProgressDialog pDialog;

    private  boolean isScheduled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_paper);

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);


        this.btn_StartExam = (Button)findViewById(R.id.btn_appeartest);
        this.spn_PaperList = (Spinner)findViewById(R.id.spin_paperlist_sp);
        this.spn_SubjectList = (Spinner)findViewById(R.id.spn_subjectList_sp);


        this.userName = (TextView)findViewById(R.id.std_name_schedule);
        this.userName.setText("Welcome, " + this.STDNAME);

        loadSubjects();

        this.spn_SubjectList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                getGeneratedPaper();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }



    public void startExam(View view){

        if(!isScheduled){
            isScheduled = true;
            showProgress();
            PostData p = new PostData();
            p.execute("SCHEDULE_QP", this.UID, this.spn_PaperList.getSelectedItem().toString(), this.spn_SubjectList.getSelectedItem().toString());
        }
    }

    public void showProgress(){

        pDialog = new ProgressDialog(this); // this = YourActivity
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Loading. Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
    }


    public void getGeneratedPaper(){

        this.btn_StartExam.setEnabled(false);
        this.spn_PaperList.setEnabled(false);
        PostData p = new PostData();
        p.execute("GET_GENERATED_PAPER",this.UID,this.spn_SubjectList.getSelectedItem().toString());

    }

    public void loadSubjects(){

        PostData p = new PostData();
        p.execute("DB_SUBJECT_LIST", this.UID);


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

                final ArrayList<String> current_paper = new ArrayList<String>();

                final ArrayList<String> subList = new ArrayList<String>();


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                // Generic
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", cmd));


                if(cmd.equals("GET_GENERATED_PAPER")){

                    subject = params[2];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));
                    cmdIndex = 1;

                }else if(cmd.equals("DB_GET_AUTHOR")){
                     subject = params[2];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));
                    cmdIndex = 2;
                }else if(cmd.equals("SCHEDULE_QP")){

                    pid = params[2];
                    subject = params[3];
                    nameValuePairs.add(new BasicNameValuePair("paper_id", pid));
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));

                    cmdIndex = 3;
                }else if(cmd.equals("DB_SUBJECT_LIST")){
                    cmdIndex = 5;
                }

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

                    switch(cmdIndex){

                        case 1:
                            for(int i = 0; i < json_root.length(); i++){
                                 current_paper.add(json_root.get(json_root.names().getString(i)).toString());

                            }


                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(SchedulePaper.this, android.R.layout.simple_spinner_item, current_paper);

                                    spn_PaperList.setAdapter(adopter);
                                    spn_PaperList.setEnabled(true);
                                    btn_StartExam.setEnabled(true);

                                }
                            });


                            break;
                        case 2:





                            break;
                        case 3:

                            SharedPreferences.Editor editor = getSharedPreferences("USER_INFO",MODE_PRIVATE).edit();
                            editor.putString("PID",pid);
                            editor.putString("EXTYPE","1");
                            editor.commit();

                            runOnUiThread(new Runnable() {

                                public void run() {


                                    if(pDialog != null)
                                        pDialog.dismiss();

                                    Intent examView = new Intent(SchedulePaper.this,RuntimeExam.class);
                                    startActivity(examView);

                                }
                            });


                            //Intent dbIntent = new Intent(GeneratePaper.this, SchedulePaper.class);
                         //   startActivity(dbIntent);
                            break;
                        case 5:
                            JSONObject subject_root = json.getJSONObject("data");
                            for(int i = 0; i < subject_root.length(); i++){
                                subList.add(subject_root.get(subject_root.names().getString(i)).toString());

                            }

                            Collections.sort(subList,String.CASE_INSENSITIVE_ORDER);

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(SchedulePaper.this, android.R.layout.simple_spinner_dropdown_item, subList);

                                    spn_SubjectList.setAdapter(adopter);


                                }
                            });
                            Log.d("Subject found","Subject found");
                            break;



                    }




                }else{

                    runOnUiThread(new Runnable() {

                        public void run() {

                            //Toast.makeText(getApplicationContext(), "Data Not Found!", Toast.LENGTH_SHORT).show();

                        }
                    });

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
