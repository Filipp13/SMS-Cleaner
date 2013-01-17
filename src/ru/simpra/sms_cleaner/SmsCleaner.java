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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_cleaner);
		this.lViewSmS = (ListView) findViewById(R.id.list);
		fetchSMSList();
		populateSmSList(this.smsList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sms_cleaner, menu);
		return true;
	}

	private void populateSmSList(ArrayList<String> smsList) {
		if (smsList.size() > 0) {
			TextView listHeader = (TextView) findViewById(R.id.listHeader);
			listHeader.append("Найдено: " + this.idList.size());
		} else {

		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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

	private ProgressDialog startDialog(String msg, int maxVal) {
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(msg);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0); // set percentage completed to 0%
		progressDialog.setMax(maxVal);
		progressDialog.show();
		return progressDialog;
	}

	public void fetchSMSList() {

		
		
		Uri uriSms = Uri.parse("content://sms/inbox");
		final Cursor smsLookup = getContentResolver().query(uriSms,
				new String[] { "_id", "address", "date", "body" }, null, null,
				null);
		this.smsQ = smsLookup.getCount();
		final ProgressDialog pd = startDialog("Мы начинаем КВН", this.smsQ);
		final Handler responseHandler = new Handler() 
        {                               
            public void handleMessage(Message msg)  
            {
                super.handleMessage(msg);
                try 
                {
                    pd.dismiss();
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }                       
            }
        };
		
		Thread thread = new Thread(new Runnable() {    
            @Override
            public void run() {
                //downloading code
                responseHandler.sendEmptyMessage(0);
            }
         });
         thread.start();
		
		smsLookup.moveToFirst();
		int i = 1;
		while (smsLookup.moveToNext()) {
			i++;
			String address = smsLookup.getString(1);
			String body = smsLookup.getString(3);
			String _id = smsLookup.getString(0);
			for (String s : regexp) {
				Pattern p = Pattern.compile(s);
				Matcher m = p.matcher(body);
				if (m.find()) {
					String name = getContactDisplayNameByNumber(address);
					smsList.add(name + "\nПолучено: " + body.substring(0, 30)
							+ "...");
					idList.add(_id);
				}
			}
		}
		smsLookup.close();
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
				// String contactId =
				// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	public void deleteFound() {
		
		for (int i = 0; i < this.idList.size(); i++) {
			// getContentResolver().delete(Uri.parse("content://sms/" +
			// idList.get(i)),null,null);
		}
		populateSmSList(new ArrayList<String>());
	}

}
