package com.wadektech.mtihanirevise.JSON;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.wadektech.mtihanirevise.adapter.PdfAdapter;
import com.wadektech.mtihanirevise.pojo.Model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONParser extends AsyncTask<Void,Void,Boolean> {
    Context c;
    String jsonData;
    RecyclerView rv_pdf;

    ProgressDialog pd;
    ArrayList<Model> users = new ArrayList<>();

    public JSONParser(Context c, String jsonData, RecyclerView rv_pdf) {
        this.c = c;
        this.jsonData = jsonData;
        this.rv_pdf = rv_pdf;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd=new ProgressDialog(c);
        pd.setTitle("Parse");
        pd.setMessage("Parsing...Please wait");
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return parse();
    }

    @Override
    protected void onPostExecute(Boolean isParsed) {
        super.onPostExecute(isParsed);

        pd.dismiss();
        if(isParsed)
        {
            //BIND
            rv_pdf.setAdapter(new PdfAdapter(c,users));
        }else
        {
            Toast.makeText(c, "Unable To Parse,Check Your Log output", Toast.LENGTH_SHORT).show();
        }

    }

    private Boolean parse()
    {
        try
        {
            JSONArray ja=new JSONArray(jsonData);
            JSONObject jo;

            users.clear();
            Model user;

            for (int i=0;i<ja.length();i++)
            {
                jo=ja.getJSONObject(i);

                String name=jo.getString("name");
                String username=jo.getString("image");


                user=new Model();

                user.setTitle(name);
                user.setPdf(username);


                users.add(user);
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
