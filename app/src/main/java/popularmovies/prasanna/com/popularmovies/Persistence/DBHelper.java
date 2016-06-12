package popularmovies.prasanna.com.popularmovies.Persistence;

/**
 * Created by Prasanna on 6/12/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FavMovies.db";

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_MOVIE_TABLE = "CREATE TABLE " + DBContract.MovieEntry.TABLE_NAME + " (" +
                DBContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                DBContract.MovieEntry.COL_TMDB_ID + " INTEGER UNIQUE NOT NULL, " +
                DBContract.MovieEntry.COL_TITLE + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_DESC + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_POSTER_PATH + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_REL_DATE + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_BACKDROP_PATH + " TEXT NOT NULL, " +
                DBContract.MovieEntry.COL_VOTE_AVG + " TEXT NOT NULL" +
                " );";

        db.execSQL(CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
        //IDK if necessary
    }
}