package com.joelotter.raspberry_fi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String fbId;
	private String search;
	private static String ip;
	private Activity act;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        act = this;
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fbId = extras.getString("fbId");
            ip = "http://" + extras.getString("ip");
        }
        new getQueue().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if (item.getItemId() == R.id.menu_refresh){
    		new getQueue().execute();
    	}
    	if (item.getItemId() == R.id.menu_add){
    		addSong();
    	}
    	return false;
    }
    
    public void addSong(){
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add to queue");
    	alert.setMessage("Enter your search term below.");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);
    	alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  search = input.getText().toString();
    	  new send().execute();
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    // Canceled.
    	  }
    	});

    	alert.show();
    }
    
    public static Bitmap loadBitmap(String url) {
    	Bitmap bitmap = null;
    	try {
    		  bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
    		} catch (MalformedURLException e) {
    		  e.printStackTrace();
    		} catch (IOException e) {
    		  e.printStackTrace();
    		}
		return bitmap;
    }
    
    class getQueue extends AsyncTask<String, Void, ListAdapter>{
    	
    	ArrayList<Song> songs = new ArrayList<Song>();
    	String nowName;
    	String nowArtist;
    	String nowPhotoUrl;
    	String nowFacebook;
    	Bitmap bmp;

		@Override
		protected ListAdapter doInBackground(String...arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			// Creating HTTP Post
			HttpGet httpGet = new HttpGet(ip + "/update");

			// Making HTTP Request
			try {
				HttpResponse response = httpClient.execute(httpGet);
				String rec = EntityUtils.toString(response.getEntity());

				// writing response to log
				Log.d("Http Response:", rec);
				
				//Parse the json
				JSONArray js = new JSONArray(rec);
				
				//Update the 'now playing' bit
				JSONObject song1 = js.getJSONObject(0);
				nowName = song1.getString("name");
				nowArtist = song1.getString("artist");
				nowPhotoUrl = song1.getString("photo_url");
				nowFacebook = song1.getString("facebook_name");
				bmp = MainActivity.loadBitmap(nowPhotoUrl);
				
				for (int j=1; j < js.length(); j++){
					JSONObject rel = js.getJSONObject(j);
					String name = rel.getString("name");
					String artist = rel.getString("artist");
					String fb = rel.getString("facebook_name");
					String songId = rel.getString("song_id");
					int up = rel.getInt("upvotes");
					int down = rel.getInt("downvotes");
					songs.add(new Song(name, artist, fb, up, down, songId));
				}
			} catch (ClientProtocolException e) {
				// writing exception to log
				e.printStackTrace();
			} catch (IOException e) {
				// writing exception to log
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ListAdapter adapter = new SongAdapter(getBaseContext(), R.layout.list_item, songs, fbId, ip, act);
			
			return adapter;
		}
		
		@Override
        protected void onPostExecute(ListAdapter result) {
			ListView list = ((ListView) findViewById(R.id.queue));
			list.setAdapter(result);
			((ImageView) findViewById(R.id.playingImg)).setImageBitmap(bmp);
			((TextView) findViewById(R.id.playingArtist)).setText(nowArtist);
			((TextView) findViewById(R.id.playingTitle)).setText(nowName);
			((TextView) findViewById(R.id.playingUser)).setText(nowFacebook);
        }
	}
    
    private class send extends AsyncTask<String, Void, ListAdapter>{
    	
    	ArrayList<Song> songs = new ArrayList<Song>();
    	String nowName;
    	String nowArtist;
    	String nowPhotoUrl;
    	String nowFacebook;
    	Bitmap bmp;

		@Override
		protected ListAdapter doInBackground(String...arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			// Creating HTTP Post
			HttpPost httpPost = new HttpPost(ip + "/add");

			// Building post parameters
			// key and value pair
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("facebook_id", fbId));
			nameValuePair.add(new BasicNameValuePair("name", search));

			// Url Encoding the POST parameters
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {
				// writing error to Log
				e.printStackTrace();
			}

			// Making HTTP Request
			try {
				HttpResponse response = httpClient.execute(httpPost);
				String rec = EntityUtils.toString(response.getEntity());

				// writing response to log
				Log.d("Http Response:", rec);
				//Parse the json
				JSONArray js = new JSONArray(rec);
				
				//Update the 'now playing' bit
				JSONObject song1 = js.getJSONObject(0);
				nowName = song1.getString("name");
				nowArtist = song1.getString("artist");
				nowPhotoUrl = song1.getString("photo_url");
				nowFacebook = song1.getString("facebook_name");
				bmp = MainActivity.loadBitmap(nowPhotoUrl);
				
				for (int j=1; j < js.length(); j++){
					JSONObject rel = js.getJSONObject(j);
					String name = rel.getString("name");
					String artist = rel.getString("artist");
					String fb = rel.getString("facebook_name");
					String songId = rel.getString("song_id");
					int up = rel.getInt("upvotes");
					int down = rel.getInt("downvotes");
					songs.add(new Song(name, artist, fb, up, down, songId));
				}
			} catch (ClientProtocolException e) {
				// writing exception to log
				e.printStackTrace();
			} catch (IOException e) {
				// writing exception to log
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ListAdapter adapter = new SongAdapter(getBaseContext(), R.layout.list_item, songs, fbId, ip, act);
			
			return adapter;
		}
		
		@Override
        protected void onPostExecute(ListAdapter result) {
			ListView list = ((ListView) findViewById(R.id.queue));
			list.setAdapter(result);
			((ImageView) findViewById(R.id.playingImg)).setImageBitmap(bmp);
			((TextView) findViewById(R.id.playingArtist)).setText(nowArtist);
			((TextView) findViewById(R.id.playingTitle)).setText(nowName);
			((TextView) findViewById(R.id.playingUser)).setText(nowFacebook);
        }
		
	}
}
