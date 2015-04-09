package hkust.comp4521.audio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import hkust.comp4521.audio.player.MusicPlayer;
import hkust.comp4521.audio.player.PlayerState;

public class SongPlaying extends Fragment implements OnClickListener, SeekBar.OnSeekBarChangeListener, Observer {


    private static final String TAG = "SongPlaying";
    private static ImageButton playerButton, rewindButton, forwardButton;
    public static Handler handler;
    private TextView songTitleText;
    private ImageView songImage;

    private SeekBar songProgressBar;
    private TextView complTime, remTime;


    /*
     * Class Name: MusicController
     *
     *    This service implements support for playing a music file using the MediaPlayer class in Android.
     *    It supports the following intent actions:
     *
     *    ACTION_PLAY_PAUSE: toggles the player between playing and paused states
     *    ACTION_RESUME: resume playing the current song
     *    ACTION_PAUSE: pause the currently playing song
     *    ACTION_REWIND: rewind the currently playing song by one step
     *    ACTION_FORWARD: forward the currently playing song by one step
     *    ACTION_STOP: stop the currently playing song
     *    ACTION_RESET: reset the music player and release the MediaPlayer associated with it
     *    ACTION_REPOSITION: repositions the playing position of the song to value% and resumes playing
     *
     * Class Name: MusicPlayer
     *
     *    progress(): returns the percentage of the playback completed. useful to update the progress bar
     *    completedTime(): Amount of the song time completed playing
     *    remainingTime(): Remaining time of the song being played
     *
     *    You should use these actions and methods to manage the playing of the song.
     *    In this exercise, the MusicPlayer will play only two specific songs that are hard coded.
     *    We will relax this in the next version of the MusicPlayer class
     *
     */
    private MusicPlayer player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // The music player is implemented as a Java Singleton class so that only one
        // instance of the player is present within the application. The getMusicPlayer()
        // method returns the reference to the instance of the music player class
        // get a reference to the instance of the music player
        // add this fragment as an observer.
        player = MusicPlayer.getMusicPlayer();

        handler = new Handler();

        Log.i(TAG, "After Bind to Service");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Create a layout for the fragment with the buttons:
        // and set it to the view of this fragment
        View view = inflater.inflate(R.layout.songplaying, container, false);

        Log.i(TAG, "onCreateView()");

        // Get the references to the buttons from the layout of the activity
        playerButton = (ImageButton) view.findViewById(R.id.play);
        playerButton.setOnClickListener(this);

        rewindButton = (ImageButton) view.findViewById(R.id.rewind);
        rewindButton.setOnClickListener(this);

        forwardButton = (ImageButton) view.findViewById(R.id.forward);
        forwardButton.setOnClickListener(this);

        //	get	a	reference	to	the	song	title	TextView and songImage ImageView	in	the	UI
        songTitleText = (TextView) view.findViewById(R.id.songTitle);
        songImage = (ImageView) view.findViewById(R.id.songImage);

        // get reference to the seekbar, completion time and remaining time textviews
        songProgressBar = (SeekBar) view.findViewById(R.id.songProgressBar);
        songProgressBar.setMax(100);
        songProgressBar.setOnSeekBarChangeListener(this);
        complTime = (TextView) view.findViewById(R.id.songCurrentDurationLabel);
        remTime = (TextView) view.findViewById(R.id.songRemainingDurationLabel);

