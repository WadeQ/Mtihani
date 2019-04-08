package com.wadektech.mtihanirevise.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;

import com.wadektech.mtihanirevise.pojo.PDFObject;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;

import java.util.List;

public class AdminPanelViewModel extends ViewModel {
    private MtihaniRepository mRepository;

    public AdminPanelViewModel() {
        mRepository = MtihaniRepository.getInstance();
    }

    public void getAdminPassword() {
        mRepository.getAdminPassword();
    }

    public LiveData<String> getAdminPasswordResponse() {
        return mRepository.getAdminPasswordResponse();
    }

    public void uploadPDF(Uri pdfUri, String category,String fileName) {
        mRepository.uploadPDFFile(pdfUri,category,fileName);
    }
    public void uploadMultiplePDFs(List<PDFObject> pdfObjects, String category){
        mRepository.uploadPDFs(pdfObjects,category);
    }
    public LiveData<String> getUploadProgress(){
        return mRepository.getUploadResponse();
    }
}
