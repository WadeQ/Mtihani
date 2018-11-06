package com.wadektech.mtihanirevise.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.wadektech.mtihanirevise.database.Downloader;
import com.wadektech.mtihanirevise.pojo.Model;
import com.wadektech.mtihanirevise.pdfViewer.ItemDetailActivity;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.viewHolders.PdfViewHolder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfViewHolder>{

    List<Model> users ;
    Context context ;
    String pdf ;

    public PdfAdapter(List<Model> users, Context context) {
        this.users = users;
        this.context = context;
    }

    public PdfAdapter(Context context, ArrayList<Model> users) {

    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View pdfView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item , parent , false);
        return new PdfViewHolder(pdfView);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        holder.mSubjectName.setText(users.get(position).getTitle());
        holder.mSubjectName.setText(users.get(position).getPdf());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    public void searchFilter(ArrayList<Model> listItems){
        //re-initialize the variable ArrayList
        users = new ArrayList<>();
        users.addAll(listItems);
        //refresh adapter since we implemented changes
        notifyDataSetChanged();
    }
    ////open activity
    private void openDetailActivity(String...details)
    {
        Intent i=new Intent(context,ItemDetailActivity.class);
        i.putExtra("NAME_KEY",details[0]);
        i.putExtra("PDF_KEY",details[1]);

        //c.startActivity(i);

        String extStorageDirectory = Environment.getExternalStorageDirectory()
                .toString();
        File folder = new File(extStorageDirectory, "pdf");
        folder.mkdir();
        File file = new File(folder, "Read.pdf");
        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Downloader.DownloadFile(pdf, file);
        File newFile = new File(Environment.getExternalStorageDirectory()+"/Mypdf/Read.pdf");

        //PackageManager packageManager = ItemListActivity.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);

        //pdfView.fromUri(uri);
        intent.setDataAndType(uri, "application/pdf");
        context.startActivity(intent);
    }

    private void showPdf()
    {

    }
}
