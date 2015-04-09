package hkust.comp4521.audio;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import hkust.comp4521.audio.player.MusicPlayer;

// The service is going to be implemented as a Started Service. The activity and fragment will issue their commands
// through the use of startService() specifying an Intent that conveys the action to be taken by the service.
// When an already runnign service is called using startService(), only the onStartCommand() method is executed.
// This method will handle the incoming intents when startService() is called from the activity/fragment

public class MusicController extends Service implements MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener  {

    private static final String TAG = "MusicController";

    private NotificationManager mNotificationManager;
    private int noteId = 1;

    AudioManager mAudioManager;

    MusicPlayer player = null;

    //private static int songIndex = 0;
    //private static long songIndex = 0;

    boolean focus_loss_paused = false;

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        // Pop up a message on the screen to show that the service is started
        Toast.makeText(this, "MusicController Service Created", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Service: onCreate()");

        // The music player is implemented as a Java Singleton class so that only one
        // instance of the player is present within the application. The getMusicPlayer()
        // method returns the reference to the instance of the music player class
        // get a reference to the instance of the music player
        // set the context for the music player to be this service
        player = MusicPlayer.getMusicPlayer();
        player.setContext(this);

        //get access to the notification manager
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // get access to the AudioManager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //startSong(songIndex);

    }

