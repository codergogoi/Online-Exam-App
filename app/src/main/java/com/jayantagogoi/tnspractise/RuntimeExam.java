package com.jayantagogoi.tnspractise;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RuntimeExam extends FragmentActivity {


    MyPageAdopter pageAdopter;

    ViewPager pager;

    String paperID,UID,STDNAME,EXAMTYPE = "";
    JSONArray questionData;
    LinearLayout qCode;
    GridView qView;
    ArrayAdapter<String> adopter;

    TextView txtTimer;
    private long examTime;
    private final String FORMAT = "%02d:%02d:%02d";
    private final String baseURL = "http://testnscore.com/api/index.php";

    private boolean isExamStarted;
    ProgressDialog pDialog;
    private CountDownTimer cDownTimer;
    private long  timeToGo;
    private boolean isTimerStarted;
    private  LinearLayout bg1,bg2,bg3;

    private boolean isDataSubmitted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtime_exam);
        isDataSubmitted= true;

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);
        this.paperID = pref.getString("PID",null);
        this.EXAMTYPE = pref.getString("EXTYPE",null);
        qCode = (LinearLayout)findViewById(R.id.q_code);
        qView = (GridView)findViewById(R.id.q_grid);
        this.txtTimer = (TextView)findViewById(R.id.timer_info);
        this.examTime = 20; // set this value from server
        bg1 = (LinearLayout)findViewById(R.id.exam_bg1);
        bg2 = (LinearLayout)findViewById(R.id.exam_bg2);
        bg3 = (LinearLayout)findViewById(R.id.exam_bg3);



        //Toast.makeText(this,"PID is"+ this.paperID , Toast.LENGTH_LONG).show();


        if(isNetworkAvailable() && !isExamStarted) {

                isExamStarted = true;
                startExamNow();


        }else{

            AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
            builder.setTitle("Internet Not Available");
            builder.setMessage("You have not Internet connection! Please check it!");
            builder.setCancelable(false);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                    dialog.dismiss();


                }
            });


            AlertDialog alert = builder.create();
            alert.show();
        }


    }


    private boolean isNetworkAvailable(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


    public void tickCountDown(){


        cDownTimer = new CountDownTimer(timeToGo,1000){


            @Override
            public void onTick(long millisUntilFinished) {

                timeToGo = millisUntilFinished;


                String timeFormat = String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                txtTimer.setText(""+ timeFormat);

            }

            @Override
            public void onFinish() {
                txtTimer.setText("Opps! Paper Submitted automatically!");
                endExam();

            }
        }.start();

    }

    public void pause(View v){

        if(isTimerStarted){
            bg1.setVisibility(View.GONE);
            bg2.setVisibility(View.GONE);
            bg3.setVisibility(View.VISIBLE);
            isTimerStarted = false;
            cDownTimer.cancel();
        }else{
            bg1.setVisibility(View.VISIBLE);
            bg2.setVisibility(View.VISIBLE);
            bg3.setVisibility(View.GONE);
            isTimerStarted = true;
            tickCountDown();
        }
     }


    public void createColourCode(){

        final ArrayList<String> q_no = new ArrayList<String>();

        int qNumber = 0;
        for(int i = 0; i < questionData.length(); i++){

            qNumber++;
            q_no.add(""+qNumber);

        }

       // adopter = new ArrayAdapter<String>(RuntimeExam.this, android.R.layout.simple_gallery_item, q_no);

        qView.setAdapter(new ArrayAdapter<String>(RuntimeExam.this, android.R.layout.simple_list_item_1, q_no) {


            /*
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
               // TextView tv = (TextView) view;
               // tv.setTextColor(Color.BLUE);

                return tv;
            }

            */
        });


        qView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //v.setBackgroundColor(Color.RED);
                // Log.d("ID",""+position);
                gotoCurrent(position);
            }


        });









    }





    @Override
    public void onBackPressed() {

        if(isDataSubmitted) {

            AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
            builder.setTitle("Quit Exam");
            builder.setMessage("Are you sure to quit exam! \nOnce quit you can't appear This paper any more!");

            builder.setPositiveButton("Finish Exam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    finish();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }



    }

    @Override
    public void onResume() {
        super.onResume();

/*
        AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
        builder.setTitle("On resume!");
        builder.setCancelable(false);
        builder.setMessage("Your exam has been submitted by System!");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });


        AlertDialog alert = builder.create();
        alert.show();
    */

        /*
        MyApplication myApp = (MyApplication)this.getApplication();
        if (myApp.wasInBackground)
        {
            //Do specific came-here-from-background code
        }

        myApp.stopActivityTransitionTimer();
        */
    }

    @Override
    public void onPause()
    {
         
        AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
        builder.setTitle("Notification!");
        builder.setCancelable(false);
        builder.setMessage("During the exam Please don't Minimize Application! It may interrupt exam progress!");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


            }
        });


        AlertDialog alert = builder.create();
        alert.show();
        super.onPause();

    }




    public void setupExamView(){

        List<Fragment> fragments = getFragments();
        pageAdopter = new MyPageAdopter(getSupportFragmentManager(),fragments);

        pager = (ViewPager) findViewById(R.id.question_viewer);
        pager.setAdapter(pageAdopter);

        createColourCode();

        pager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return false; //This is important, if you return TRUE the action of swipe will not take place.
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:

                        //Log.d("HII","index");
                        checkAttempted();

                        break;

                }
                return false;
            }
        });
    }

    public void startExamNow(){

        PostData p = new PostData();
        p.execute("START_EXAM", this.UID, this.paperID);
    }

    public void finishedExam(View v) {

        //endExam();

        confirmExam();
        /*

        if(isNetworkAvailable()) {



        }else{

            AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
            builder.setTitle("Internet Not Available");
            builder.setMessage("You have not Internet connection! Please check it!");
            builder.setCancelable(false);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                    dialog.dismiss();


                }
            });


            AlertDialog alert = builder.create();
            alert.show();
        }
        */

     }

    public void confirmExam(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finish Exam");
        builder.setMessage("Are you sure to Finish Exam?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                endExam();
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


    public void endExam(){

        showProgress();
        PostData p = new PostData();
        p.execute("CHECK_QP", this.UID);
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

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                // Generic
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", cmd));


                if(cmd.equals("START_EXAM")){

                    pid = params[2];
                    nameValuePairs.add(new BasicNameValuePair("paper_id", pid));
                    cmdIndex = 1;

                }else if(cmd.equals("DB_PDF_GET")) {
                    pid = params[2];
                    nameValuePairs.add(new BasicNameValuePair("paperid", pid));
                    cmdIndex = 2;
                }else if(cmd.equals("CHECK_PENDING_EXAM")){
                    cmdIndex = 3;

                }else if(cmd.equals("CHECK_QP")){

                    // Finalize Exam
                    runOnUiThread(new Runnable() {

                        public void run() {

                           // Toast.makeText(getApplicationContext(), "Please wait while processing!", Toast.LENGTH_LONG).show();

                        }
                    });

                    SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
                    nameValuePairs.add(new BasicNameValuePair("paper_id", paperID));
                    nameValuePairs.add(new BasicNameValuePair("extype", EXAMTYPE));


                    for (int i = 1; i < (questionData.length() +1); i++) {

                        String value = pref.getString("q"+i,null);

                       // Log.i("ANS","->"+value);
                        nameValuePairs.add(new BasicNameValuePair("ans[]", value));
                    }

                    cmdIndex = 4;
                }

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                String responseText = null;



                responseText = EntityUtils.toString(response.getEntity());

                Log.d("API", responseText);
                JSONObject json = new JSONObject(responseText);

                String status = json.getString("status");

                Log.d("RESPONS", ""+json.toString());



                if(status.equals("200")){



                    switch(cmdIndex){

                        case 0:

                            break;
                        case 1:

                          //  JSONArray jsonArray = json.getJSONArray("data");

                            questionData = json.getJSONArray("data");

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
                                    builder.setTitle("Exam Ready");
                                    builder.setMessage("Your paper is ready");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton("Start Exam", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            setupExamView();
                                            dialog.dismiss();
                                            isTimerStarted = true;
                                            timeToGo = (examTime * 60) * 1000 ;
                                            tickCountDown();



                                        }
                                    });

                                    AlertDialog alert = builder.create();
                                    alert.show();

                                }
                            });




                            break;
                        case 2:
                            // get PDF file
                            runOnUiThread(new Runnable() {

                                public void run() {

                                  //  Toast.makeText(getApplicationContext(), "PDF file is not authorised for Mobile platform!", Toast.LENGTH_SHORT).show();

                                }
                            });

                            break;
                        case 4:
                            // Finalize Exam
                            runOnUiThread(new Runnable() {

                                public void run() {

                                    if(pDialog != null)
                                        pDialog.dismiss();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(RuntimeExam.this);
                                    builder.setTitle("Exam Finished!");
                                    builder.setMessage("Thank you. Your score will be update soon.");
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
                            });

                           // Log.d("RES", json.toString());



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

    public void nextClick(View v){

           setCurrentQ(true, true);
          questionReview();

    }
    public void prevClick(View v){

        setCurrentQ(true, false);
        questionReview();
    }

    public void checkAttempted(){

        int currentIndex = pager.getCurrentItem();
        String Key = "q"+(currentIndex+1)+"a";
        SharedPreferences pref = getSharedPreferences("USER_INFO", MODE_PRIVATE);
        String result =  pref.getString(Key, null);


       // try {

            View nextChild = ((ViewGroup) qView).getChildAt(currentIndex);

            TextView tv = (TextView) nextChild;

            if (result.equals("")) {
                //  qView.setItemChecked(currentIndex,false);
                //             qView.getChildAt(currentIndex).setBackgroundResource(R.drawable.logo);
                if(tv != null){
                    tv.setTextColor(Color.RED);
                    tv.setTypeface(null, Typeface.BOLD_ITALIC);
                    // qView.getChildAt(currentIndex).setBackgroundColor(Color.RED);
                }

            } else {
                if(tv != null) {
                    tv.setTextColor(getResources().getColor(R.color.darkgreen));
                    tv.setTypeface(null, Typeface.BOLD);
                }
                // qView.setItemChecked(currentIndex,true);
                // qView.getChildAt(currentIndex).setBackgroundColor(Color.GREEN);
            }
        //}catch(Exception ex){

          //  Log.d("Exception :",""+ex);
        //}

    }

    public  void questionReview(){


        for(int i = 0; i <  pager.getCurrentItem(); i++ ){


            String Key = "q"+(i+1)+"a";
            SharedPreferences pref = getSharedPreferences("USER_INFO", MODE_PRIVATE);
            String result =  pref.getString(Key, null);

            // try {

            View nextChild = ((ViewGroup) qView).getChildAt(i);

            TextView tv = (TextView) nextChild;

            if (result.equals("") && result.length() < 1) {
                //  qView.setItemChecked(currentIndex,false);
                //             qView.getChildAt(currentIndex).setBackgroundResource(R.drawable.logo);
                if(tv != null){
                    tv.setTextColor(Color.RED);
                    tv.setTypeface(null, Typeface.BOLD_ITALIC);
                    // qView.getChildAt(currentIndex).setBackgroundColor(Color.RED);
                }

            } else {
                if(tv != null) {
                    tv.setTextColor(getResources().getColor(R.color.darkgreen));
                    tv.setTypeface(null, Typeface.BOLD);
                }
                // qView.setItemChecked(currentIndex,true);
                // qView.getChildAt(currentIndex).setBackgroundColor(Color.GREEN);
            }


        }




    }

    public void setCurrentQ(boolean smoothTransitation,boolean NEXT){

        int currentIndex = pager.getCurrentItem();
        checkAttempted();

       // Log.e("ITEM", "" + currentIndex);

        if(NEXT) {
            if (currentIndex < questionData.length()) {


                currentIndex++;
                qView.setSelection(currentIndex);
                pager.setCurrentItem(currentIndex, smoothTransitation);
            }

        }else{

            if (currentIndex > 0) {
                currentIndex--;
                pager.setCurrentItem(currentIndex, smoothTransitation);
            }

        }
    }


    public void gotoCurrent(int index){


                pager.setCurrentItem(index, true);

    }

    public void showProgress(){

        pDialog = new ProgressDialog(this); // this = YourActivity
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Loading. Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
    }




    private List<Fragment> getFragments(){

        List<Fragment> fList = new ArrayList<Fragment>();

        Log.e("COUNT",""+questionData.length());


        try {
            int qno = 0;

            for (int i = 0; i < questionData.length(); i++) {

                qno++;
                JSONObject jobj = questionData.getJSONObject(i);

                String q_details = qno+". "+jobj.getString("q_details");

                String q_type = jobj.getString("q_type");
                String q_ans1 = jobj.getString("q_ans_1");
                String q_ans2 = jobj.getString("q_ans_2");
                String q_ans3 = jobj.getString("q_ans_3");
                String q_ans4 = jobj.getString("q_ans_4");
                String q_img = jobj.getString("q_img");
                String q_sl = jobj.getString("q_sl"); // actual qno
                String q_no = "q"+qno; // pref key
                String tm = jobj.getString("q_time");
                examTime = Long.parseLong(tm);

                String currentAns = "A|"+q_sl+"";
                SharedPreferences.Editor pref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE).edit();

                pref.putString(q_no,currentAns);

                pref.commit();

                if(q_type.equals("TF") || q_type.equals("TFP")) {
                    if (q_ans1.equals("T")) {

                        q_ans1 = "TRUE";
                    }else{
                        q_ans1 = "FALSE";
                    }

                    if (q_ans2.equals("T")) {

                        q_ans2 = "TRUE";
                    }else{
                        q_ans2 = "FALSE";
                    }
                }

                fList.add(QframeFragment.newInsance(q_type, q_details, "", q_ans1, q_ans2, q_ans3, q_ans4, q_img, q_sl, q_no));




            }

            if(EXAMTYPE.equals("1")){
                examTime = 20;
            }

        }catch (JSONException ex){

            Log.e("EX:", ""+ex);
        }



        return fList;
    }




    private  class MyPageAdopter extends FragmentPagerAdapter{

        private List<Fragment> fragments;

        public MyPageAdopter(android.support.v4.app.FragmentManager fm, List<Fragment> fragments){

            super(fm);
            this.fragments = fragments;
        }


        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }


}
