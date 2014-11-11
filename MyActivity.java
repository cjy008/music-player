package com.example.c5d6e7.musicplayer;

//import com.example.c5d6e7.musicplayer.MusicService.MusicBinder;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class MyActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private SimpleCursorAdapter adapter;
    //private ArrayList<Song> songlist;
    private ListView songView;
    private Button buttonPlay;
    MediaPlayer mPlayer;
    private EditText titleInput, urlInput;
    private Uri dbUri;

    //private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private CommentsDataSource datasource;
    List<Comment> values;
    String FetchComment;
    String[] split;
    int listPosition;


    String url, title;
    //= "http://k007.kiwi6.com/hotlink/1b24q0gi3m/lydia_paek_eyes_nose_lips_cover_mp3galau.com.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        MyActivity.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        final Button updateButton = (Button) findViewById(R.id.UpdateButton);
        final Button query = (Button) findViewById(R.id.QueryButton);
        final Button delete = (Button) findViewById(R.id.DeleteButton);
        Button service = (Button) findViewById(R.id.ServiceButton);
        Button stop = (Button) findViewById(R.id.StopButton);
        titleInput = (EditText) findViewById(R.id.TitleInput);
        urlInput = (EditText) findViewById(R.id.URLInput);

        datasource = new CommentsDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        values = datasource.getAllComments();

        ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
                Log.d("Testone", "pos 1");
                Comment comment = null;
                url = getURL(updateButton);
                Log.d("Testone", "pos 2");
                title = getTitle(updateButton);
                url = "Example 1";
                String[] comments = new String[]{title, url};
                comments[0] = "HELLOW WORLD";
                Log.d("Testone", "pos 3");
                    comment = datasource.createComment(comments[0]);
                Log.d("Testone", "pos 4");
                adapter.add(comment);
                Log.d("Testone", "pos 5");
                adapter.notifyDataSetChanged();
            }
        });

        /**delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
                Comment comment = null;
                comment = (Comment) getListAdapter().getItem(listPosition);
                datasource.deleteComment(comment);
                adapter.remove(comment);
                adapter.notifyDataSetChanged();
            }
        })**/

        buttonPlay = (Button) findViewById(R.id.PlayButton);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonPlay.setText("Downloading...");
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //url = split[1];
                url = getURL(buttonPlay);
                //new DownloadAsync().execute(url);
                try {
                    mPlayer.setDataSource(url);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), "URL Problem", Toast.LENGTH_SHORT).show();
                    buttonPlay.setText("Play");
                    return;
                } catch (SecurityException e) {
                    Toast.makeText(getApplicationContext(), "URL Problem", Toast.LENGTH_SHORT).show();
                    buttonPlay.setText("Play");
                    return;
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), "URL Problem", Toast.LENGTH_SHORT).show();
                    buttonPlay.setText("Play");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    buttonPlay.setText("Play");
                    return;
                }
                try {
                    mPlayer.prepare();
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), "URL Problem", Toast.LENGTH_SHORT).show();
                    buttonPlay.setText("Play");
                    return;
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "URL Problem", Toast.LENGTH_SHORT).show();
                    buttonPlay.setText("Play");
                    return;
                }
                mPlayer.start();
                buttonPlay.setText("Playing");
            }
        });

        query.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                onClickQuery(query);
            }
        });

        stop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                mPlayer.stop();
                buttonPlay.setText("Play");
                Toast.makeText(getApplicationContext(), "Music has stopped playing", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onClickQuery(View view){
         String URL = "content://com.example.c5d6e7.musicplayer/songs";
         Uri title = Uri.parse(URL);
         Cursor c = managedQuery(title, null, null, null, "name");
         String column_url = c.getString(c.getColumnIndex(Database.COLUMN_URL)) + ", " + c.getString(c.getColumnIndex(Database.COLUMN_COMMENT));
         if (c.moveToFirst()) {
             do
                Toast.makeText(this, column_url, Toast.LENGTH_LONG).show();
             while (c.moveToNext());
         }
    }

    protected void onResume(){
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    protected void onPause(){
        datasource.close();
        super.onPause();
//        saveState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDatabase(){
        Intent i = new Intent(this,DatabaseActivity.class );
        startActivity(i);
    }

    protected void onListItemClick(ListView l, View v, int position, long id){
        listPosition = position;
        super.onListItemClick(l, v, position, id);
        FetchComment = values.get(position).getComment();
        split = FetchComment.split("\\s\\s+");
        //Toast.makeText(this, "" + listPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch(item.getItemId()){
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Uri uri = Uri.parse(DatabaseContentProvider.CONTENT_URI + "/" + info.id);
                getContentResolver().delete(uri, null, null);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void fillData(){
        String[] from = new String[] {Database.COLUMN_COMMENT};
        int[] to = new int[] {R.id.URLInput};

        getLoaderManager().initLoader(0,null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.activity_my, null, from, to, 0);
        setListAdapter(adapter);
    }

    private void fillData(Uri uri){
        String[] projection = {Database.COLUMN_COMMENT};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            titleInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(Database.COLUMN_COMMENT)));
            urlInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(Database.COLUMN_URL)));

            cursor.close();
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "DELETE");
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String[] projection = {Database.COLUMN_COMMENT, Database.COLUMN_URL};
        CursorLoader cursorLoader = new CursorLoader(this, DatabaseContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data ){
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader){
        adapter.swapCursor(null);
    }

    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //saveState();
        outState.putParcelable(DatabaseContentProvider.CONTENT_ITEM_TYPE, dbUri);
    }

    private void saveState(){
        String sTitle = titleInput.getText().toString();
        String sUrl = urlInput.getText().toString();

        if(sUrl.length() == 0 && sTitle.length() == 0){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Database.COLUMN_COMMENT, sTitle);
        values.put(Database.COLUMN_URL, sUrl);

        if(dbUri == null){
            dbUri = getContentResolver().insert(DatabaseContentProvider.CONTENT_URI, values);
        }
        else{
            getContentResolver().update(dbUri, values, null, null);
        }
    }

    public String getTitle(View v){
        title = titleInput.getText().toString();
        title.trim();
        if(title == "" || title == null || title == " "){
            Toast.makeText(this, "Invalid Title", Toast.LENGTH_SHORT).show();
            return null;
        }
        else {
            Toast.makeText(this, title + " added to Song List(Title)", Toast.LENGTH_SHORT).show();
        }
        titleInput.setText("");
        return title;
    }

    public String getURL(View v){
        url = urlInput.getText().toString();
        url.trim();
        if(url == "" || url == null || url == " ") {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            return null;
        }
        else {
            Toast.makeText(this, url + " added to Song List(URL)", Toast.LENGTH_SHORT).show();
        }
        urlInput.setText("");
        return url;
    }

}
