package com.wadektech.mtihanirevise.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.wadektech.mtihanirevise.R
import hotchemi.android.rate.AppRate
import timber.log.Timber


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener
    {
        private var _darkMode : SwitchPreferenceCompat ?= null
        private var _notificationsOff : SwitchPreferenceCompat?= null
        private var _rating : Preference?= null
        private var _share : Preference?= null
        private var _contact : Preference?= null
        private var _appVersion : Preference?= null
        private var _bugs : Preference?= null
        private var _logout : Preference?= null
        var mAuth: FirebaseAuth? = null

        private lateinit var materialDesignAnimatedDialog: NiftyDialogBuilder

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            materialDesignAnimatedDialog = NiftyDialogBuilder.getInstance(requireContext())

            mAuth = FirebaseAuth.getInstance()

            _darkMode = preferenceManager.findPreference("dark")
            _darkMode?.onPreferenceChangeListener = this

            _notificationsOff = preferenceManager.findPreference("notifications")
            _notificationsOff?.onPreferenceChangeListener = this

            _rating = findPreference("rate")
            _rating?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
            _share = findPreference("share")
            _share?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
            _contact = findPreference("contact")
            _contact?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
            _appVersion = findPreference("app_version")
            _appVersion?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
            _bugs = findPreference("bugs")
            _bugs?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
            _logout = findPreference("logout")
            _logout?.setOnPreferenceClickListener {
                onPreferenceClick(it)
            }
        }

        override fun onPreferenceChange(preference: Preference?, key: Any?): Boolean {
            TODO("Not yet implemented")
        }

        override fun onPreferenceClick(preference: Preference?): Boolean {
            Timber.d("detected changes on prefs listeners")
            when {
                preference!!.key == "rate" -> {
                    rateApp()
                }
                preference.key== "share" -> {
                    shareApp()
                }
                preference.key== "contact" -> {
                    //open the developer profile
                    startActivity(Intent(context, DeveloperProfile::class.java))
                    return true
                }
                preference.key== "app_version" -> {

                }
                preference.key== "bugs" -> {
                    sendBugs()
                }
                preference.key== "logout" -> {
                   logOut()
                }
            }
            return true
        }

        private fun sendBugs(){
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("wadektech@gmail.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "Mtihani Revise Bug Report")
            i.putExtra(Intent.EXTRA_TEXT, "Share here...")
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show()
            }
        }

        private fun rateApp(){
            AppRate.with(context)
                    .setInstallDays(1)
                    .setLaunchTimes(3)
                    .setRemindInterval(2)
                    .monitor()
            AppRate.showRateDialogIfMeetsConditions(requireActivity())
            AppRate.with(context).showRateDialog(requireActivity())
        }

        private fun shareApp(){
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey, want access to all your K.C.S.E past exam papers from 2008 to 2017 at the convenience of your smartphone? Download Mtihani Revise at: https://play.google.com/store/apps/details?id=com.google.android.apps.plus")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
    }

        private fun logOut(){
            materialDesignAnimatedDialog
                    .withTitle("Logout")
                    .withMessage("Are you sure you want to log out of Mtihani Revise? Your session will be deleted.")
                    .withDialogColor("#d35400")
                    .withButton1Text("OK")
                    .isCancelableOnTouchOutside(true)
                    .withButton2Text("Cancel")
                    .withDuration(700)
                    .withEffect(Effectstype.Fall)
                    .setButton1Click { signOut() }
                    .setButton2Click { materialDesignAnimatedDialog.dismiss() }
            materialDesignAnimatedDialog.show()
        }

        private fun signOut() {
            //signOut user from firebase database
            mAuth?.signOut()
            //send intent to the Login activity
            val intent = Intent(requireContext(), MainSliderActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}