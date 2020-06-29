package com.wadektech.mtihanirevise.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.wadektech.mtihanirevise.R
import com.wadektech.mtihanirevise.utils.snackbar
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
            Preference.OnPreferenceClickListener,
            SharedPreferences.OnSharedPreferenceChangeListener
    {
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

            val sharedPreferences = preferenceScreen.sharedPreferences
            val prefScreen: PreferenceScreen = preferenceScreen
            //Used index 0 to get the ListPreference since there is only one preference in our pref screens at the moment
            val p = prefScreen.getPreference(0)
            //Get the value of the listPreference from sharedPref
            //Set the default value of the preference to System Default
            val value = sharedPreferences.getString(p.key, (R.string.default_mode_value.toString()))
            if (value!=null){
                setPreferenceSummary(p, value)
            }

            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

            materialDesignAnimatedDialog = NiftyDialogBuilder.getInstance(requireContext())

            mAuth = FirebaseAuth.getInstance()

            _notificationsOff = preferenceManager.findPreference("notifications")
            _notificationsOff?.onPreferenceChangeListener

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
/*
the logic here is to first check if the toggle switch is checked,
when checked we then check if the stored key value
corresponds to our key then implement corresponding functionality.
* */
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            val pref = findPreference<ListPreference>(key.toString())
            if (pref!=null){
                val value = sharedPreferences?.getString(key, (R.string.default_mode_value.toString()))
                //Change the theme
                when(value){
                    "System Default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "Dark Mode" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "Day Mode" ->  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                //Set the summary
                if (value != null) {
                    setPreferenceSummary(pref, value)
                }
            }
        }

        override fun onPreferenceClick(preference: Preference?): Boolean {
            Timber.d("detected clicks on prefs listeners")
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
                    snackbar(requireView(),"This is the current version...")
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

        private fun setPreferenceSummary(preference: Preference?, value : String){
            // Figure out the label of the selected value
            val listPreference = preference as ListPreference
            val prefIndex: Int = listPreference.findIndexOfValue(value)
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.summary = listPreference.entries[prefIndex]
            }
        }

        private fun sendBugs(){
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("wadektech@gmail.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "Mtihani Revise Bug Report")
            i.putExtra(Intent.EXTRA_TEXT, "Bug report...")
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                snackbar(requireView(),"There are no email clients installed.")
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

        private fun toggleNotificationsOff(key : String) {
            if (key == "notifications"){
                snackbar(requireView(), "Turned off notifications...")
            }
        }

        override fun onResume() {
            super.onResume()
            Timber.d("register shared preference listener")
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            Timber.d("unregister shared preference listener")
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            Timber.d("unregister shared preference listener")
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}