package com.example.avani.musicappchallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;
import com.rdio.android.api.RdioListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;


public class PlayActivity extends ActionBarActivity implements RdioListener {
    private static final String TAG = "MusicAppChallenge";

    private static final String appKey = "npj8hbjk4c555dv25fsux8zr";
    private static final String appSecret = "W3pFudjyw6";

    private static Rdio rdio;

    private MediaPlayer player;

    private String songName;

    private Track firstPlayTrack;

    private TextView txtTrackName;
    private TextView txtAlbumArtistName;
    private ImageView albumArt;

    private ProgressDialog waitingDialog;

    private class Track {
        public String key;
        public String trackName;
        public String artistName;
        public String albumName;
        public String albumArt;

        public Track(String k, String name, String artist, String album, String uri) {
            key = k;
            trackName = name;
            artistName = artist;
            albumName = album;
            albumArt = uri;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent resultIntent = getIntent();
        songName = resultIntent
                .getStringExtra(MainActivity.EXTRA_MESSAGE);

        /* create Rdio */
        if (rdio == null) {
            rdio = new Rdio(appKey, appSecret, null, null, this, this);

            /* User authentication not done so i guess will only play 1 min preview */
        }
        rdio.prepareForPlayback();

        txtTrackName = (TextView) findViewById(R.id.trackName);
        txtAlbumArtistName = (TextView) findViewById(R.id.albumArtistName);
        albumArt = (ImageView) findViewById(R.id.albumArt);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Cleaning up..");

        // Make sure to call the cleanup method on the API object
        rdio.cleanup();
        rdio = null;

        // If we allocated a player, then cleanup after it
        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
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

    @Override
    public void onRdioAuthorised(String accessToken, String accessTokenSecret) {
        Log.i(TAG, "No need to save");
    }

    @Override
    public void onRdioReadyForPlayback() {
        /* search the track */
        searchTrack();
    }

    @Override
    public void onRdioUserPlayingElsewhere() {
        Log.w(TAG, "Tell the user that playback is stopping.");
    }

    /*
     * Search the tracks with name matching string provided
     */
    private void searchTrack() {
        waitingDialog = ProgressDialog.show(PlayActivity.this, getString(R.string.waiting_title), getString(R.string.waiting_message));

        List<NameValuePair> args = new LinkedList<NameValuePair>();
        args.add(new BasicNameValuePair("query", songName));
        args.add(new BasicNameValuePair("types", "Track"));
        args.add(new BasicNameValuePair("never_or", "true"));
        args.add(new BasicNameValuePair("count", "10"));
        rdio.apiCall("search", args, new RdioApiCallback() {
            @Override
            public void onApiSuccess(JSONObject result) {
                try {
                    JSONObject track_result = result.getJSONObject("result");
                    JSONArray results = track_result.getJSONArray("results");

                    if (results.length() == 0) {
                        txtAlbumArtistName.setVisibility(View.INVISIBLE);
                        albumArt.setVisibility(View.INVISIBLE);
                        txtTrackName.setText(getString(R.string.no_song_found));
                        return;
                    }
                    JSONObject firstTrack = results.getJSONObject(0);

                    String key = firstTrack.getString("key");
                    String name = firstTrack.getString("name");
                    String artist = firstTrack.getString("artist");
                    String albumName = firstTrack.getString("album");
                    String albumArt = firstTrack.getString("icon");

                    firstPlayTrack = new Track(key, name, artist, albumName, albumArt);
                    Log.w(TAG, "Found track with key =>" + key);

                    /* call to play the track */
                    waitingDialog.dismiss();
                    playTrack();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to handle JSONObject: ", e);
                }
                finally {
                    waitingDialog.dismiss();
                }
            }

            @Override
            public void onApiFailure(String methodName, Exception e) {
                Log.e(TAG, "Something bad happened. ", e);
            }
        });
    }

    private void playTrack() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        if (firstPlayTrack == null) {
            Log.e(TAG, "Track is Null");
            return;
        }

        AsyncTask<Track, Void, Track> task = new AsyncTask<Track, Void, Track>() {
            @Override
            protected Track doInBackground(Track... params) {
                Track track = params[0];
                try {
                    player = rdio.getPlayerForTrack(track.key, null, true);
                    player.prepare();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Toast.makeText(PlayActivity.this, getString(R.string.playback_finished), Toast.LENGTH_SHORT).show();
                        }
                    });
                    player.start();
                } catch (Exception e) {
                    Log.e("Test", "Exception " + e);
                }
                return track;
            }

            @Override
            protected void onPostExecute(Track track) {
                txtTrackName.setText(firstPlayTrack.trackName);
                txtAlbumArtistName.setText(firstPlayTrack.albumName + " - " + firstPlayTrack.artistName);
                Toast.makeText(PlayActivity.this, getString(R.string.playback_started), Toast.LENGTH_SHORT).show();
            }
        };
        task.execute(firstPlayTrack);

        AsyncTask<Track, Void, Bitmap> artworkTask = new AsyncTask<Track, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Track... params) {
                Track track = params[0];
                try {
                    String artworkUrl = track.albumArt.replace("square-200", "square-600");
                    Log.i(TAG, "Downloading album art: " + artworkUrl);
                    Bitmap bm = null;
                    try {
                        URL aURL = new URL(artworkUrl);
                        URLConnection conn = aURL.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error getting bitmap", e);
                    }
                    return bm;
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading artwork", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap artwork) {
                if (artwork != null) {
                    albumArt.setImageBitmap(artwork);
                } else
                    albumArt.setImageResource(R.drawable.blank_album_art);
            }
        };
        artworkTask.execute(firstPlayTrack);
    }
}