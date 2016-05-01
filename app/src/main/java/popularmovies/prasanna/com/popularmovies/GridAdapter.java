package popularmovies.prasanna.com.popularmovies;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        final CardView cv = (CardView) convertView.findViewById(R.id.card_view);
        Uri posterUri = Uri.parse(movie.getPosterPath());


//Set text
        textView.setText(movie.getOriginalTitle());
//Set color and image
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

                int colorFrom = Color.WHITE;
                int colorTo = swatches.get(max).getRgb();
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(500);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        cv.setCardBackgroundColor((int) animator.getAnimatedValue());
                        Log.d("APP", "Setting BG");

                    }

                });
                colorAnimation.start();
                textView.setTextColor(swatches.get(max).getTitleTextColor());

            }
        };

        Picasso.with(getContext()).load(BASE_URL + posterUri).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                posterImageView.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(paletteListener);
                Log.d("APP", "Loaded BM");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(getContext(), "Image load failed", Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
}
