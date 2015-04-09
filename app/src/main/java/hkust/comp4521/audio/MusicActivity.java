package hkust.comp4521.audio;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class MusicActivity extends Activity implements Playlist.OnSongSelectedListener {


    private static final String TAG = "MusicActivity";
    //private static long songIndex = 0;

    // private MusicPlayer player;

    // indicates if the player is running on a small screen device (false) or tablet (true)
    private boolean dualview = false;

    private GestureDetector mDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a layout for the Activity with four buttons:
        // Rewind, Pause, Play and Forward and set it to the view of this activity
        setContentView(R.layout.main);

        startService(new Intent(this, MusicController.class));

        // If the view being used contains the SongPlaying fragment in the layout, then
        // we are using dualview layout and the screen size is large. So both fragments
        // are on the screen. Set dualview to true
        if (findViewById(R.id.song) != null)
            dualview = true;

        if (!dualview) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
            if (findViewById(R.id.fragment_container) != null) {
                Fragment firstFragment = new SongPlaying();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.fragment_container, firstFragment, getResources().getString(R.string.NowPlaying));
                ft.commit();
                Log.i(TAG, "First Fragment: " + firstFragment.getTag() + " Res ID: " + firstFragment.getId());
            }
        }

        mDetector = new GestureDetector(this, new MyGestureListener());

    }

    @Override
    public void onSongSelected(long id) {
        // This method is for the OnSongSelectedListener interface. When the user selects a song in the
        // play list, then this method is invoked

        //songIndex = id;

        // create an intent to send to MusicController service
        Intent intent = new Intent( getApplicationContext(), MusicController.class );
        // Add the action to the intent. Here we are trying to start the song
        intent.setAction( Constants.ACTION_SONG );
        // add the song ID to the intent
        intent.putExtra("SongID",id);
        // call startService to deliver the intent to onStartCommand() in the service
        // where it will be handled.
        startService( intent );

        if (!dualview) {

            Fragment firstFragment = getFragmentManager().findFragmentByTag(getResources().getString(R.string.NowPlaying));
            Log.i(TAG, "First Fragment: " + firstFragment.getTag() + " Res ID: " + firstFragment.getId());
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, firstFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {

        Log.i(TAG, "Activity: onDestroy()");

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.i(TAG, "Activity: onPause()");
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        Log.i(TAG, "Activity: onRestart()");
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.i(TAG, "Activity: onResume()");
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "Activity: onStart()");
    }

    @Override
    protected void onStop() {

        super.onStop();
        Log.i(TAG, "Activity: onStop()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Handle the presses of the action bar items
        switch (item.getItemId()) {

            case R.id.action_playlist:
                if (!dualview) {

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment secondFragment = getFragmentManager().findFragmentByTag(getResources().getString(R.string.SongList));
                    if (secondFragment == null) {
                        secondFragment = new Playlist();
                    }
                    ft.replace(R.id.fragment_container, secondFragment, getResources().getString(R.string.SongList));
                    ft.addToBackStack(null);
                    ft.commit();
                    Log.i(TAG, "Second Fragment: " + secondFragment.getTag() + " Res ID: " + secondFragment.getId());
                }
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        this.mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
                               float velocityY) {

            // gesture is from left to right
            if (velocityX > 0) {

                // bring the playlist fragment to the front and once the user selects a song
                // from the list, return the information about the selected song
                // to MusicActivity
                if (!dualview) {

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment secondFragment = getFragmentManager().findFragmentByTag(getResources().getString(R.string.SongList));
                    if (secondFragment == null) {
                        secondFragment = new Playlist();
                    }
                    ft.replace(R.id.fragment_container, secondFragment, getResources().getString(R.string.SongList));
                    ft.addToBackStack(null);
                    ft.commit();
                    Log.i(TAG, "Second Fragment: " + secondFragment.getTag() + " Res ID: " + secondFragment.getId());
                }
            }
            return true;
        }

    }
}