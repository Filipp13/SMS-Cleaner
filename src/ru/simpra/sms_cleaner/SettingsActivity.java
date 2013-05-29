package ru.simpra.sms_cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ru.simpra.sms_cleaner.SMSCleanerDB;

public class SettingsActivity extends Activity {

	private List<String> regexp = new ArrayList<String>();
	private ListView lViewTemplates;
	ArrayAdapter<String> adapter;

	SMSCleanerDB dbh;
	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		final Button button = (Button) findViewById(R.id.addNewRegexp);
		this.lViewTemplates = (ListView) findViewById(R.id.list_templates);
		dbh = new SMSCleanerDB(this);
		db = dbh.getWritableDatabase();
		
		
		lViewTemplates.setOnItemLongClickListener(new OnItemLongClickListener() {

		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view,
		          int position, long id) {
		    	  deleteRegexpr(position);
		        return true;
		      }
		    });
		  
		fetchTemplates();
		populateTemplatesList(new ArrayList<String>(regexp));
		
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickListener(v);
            }
        });
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (item.getItemId()) {
		case R.id.menu_help:
			 Intent intent = new Intent(this, HelpActivity.class);
		     startActivity(intent);
		     return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	private void populateTemplatesList(ArrayList<String> templates)
	{
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, templates);
		this.lViewTemplates.setAdapter(adapter);
	}
	
	
	private void fetchTemplates()
	{
		regexp.clear();
		Cursor c = db.query(SMSCleanerDB.DB_TABLE_REGEXPR, new String[] {SMSCleanerDB.DB_COLUMN_NAME_REGEXPR}, null, null, null, null, SMSCleanerDB.DB_COLUMN_NAME_ID + " DESC");
		while (c.moveToNext()) {
			String str = counterReplaceRegexp(c.getString(0));
			regexp.add(str);
		}
	}
	
	private String replaceRegexp(String str)
	{
		str = str.replace("%N+%", "\\d+");
		str = str.replace("%N%", "\\d");
		str = str.replace(".", "%dot%");
		str = str.replace("%A+%", ".+");
		str = str.replace("%A%", ".");
		str = str.replace("%dot%", ".");
		str = str.replace("?", "\\?");
		str = str.replace("^", "\\^");
		str = str.replace("$", "\\$");
		str = str.replace("(", "\\(");
		str = str.replace(")", "\\)");
		str = str.replace("*", "\\*");
		return str;
	}
	
	private String counterReplaceRegexp(String str)
	{
		str = str.replace("\\d+", "%N+%");
		str = str.replace("\\d", "%N%");
		str = str.replace(".", "%dot%");
		str = str.replace(".+", "%A+%");
		str = str.replace(".", "%A%");
		str = str.replace("%dot%", ".");
		str = str.replace("\\?", "?");
		str = str.replace("\\^", "^");
		str = str.replace("\\$", "$");
		str = str.replace("\\(", "(");
		str = str.replace("\\)", ")");
		str = str.replace("\\*", "*");
		return str;
	}
	
	private String cleanRegexp(String str)
	{
		str = str.replace("\\", "");
		str = str.replace("\'", "");
		str = str.replace("\"", "");
		return str;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return true;
	}
	
	void onClickListener(View target)
	{
		switch (target.getId()) {
		case R.id.addNewRegexp:
			addRegexp();
		}
	}
	
	private void addRegexp()
	{
		TextView regexprView = (TextView) findViewById(R.id.newRegexp);
    	String regexpr = replaceRegexp(cleanRegexp(regexprView.getText().toString().trim()));
		if(regexpr.length() > 0)
		{
			ContentValues cv = new ContentValues();
			db.beginTransaction();
			cv.put(SMSCleanerDB.DB_COLUMN_NAME_REGEXPR, regexpr);
			db.insert(SMSCleanerDB.DB_TABLE_REGEXPR, null, cv);
			db.setTransactionSuccessful();
			db.endTransaction();
			Toast.makeText(getApplicationContext(), R.string.regexpr_added, Toast.LENGTH_LONG).show();
			regexp.add(0,counterReplaceRegexp(regexpr));
			populateTemplatesList(new ArrayList<String>(regexp));
		}
		else
		{
			Toast.makeText(getApplicationContext(), R.string.wrong_regexp, Toast.LENGTH_LONG).show();
		}
		
		regexprView.setText("");
	}
	
	private void deleteRegexpr(int position)
	{
		Object o = lViewTemplates.getItemAtPosition(position);
		String str = (String)o;
		str = replaceRegexp(str);
		db.beginTransaction();
		int i = db.delete(SMSCleanerDB.DB_TABLE_REGEXPR, SMSCleanerDB.DB_COLUMN_NAME_REGEXPR + " = \"" + str + "\"", null);
		if(i > 0);
		{
			db.setTransactionSuccessful();
			
		}
		db.endTransaction();
		if(i > 0)
		{
			fetchTemplates();
			populateTemplatesList(new ArrayList<String>(regexp));
			Toast.makeText(getApplicationContext(), R.string.regexpr_deleted, Toast.LENGTH_LONG).show();
		}
		
  	  	
	}

}
