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
            Bitmap x;
            int width = Integer.parseInt(strings[1]);
            int height = Integer.parseInt(strings[2]);
            
            String urlString = strings[0];
            
            // Handle data URLs
            if (urlString.startsWith("data:")) {
                String base64 = urlString.substring(urlString.indexOf(",") + 1);
                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                x = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                // The decoded bitmap has the raw pixel dimensions, e.g., 120px.
                // We calculate the scale factor (e.g., 120px / 40dp = 3.0f)
                if (width > 0) {
                    float scale = (float) x.getWidth() / width;
                    // We set the density, informing Android this is a high-res asset.
                    // 160 is the baseline density for mdpi (1x).
                    x.setDensity((int) (scale * 160f));
                }
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
            
            //return new BitmapDrawable(Resources.getSystem(), Bitmap.createScaledBitmap(x, width, height, true));
            return new BitmapDrawable(Resources.getSystem(), x);
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
