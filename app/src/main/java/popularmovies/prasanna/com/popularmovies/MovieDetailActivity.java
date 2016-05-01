package popularmovies.prasanna.com.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import popularmovies.prasanna.com.popularmovies.Model.Movie;

/**
 * Created by Prasanna on 5/1/2016.
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        MovieDetailFragment movieDetailFragment = MovieDetailFragment.getInstance((Movie) getIntent().getExtras().getSerializable("movie"));
        getSupportFragmentManager().beginTransaction().replace(R.id.detail_layout, movieDetailFragment, "detail_fragment").commit();

    }
}