    @Override
    public void onDestroy() {


        // Pop up a message on the screen to show that the service is started
        Toast.makeText(this, "MusicController Service Stopped", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Service: onDestroy()");

        player.reset();
        player = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.i(TAG, "Service: onStartCommand()");

        // Any time startService() is called, the intent is delivered here and can be handled.
        handleIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    //	handle	the	intent	delivered	to	onStartCommand().
    private	void	handleIntent(	Intent	intent	)	{
        if(	intent	==	null	||	intent.getAction()	==	null	)
            return;
        //	get	the	action	specified	in	the	intent.	The	actioins	are	given	in	Constants.
        String	action	=	intent.getAction();
        if(	action.equalsIgnoreCase(	Constants.ACTION_PLAY_PAUSE	)	)	{
            play_pause();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_PLAY	)	)	{
            resume();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_PAUSE	)	)	{
            play_pause();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_FORWARD	)	)	{
            forward();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_REWIND	)	)	{
            rewind();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_PREVIOUS	)	)	{
            return;
        }	else	if(	action.equalsIgnoreCase(    Constants.ACTION_NEXT	)	)	{
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_STOP	)	)	{
            stop();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_RESET	)	)	{
            reset();
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_SONG	)	) {
            reset();
            long id = intent.getLongExtra("SongID",0);
            startSong(id);
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_REPOSITION	)	)	{
            int	position	=	intent.getIntExtra("Position",0);
            reposition(position);
        }	else	if(	action.equalsIgnoreCase(	Constants.ACTION_COMPLETED	)	)	{
            reset();
            //startSong(songIndex);
            long id = intent.getLongExtra("SongID",0);
            startSong(id);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Toast.makeText(this, "MusicController Music Player Failed", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Service: Music player failed");

        if (mp != null) {
            try {
                mp.stop();
                mp.release();
            }
            finally {
                mp = null;
            }
        }

        return false;
    }

    public	void	startSong(long index)	{
        Log.i(TAG,	"Service: startSong()");
        if	(player	!=	null)	{
            //songIndex	=	index;
            player.start(index);
            putNotification();
            //	wait	until	you	get	focus	of	the	audio	stream
            while	(!requestFocus());
        }
        else
            Log.i(TAG,	"Service: startSong Null Player");
    }


    public	void	play_pause()	{
        if	(player	!=	null)	{
            //	if	player	is	playing,	then	abandon	audio	focus.	we	are	pausing	playback
            //	else,	get	audio	focus	before	we	proceed	to	play	the	song.
            if	(player.isPlaying())	{
                //	update	the	pause	button	in	the	notification	to	play	button
                updateNotification(Constants.ACTION_PLAY	);
            }
            else	{
                //	update	the	pause	button	in	the	notification	to	pause	button
                updateNotification(Constants.ACTION_PAUSE	);
            }
            Log.i(TAG,	"Service:	play_pause()");
            player.play_pause();
        }
        else
            Log.i(TAG,	"Service:	play_pause()	Null	Player");
    }

    public	void	resume()	{
        if	(player	!=	null)	{
            Log.i(TAG,	"Service:	resume()");
            player.resume();
            //	update	the	pause	button	in	the	notification	to	pause	button
            updateNotification(Constants.ACTION_PAUSE	);
        }
        else
            Log.i(TAG,	"Service:	resume()	Null	Player");
    }
    public	void	pause()	{
        if	(player	!=	null)	{
            Log.i(TAG,	"Service:	pause()");
            player.pause();
            //	update	the	pause	button	in	the	notification	to	play	button
            updateNotification(Constants.ACTION_PLAY);
        }
        else
            Log.i(TAG,	"Service:	pause()	Null	Player");
    }

    public void rewind() {

        if (player != null) {
            Log.i(TAG, "Service: rewind()");
            player.rewind();
        }
        else
            Log.i(TAG, "Service: rewind() Null Player");
    }

    public void forward() {

        if (player != null) {
            Log.i(TAG, "Service: forward()");
            player.forward();
        }
        else
            Log.i(TAG, "Service: forward() Null Player");
    }

    public void stop() {

        if (player != null) {
            Log.i(TAG, "Service: stop()");
            player.stop();
        }
        else
            Log.i(TAG, "Service: stop() Null Player");
    }

    public void reset() {

        if (player != null) {
            Log.i(TAG, "Service: reset()");

            // abandon focus of the audio stream
            while(!abandonFocus());

            player.reset();
            cancelNotification();
        }
        else
            Log.i(TAG, "Service: reset() Null Player");
    }

    public void reposition(int position) {

        if (player != null) {
            Log.i(TAG, "Service: reposition()");
            player.reposition(position);
        }
        else
            Log.i(TAG, "Service: reposition() Null Player");
    }

    // This method creates the action associated with the button in the Notification.
    // This creates a pending Intent and associates it with the button click.
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private Notification.Action createAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MusicController.class);
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();

    }

    // put the notification into the notification bar. This method is called when the song is first
    // initialized. It will be updated with control buttons by updateNotification().
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void putNotification(){

        Bitmap largeIcon;

        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.note_blue);

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.listen_icon)
                        .setLargeIcon(Bitmap.createScaledBitmap(largeIcon, 72, 72, false))
                        .setOngoing(true)
                        .setContentTitle("Music Player")
                        .setContentText("");

        // Creates an explicit intent for the MusicActivity Activity
        Intent resultIntent = new Intent(this, MusicActivity.class);

        // create a pending intent that will be fired when notification is touched.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent);

        Log.i(TAG, "Service: putNotification()");

        // noteId allows you to update the notification later on.
        // set the service as a foreground service
        startForeground(noteId,mBuilder.build());

    }

    // updateNotification() updates the information in the notification and adds the player
    // control buttons.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private	void	updateNotification(String	action){
        Bitmap	largeIcon;
        largeIcon	=	BitmapFactory.decodeResource(getResources(),
                R.drawable.note_blue);
        Notification.Builder mBuilder =
                new	Notification.Builder(this)
                        .setSmallIcon(R.drawable.listen_icon)
                        .setLargeIcon(Bitmap.createScaledBitmap(largeIcon, 72, 72, false))
                        .setOngoing(true)
                        .setContentTitle("Music	Player")
                        .setContentText(player.getSongTitle());
        //	We	can	use	the	MediaStyle	notification	only	for	Lollipop	and	above
        if	(Build.VERSION.SDK_INT	>=	Build.VERSION_CODES.LOLLIPOP)	{
            //	five	action	buttons	are	added	to	the	notification.
            mBuilder.addAction(createAction(android.R.drawable.ic_media_previous,
                    "Previous",	Constants.ACTION_PREVIOUS));
            mBuilder.addAction(createAction(android.R.drawable.ic_media_rew,
                    "Rewind",	Constants.ACTION_REWIND));
            if	(action.equalsIgnoreCase(Constants.ACTION_PLAY))	{
                mBuilder.addAction(createAction(android.R.drawable.ic_media_play,
                        "Play",	Constants.ACTION_PLAY));
            }	else	if	(action.equalsIgnoreCase(Constants.ACTION_PAUSE))	{
                mBuilder.addAction(createAction(android.R.drawable.ic_media_pause,
                        "Pause",	Constants.ACTION_PAUSE));
            }	else	{
                Log.i(TAG,	"Unknown	Action:	"	+	action);
            }
            mBuilder.addAction(createAction(android.R.drawable.ic_media_ff,	"Fast Foward",
                    Constants.ACTION_FORWARD));
                    mBuilder.addAction(createAction(android.R.drawable.ic_media_next,
                            "Next",	Constants.ACTION_NEXT));
            //	Set	the	notification	style	to	MediaStyle.	This	allows	us	to	put	player control
            //	buttons	into	the	notification
            Notification.MediaStyle	style	=	new	Notification.MediaStyle();
            //	We	add	in	three	action	buttons	to	the	compact	notification	view	(rewind,  play/pause,	forward)
            style.setShowActionsInCompactView(1,	2,	3);
            mBuilder.setStyle(style);
        }
        else	if	(Build.VERSION.SDK_INT	>=	Build.VERSION_CODES.JELLY_BEAN	)	{
            mBuilder.addAction(android.R.drawable.ic_media_rew,	"Rewind",
                    createPenIntent(Constants.ACTION_REWIND));
            if	(action.equalsIgnoreCase(Constants.ACTION_PLAY))	{
                mBuilder.addAction(android.R.drawable.ic_media_play,	"Play",
                        createPenIntent(Constants.ACTION_PLAY));
            }	else	if	(action.equalsIgnoreCase(Constants.ACTION_PAUSE))	{
                mBuilder.addAction(android.R.drawable.ic_media_pause,	"Pause",
                        createPenIntent(Constants.ACTION_PAUSE));
            }	else	{
                Log.i(TAG,	"Unknown	Action:	"	+	action);
            }
            mBuilder.addAction(android.R.drawable.ic_media_ff,	"Fast	Foward",
                    createPenIntent(Constants.ACTION_FORWARD));
        }	else	{
            Log.i(TAG,	"Version below Jelly Bean, so all notification buttons turned off");
        }
        //	Creates	an	explicit	intent	for	the	MusicActivity	Activity
        Intent	resultIntent	=	new	Intent(this,	MusicActivity.class);
        PendingIntent	resultPendingIntent	=	PendingIntent.getActivity(this,	0,
                resultIntent,	0);
        mBuilder.setContentIntent(resultPendingIntent);
        Log.i(TAG,	"Service:	updateNotification()");
        //	noteId	allows	you	to	update	the	notification	later	on.
        mNotificationManager.notify(noteId,	mBuilder.build());
    }

    //	This	method	creates	the	action	associated	with	the	button	in	the	Notification.
    //	This	creates	a	pending	Intent	and	associates	it	with	the	button	click.
    private	PendingIntent	createPenIntent(	String	intentAction	)	{
        Intent	intent	=	new	Intent(	getApplicationContext(),	MusicController.class);
        intent.setAction(	intentAction	);
        return PendingIntent.getService(getApplicationContext(),	1,	intent,	0);
    }

    private void cancelNotification() {
        mNotificationManager.cancel(noteId);
        stopForeground(true);
    }

    // service requests audio focus so that it can play the music
    public boolean requestFocus() {

        return (AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN));
    }

    // service releases audio focus when playback is paused
    public boolean abandonFocus() {

        return (AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this));
    }

    // callback method invoked when any change in audio focus is detected
    @Override
    public	void	onAudioFocusChange(int	focusChange)	{
        //	temporary	loss	of	audio	focus.	pause until	it	is	restored
        if	(focusChange	==	AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)	{
            if	(player.isPlaying())	{
                //	player	paused	due	to	focus	loss.	should	resume	when	regaining	focus
                focus_loss_paused	=	true;
                pause();
            }
        }
        //	gained	audio	focus.	so	resume	playback	of	song.	The	music
        //	must	have	been	playing	when	the	audiofocus	was	lost	earlier.
        else	if	(focusChange	==	AudioManager.AUDIOFOCUS_GAIN)	{
            //	player	was	paused	due	to	focus	loss,	so	resume	playing
            if	(focus_loss_paused)	{
                focus_loss_paused	=	false;
                resume();
            }
        }
        //	audio	focus	permanently	lost.	so	stop	all	music	playback.
        else	if	(focusChange	==	AudioManager.AUDIOFOCUS_LOSS)	{
            pause();
        }
    }
}
