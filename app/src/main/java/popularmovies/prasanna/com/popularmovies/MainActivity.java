package popularmovies.prasanna.com.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment movieFrag = new MovieGridFragment();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, movieFrag, "movie_tag").commit();

        //https://api.themoviedb.org/3/movie/550?api_key=c0cae73164556944a8b6f9599cc3df5f


    }
}
