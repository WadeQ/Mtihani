package com.wadektech.mtihani.core.repository

import android.annotation.SuppressLint
import android.util.Log
import com.wadektech.mtihani.app.MtihaniRevise.Companion.app
import com.wadektech.mtihani.core.SingleLiveEvent
import com.wadektech.mtihani.pdf.domain.pojo.SinglePDF
import com.wadektech.mtihani.core.InjectorUtils
import timber.log.Timber
import com.google.firebase.storage.FirebaseStorage
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.firebaseDataSource.MessagesBoundaryCallback
import androidx.paging.LivePagedListBuilder
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.*
import com.wadektech.mtihani.core.MtihaniAppExecutors
import com.google.firebase.storage.FileDownloadTask
import com.wadektech.mtihani.chat.data.localDatasource.MtihaniDatabase
import com.wadektech.mtihani.core.Constants
import com.wadektech.mtihani.chat.data.firebaseDataSource.UsersBoundaryCallback
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.data.localDatasource.room.User
import java.io.File
import java.lang.Exception
import java.util.*

class MtihaniRepository {
    private var adminPassword: SingleLiveEvent<String>? = null
    var uploadResponse: SingleLiveEvent<String>? = null
        get() = if (field != null) {
            field
        } else {
            field = InjectorUtils.provideSingleLiveEvent()
            field
        }
        private set
    private var pdfPerCategoryResponse: SingleLiveEvent<List<SinglePDF>>? = null
    private var pdfsDownloadResponse: SingleLiveEvent<String>? = null
    private var singlePDFDownloadResponse: SingleLiveEvent<String>? = null
    private var progressUpdate: SingleLiveEvent<Int>? = null
    val adminPasswordResponse: SingleLiveEvent<String>?
        get() = if (adminPassword != null) {
            adminPassword
        } else {
            adminPassword = InjectorUtils.provideSingleLiveEvent()
            adminPassword
        }

    fun getSinglePDFDownloadResponse(): SingleLiveEvent<String>? {
        return if (singlePDFDownloadResponse != null) {
            singlePDFDownloadResponse
        } else {
            singlePDFDownloadResponse = InjectorUtils.provideSingleLiveEvent()
            singlePDFDownloadResponse
        }
    }

    fun getProgressUpdate(): SingleLiveEvent<Int>? {
        return if (progressUpdate != null) {
            progressUpdate
        } else {
            progressUpdate = InjectorUtils.provideIntSingleLiveEvent()
            progressUpdate
        }
    }

    fun getPdfPerCategoryResponse(): SingleLiveEvent<List<SinglePDF>>? {
        return if (pdfPerCategoryResponse != null) {
            pdfPerCategoryResponse
        } else {
            pdfPerCategoryResponse = InjectorUtils.provideListSingleLiveEvent()
            pdfPerCategoryResponse
        }
    }

    fun getAdminPassword() {
        adminPassword = InjectorUtils.provideSingleLiveEvent()
        val db = FirebaseFirestore.getInstance()
        val password = db.collection("admin_password")
        password.document("password_id")
            .get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                if (snapshot.exists()) {
                    adminPassword = if (snapshot["password"] != null) {
                        adminPassword?.value = snapshot["password"].toString()
                        null
                    } else {
                        adminPassword?.value = "password is empty"
                        null
                    }
                }
            }
            .addOnFailureListener {
                    e: Exception? ->
                adminPassword?.setValue("Unable to authenticate, please try again")
            }
    }

    fun getPdfsDownloadResponse(): SingleLiveEvent<String>? {
        return if (pdfsDownloadResponse != null) {
            pdfsDownloadResponse
        } else {
            pdfsDownloadResponse = InjectorUtils.provideSingleLiveEvent()
            pdfsDownloadResponse
        }
    }

    @SuppressLint("TimberArgCount")
    private fun savePDFDownloadUrlInDb(pdfUrl: String, category: String, fileName: String) {
        val db = FirebaseFirestore.getInstance()
        val map = InjectorUtils.provideStringHashMap()
        map["pdfUrl"] = pdfUrl
        map["category"] = category
        map["fileName"] = fileName
        val ref = db.collection("PDFs")
        ref.document()
            .set(map)
            .addOnSuccessListener { aVoid: Void? -> Timber.tag(TAG).d("token sent to server!") }
            .addOnFailureListener { e: Exception ->
                Timber.tag(TAG).d("failed to send token to server: %s%s", e.toString())
            }
    }

    fun downloadPDFPerCategory(category: String?) {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("PDFs")
        ref.whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                if (!snapshot.isEmpty) {
                    pdfsDownloadResponse!!.value = "loaded"
                    pdfPerCategoryResponse!!.setValue(snapshot.toObjects(SinglePDF::class.java))
                } else {
                    pdfsDownloadResponse!!.setValue("empty")
                }
            }
            .addOnFailureListener { e: Exception -> pdfsDownloadResponse!!.setValue(e.toString()) }
    }

    fun downloadPDF(fileName: String?) {
        if (progressUpdate == null) progressUpdate = InjectorUtils.provideIntSingleLiveEvent()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference("PDF_Files")
        val islandRef = storageRef.child(fileName!!)
        val rootPath = File(
            Objects.requireNonNull(app)
                ?.applicationContext
                ?.getExternalFilesDir(null)
                ?.absolutePath , "Mtihani"
        )
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }
        val localFile = File(rootPath, fileName)
        islandRef.getFile(localFile)
            .addOnProgressListener { taskSnapshot: FileDownloadTask.TaskSnapshot ->
                val count =
                    (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressUpdate!!.setValue(count)
            }
            .addOnSuccessListener {
                //  updateDb(timestamp,localFile.toString(),position);
                singlePDFDownloadResponse!!.setValue("success")
            }.addOnFailureListener { exception: Exception ->
                // Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                singlePDFDownloadResponse!!.value = "An error occurred."
                Timber.d("Error downloading pdf: %s", exception.cause.toString())
            }
    }

    companion object {
        private val LOCK = Any()
        private var sInstance: MtihaniRepository? = null
        const val TAG = "MtihaniRepository"

        @JvmStatic
        @get:Synchronized
        val instance: MtihaniRepository?
            get() {
                if (sInstance == null) {
                    synchronized(LOCK) { sInstance = MtihaniRepository() }
                }
                return sInstance
            }

    }
}