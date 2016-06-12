package popularmovies.prasanna.com.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import popularmovies.prasanna.com.popularmovies.Model.Movie;
import popularmovies.prasanna.com.popularmovies.Model.Review;
import popularmovies.prasanna.com.popularmovies.Model.ReviewResults;
import popularmovies.prasanna.com.popularmovies.Model.Video;
import popularmovies.prasanna.com.popularmovies.Model.VideoResults;
import popularmovies.prasanna.com.popularmovies.Persistence.DBContract;
import popularmovies.prasanna.com.popularmovies.Persistence.FavoritesProvider;
import popularmovies.prasanna.com.popularmovies.WebServices.WebHelper;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Prasanna on 5/1/2016.
 */
public class MovieDetailFragment extends Fragment {

    private ImageView poster;
    private TextView title;
    private TextView relDate;
    private TextView overview;
    private TextView rating;
    private TextView origTitle;
    private MenuItem share, fav;
    private boolean offlineMode, favorite;
    private Movie movie;
    private boolean trailerSharePrepared;
    private View parentView;
    private LinearLayout trailerView;
    private LinearLayout reviewView;
    private Uri trailerUri;

    public MovieDetailFragment() {

    }

    public static MovieDetailFragment getInstance(Movie param1) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("movie", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (movie == null) {
            if (getArguments() != null) {
                movie = (Movie) getArguments().getSerializable("movie");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.movie_detail, container, false);
        setHasOptionsMenu(true);

        poster = (ImageView) v.findViewById(R.id.detail_poster_image);
        relDate = (TextView) v.findViewById(R.id.detail_year);
        overview = (TextView) v.findViewById(R.id.detail_plot);
        rating = (TextView) v.findViewById(R.id.detail_rating);
        origTitle = (TextView) v.findViewById(R.id.detail_orig_title);
        trailerView = (LinearLayout) v.findViewById(R.id.trailerView);
        reviewView = (LinearLayout) v.findViewById(R.id.reviewView);

        Picasso.with(getContext()).load(GridAdapter.BASE_URL + movie.getPosterPath()).into(poster);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(movie.getReleaseDate(), new ParsePosition(0));
        relDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(movie.getOverview());
        rating.setText(String.format(Locale.US, "%2.1f / 10", Double.parseDouble("" + movie.getVoteAverage())));
        origTitle.setText(movie.getOriginalTitle());


        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            //fetch trailers and reviews
            new FetchVideoTask(getContext()).execute(movie.getId());
            new FetchReviewTask(getContext()).execute(movie.getId());
        } else {
            offlineMode = true;
            Snackbar.make(container, "Offline Mode", Snackbar.LENGTH_LONG).show();
            reviewView.setVisibility(View.GONE);
            trailerView.setVisibility(View.GONE);
            v.findViewById(R.id.trailerTitle).setVisibility(View.GONE);
            v.findViewById(R.id.reviewTitle).setVisibility(View.GONE);
        }

        Cursor c = getContext().getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, new String[]{DBContract.MovieEntry.COL_TMDB_ID}, DBContract.MovieEntry.COL_TMDB_ID + "=?", new String[]{Long.toString(movie.getId())}, null);

        if (c != null && c.moveToFirst()) {
            favorite = true;
        }

        if (c != null) c.close();


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        fav = menu.findItem(R.id.fav_btn);
        if (favorite) {
            if (fav != null) {
                fav.setIcon(R.mipmap.ic_favorite_white_24dp);
            }
        }

        share = menu.findItem(R.id.share_btn);
        if (share != null && offlineMode) {
            share.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fav_btn) {
            item.setIcon(R.mipmap.ic_favorite_white_24dp);

            if (!favorite) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBContract.MovieEntry.COL_TMDB_ID, movie.getId());
                contentValues.put(DBContract.MovieEntry.COL_TITLE, movie.getTitle());
                contentValues.put(DBContract.MovieEntry.COL_DESC, movie.getOverview());
                contentValues.put(DBContract.MovieEntry.COL_POSTER_PATH, movie.getPosterPath());
                contentValues.put(DBContract.MovieEntry.COL_REL_DATE, movie.getReleaseDate());
                contentValues.put(DBContract.MovieEntry.COL_ORIGINAL_TITLE, movie.getOriginalTitle());
                contentValues.put(DBContract.MovieEntry.COL_BACKDROP_PATH, movie.getBackdropPath());
                contentValues.put(DBContract.MovieEntry.COL_VOTE_AVG, Double.toString(movie.getVoteAverage()));

                Uri u = getContext().getContentResolver().insert(FavoritesProvider.Movies.CONTENT_URI, contentValues);

