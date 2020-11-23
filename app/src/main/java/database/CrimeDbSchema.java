package database;

// the class that defines the schema
public class CrimeDbSchema {

    // contains the class that defines the table
    public static final class CrimeTable {

        // the table name
        public static final String NAME = "crimes";

        //contains the class that defines the columns
        public static final class Cols {
            // column names
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
        }
    }
}
