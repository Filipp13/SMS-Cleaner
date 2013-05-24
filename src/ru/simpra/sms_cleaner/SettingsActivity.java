package ru.simpra.sms_cleaner;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
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
			regexp.add(c.getString(0));
		}
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
		String regexpr = regexprView.getText().toString();
		ContentValues cv = new ContentValues();
		db.beginTransaction();
		cv.put(SMSCleanerDB.DB_COLUMN_NAME_REGEXPR, regexpr);
		db.insert(SMSCleanerDB.DB_TABLE_REGEXPR, null, cv);
		db.setTransactionSuccessful();
		db.endTransaction();
		Toast.makeText(getApplicationContext(), R.string.regexpr_added, Toast.LENGTH_LONG).show();
		regexp.add(0,regexpr);
		regexprView.setText("");
		populateTemplatesList(new ArrayList<String>(regexp));
	}
	
	private void deleteRegexpr(int position)
	{
		Object o = lViewTemplates.getItemAtPosition(position);
		String str = (String)o;
		db.beginTransaction();
		db.delete(SMSCleanerDB.DB_TABLE_REGEXPR, SMSCleanerDB.DB_COLUMN_NAME_REGEXPR + " = '" + str + "'", null);
		db.setTransactionSuccessful();
		db.endTransaction();
		fetchTemplates();
  	  	Toast.makeText(getApplicationContext(), R.string.regexpr_deleted, Toast.LENGTH_LONG).show();
  	    populateTemplatesList(new ArrayList<String>(regexp));
	}

}
