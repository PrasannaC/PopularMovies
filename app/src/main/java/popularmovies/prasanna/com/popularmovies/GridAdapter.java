package popularmovies.prasanna.com.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import popularmovies.prasanna.com.popularmovies.Model.Movie;

/**
 * Created by Prasanna on 4/28/2016.
 */
public class GridAdapter extends ArrayAdapter<Movie> {

    public static final String BASE_URL = "http://image.tmdb.org/t/p/w185";

    public GridAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.grid_item, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);

        final ImageView posterImageView = (ImageView) convertView.findViewById(R.id.poster_image);

        final TextView textView = (TextView) convertView.findViewById(R.id.info_text);
        textView.setText(movie.getOriginalTitle() + " (" + movie.getReleaseDate().substring(0, 4) + ")");
        final CardView cv = (CardView) convertView.findViewById(R.id.card_view);
        final Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                int max = 0;
                int maxPop = 0;
                List<Palette.Swatch> swatches = palette.getSwatches();
                for (int i = 0; i < swatches.size(); i++) {
                    if (swatches.get(i).getPopulation() > maxPop) {
                        maxPop = swatches.get(i).getPopulation();
                        max = i;
                    }
                }
                cv.setCardBackgroundColor(swatches.get(max).getRgb());
                textView.setTextColor(swatches.get(max).getTitleTextColor());
            }
        };
        Uri posterUri = Uri.parse(movie.getPosterPath());
        Picasso.with(getContext()).load(BASE_URL + posterUri).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                posterImageView.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(paletteListener);

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {


            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
        });


        return convertView;
    }
}
//FUCK

class Worker implements Runnable {

    Palette palette;
    CardView cardView;
    TextView textView;

    public Worker(Palette palette, CardView cv, TextView tv) {

        this.cardView = cv;
        this.palette = palette;
        this.textView = tv;
    }

    @Override
    public void run() {
        int max = 0;
        int maxPop = 0;
        List<Palette.Swatch> swatches = palette.getSwatches();
        for (int i = 0; i < swatches.size(); i++) {
            if (swatches.get(i).getPopulation() > maxPop) {
                maxPop = swatches.get(i).getPopulation();
                max = i;
            }
        }
        cardView.setCardBackgroundColor(swatches.get(max).getRgb());
        textView.setTextColor(swatches.get(max).getTitleTextColor());
    }
}