package com.happenstance.projsc.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.happenstance.projsc.about.AboutActivity;
import com.happenstance.projsc.FloatingButtonService;
import com.happenstance.projsc.R;
import com.happenstance.projsc.about.VersionHistoryActivity;
import com.happenstance.projsc.utils.Utilities;

public class SettingsFragment extends PreferenceFragmentCompat {

    Preference.OnPreferenceChangeListener opclNumberDial = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String s = (String) newValue;
            if (s.length() == 0) {
                Utilities.showDialogOK(getContext(), "Please try again", "You must enter a value", null);
                return false;
            } else if (!Utilities.isInteger(s)) {
                Utilities.showDialogOK(getContext(), "Please try again", "Value must be a number", null);
                return false;
            } else if (Integer.valueOf(s) > 100
                    || Integer.valueOf(s) < 0) {
                Utilities.showDialogOK(getContext(), "Please try again", "Value must be a number between 0 and 100", null);
                return false;
            } else {
                return true;
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference prefImageQuality = findPreference(getString(R.string.settings_image_quality_key));
        setNumberDialValidation(prefImageQuality);
        prefImageQuality.setOnPreferenceChangeListener(opclNumberDial);

        EditTextPreference prefButtonOpacity = findPreference(getString(R.string.settings_button_opacity_key));
        setNumberDialValidation(prefButtonOpacity);
        prefButtonOpacity.setOnPreferenceChangeListener(opclNumberDial);
        prefButtonOpacity.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String buttonTransparency = (String) newValue;
                FloatingButtonService.setButtonOpacity(getContext(), buttonTransparency);
                return true;
            }
        });

        ListPreference listPreference = findPreference(getString(R.string.settings_button_color_key));
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newButtonColor = (String) newValue;
                FloatingButtonService.setButtonColor(getContext(), newButtonColor);
                return true;
            }
        });

        ListPreference lpButtonSize = findPreference(getString(R.string.settings_button_size_key));
        lpButtonSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newButtonSize = (String) newValue;
                FloatingButtonService.setButtonSize(getContext(), newButtonSize);
                return true;
            }
        });

        SwitchPreference spSnip = findPreference(getString(R.string.settings_switch_snip_key));
        spSnip.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    spSnip.setSummary(getString(R.string.switch_preference_snipping_tool_on_summary));
                } else {
                    spSnip.setSummary(getString(R.string.switch_preference_snipping_tool_off_summary));
                }
                return true;
            }
        });

        SwitchPreference spStatusBar = findPreference(getString(R.string.settings_switch_status_bar));
        spStatusBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                   spStatusBar.setSummary(getString(R.string.switch_preference_status_bar_on_summary));
                } else {
                    spStatusBar.setSummary(getString(R.string.switch_preference_status_bar_off_summary));
                }
                return true;
            }
        });

        SwitchPreference spNavigationBar = findPreference(getString(R.string.settings_switch_navigation_bar));
        spNavigationBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    spNavigationBar.setSummary(getString(R.string.switch_preference_navigation_bar_on_summary));
                } else {
                    spNavigationBar.setSummary(getString(R.string.switch_preference_navigation_bar_off_summary));
                }
                return true;
            }
        });

        Preference prefAbout = findPreference(getString(R.string.settings_about_app_key));
        Intent intentAbout = new Intent(getContext(), AboutActivity.class);
        prefAbout.setIntent(intentAbout);

        Preference prefHistory = findPreference("version_history");
        Intent intentHistory = new Intent(getContext(), VersionHistoryActivity.class);
        prefHistory.setIntent(intentHistory);
    }

    private void setNumberDialValidation(EditTextPreference prefButtonTransparency) {
        if (prefButtonTransparency != null) {
            prefButtonTransparency.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    if (s.toString().length() == 0) {
                                        editText.setError("Enter a value");
                                    } else if (!Utilities.isInteger(s.toString())) {
                                        editText.setError("Not a number");
                                    } else if (Integer.valueOf(s.toString()) > 100
                                            || Integer.valueOf(s.toString()) < 0) {
                                        editText.setError("Value must be between 0 and 100");
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });
                        }
                    });
        }

    }
}