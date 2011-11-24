package de.android.test1;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Test1Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onClickOk(View v) {
    	Intent i = new Intent(Test1Activity.this, NextActivity.class);
    	this.startActivity(i);
    }
    
    public void onClickLogin(View v) {
    	HttpClient httpclient = new DefaultHttpClient();
    	
    	//RESTful WebService
    	HttpPost httppost = new HttpPost("http://10.208.184.41/userfront.php/api/51,32/0,5/gpsads.xml");
    	
    	EditText uname = (EditText)findViewById(R.id.username);
        String username = uname.getText().toString();
        
        EditText pword = (EditText)findViewById(R.id.password);
        String password = pword.getText().toString();
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));





    	Intent i = new Intent(Test1Activity.this, NextActivity.class);
    	this.startActivity(i);
    }
    
    public void onClickCancel(View v) {
    	finish();
    }
}