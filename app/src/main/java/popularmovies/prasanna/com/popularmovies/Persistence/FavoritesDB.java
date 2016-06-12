package popularmovies.prasanna.com.popularmovies.Persistence;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Prasanna on 5/3/2016.
 */

@Database(version = FavoritesDB.DATABASE_VERSION)
public final class FavoritesDB {
    public static final int DATABASE_VERSION = 1;

    @Table(DBContract.MovieEntry.class)
    public static final String TABLE_NAME = "favmovies";

}