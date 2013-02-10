package com.joelotter.raspberry_fi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class Login extends Activity{
	
	Facebook facebook = new Facebook("490522407651671");
	private SharedPreferences mPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
		
		authFb();
	}
	
	public void authFb(){
    	if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {"publish_stream"}, new DialogListener() {
                public void onComplete(Bundle values) {
                	SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }
    
                public void onFacebookError(FacebookError error) {
                	Log.d("FacebookError", error.getMessage());
                	Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
    
                public void onError(DialogError e) {
                	Log.d("Error", e.getMessage());
                	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
    
                public void onCancel() {
                	finish();
                }
            });
        }
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Connect");
    	alert.setMessage("Enter the [IP]:[PORT] of the Pi-Fi.");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);
    	alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  String ip = input.getText().toString();
    	  fbLogin(ip);
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    finish();
    	  }
    	});

    	alert.show();
    }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
	
	public void fbLogin(final String ipStr){
		
		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
		mAsyncRunner.request("me", new RequestListener(){

			@Override
			public void onComplete(String response, Object state) {
				JSONObject json = null;
				try {
					json = Util.parseJson(response);
					sendToServer(json.getString("id"), ipStr);
				} catch (FacebookError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void sendToServer(String toSend, String ip){
		Intent newIntent = new Intent(this, MainActivity.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		newIntent.putExtra("fbId", toSend);
		newIntent.putExtra("ip", ip);
        this.startActivity(newIntent);
        this.finish();
	}
}