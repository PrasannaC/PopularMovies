package popularmovies.prasanna.com.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import popularmovies.prasanna.com.popularmovies.Model.Movie;

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

    private Movie movie;

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

        Picasso.with(getContext()).load(GridAdapter.BASE_URL + movie.getPosterPath()).into(poster);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(movie.getReleaseDate(), new ParsePosition(0));
        relDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(movie.getOverview());
        rating.setText(String.format(Locale.US, "%2.1f / 10", Double.parseDouble("" + movie.getVoteAverage())));
        origTitle.setText(movie.getOriginalTitle());
        return v;
    }
}
