package ru.simpra.sms_cleaner;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SmsCleaner extends Activity {

	private String[] regexp = {
			"Услуга.+Кто Звонил?.+этот абонент звонил Вам \\d+ раз",
			"Этот абонент снова в сети",
			"Ваш запрос достален абоненту \\+?\\d+. Осталось запросов: \\d+",
			"От данного абонента пропущено вызовов \\d+",
			"Этот абонент доступен для звонка",
			"Этот абонент звонил.+\\d+ раз",
			"Этот абонент оставил Вам \\d+ нов(ое|ых) голосов(ое|ых) сообщени(я|е|й)",
			"Абонент \\+?\\d+ просит Вас.+перезвонить.+",
			"Абонент \\+?\\d+ не смог Вам дозвониться"
			};
	private ArrayList<String> idList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_cleaner);
        
        ArrayList<ArrayList> List = fetchSMSList();
        ArrayList<String> smsList = List.get(0);
        ListView lViewSMS = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsList);
        lViewSMS.setAdapter(adapter);
        this.idList = List.get(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_sms_cleaner, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	   switch (item.getItemId()) {
    	    case R.id.menu_delete:
    	        DeleteFound();
    	        return true;
    	    default:
    	        return super.onOptionsItemSelected(item);
    	    }
    }
    
    public ArrayList<ArrayList> fetchSMSList()
    {
    	ArrayList<String> smsList = new ArrayList<String>();
    	ArrayList<String> idList = new  ArrayList<String>();
    	ArrayList<ArrayList> List = new  ArrayList<ArrayList>();
    	Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"},null,null,null);
        cursor.moveToFirst();
        while  (cursor.moveToNext())
        {
        	   String address = cursor.getString(1);
               String body = cursor.getString(3);
               String _id = cursor.getString(0);
               for(String s:this.regexp)
               {
	               Pattern p = Pattern.compile(s);
	               Matcher m = p.matcher(body);
	               if (m.find()) {
	            	   String name =  getContactDisplayNameByNumber(address);
	            	   smsList.add(name+"\nПолучено: "+body.substring(0, 30)+"...");
	            	   idList.add(_id);
	               }
               }
        }
        cursor.close();
        List.add(smsList);
        List.add(idList);
    	
    	return List;
    }
    
    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "?";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }
    
    public void DeleteFound()
    {
    	for(int i = 0; i < this.idList.size(); i++) {
    		getContentResolver().delete(Uri.parse("content://sms/" + idList.get(i)),null,null);
        }
    	ListView lViewSMS = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        lViewSMS.setAdapter(adapter);
    }
    
}
