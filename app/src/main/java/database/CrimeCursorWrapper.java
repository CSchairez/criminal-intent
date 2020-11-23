package database;

import android.app.criminalintent.Crime;
import android.database.Cursor;
import android.database.CursorWrapper;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper{
    public CrimeCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Crime getCrime() {
        // methods inherited from class CursorWrapper
        // extracts the data from the Crime Table
        String uuidString = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.SUSPECT));


        // which is used to populate a new Crime object
        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);

        // which gets returned
        return crime;
    }
}
