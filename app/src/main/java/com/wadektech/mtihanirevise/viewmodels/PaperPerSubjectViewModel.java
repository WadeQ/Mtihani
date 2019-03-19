package com.wadektech.mtihanirevise.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.wadektech.mtihanirevise.pojo.SinglePDF;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;

import java.util.List;

public class PaperPerSubjectViewModel extends ViewModel {
    private MtihaniRepository mRepository;

    public PaperPerSubjectViewModel() {
        mRepository=MtihaniRepository.getInstance();
    }
    public LiveData<String> getPdfsDownloadResponse(){
        return mRepository.getPdfsDownloadResponse();
    }
    public LiveData<List<SinglePDF>> getPdfPerCategoryResponse(){
        return mRepository.getPdfPerCategoryResponse();
    }

    public void downloadPDFsPerCategory(String category){
        mRepository.downloadPDFPerCategory(category);
    }
    public void downloadPDF(String fileName){
        mRepository.downloadPDF(fileName);
    }
    public LiveData<String> getSinglePDFDownloadResponse(){
        return mRepository.getSinglePDFDownloadResponse();
    }
    public LiveData<Integer> getProgressUpdate(){
        return mRepository.getProgressUpdate();
    }
}
