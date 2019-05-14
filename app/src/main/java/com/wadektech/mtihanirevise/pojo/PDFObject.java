package com.wadektech.mtihanirevise.pojo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class PDFObject implements Parcelable {
    private String fileName;
    private Uri pdfUri;

    public PDFObject(String fileName, Uri pdfUri) {
        this.fileName = fileName;
        this.pdfUri = pdfUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public Uri getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(Uri pdfUri) {
        this.pdfUri = pdfUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeParcelable(this.pdfUri, flags);
    }

    protected PDFObject(Parcel in) {
        this.fileName = in.readString();
        this.pdfUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<PDFObject> CREATOR = new Creator<PDFObject>() {
        @Override
        public PDFObject createFromParcel(Parcel source) {
            return new PDFObject (source);
        }

        @Override
        public PDFObject[] newArray(int size) {
            return new PDFObject[size];
        }
    };
}
