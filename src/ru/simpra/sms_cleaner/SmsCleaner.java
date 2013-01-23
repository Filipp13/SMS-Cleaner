package ru.simpra.sms_cleaner;

import java.util.ArrayList;
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
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SmsCleaner extends Activity {

	final private String[] regexp = {
			"Услуга.+Кто Звонил?.+этот абонент звонил Вам \\d+ раз",
			"Этот абонент снова в сети",
			"Ваш запрос достален абоненту \\+?\\d+. Осталось запросов: \\d+",
			"От данного абонента пропущено вызовов \\d+",
			"Этот абонент доступен для звонка",
			"Этот абонент звонил.+\\d+ раз",
			"Этот абонент оставил Вам \\d+ нов(ое|ых) голосов(ое|ых) сообщени(я|е|й)",
			"Абонент \\+?\\d+ просит Вас.+перезвонить.+",
			"Абонент \\+?\\d+ не смог Вам дозвониться" };
	private ArrayList<String> idList = new ArrayList<String>();
	private ArrayList<String> smsList = new ArrayList<String>();
	private int smsQ = 0;
	private ListView lViewSmS;
	ArrayAdapter<String> adapter;
	private ProgressDialog pd;
	
	@SuppressLint("HandlerLeak")
	private Handler h = new Handler() {
        @Override
            public void handleMessage(Message msg) {
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
		System.out.println("pd");
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	public void fetchSMSList() {

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
	      				Pattern p = Pattern.compile(s);
	      				Matcher m = p.matcher(body);
	      				if (m.find()) {
	      					String name = getContactDisplayNameByNumber(address);
	      					smsList.add(name + "\n"+getString(R.string.received)+": " + body.substring(0,30)
	      							+ "...");
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
		String name = "?";

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
