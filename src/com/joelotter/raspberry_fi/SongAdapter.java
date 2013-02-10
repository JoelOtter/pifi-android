package com.joelotter.raspberry_fi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SongAdapter extends ArrayAdapter<Song> {

public SongAdapter(Context context, int textViewResourceId) {
    super(context, textViewResourceId);
    // TODO Auto-generated constructor stub
}

private List<Song> songs;
private String fbId;
private String ip;
private String songId;
private Activity act;

public SongAdapter(Context context, int resource, List<Song> items, String fbId, String ip, Activity act) {

    super(context, resource, items);

    this.songs = items;
    this.ip = ip;
    this.fbId = fbId;
    this.act = act;

}

@Override
public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {

        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        v = vi.inflate(R.layout.list_item, null);

    }

    Song p = songs.get(position);

    if (p != null) {

        TextView name = (TextView) v.findViewById(R.id.queueTitle);
        TextView artist = (TextView) v.findViewById(R.id.queueArtist);
        TextView id = (TextView) v.findViewById(R.id.queueUser);
        TextView up = (TextView) v.findViewById(R.id.upText);
        TextView down = (TextView) v.findViewById(R.id.downText);
        ImageView upBut = (ImageView) v.findViewById(R.id.upBut);
        ImageView downBut = (ImageView) v.findViewById(R.id.downBut);
        upBut.setTag(p.getSongId());
        upBut.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
				songId = v.getTag().toString();
				new upvote().execute();
			}
        });
        downBut.setTag(p.getSongId());
        downBut.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
				songId = v.getTag().toString();
				new downvote().execute();
			}
        });

        if (name != null) {
            name.setText(p.getName());
        }
        if (artist != null) {
            artist.setText(p.getArtist());
        }
        if (id != null) {
            id.setText(p.getId());
        }
        if (up != null) {
            up.setText(p.getUp());
        }
        if (down != null) {
            down.setText(p.getDown());
        }
    }

    return v;

}

private class upvote extends AsyncTask<String, Void, ListAdapter>{
	
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
		HttpPost httpPost = new HttpPost(ip + "/upvote");

		// Building post parameters
		// key and value pair
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		nameValuePair.add(new BasicNameValuePair("facebook_id", fbId));
		nameValuePair.add(new BasicNameValuePair("song_id", songId));

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
		ListAdapter adapter = new SongAdapter(getContext(), R.layout.list_item, songs, fbId, ip, act);
		
		return adapter;
	}
	
	@Override
    protected void onPostExecute(ListAdapter result) {
		ListView list = ((ListView) act.findViewById(R.id.queue));
		list.setAdapter(result);
		((ImageView) act.findViewById(R.id.playingImg)).setImageBitmap(bmp);
		((TextView) act.findViewById(R.id.playingArtist)).setText(nowArtist);
		((TextView) act.findViewById(R.id.playingTitle)).setText(nowName);
		((TextView) act.findViewById(R.id.playingUser)).setText(nowFacebook);
    }
	
}

private class downvote extends AsyncTask<String, Void, ListAdapter>{
	
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
		HttpPost httpPost = new HttpPost(ip + "/downvote");

		// Building post parameters
		// key and value pair
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		nameValuePair.add(new BasicNameValuePair("facebook_id", fbId));
		nameValuePair.add(new BasicNameValuePair("song_id", songId));

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
		ListAdapter adapter = new SongAdapter(getContext(), R.layout.list_item, songs, fbId, ip, act);
		
		return adapter;
	}
	
	@Override
    protected void onPostExecute(ListAdapter result) {
		ListView list = ((ListView) act.findViewById(R.id.queue));
		list.setAdapter(result);
		((ImageView) act.findViewById(R.id.playingImg)).setImageBitmap(bmp);
		((TextView) act.findViewById(R.id.playingArtist)).setText(nowArtist);
		((TextView) act.findViewById(R.id.playingTitle)).setText(nowName);
		((TextView) act.findViewById(R.id.playingUser)).setText(nowFacebook);
    }
	
}}
