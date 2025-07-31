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
            
            // Apply density-aware scaling to maintain icon visibility without blur
            // Use a scale factor instead of exact dimensions to preserve quality
            float scaleFactor = 2.0f; // Adjust this value based on your icon sizes
            
            BitmapDrawable drawable = new BitmapDrawable(Resources.getSystem(), x);
            
            // Set bounds to control display size without actually resizing the bitmap
            int scaledWidth = (int)(x.getWidth() * scaleFactor);
            int scaledHeight = (int)(x.getHeight() * scaleFactor);
            drawable.setBounds(0, 0, scaledWidth, scaledHeight);
            
            return drawable;
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
