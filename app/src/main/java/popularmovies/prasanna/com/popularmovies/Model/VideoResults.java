package popularmovies.prasanna.com.popularmovies.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Prasanna on 6/12/2016.
 */

public class VideoResults {

    @SerializedName("id")
    long id;

    @SerializedName("results")
    List<Video> videos;

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}