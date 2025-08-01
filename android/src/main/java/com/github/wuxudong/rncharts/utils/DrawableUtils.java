package com.github.wuxudong.rncharts.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Base64;
import android.content.res.AssetManager;

public class DrawableUtils {
    public static Drawable drawableFromUrl(String url, final int width, final int height) {
        try {
            return new DrawableLoadingAsyncTask().execute(url, Integer.toString(width), Integer.toString(height)).get();
        } catch (Exception e) {
            // draw dummy drawable when execution fail
            e.printStackTrace();
            return new ShapeDrawable();
        }
    }

    static class DrawableLoadingAsyncTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... strings) {
            try {
                // This width/height from JS is in dp, e.g., 30
                int dpWidth = Integer.parseInt(strings[1]);
                int dpHeight = Integer.parseInt(strings[2]);

                // Get the screen's density to convert dp to pixels
                float density = Resources.getSystem().getDisplayMetrics().density;
                int targetPixelWidth = (int) (dpWidth * density);
                int targetPixelHeight = (int) (dpHeight * density);

                Bitmap x;
                String urlString = strings[0];

                // Handle data URLs
                if (urlString.startsWith("data:")) {
                    String base64 = urlString.substring(urlString.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                    x = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                // Handle file URLs
                else if (urlString.startsWith("file://")) {
                    x = BitmapFactory.decodeFile(urlString.replace("file://", ""));
                }
                // Handle HTTP URLs
                else {
                    HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    x = BitmapFactory.decodeStream(input);
                }

                // Create a new bitmap scaled to the correct TARGET PIXEL size
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(x, targetPixelWidth, targetPixelHeight, true);
                return new BitmapDrawable(Resources.getSystem(), scaledBitmap);

            } catch(Exception e) {
                e.printStackTrace();
                return new ShapeDrawable();
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
        }
    };
}
