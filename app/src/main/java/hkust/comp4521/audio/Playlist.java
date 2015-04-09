package hkust.comp4521.audio;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Playlist extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "Playlist";
    SimpleCursorAdapter mAdapter;


    // the host activity should register itself as a listener and implement the interface methods
    // this variable keeps track of the reference to the host activity
    OnSongSelectedListener mListener = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {


        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    // for a listfragment, we need not implement onCreateView() since this is implicit

    @Override
    public	void	onActivityCreated(Bundle	savedInstanceState)	{
        super.onActivityCreated(savedInstanceState);
        //	Give	some	text	to	display	if	there	is	no	data.		In	a	real
        //	application	this	would	come	from	a	resource.
        setEmptyText("No	songs	yet");
        //	We	have	a	menu	item	to	show	in	action	bar.
        setHasOptionsMenu(true);
        //	create an empty adapter we will use to display the data from MediaStore content  provider
                //	to	be	displayed	in	the	listview
                mAdapter	=	new	SimpleCursorAdapter(getActivity(),	R.layout.playlist_item,
                null,
                new	String[]	{	MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST},
                new	int[]	{	R.id.songlist,	R.id.songArtist	},	0);
        setListAdapter(mAdapter);
        //	prepare	the	loader.	Either	re-connect	to	an	existing	one,	or	start	a	new	one
        getLoaderManager().initLoader(0,	null,
                (LoaderManager.LoaderCallbacks<Cursor>)	this);
        //	get	a	reference	to	the	listview
        ListView	lv	=	getListView();
        lv.setOnItemClickListener(
                new	OnItemClickListener()	{
                    @Override
                    public	void	onItemClick(AdapterView<?>	arg0,	View	arg1,
                                                  int	position,	long	id)	{
                        //	position	gives	the	index	of	the	song	selected
                        //	return	the	information	about	the	selected	song	to
                        //      MusicActivity	through	the	interface	method
                                //mListener.onSongSelected( (long) position );
                        mListener.onSongSelected(id);
                    }
                });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    //	These	are	the	MediaStore	columns	that	we	will	retrieve.
    static	final	String[] MUSIC_SUMMARY_PROJECTION	=	new	String[]	{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,	//	name	of	the	music	file
            MediaStore.Audio.Media.TITLE,	//	title	of	the	song
            MediaStore.Audio.Media.ARTIST	//Artist's	name
    };

    @Override
    public	Loader<Cursor>	onCreateLoader(int	id,	Bundle	args)	{
        //	This	is	called	when	a	new	loader	needs	to	be	created.	This	case
        //	has	only	one	loader	so	we	don't	care	about	loader	ID.
        //	First,	pick	the	base	URI	to	use
        Uri baseUri;
        String	select;
        CursorLoader	curLoader;
        baseUri	=	MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG,	"onCreateLoader");
        //	now	create	the	cursor	loader	and	return	it	so	that	the	loader	will	take	care
        //	of	creating	the	Cursor	for	the	data	being	displayed
        select = "((" + MediaStore.Audio.Media.DISPLAY_NAME + " NOTNULL) AND ("
                + MediaStore.Audio.Media.DISPLAY_NAME + " != '' ) AND ("
                + MediaStore.Audio.Media.IS_MUSIC + " != 0))";
        curLoader = new CursorLoader(getActivity(), baseUri, MUSIC_SUMMARY_PROJECTION,
                select,null, MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC"
        );
        return	curLoader;
    }

    @Override
    public	void	onLoadFinished(Loader<Cursor>	loader,	Cursor	data)	{
        //	swap	the	new	cursor	in.	The	old	cursor	will	be	closed	by	the	framework.
        Log.i(TAG,	"onLoadFinished,	starting");
        mAdapter.swapCursor(data);
    }

    @Override
    public	void	onLoaderReset(Loader<Cursor>	arg0)	{
        //	This	is	called	when	the	last	Cursor	provided	to	onLoadFinished()	above	is
        //	to	be	clased.	We	need	to	make	sure	we	are	no	longer	using	it.
        mAdapter.swapCursor(null);
    }

    // the interface that must be implemented by the host activity for communicating from the
    // fragment to the activity
    public interface OnSongSelectedListener {
        public void onSongSelected(long id);
    }
}
