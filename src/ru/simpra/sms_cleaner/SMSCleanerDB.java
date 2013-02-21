package ru.simpra.sms_cleaner;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SMSCleanerDB extends SQLiteOpenHelper
{
	final public static String DB_TABLE_REGEXPR = "regexpr";
	
	final public static String DB_COLUMN_NAME_ID = "_id";
	final public static String DB_COLUMN_NAME_REGEXPR = "regexpr";
	
	final private static int DB_VERSION = 1;
	
	
	final String LOG_TAG = "SMSCLeanerDBLog";
	final private String[] regexp = {
			"������.+��� ������?.+���� ������� ������ ��� \\d+ ���",
			"���� ������� ����� � ����",
			"��� ������ �������� �������� \\+?\\d+. �������� ��������: \\d+",
			"�� ������� �������� ��������� ������� \\d+",
			"���� ������� �������� ��� ������",
			"���� ������� ������.+\\d+ ���",
			"���� ������� ������� ��� \\d+ ���(��|��) �������(��|��) ��������(�|�|�)",
			"������� \\+?\\d+ ������ ���.+�����������.+",
			"������� \\+?\\d+ �� ���� ��� �����������" };
	
	public SMSCleanerDB(Context context)
	{
		super(context, "SMSCleaner", null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		
		db.execSQL(
				"create table " + DB_TABLE_REGEXPR + "("
				+ DB_COLUMN_NAME_ID + " integer primary key autoincrement,"
				+ DB_COLUMN_NAME_REGEXPR + " text);"
				);

		db.beginTransaction();
		for (String s : regexp) {
			cv.put("regexpr", s);
			db.insert(SMSCleanerDB.DB_TABLE_REGEXPR, null, cv);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
