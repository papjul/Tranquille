package dummydomain.yetanothercallblocker;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import dummydomain.yetanothercallblocker.utils.DebuggingUtils;
import dummydomain.yetanothercallblocker.utils.FileUtils;

public class AdvancedSettingsFragment extends BaseSettingsFragment {

    private static final String PREF_SCREEN_ADVANCED = "screenAdvanced";
    private static final String PREF_COUNTRY_CODES_INFO = "countryCodesInfo";
    private static final String PREF_EXPORT_LOGCAT = "exportLogcat";

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedSettingsFragment.class);

    @Override
    protected String getScreenKey() {
        return PREF_SCREEN_ADVANCED;
    }

    @Override
    protected void initScreen() {
        String countryCodesExplanationSummary = getString(R.string.country_codes_info_summary)
                + ". " + getString(R.string.country_codes_info_summary_addition,
                App.getSettings().getCachedAutoDetectedCountryCode());

        Preference countryCodesInfoPreference = requirePreference(PREF_COUNTRY_CODES_INFO);
        countryCodesInfoPreference.setSummary(countryCodesExplanationSummary);
        countryCodesInfoPreference.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.settings_category_country_codes)
                    .setMessage(countryCodesExplanationSummary)
                    .setNegativeButton(R.string.back, null)
                    .show();
            return true;
        });

        Preference.OnPreferenceChangeListener countryCodeChangeListener
                = (preference, newValue) -> {
            String value = (String) newValue;
            if (TextUtils.isEmpty(value) || Pattern.matches("^[a-zA-Z]{2}$", value)) {
                return true;
            }

            Toast.makeText(requireActivity(), R.string.country_code_incorrect_format,
                    Toast.LENGTH_SHORT).show();
            return false;
        };

        setPrefChangeListener(Settings.PREF_COUNTRY_CODE_OVERRIDE, countryCodeChangeListener);
        setPrefChangeListener(Settings.PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE,
                countryCodeChangeListener);

        requirePreference(PREF_EXPORT_LOGCAT)
                .setOnPreferenceClickListener(preference -> {
                    exportLogcat();
                    return true;
                });
    }

    private void exportLogcat() {
        Activity activity = requireActivity();

        String path = null;
        try {
            path = DebuggingUtils.saveLogcatInCache(activity);
            DebuggingUtils.appendDeviceInfo(path);
        } catch (IOException | InterruptedException e) {
            LOG.warn("exportLogcat()", e);
        }

        if (path != null) {
            FileUtils.shareFile(activity, new File(path));
        }
    }

}