        return view;
    }


    public void setSongTitle(String title) {
        songTitleText.setText(title);

        ObjectAnimator textanim = (ObjectAnimator)  ObjectAnimator.ofFloat(songTitleText, "alpha", 0f, 1f);
        textanim.setDuration(5000);
        textanim.start();

        ObjectAnimator imageanim = (ObjectAnimator)  ObjectAnimator.ofFloat(songImage, "alpha", 0f, 1f);
        imageanim.setDuration(5000);
        imageanim.start();
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy()");

        handler = null;
        player = null;

        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        Log.i(TAG, "onAttach");

    }

    @Override
    public void onDetach() {

        super.onDetach();
        Log.i(TAG, "onDetach");
    }

    @Override
    public void onResume() {
        super.onResume();

        player.addObserver(this);

        // reset the UI to reflect the current state of the player
        songProgressBar.setProgress(player.progress());
        complTime.setText(player.completedTime());
        remTime.setText("-"+player.remainingTime());
        setSongTitle(player.getSongTitle());

        if (player.isPlaying()) {
            updateSongProgress();
            playerButton.setImageResource(R.drawable.btn_pause);
        }
        else {
            playerButton.setImageResource(R.drawable.btn_play);

        }

    }

    @Override
    public void onPause() {

        handler.removeCallbacks(songProgressUpdate);

        player.deleteObserver(this);
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        Log.i(TAG, "Activity: After Bind to Service");

    }

    public void onClick(View v) {

        // Create the intent that you will use to send action to the onStartCommand()
        // in the service
        Intent intent = new Intent( getActivity(), MusicController.class );

        // Based on which button is clicked, set the appropriate action in the intent
        switch (v.getId()) {

            case R.id.play:
                intent.setAction(Constants.ACTION_PLAY_PAUSE);
                break;

            case R.id.forward:
                intent.setAction(Constants.ACTION_FORWARD);
                break;

            case R.id.rewind:
                intent.setAction(Constants.ACTION_REWIND);
                break;

            default:
                break;
        }
        // send the intent to service by calling startService. This will result in
        // a call to the onStartCommand() of the service if the service is already running.
        getActivity().startService(intent);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        // The update method is called whenever the Music Player experiences change of state
        // arg1 returns the current state of the player in the form of PlayerState enum variable
        // Use the switch to recognize which state the player just entered and take appropriate
        // action to handle the change of state. Here we update the play/pause button accordingly

        switch ((PlayerState) arg1) {

            case Ready:
                Log.i(TAG, "Player State Changed to Ready");
                playerButton.setImageResource(R.drawable.btn_play);
                setSongTitle(player.getSongTitle());
                songProgressBar.setProgress(player.progress());
                complTime.setText(player.completedTime());
                remTime.setText("-"+player.remainingTime());
                break;

            case Paused:
                Log.i(TAG, "Player State Changed to Paused");
                playerButton.setImageResource(R.drawable.btn_play);
                cancelUpdateSongProgress();
                songProgressBar.setProgress(player.progress());
                complTime.setText(player.completedTime());
                remTime.setText("-"+player.remainingTime());
                break;

            case Stopped:
                Log.i(TAG, "Player State Changed to Stopped");
                playerButton.setImageResource(R.drawable.btn_play);
                cancelUpdateSongProgress();
                break;

            case Playing:
                playerButton.setImageResource(R.drawable.btn_pause);
                Log.i(TAG, "Player State Changed to Playing");
                updateSongProgress();
                break;

            case Reset:
                Log.i(TAG, "Player State Changed to Reset");
                playerButton.setImageResource(R.drawable.btn_play);
                cancelUpdateSongProgress();
                break;

            default:
                break;

        }

    }

    public void updateSongProgress() {
        handler.postDelayed(songProgressUpdate, 500);
    }

    private Runnable songProgressUpdate = new Runnable() {

        @Override
        public void run() {
            // Initialize the progress bar and the status TextViews
            // We want to modify the progress bar. But we can do it only from
            // the UI thread. To do this we make use of the handler.

            songProgressBar.setProgress(player.progress());
            complTime.setText(player.completedTime());
            remTime.setText("-"+player.remainingTime());

            // schedule another update 500 ms later
            handler.postDelayed(songProgressUpdate, 500);

        }

    };

    public void cancelUpdateSongProgress() {

        // cancel all callbacks that are already in the handler queue
        handler.removeCallbacks(songProgressUpdate);
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {

        cancelUpdateSongProgress();

        if (fromUser && player.isPlaying()) {
            Intent intent = new Intent(getActivity(), MusicController.class);
            intent.setAction(Constants.ACTION_REPOSITION);
            intent.putExtra("Position",progress);
            getActivity().startService(intent);

        }

        updateSongProgress();
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}