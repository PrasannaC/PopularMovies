package popularmovies.prasanna.com.popularmovies.WebServices;

import popularmovies.prasanna.com.popularmovies.Model.MovieResults;
import popularmovies.prasanna.com.popularmovies.Model.ReviewResults;
import popularmovies.prasanna.com.popularmovies.Model.VideoResults;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Prasanna on 4/26/2016.
 */
public interface WebHelper {

    @GET("discover/movie")
    Call<MovieResults> getPopularMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy, @Query("vote_count.gte") String minVotes);

    @GET("movie/{id}/videos")
    Call<VideoResults> getVideosForMovie(@Path("id") long id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResults> getReviewsForMovie(@Path("id") long id, @Query("api_key") String apiKey);
}
