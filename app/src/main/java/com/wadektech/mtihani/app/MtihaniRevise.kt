package com.wadektech.mtihani.app

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import timber.log.Timber

class MtihaniRevise : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
        setUpExceptionHandler()
        app = this
        initTheme()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttpDownloader(applicationContext))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)
    }

    companion object {
        var app: MtihaniRevise? = null
            private set
    }

    private fun setUpExceptionHandler() {
        Handler(Looper.getMainLooper()).post {
            while (true){
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    uncaughtException(Looper.getMainLooper().thread, e)
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            uncaughtException(t,e)
        }
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun uncaughtException(thread: Thread, throwable: Throwable) {
        Timber.d("MtihaniApplication() : report is caused by ${throwable.cause} " +
                "and issue is ${throwable.message}")
    }

    private fun initTimber(){
        Timber.plant(Timber.DebugTree())
    }

    private fun initTheme() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = preferences.getString("night_mode_state_key", "")
        if (selectedTheme!=null){
            when(selectedTheme){
                "System Default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "Dark Mode" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "Day Mode" ->  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}