package android.app.criminalintent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.CrimeCursorWrapper;
import database.CrimeDbSchema;

import static android.app.criminalintent.CrimeFragment.TAG;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;
    private Context mContext;  // will use in ch16
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context){
        //mCrimes = new ArrayList<>();
        // stores the context for later use.
        mContext = context.getApplicationContext();

        // causes CrimeBaseHelper to do some important work...
        // gets us a reference to an actual database!
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public static CrimeLab get(Context context){
        if(sCrimeLab == null)
            sCrimeLab = new CrimeLab(context);
        return sCrimeLab;
    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeDbSchema.CrimeTable.NAME, null, values);
    }

    public List<Crime> getCrimes(){
        // return new ArrayList<>(); this is replaced with:
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = (CrimeCursorWrapper) queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }
        return crimes;
    }

    public void removeCrime(UUID id){
        Crime c  = getCrime(id);
        getCrimes().remove(c);
        mDatabase.execSQL("DELETE FROM " + CrimeDbSchema.CrimeTable.NAME+ " WHERE " +
                CrimeDbSchema.CrimeTable.Cols.UUID +
                " = " + "'"+c.getId().toString()+"'" );

    }

    public Crime getCrime(UUID id){
        // return null; replace this with:
        CrimeCursorWrapper cursor = (CrimeCursorWrapper) queryCrimes(
                CrimeDbSchema.CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try{
            if(cursor.getCount() == 0)  // if crime wasn't found
                return null;

            cursor.moveToFirst();       // go to top row
            return cursor.getCrime();   // get it and return it
        }
        finally{
            cursor.close();             // close the door
        }

    }

    public File getPhotoFile(Crime crime){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFileName());
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();

        values.put(CrimeDbSchema.CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeDbSchema.CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeDbSchema.CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeDbSchema.CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeDbSchema.CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    public void updateCrime(Crime c){
        String uuidString = c.getId().toString();
        ContentValues values = getContentValues(c);

        mDatabase.update(
                CrimeDbSchema.CrimeTable.NAME,      // name of table with row to update
                values,                             // the values to update row with
                // building the where clause for the SQL command
                CrimeDbSchema.CrimeTable.Cols.UUID + " = ?", new String[] { uuidString }
        );
    }

    private Cursor queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeDbSchema.CrimeTable.NAME,
                null, // selects ALL columns
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new CrimeCursorWrapper(cursor);
    }


}
