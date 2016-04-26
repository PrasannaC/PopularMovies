package popularmovies.prasanna.com.popularmovies.WebServices;

import popularmovies.prasanna.com.popularmovies.Model.MovieResults;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Prasanna on 4/26/2016.
 */
public interface WebHelper {

    @GET("discover/movie")
    Call<MovieResults> getPopularMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy, @Query("vote_count.gte") String minVotes);
}
