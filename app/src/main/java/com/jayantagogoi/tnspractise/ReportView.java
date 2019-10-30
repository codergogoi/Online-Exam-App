package com.jayantagogoi.tnspractise;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReportView extends Activity {

    String UID = "";
    String STDNAME = "";
    String paperID = "";
    JSONArray reportData;
    ImageView reportImg;
    Bitmap bitmap;

    TextView txtRPaperId,txtRSubject,txtRExamDate,txtRTotalMarks,txtRObtainMarks,txtRPercentage;
    private final String baseURL = "http://testnscore.com/api/index.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        SharedPreferences pref = getSharedPreferences("USER_INFO",MODE_PRIVATE);
        this.UID =  pref.getString("UID",null);
        this.STDNAME = pref.getString("NAME",null);
        this.paperID = pref.getString("PID", null);

        this.txtRPaperId = (TextView)findViewById(R.id.txtRPaperId);
        this.txtRSubject = (TextView)findViewById(R.id.txtRSubject);
        this.txtRExamDate = (TextView)findViewById(R.id.txtRExamDate);
        this.txtRTotalMarks = (TextView)findViewById(R.id.txtRTotalScore);
        this.txtRObtainMarks = (TextView)findViewById(R.id.txtRObtainScore);
        this.txtRPercentage = (TextView)findViewById(R.id.txtRPercentage);
        this.reportImg = (ImageView)findViewById(R.id.imgRreport);
        TextView stdName = (TextView)findViewById(R.id.txtRStdName);
        stdName.setText("Hi "+ STDNAME +"\nYour Report Details\n--------------------\n");
        viewCurrentReport();

    }

    public void viewCurrentReport(){

        PostData p = new PostData();
        p.execute("DB_VIEW_REPORT",this.UID,paperID);
    }

    public void setreportData(){

        LoadImage li = new LoadImage();

        String imgUrl = "";

        try {

            for (int i = 0; i < reportData.length(); i++) {

                JSONObject jobj = reportData.getJSONObject(i);

                txtRPaperId.setText("Paper ID : "+jobj.getString("paper_id"));
                txtRSubject.setText("Subject :\t"+jobj.getString("subject"));
                txtRExamDate.setText("Exam Date :\t"+jobj.getString("date"));
                txtRTotalMarks.setText("Total Marks :\t"+jobj.getString("total"));
                txtRObtainMarks.setText("Your Score :\t"+jobj.getString("obtain"));
                txtRPercentage.setText("Percentage :\t"+jobj.getString("percentage")+"%");
                imgUrl = jobj.getString("img");

            }

            if(!imgUrl.equals("")){
                String imageURL = imgUrl.replace(" ","%20");
                li.execute(imageURL);
            }

        }catch (JSONException ex){

            Log.e("EX:", "" + ex);
        }

    }

    private  class PostData extends AsyncTask<String, Void, Void> {


        protected Void doInBackground(String ...params){

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(baseURL);

            try{

                String cmd,uid,pid = "";

                cmd = params[0];
                uid = params[1];
                pid = params[2];




                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                // Generic
                nameValuePairs.add(new BasicNameValuePair("uid",uid));
                nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                nameValuePairs.add(new BasicNameValuePair("cmd", cmd));


              if(cmd.equals("DB_VIEW_REPORT")) {
                     nameValuePairs.add(new BasicNameValuePair("paper_id", pid));
                 }



                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute response
                HttpResponse response = httpClient.execute(httpPost);

                String responseText = null;

                responseText = EntityUtils.toString(response.getEntity());

                // Log.d("API", responseText);
                JSONObject json = new JSONObject(responseText);

                String status = json.getString("status");
                Log.d("RESPONS", ""+json.toString());


                if(status.equals("200")){

                    reportData = json.getJSONArray("data");

                    // get report data
                    runOnUiThread(new Runnable() {

                        public void run() {

                            setreportData();
                           // Toast.makeText(getApplicationContext(), "PDF file is not authorised for Mobile platform!", Toast.LENGTH_SHORT).show();

                        }
                    });





                }else{

                    runOnUiThread(new Runnable() {

                        public void run() {

                            Toast.makeText(getApplicationContext(), "Data Not Found!", Toast.LENGTH_SHORT).show();

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


    private class LoadImage extends AsyncTask<String, String, Bitmap>{


        @Override
        protected Bitmap doInBackground(String... params) {

            try{

                bitmap = BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());

            }catch(Exception ex){
                ex.printStackTrace();
            }

            return  bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(bitmap != null){
                reportImg.setImageBitmap(bitmap);
            }
        }
    }



}
