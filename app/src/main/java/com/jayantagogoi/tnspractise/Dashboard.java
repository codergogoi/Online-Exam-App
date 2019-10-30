package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Dashboard extends Activity {

    String UID = "";
    String STDNAME = "";
    String isEx,isSub ="";
    Intent currentIntent;
    TextView txtUname ;
    RadioGroup report_type;
    Spinner  paperList;
    Button getReport;
    Spinner subjectList;
    RadioButton r0,r1;
    TextView txtTimer;
    int countClick;
    JSONArray paperArray;

    String paper_Id,exam_date,subject;

    private final String FORMAT = "%02d:%02d:%02d";
    CountDownTimer mCountDownTimer;

    long s_days,s_hour,s_minutes = 0;


    long mInitialTime = DateUtils.DAY_IN_MILLIS * 0 +
            DateUtils.HOUR_IN_MILLIS * 0 +
            DateUtils.MINUTE_IN_MILLIS * 20 +
            DateUtils.SECOND_IN_MILLIS * 20;

    private final String baseURL = "http://testnscore.com/api/index.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        currentIntent = getIntent();
        paperList = (Spinner)findViewById(R.id.paper_list);
        subjectList = (Spinner)findViewById(R.id.subject_list);

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);
        this.isEx = pref.getString("ISEX",null);
        this.isSub = pref.getString("ISSUB", null);

        checkIfExamIsReady();



        this.txtUname = (TextView)findViewById(R.id.std_name);
        this.txtUname.setText("Welcome, " + this.STDNAME);
        this.report_type = (RadioGroup)findViewById(R.id.r_type);
        this.getReport = (Button)findViewById(R.id.get_report);


        this.r0 = (RadioButton)findViewById(R.id.r_0);
        this.r1 = (RadioButton)findViewById(R.id.r_1);

        Button btnExamlist = (Button)findViewById(R.id.exam_list);

        if(isEx.equals("YES")){

            btnExamlist.setVisibility(View.GONE);
            r0.setVisibility(View.GONE);
            r1.setChecked(true);
        }

        Button btnGenerate = (Button)findViewById(R.id.btngnerate_qp);

        Button btnAppearTest = (Button)findViewById(R.id.btnappear_test);


        if(isSub.equals("YES")){

            btnGenerate.setVisibility(View.VISIBLE);
            btnAppearTest.setVisibility(View.VISIBLE);

        }else{
             r1.setVisibility(View.GONE);
            r0.setChecked(true);
            btnGenerate.setVisibility(View.GONE);
            btnAppearTest.setVisibility(View.GONE);
        }

        this.txtTimer = (TextView)findViewById(R.id.timer_info);
        loadSubjects();

        this.subjectList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(countClick > 0) {
                    getPaperList();
                }
                countClick++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Toast.makeText(getApplicationContext(),"Message :"+ this.UID,Toast.LENGTH_LONG).show();

       // tickCountDown(20);
     }



    public  void settings(View view){

        Intent settingsIntent = new Intent(this, Settings.class);
        startActivity(settingsIntent);

    }

    public  void examListView(View view){

        Intent examList = new Intent(this, ExamList.class);
        startActivity(examList);


    }

    public void generateQpaper(View view){


        Intent generateqp = new Intent(this, GeneratePaper.class);
        startActivity(generateqp);

    }

    public void schedulePaperNow(View view){


        Intent schedule = new Intent(this, SchedulePaper.class);
        startActivity(schedule);

    }


    public void loadSubjects(){

        PostData p = new PostData();
        p.execute("DB_SUBJECT_LIST", this.UID);


    }

    public void checkIfExamIsReady(){

        PostData p = new PostData();
        p.execute("NOTIFICATION", this.UID);


    }
    public  void getPaperList(){

        this.getReport.setEnabled(false);
        this.paperList.setEnabled(false);

        int selectedId = report_type.getCheckedRadioButtonId();

        RadioButton selectedBtn = (RadioButton)findViewById(selectedId);

        //param
        String reportType = selectedBtn.getText().toString();

        String reportindex = "0";
        if(reportType.equals("School TEST")){

            reportindex = "0";
        }else {
            reportindex = "1";
        }

        //param
        Spinner sub_list = (Spinner)findViewById(R.id.subject_list);
        String currentSubject = sub_list.getSelectedItem().toString();

        //Log.d("API","&"+ reportType+"&"+currentSubject);

        PostData p = new PostData();
        p.execute("DB_PAPER_LIST", this.UID, currentSubject, reportindex);
    }

    public void contactUs(View v){

        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.testnscore.com/contact.html"));
        startActivity(browser);

    }



    public void viewReport(View view){

        SharedPreferences.Editor editor = getSharedPreferences("USER_INFO",MODE_PRIVATE).edit();
        editor.putString("PID",paperList.getSelectedItem().toString());
        editor.commit();

        Intent reportViewIntent = new Intent(this,ReportView.class);
        startActivity(reportViewIntent);
    }

    public void startClock(){

       // SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z");

        Date dt = new Date();

        try {

                JSONObject jobj = paperArray.getJSONObject(0);

                paper_Id = jobj.getString("paper_id");
                exam_date = jobj.getString("date");
                subject = jobj.getString("subject");
                s_days = Long.parseLong(jobj.getString("days"));
                s_hour = Long.parseLong(jobj.getString("hour"));
                s_minutes = Long.parseLong(jobj.getString("minutes"));


           // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z");
            //dt = dateFormat.parse(exam_date);


        }catch (JSONException ex){

        }

        //Log.d("Date", "hello " + dt);
        //Log.d("SD", "" + exam_date);

        mInitialTime = DateUtils.DAY_IN_MILLIS * s_days +
                DateUtils.HOUR_IN_MILLIS * s_hour+
                DateUtils.MINUTE_IN_MILLIS * s_minutes +
                DateUtils.SECOND_IN_MILLIS * 30;

        mCountDownTimer = new CountDownTimer(mInitialTime, 1000) {
            StringBuilder time = new StringBuilder();
            @Override
            public void onFinish() {

                SharedPreferences.Editor editor = getSharedPreferences("USER_INFO",MODE_PRIVATE).edit();
                editor.putString("PID",paper_Id);
                editor.putString("EXTYPE", "0");
                editor.commit();

                startExam();
                // Load Exam
            }

            @Override
            public void onTick(long millisUntilFinished) {
                time.setLength(0);
                // Use days if appropriate
                if(millisUntilFinished > DateUtils.DAY_IN_MILLIS) {
                    long count = millisUntilFinished / DateUtils.DAY_IN_MILLIS;
                    if(count > 1)
                        time.append(count).append(" days ");
                    else
                        time.append(count).append(" day ");

                    millisUntilFinished %= DateUtils.DAY_IN_MILLIS;
                }

                time.append(DateUtils.formatElapsedTime(Math.round(millisUntilFinished / 1000d)));
                txtTimer.setText(time.toString());
            }
        }.start();


    }

    public void startExam(){

        Intent startExamIntent = new Intent(this,RuntimeExam.class);
        startActivity(startExamIntent);


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


                if(cmd.equals("DB_PAPER_LIST")){
                    subject = params[2];
                    subType = params[3];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));

                    nameValuePairs.add(new BasicNameValuePair("subtype", subType));
                    cmdIndex = 1;

                }else if(cmd.equals("NOTIFICATION")){
                    cmdIndex = 3;
                }else if(cmd.equals("DB_SUBJECT_LIST")){
                    cmdIndex = 5;
                }else if(cmd.equals("LOGOUT")){
                    cmdIndex = 4;
                }



                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                String responseText = null;

                responseText = EntityUtils.toString(response.getEntity());

               // Log.d("API", responseText);
                JSONObject json = new JSONObject(responseText);

                String status = json.getString("status");

                Log.d("RESPONSE", json.toString());
               // Log.d("CMD", cmd);

                if(status.equals("200")){

                   //


                    switch(cmdIndex){

                        case 0:

                            break;
                        case 1:
                            JSONObject json_root = json.getJSONObject("data");
                            for(int i = 0; i < json_root.length(); i++){
                                current_paper.add(json_root.get(json_root.names().getString(i)).toString());

                            }

                            Collections.sort(current_paper,String.CASE_INSENSITIVE_ORDER);

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(Dashboard.this, android.R.layout.simple_spinner_dropdown_item, current_paper);

                                    paperList.setAdapter(adopter);
                                    getReport.setEnabled(true);
                                    paperList.setEnabled(true);

                                }
                            });

                            break;

                        case 3:

                            paperArray = json.getJSONArray("data");


                             runOnUiThread(new Runnable() {

                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
                                    builder.setTitle("Exam Notification");
                                    builder.setCancelable(false);
                                    builder.setMessage("You check the most recent exams on Exam list");

                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();
                                            startClock();

                                        }
                                    });


                                    AlertDialog alert = builder.create();
                                    alert.show();

                                }
                            });


                            break;
                        case 4:
                            existApp();
                            break;
                        case 5:
                            JSONObject subject_root = json.getJSONObject("data");
                            for(int i = 0; i < subject_root.length(); i++){
                                subList.add(subject_root.get(subject_root.names().getString(i)).toString());

                            }

                            Collections.sort(subList,String.CASE_INSENSITIVE_ORDER);

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(Dashboard.this, android.R.layout.simple_spinner_dropdown_item, subList);

                                    subjectList.setAdapter(adopter);


                                }
                            });
                            Log.d("Subject found","Subject found");
                            break;


                    }




                }else{

                    /*
                    runOnUiThread(new Runnable() {

                        public void run() {

                            Toast.makeText(getApplicationContext(), "Data Not Found!", Toast.LENGTH_SHORT).show();

                        }
                    });
                    */

                 }



            }catch(ClientProtocolException e){


            }catch(IOException e){

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return  null;

        }


    }

    public void onLogout(View v){

        logoutMe();

    }


    public void logoutMe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout TNS");
        builder.setMessage("Are you sure to Exit TNS?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                PostData p = new PostData();
                p.execute("LOGOUT",UID);
                dialog.dismiss();

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onBackPressed() {

        logoutMe();



    }

    public void existApp(){

        Log.e("EXIT", "LogOut");
        finish();

    }
}
