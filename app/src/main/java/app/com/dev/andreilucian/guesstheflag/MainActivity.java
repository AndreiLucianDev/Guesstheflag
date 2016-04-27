package app.com.dev.andreilucian.guesstheflag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //keys for reading data from SharedPreferences
    public final static String CHOICES = "pref_numberOfChoices";
    public final static String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true;             //used to force portrait mode
    private boolean preferencesChanged = true;      //did preference change?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferencesChangedListener);

        //determine screen size
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        //if device is a tablet set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE){
            phoneDevice = false;
        }

        //if running on phone-sized device, allow only portrait mode
        if (phoneDevice){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged){
            //now that the preferences have been set
            //initialize MainActivityFragment and start the quiz
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().
                    findFragmentById(R.id.quizfragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            Log.d("Tag", "arrivato qui");
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //show menu if app is running on a phone or a portrait-oriented tablet
        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // displays the SettingsActivity when running on a phone
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);
    }

    // listener for changes to the app's SharedPreferences
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangedListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;      //user changed app settings

                    MainActivityFragment quizFragment = (MainActivityFragment)getSupportFragmentManager().
                            findFragmentById(R.id.quizfragment);

                    //number choices changed
                    if (key.equals(CHOICES)){
                        quizFragment.updateGuessRows(sharedPreferences);
                    }else if (key.equals(REGIONS)){         //region to include changed
                        Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null && regions.size() > 0){
                            quizFragment.updateRegions(sharedPreferences);
                            quizFragment.resetQuiz();
                        }else{                              //must select one region, set default North_america
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), R.string.default_region_message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(getApplicationContext(), R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
                }
            };
}