                favorite = true;
            } else {
                favorite = false;
                item.setIcon(R.mipmap.ic_favorite_border_white_24dp);
                int n = getContext().getContentResolver().delete(FavoritesProvider.Movies.CONTENT_URI, DBContract.MovieEntry.COL_TMDB_ID + "=?", new String[]{Long.toString(movie.getId())});
            }
        }

        if (item.getItemId() == R.id.share_btn) {
            if (trailerSharePrepared) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, trailerUri.toString());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.string_share_trailer)));
            } else {
                Snackbar.make(parentView, getContext().getString(R.string.string_trailer_not_loaded), Snackbar.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    private class FetchVideoTask extends AsyncTask<Long, Void, List<Video>> {

        // http://stackoverflow.com/a/8842839/2663152
        public static final String YT_THUMB_BASE = "http://img.youtube.com/vi/%s/0.jpg";
        public static final String YT_VIDEO_BASE = "http://www.youtube.com/watch?v=";
        boolean networkError = false;
        private Context mContext;

        public FetchVideoTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Video> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieGridFragment.FetchMoviesTask.TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            WebHelper movieService = retrofit.create(WebHelper.class);
            Call<VideoResults> call = movieService.getVideosForMovie(params[0], mContext.getString(R.string.api_key));

            try {
                Response<VideoResults> response = call.execute();
                return response.body().getVideos();
            } catch (IOException e) {
                e.printStackTrace();
                networkError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Video> videos) {
            super.onPostExecute(videos);

            if (videos != null && videos.size() != 0) {
                trailerUri = Uri.parse(YT_VIDEO_BASE + videos.get(0).getKey());
                trailerSharePrepared = true;
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(YT_VIDEO_BASE + videos.get((Integer) v.getTag()).getKey()));
                        startActivity(i);
                    }
                };

                for (int i = 0; i < videos.size(); i++) {
                    //If we have more than two trailers, add more image views.
                    if (i >= 2) {
                        ImageView imageView = new ImageView(mContext);
                        imageView.setLayoutParams(trailerView.getChildAt(0).getLayoutParams());
                        trailerView.addView(imageView);
                    }
                    Picasso.with(mContext).load(Uri.parse(String.format(YT_THUMB_BASE, videos.get(i).getKey()))).into((ImageView) trailerView.getChildAt(i));
                    trailerView.getChildAt(i).setTag(i);
                    trailerView.getChildAt(i).setOnClickListener(onClickListener);
                }

                //If we have only 1 trailer, disable the second ImageView.
                if (videos.size() < 2) {
                    trailerView.getChildAt(1).setVisibility(View.GONE);
                }
            } else {
                if (networkError) {
                    Snackbar.make(parentView, "Network Error", Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }
    }

    public class FetchReviewTask extends AsyncTask<Long, Void, List<Review>> {

        private Context mContext;
        private boolean networkError = false;

        public FetchReviewTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Review> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieGridFragment.FetchMoviesTask.TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            WebHelper movieService = retrofit.create(WebHelper.class);

            Call<ReviewResults> call = movieService.getReviewsForMovie(movie.getId(), mContext.getString(R.string.api_key));

            try {
                Response<ReviewResults> response = call.execute();
                return response.body().getResults();
            } catch (IOException e) {
                e.printStackTrace();
                networkError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Review> reviews) {
            super.onPostExecute(reviews);

            if (reviews != null) {
                if (reviews.size() == 0) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                    return;
                }

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(reviews.get((Integer) v.getTag()).getUrl()));
                        startActivity(i);
                    }
                };

                for (int i = 0; i < reviews.size(); i++) {
                    if (i >= 1) {
                        TextView v = new TextView(mContext);
                        v.setLayoutParams(reviewView.getChildAt(0).getLayoutParams());
                        v.setMaxLines(20);
                        v.setEllipsize(TextUtils.TruncateAt.END);
                        TypedValue outValue = new TypedValue();
                        mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                        v.setBackgroundResource(outValue.resourceId);
                        reviewView.addView(v);
                    }
                    String s1 = (reviews.get(i).getContent().length() > 400 ? reviews.get(i).getContent().substring(0, 400) : reviews.get(i).getContent());
                    ((TextView) reviewView.getChildAt(i)).setText(String.format(mContext.getString(R.string.string_review_format), reviews.get(i).getAuthor(), s1));
                    reviewView.getChildAt(i).setTag(i);
                    reviewView.getChildAt(i).setOnClickListener(onClickListener);
                }
            } else {
                if (networkError) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_review_network_error);
                } else {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                }
            }
        }
    }
}
