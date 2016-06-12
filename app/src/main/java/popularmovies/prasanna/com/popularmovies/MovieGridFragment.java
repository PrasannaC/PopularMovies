package popularmovies.prasanna.com.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import popularmovies.prasanna.com.popularmovies.Model.Movie;
import popularmovies.prasanna.com.popularmovies.Model.MovieResults;
import popularmovies.prasanna.com.popularmovies.Persistence.DBContract;
import popularmovies.prasanna.com.popularmovies.Persistence.DBHelper;
import popularmovies.prasanna.com.popularmovies.Persistence.FavoritesProvider;
import popularmovies.prasanna.com.popularmovies.WebServices.WebHelper;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Prasanna on 4/27/2016.
 */
public class MovieGridFragment extends Fragment {

    public List<Movie> movies = null;
    public GridView movieGridView;
    public GridAdapter movieGridAdapter;

    public MovieGridFragment() {
        // constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FetchMoviesTask asyncTask = new FetchMoviesTask(getContext());
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.movie_grid, container, false);
        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            asyncTask.execute(FetchMoviesTask.SORT_BY_POPULARITY);
        } else {
            Toast.makeText(getContext(), "Network Unavailable", Toast.LENGTH_SHORT).show();
        }


        movieGridView = (GridView) v.findViewById(R.id.movie_gridView);
        if (movies == null) {

        }
        movieGridAdapter = new GridAdapter(getContext(), new ArrayList<Movie>());
        movieGridView.setAdapter(movieGridAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getContext(), MovieDetailActivity.class);
                i.putExtra("movie", (Movie) adapterView.getItemAtPosition(position));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    List<Pair<View, String>> transitionPairs = new ArrayList<>();
                    transitionPairs.add(Pair.create(view.findViewById(R.id.poster_image), "poster_pic"));
                    transitionPairs.add(Pair.create(view.findViewById(R.id.info_text), "movie_title"));
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(getActivity(), transitionPairs.toArray(new Pair[transitionPairs.size()]));
                    ActivityCompat.startActivity(getActivity(), i, options.toBundle());
                } else {
                    startActivity(i);
                }
            }
        });

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        FetchMoviesTask asyncTask = new FetchMoviesTask(getContext());
        switch (item.getItemId()) {
            case R.id.sort_by_popularity:
                if (networkInfo != null && networkInfo.isConnected()) {
                    asyncTask.execute(FetchMoviesTask.SORT_BY_POPULARITY);
                } else {
                    Toast.makeText(getContext(), "Network Unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sort_by_rating:
                if (networkInfo != null && networkInfo.isConnected()) {
                    asyncTask.execute(FetchMoviesTask.SORT_BY_RATING);
                } else {
                    Toast.makeText(getContext(), "Network Unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.favoritesOnly:
                new DbReader(getContext()).execute();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_menu, menu);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
        public static final String SORT_BY_POPULARITY = "popularity.desc";
        public static final String SORT_BY_RATING = "vote_average.desc";

        boolean networkError = false;
        private Context mContext;

        public FetchMoviesTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Movie> doInBackground(String... args) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            try {
                WebHelper webHelper = retrofit.create(WebHelper.class);
                Call<MovieResults> call = webHelper.getPopularMovies(mContext.getString(R.string.api_key), args[0], (args[0].equals(SORT_BY_RATING) ? "1000" : "0"));
                Response<MovieResults> movieResponse = call.execute();
                return movieResponse.body().getResults();
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof UnknownHostException) {
                    networkError = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
                movieGridAdapter.clear();
                movieGridAdapter.addAll(movies);
                movieGridAdapter.notifyDataSetChanged();
            } else {
                if (networkError) {
                    Toast.makeText(mContext, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class DbReader extends AsyncTask<Void, Void, List<Movie>> {

        private Context mContext;

        public DbReader(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {

            DBHelper dbHelper = new DBHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            List<Movie> movieList = new ArrayList<>();

            Cursor c = mContext.getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, null, null, null, null);

            if (c != null) {
                int colTmdbId = c.getColumnIndex(DBContract.MovieEntry.COL_TMDB_ID);
                int colTitle = c.getColumnIndex(DBContract.MovieEntry.COL_TITLE);
                int colDesc = c.getColumnIndex(DBContract.MovieEntry.COL_DESC);
                int colPoster = c.getColumnIndex(DBContract.MovieEntry.COL_POSTER_PATH);
                int colRelDate = c.getColumnIndex(DBContract.MovieEntry.COL_REL_DATE);
                int colOgTitle = c.getColumnIndex(DBContract.MovieEntry.COL_ORIGINAL_TITLE);
                int colBackdrop = c.getColumnIndex(DBContract.MovieEntry.COL_BACKDROP_PATH);
                int colVoteAvg = c.getColumnIndex(DBContract.MovieEntry.COL_VOTE_AVG);

                if (c.moveToFirst()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                while (c.moveToNext()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                c.close();
            }
            db.close();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> moviesResult) {
            super.onPostExecute(moviesResult);
            if (moviesResult != null && !moviesResult.isEmpty()) {
                movieGridAdapter.clear();
                movieGridAdapter.addAll(moviesResult);
                movieGridAdapter.notifyDataSetChanged();
                movieGridView.invalidate();
            } else {
                final Snackbar s = Snackbar.make(MovieGridFragment.this.movieGridView, R.string.string_no_favorites, Snackbar.LENGTH_INDEFINITE);
                s.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.dismiss();
                    }
                });
                s.show();
            }
        }
    }
}



