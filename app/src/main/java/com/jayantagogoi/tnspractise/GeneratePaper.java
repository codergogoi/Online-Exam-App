package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class GeneratePaper extends Activity {

    String UID = "";
    String STDNAME = "";
    String AUTHORSTATE = "";
    TextView txtUname;
    String STD = "";
    String TESTCOUNT = "";

    Spinner spn_AuthoList,spn_SubjectList;
    ListView lst_ChapterList;
    Button btnGenerateQp;
    private boolean isGenerated;

    ArrayList<String> chapterListed = new ArrayList<String>();
    private final String baseURL = "http://testnscore.com/api/index.php";

    ProgressDialog pDialog;

     int numbersOfQsn = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_paper);

        txtUname = (TextView)findViewById(R.id.std_name);

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);
        this.AUTHORSTATE = pref.getString("AUTHOR","0");
        this.STD = pref.getString("STD",null);
        this.TESTCOUNT = pref.getString("TEST_COUNT","20");


        this.txtUname = (TextView)findViewById(R.id.std_name);
        this.txtUname.setText("Welcome, " + this.STDNAME);

        spn_AuthoList = (Spinner)findViewById(R.id.spin_author);
        lst_ChapterList = (ListView)findViewById(R.id.lst_chapterList);
        spn_SubjectList = (Spinner)findViewById(R.id.spin_subjectlist);
        lst_ChapterList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btnGenerateQp = (Button)findViewById(R.id.btn_generate);
        loadSubjects();


        this.spn_SubjectList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fetchChapter(0);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.spn_AuthoList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fetchChapter(1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.lst_ChapterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                validateSubmit();
            }
        });

    Log.d("STD :",this.STD+" Test Count :"+ this.TESTCOUNT);

    }

    public void showProgress(){

        pDialog = new ProgressDialog(this); // this = YourActivity
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Generating your paper please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
    }

    public void validateSubmit(){



        int count = 0;
        SparseBooleanArray checked = this.lst_ChapterList.getCheckedItemPositions();
        int size = checked.size();
        // Log.d("SELECTED", ""+size);

        for(int i = 0; i < size; i++){

            if(checked.valueAt(i) == true){

                  //  Tag tag = (Tag)this.lst_ChapterList.getItemAtPosition(checked.keyAt(i));
                 count++;
            }

        }

        if(count < 6 && count > 1){

            this.btnGenerateQp.setEnabled(true);
        }else{
            this.btnGenerateQp.setEnabled(false);
            Toast.makeText(this,"Please select Min 2 and Max 5 Chapters only",Toast.LENGTH_SHORT).show();

        }


    }

    public void getAllSelectedChapter(View view){


        btnGenerateQp.setEnabled(false);


        if(this.STD.equals("X") || this.STD.equals("IX")){


            final CharSequence[] qNumbers = {"20","30"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Number of Question");

            builder.setSingleChoiceItems(qNumbers, numbersOfQsn, new DialogInterface.OnClickListener(){

                public void onClick(DialogInterface dialog, int item){

                    switch(item){

                        case 0:
                            Log.d("selected","20 Qsn");
                            numbersOfQsn = 20;
                            break;
                        case 1:
                            numbersOfQsn = 30;
                            Log.d("Selected 30", "30 Qsn");
                            break;
                    }

                    generateQuestionPaper();
                    dialog.dismiss();

                }

            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }else{
            numbersOfQsn = 20;
            generateQuestionPaper();

        }


    }


    public void generateQuestionPaper() {


        Log.d("Numbers of Questions :", "" + numbersOfQsn);


        if (!isGenerated) {

            isGenerated = true;
            if (isNetworkAvailable()) {

                showProgress();


                SparseBooleanArray checked = this.lst_ChapterList.getCheckedItemPositions();
                int size = checked.size();

                for (int i = 0; i < size; i++) {

                    if (checked.valueAt(i) == true) {

                        String items = this.lst_ChapterList.getItemAtPosition(checked.keyAt(i)).toString();
                        this.chapterListed.add(items);
                    }

                }

                PostData p = new PostData();
                p.execute("GENERATE_QP", this.UID, this.spn_SubjectList.getSelectedItem().toString(),""+this.numbersOfQsn);

            } else {


                AlertDialog.Builder builder = new AlertDialog.Builder(GeneratePaper.this);
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
    }


    public void loadSubjects(){

        PostData p = new PostData();
        p.execute("DB_SUBJECT_LIST", this.UID);


    }

    public void fetchChapter(int requestType){

        this.btnGenerateQp.setEnabled(false);

        PostData p = new PostData();

        if(requestType  > 1){
                p.execute("DB_GET_AUTHOR", this.UID, spn_SubjectList.getSelectedItem().toString());

        }else {
            if (this.AUTHORSTATE.equals("0")) {
                // get chapter list
                p.execute("DB_GET_CHAPTER", this.UID, spn_SubjectList.getSelectedItem().toString());
            } else {
                p.execute("DB_GET_AUTHOR", this.UID, spn_SubjectList.getSelectedItem().toString());
            }

        }

    }


    private boolean isNetworkAvailable(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

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

                final ArrayList<String> current_chapter = new ArrayList<String>();
                final ArrayList<String> current_author = new ArrayList<String>();



                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                final ArrayList<String> subList = new ArrayList<String>();

                // Generic
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", cmd));



                if(cmd.equals("DB_GET_CHAPTER")){

                    Log.e("REQUEST","Sending GET Chapter");
                    subject = params[2];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));
                   // nameValuePairs.add(new BasicNameValuePair("au", ""));
                    cmdIndex = 1;

                }else if(cmd.equals("DB_GET_AUTHOR")){
                    Log.e("REQUEST","Sending GET author");
                    subject = params[2];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));
                    cmdIndex = 2;
                }else if(cmd.equals("GENERATE_QP")){

                    subject = params[2];
                    nameValuePairs.add(new BasicNameValuePair("subject", subject));
                    String numbersOfQuestions = params[3];

                    for(String ch : chapterListed){

                        nameValuePairs.add(new BasicNameValuePair("ch[]",ch));
                        nameValuePairs.add(new BasicNameValuePair("qnumbers",numbersOfQuestions));
                    }

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

                    Log.d("UID=>", json_root.toString());

                    switch(cmdIndex){


                        case 0:

                            break;
                        case 1:
                            for(int i = 0; i < json_root.length(); i++){
                                // Log.e("JS", "key =>"+ json_root.names().getString(i));
                                current_chapter.add(json_root.get(json_root.names().getString(i)).toString());

                            }

                            Collections.sort(current_chapter, String.CASE_INSENSITIVE_ORDER);

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(GeneratePaper.this, android.R.layout.simple_list_item_multiple_choice, current_chapter);

                                    lst_ChapterList.setAdapter(adopter);
                                    lst_ChapterList.setEnabled(true);

                                }
                            });


                            break;
                        case 2:


                            for(int i = 0; i < json_root.length(); i++){
                                //Log.e("JS", "key =>"+ json_root.names().getString(i));
                                current_author.add(json_root.get(json_root.names().getString(i)).toString());

                            }

                            Collections.sort(current_author,String.CASE_INSENSITIVE_ORDER);
                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(GeneratePaper.this, android.R.layout.simple_spinner_dropdown_item, current_author);

                                    spn_AuthoList.setAdapter(adopter);
                                    spn_AuthoList.setEnabled(true);

                                }
                            });

                            break;
                        case 3:

                            if(pDialog != null)
                                pDialog.dismiss();


                            runOnUiThread(new Runnable() {

                                public void run() {

                                    Toast.makeText(getApplicationContext(), "Paper Generated Successfully! \n Ready for Test", Toast.LENGTH_SHORT).show();

                                    Intent dbIntent = new Intent(GeneratePaper.this, SchedulePaper.class);
                                    startActivity(dbIntent);
                                }
                            });



                            break;
                        case 5:
                            JSONObject subject_root = json.getJSONObject("data");
                            for(int i = 0; i < subject_root.length(); i++){
                                subList.add(subject_root.get(subject_root.names().getString(i)).toString());

                            }

                            Collections.sort(subList,String.CASE_INSENSITIVE_ORDER);

                            runOnUiThread(new Runnable() {

                                public void run() {

                                    ArrayAdapter<String> adopter = new ArrayAdapter<String>(GeneratePaper.this, android.R.layout.simple_spinner_dropdown_item, subList);

                                    spn_SubjectList.setAdapter(adopter);


                                }
                            });
                            Log.d("Subject found","Subject found");
                            break;


                    }



                }else{

                    if(status.equals("202")) {

                        JSONObject json_found = json.getJSONObject("data");

                        final String message = json_found.getString("response");

                        runOnUiThread(new Runnable() {

                            public void run() {

                                AlertDialog.Builder builder = new AlertDialog.Builder(GeneratePaper.this);
                                builder.setTitle("Attention");
                                builder.setMessage("" + message);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        Intent dbIntent = new Intent(GeneratePaper.this, SchedulePaper.class);
                                        startActivity(dbIntent);

                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.show();

                              //  Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();

                            }
                        });


                    }else {

                        runOnUiThread(new Runnable() {

                            public void run() {

                              //  Toast.makeText(getApplicationContext(), "Data Not Found!", Toast.LENGTH_SHORT).show();

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
