package ru.simpra.sms_cleaner;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ru.simpra.sms_cleaner.SMSCleanerDB;

public class SettingsActivity extends Activity {

	SMSCleanerDB dbh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		final Button button = (Button) findViewById(R.id.addNewRegexp);
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickListener(v);
            }
        });
		
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
		dbh = new SMSCleanerDB(this);
		SQLiteDatabase db = dbh.getWritableDatabase();
		TextView regexprView = (TextView) findViewById(R.id.newRegexp);
		String regexpr = regexprView.getText().toString();
		Log.d("SETTINGS", regexpr);
		ContentValues cv = new ContentValues();
		cv.put(SMSCleanerDB.DB_COLUMN_NAME_REGEXPR, regexpr);
		db.beginTransaction();
		Log.d("DB", "id "+ db.insert(SMSCleanerDB.DB_TABLE_REGEXPR, null, cv));
		db.setTransactionSuccessful();
		db.endTransaction();
	}

}
