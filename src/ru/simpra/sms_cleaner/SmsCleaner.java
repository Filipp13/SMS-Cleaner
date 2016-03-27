package ru.simpra.sms_cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ru.simpra.sms_cleaner.SMSCleanerDB;

public class SmsCleaner extends Activity {

	private List<String> regexp = new ArrayList<String>();
	private ArrayList<String> idList = new ArrayList<String>();
	private ArrayList<String> smsList = new ArrayList<String>();
	private int smsQ = 0;
	private ListView lViewSmS;
	private Pattern p;
	ArrayAdapter<String> adapter;
	private ProgressDialog pd;
	SMSCleanerDB dbh;
	SQLiteDatabase db;
	
	@SuppressLint("HandlerLeak")
	private Handler h = new Handler() {
        @Override
            public void handleMessage(Message msg) {
			//testt
        	try
        	{
        		stopProgress();
	        	populateSmSList(smsList);
        	}
        	catch(Exception e)
        	{
        		stopProgress();
        	}
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_cleaner);
		this.lViewSmS = (ListView) findViewById(R.id.list);
		dbh = new SMSCleanerDB(this);
		db = dbh.getWritableDatabase();
		
		fetchSMSList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sms_cleaner, menu);
		return true;
	}

	private ProgressDialog startProgress(String title, String message, int max)
	{
		pd = new ProgressDialog(this);
		pd.setMax(max);
		pd.setTitle(title);
		pd.setMessage(message);
		pd.setProgress(0);
		pd.setIndeterminate(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
		return pd;
	}


	private void updateProgress(int progress)
	{
		  final int j = progress;
		  runOnUiThread(new Runnable() {
			  public void run(){
			  pd.setProgress(j);
			  }
		  });   
		
	}
	
	private void stopProgress()
	{
		pd.dismiss();
	}

	private void populateSmSList(ArrayList<String> smsList) {
		TextView listHeader = (TextView) findViewById(R.id.listHeader);
		if (smsList.size() > 0) {
			listHeader.setText(getString(R.string.found)+": " + this.idList.size());
		} else {
			listHeader.setText(getString(R.string.deleted)+": " + this.idList.size());
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, smsList);
		this.lViewSmS.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (item.getItemId()) {
		case R.id.menu_delete:
			deleteFound();
			return true;
		case R.id.menu_refresh:
			fetchSMSList();
			return true;
		case R.id.menu_settings:
			 Intent intentSettings = new Intent(this, SettingsActivity.class);
		     startActivity(intentSettings);
		     return true;
		case R.id.menu_help:
			 Intent intentHelp = new Intent(this, HelpActivity.class);
		     startActivity(intentHelp);
		     return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void fetchSMSList() {

		smsList.clear();
		idList.clear();
		regexp.clear();
		Cursor c = db.query(SMSCleanerDB.DB_TABLE_REGEXPR, new String[] {SMSCleanerDB.DB_COLUMN_NAME_REGEXPR}, null, null, null, null, null);
		while (c.moveToNext()) {
			regexp.add(c.getString(0));
		}
		Uri uriSms = Uri.parse("content://sms/inbox");
  		final Cursor smsLookup = getContentResolver().query(uriSms,
  				new String[] { "_id", "address", "date", "body" }, null, null,
  				new String("date ASC"));
  		smsQ = smsLookup.getCount();
  		
  		startProgress(getString(R.string.searching_title), getString(R.string.searching_message), smsQ);

		Thread thread = new Thread() {
	          public void run() {
	      		smsLookup.moveToFirst();
	      		int i = 1;
	      		while (smsLookup.moveToNext()) {
	      			i++;
	      			String address = smsLookup.getString(1);
	      			String body = smsLookup.getString(3);
	      			String _id = smsLookup.getString(0);
	      			updateProgress(i);
	      			for (String s : regexp) {
	      				try{
	      					p = Pattern.compile(s);
	      				
	      				}
	      				catch(Exception e)
	      				{
	      					continue;
	      				}
	      				Matcher m = p.matcher(body);
	      				if (m.find()) {
	      					String name = getContactDisplayNameByNumber(address);
	      					if(body.length()>30)
	      					{
	      						body = body.substring(0,30) +  "...";
	      					}
	      					smsList.add(name + "\n"+getString(R.string.received)+": " + body);
	      					idList.add(_id);
	      				}
	      			}
	      		}
	      		smsLookup.close();          
	      		h.sendEmptyMessage(0);
	          }
	      };
	      thread.start();
	}

	public String getContactDisplayNameByNumber(String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = number;

		ContentResolver contentResolver = getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);
		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	public void deleteFound() {
		startProgress(getString(R.string.deleting_title), getString(R.string.deleting_message), idList.size());
		Thread thread = new Thread() {
	          public void run() {
	        	  for (int i = 0; i < idList.size(); i++) {
	      			  getContentResolver().delete(Uri.parse("content://sms/" +
	      			  idList.get(i)),null,null);
	      			  updateProgress(i);         
		          }
	        	  h.sendEmptyMessage(0);
	          }
	      };
	      thread.start();
	      smsList = new ArrayList<String>();
	}
}
