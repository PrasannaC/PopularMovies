package popularmovies.prasanna.com.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import popularmovies.prasanna.com.popularmovies.Model.Movie;
import popularmovies.prasanna.com.popularmovies.Model.MovieResults;
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

        View v = inflater.inflate(R.layout.movie_grid, container, false);
        FetchMoviesTask asyncTask = new FetchMoviesTask(getContext());
        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            asyncTask.execute(FetchMoviesTask.SORT_BY_POPULARITY);
            try {
                movies = asyncTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getContext(), "Network Unavailable", Toast.LENGTH_SHORT).show();
        }

        //TEST CODE -- OBSOLETE
        /*TextView tv = (TextView) v.findViewById(R.id.listTest);
        if (movies != null)
            tv.setText(movies.get(0).getOriginalTitle());*/

        movieGridView = (GridView) v.findViewById(R.id.movie_gridView);
        movieGridAdapter = new GridAdapter(getContext(), new ArrayList<Movie>());
        movieGridView.setAdapter(movieGridAdapter);

        return v;
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
}



