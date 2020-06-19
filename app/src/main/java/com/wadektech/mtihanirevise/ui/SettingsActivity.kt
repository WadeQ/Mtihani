package com.wadektech.mtihanirevise.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.wadektech.mtihanirevise.R

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

    class SettingsFragment : PreferenceFragmentCompat() {
        private var _darkMode : SwitchPreferenceCompat ?= null
        private var _notificationsOff : SwitchPreferenceCompat?= null
        private var _rating : Preference?= null
        private var _share : Preference?= null
        private var _contact : Preference?= null
        private var _social : Preference?= null
        private var _appVersion : Preference?= null
        private var _bugs : Preference?= null
        private var _logout : Preference?= null

        private lateinit var _sharedPreferences: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            _darkMode = findPreference("dark")
            _notificationsOff = findPreference("notifications")
            _rating = findPreference("rate")
            _share = findPreference("share")
            _contact = findPreference("contact")
            _social = findPreference("social")
            _appVersion = findPreference("app_version")
            _bugs = findPreference("bugs")
            _logout = findPreference("logout")

            _darkMode?.setOnPreferenceChangeListener { preference, newValue ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                true
            }
        }

        private fun loadSavedUserPreferences(){
            _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        }
    }
}