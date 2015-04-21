package com.example.avani.musicappchallenge;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String EXTRA_MESSAGE = "com.example.avani.musicappchallenge.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMicClicked(View view) {
        promptSpeechInput();
    }

    public void onSearchClicked(View view) {
        openSearchActivity();
    }

    private void promptSpeechInput() {
        try {
            Intent intent = getIntentForSpeechRecognition();
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getIntentForSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    EditText txtSpeechInput = (EditText) findViewById(R.id.main_edit_text);
                    txtSpeechInput.setText(result.get(0));
                }
                break;
            }
        }
    }

    private void openSearchActivity() {
        String songName;
        EditText txtSpeechInput = (EditText) findViewById(R.id.main_edit_text);
        songName = txtSpeechInput.getText().toString();
        /* if there is no song selected show the error toast */
        if (TextUtils.isEmpty(songName)) {
            Toast.makeText(this, getString(R.string.no_song_name), Toast.LENGTH_SHORT).show();
            return;
        }

        /*
        * pass the song name to search activity
         */
        Intent playActivityIntent = new Intent(
                getBaseContext(), PlayActivity.class);
        playActivityIntent.putExtra(EXTRA_MESSAGE, songName);
        startActivity(playActivityIntent);
    }
}
