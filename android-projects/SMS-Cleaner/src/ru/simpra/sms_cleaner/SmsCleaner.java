package ru.simpra.sms_cleaner;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SmsCleaner extends Activity {

	private String[] regexp = {
			"Услуга.+Кто Звонил?.+этот абонент звонил Вам \\d+ раз",
			"Этот абонент снова в сети"
			};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_cleaner);
        
        ArrayList<ArrayList> List = fetchSMSList();
        ArrayList<String> smsList = List.get(0);
        ListView lViewSMS = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsList);
        lViewSMS.setAdapter(adapter);
        ArrayList<String> idList = List.get(1);
        for(int i = 0; i < idList.size(); i++) {
        	System.out.println(idList.get(i));
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_sms_cleaner, menu);
        return true;
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
	            	   smsList.add("Address=> "+address+"\nSMS=> "+body);
	            	   idList.add(_id);
	               }
               }
        }
        cursor.close();
        List.add(smsList);
        List.add(idList);
    	
    	return List;
    }
    
    public void DeleteFound(ArrayList<String> idList)
    {
    	
    }
    
}
