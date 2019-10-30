package com.jayantagogoi.tnspractise;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class QframeFragment extends Fragment {

    Bitmap bitmap;
    ImageView img;
    EditText txtAns;
    EditText txtSl;
    String QPREF_SL;


    public  static final QframeFragment newInsance(String ...params){

        QframeFragment f = new QframeFragment();
        Bundle b = new Bundle(1);

        b.putString("QTYPE", params[0]);
        b.putString("QDETAILS", params[1]);
        b.putString("FIB", params[2]);
        b.putString("OP1", params[3]);
        b.putString("OP2", params[4]);
        b.putString("OP3", params[5]);
        b.putString("OP4", params[6]);
        b.putString("IMGURL", params[7]);
        b.putString("QSL",params[8]);
        b.putString("QPREF_SL", params[9]);

        f.setArguments(b);
        return f;

    }


    public QframeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       // return inflater.inflate(R.layout.fragment_qframe, container, false);

        View v = inflater.inflate(R.layout.fragment_qframe,container,false);

        LinearLayout mcqLayout = (LinearLayout)v.findViewById(R.id.e_mcq);
        LinearLayout fibLayout = (LinearLayout)v.findViewById(R.id.e_fib);
        LinearLayout leftLayout = (LinearLayout)v.findViewById(R.id.e_options); //
        LinearLayout rightLayout = (LinearLayout)v.findViewById(R.id.e_imgs);//

        TextView qdetails = (TextView)v.findViewById(R.id.e_qdetails);
        final EditText txtFib = (EditText)v.findViewById(R.id.e_fibText);
        final RadioButton op1 = (RadioButton)v.findViewById(R.id.op_1);
        final RadioButton op2 = (RadioButton)v.findViewById(R.id.op_2);
        final RadioButton op3 = (RadioButton)v.findViewById(R.id.op_3);
        final RadioButton op4 = (RadioButton)v.findViewById(R.id.op_4);
        img = (ImageView)v.findViewById(R.id.e_imgDetails);
        txtSl = (EditText)v.findViewById(R.id.e_qsl);
        txtAns = (EditText)v.findViewById(R.id.e_ans);

        RadioGroup rg = (RadioGroup)v.findViewById(R.id.e_opSelect);



        final String q_Type = getArguments().getString("QTYPE");
        String q_Detail = getArguments().getString("QDETAILS");
       // String q_fib = getArguments().getString("FIB");
        final String q_op1 = getArguments().getString("OP1");
        final String q_op2 = getArguments().getString("OP2");
        final String q_op3 = getArguments().getString("OP3");
        final String q_op4 = getArguments().getString("OP4");
        String q_imgURL = getArguments().getString("IMGURL");
        String q_sl = getArguments().getString("QSL");
        QPREF_SL  = getArguments().getString("QPREF_SL");

      //  Log.d("IMG",""+q_imgURL);
        //qdetails.setText(q_imgURL);

        qdetails.setText(Html.fromHtml(q_Detail));
        op1.setText(Html.fromHtml(q_op1));
        op2.setText(Html.fromHtml(q_op2));
        op3.setText(Html.fromHtml(q_op3));
        op4.setText(Html.fromHtml(q_op4));
        txtSl.setText(q_sl);

        LoadImage li = new LoadImage();

        if(q_Type.equals("FIB")){
            img.setVisibility(View.GONE);
            mcqLayout.setVisibility(View.GONE);
            fibLayout.setVisibility(View.VISIBLE);

            op1.setVisibility(View.INVISIBLE);
            op2.setVisibility(View.INVISIBLE);
            op3.setVisibility(View.INVISIBLE);
            op4.setVisibility(View.INVISIBLE);

        }else if(q_Type.equals("MCQ")){
            img.setVisibility(View.GONE);
              rightLayout.setVisibility(View.GONE);
            mcqLayout.setVisibility(View.VISIBLE);
            fibLayout.setVisibility(View.GONE);

            op1.setVisibility(View.VISIBLE);
            op2.setVisibility(View.VISIBLE);
            op3.setVisibility(View.VISIBLE);
            op4.setVisibility(View.VISIBLE);

        }else  if(q_Type.equals("TF")){
            img.setVisibility(View.GONE);
            rightLayout.setVisibility(View.GONE);
            mcqLayout.setVisibility(View.VISIBLE);
            fibLayout.setVisibility(View.GONE);

            op1.setText(Html.fromHtml(q_op1));
            op2.setText(Html.fromHtml(q_op2));
            op3.setVisibility(View.INVISIBLE);
            op4.setVisibility(View.INVISIBLE);

            //leftLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            //rightLayout.setVisibility(View.GONE);

        }else if(q_Type.equals("FIBP")){
            img.setVisibility(View.VISIBLE);
            fibLayout.setVisibility(View.VISIBLE);
            mcqLayout.setVisibility(View.GONE);

           // img.setImageDrawable();
            if(!q_imgURL.equals("")){
                li.execute(q_imgURL);
            }

        }else if(q_Type.equals("MCQP")){
            img.setVisibility(View.VISIBLE);
            mcqLayout.setVisibility(View.VISIBLE);
            fibLayout.setVisibility(View.GONE);

            op1.setVisibility(View.VISIBLE);
            op2.setVisibility(View.VISIBLE);
            op3.setVisibility(View.VISIBLE);
            op4.setVisibility(View.VISIBLE);


            if(!q_imgURL.equals("")){
                li.execute(q_imgURL);
            }

        }else if(q_Type.equals("TFP")){
            img.setVisibility(View.VISIBLE);
            mcqLayout.setVisibility(View.VISIBLE);
            fibLayout.setVisibility(View.GONE);

             if(!q_imgURL.equals("")){
                li.execute(q_imgURL);
            }

            op1.setText(Html.fromHtml(q_op1));
            op2.setText(Html.fromHtml(q_op2));
            op3.setVisibility(View.INVISIBLE);
            op4.setVisibility(View.INVISIBLE);
        }


        // FIB

        txtFib.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               String currentAns = "A|"+txtSl.getText()+"|"+ q_Type +"|"+txtFib.getText();
               //txtAns.setText(currentAns);
                String ansx = ""+txtFib.getText();
                setCurrentAns(QPREF_SL, currentAns,ansx);

            }
        });

        // MCQ


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton currentButton = (RadioButton)group.findViewById(checkedId);

                boolean isChecked = currentButton.isChecked();

                if(isChecked){

                    String currentOption = "";

                    if(q_Type.equals("TF") || q_Type.equals("TFP")){

                        currentOption = currentButton.getText().toString();

                    }else {

                        if (op1.isChecked()) {
                            currentOption = "A";
                        } else if (op2.isChecked()) {
                            currentOption = "B";
                        } else if (op3.isChecked()) {
                            currentOption = "C";
                        } else if (op4.isChecked()) {
                            currentOption = "D";
                        }

                    }

                    String currentAns = "A|"+txtSl.getText()+"|"+ q_Type+"|"+currentOption;
                    String ansx = ""+currentOption;

                    setCurrentAns(QPREF_SL,currentAns,ansx);
                }

            }
        });


        setCurrentAns(QPREF_SL,"A|"+txtSl.getText()+"|"+q_Type,"");


        return v;

    }

    public void setCurrentAns(String key, String Value, String ansx){

        SharedPreferences.Editor pref = getActivity().getSharedPreferences("USER_INFO", Context.MODE_PRIVATE).edit();
        // SharedPreferences pref = getActivity().getPreferences(0);
       // SharedPreferences.Editor edit = pref.edit();
        pref.putString(key,Value);
        pref.putString(key+"a",ansx);

        pref.commit();


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
                img.setImageBitmap(bitmap);
            }
        }
    }

 }
