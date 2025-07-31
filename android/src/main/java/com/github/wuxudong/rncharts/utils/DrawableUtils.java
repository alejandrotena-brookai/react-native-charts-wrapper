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
                Bitmap bitmap;
                int requestedWidth = Integer.parseInt(strings[1]);
                int requestedHeight = Integer.parseInt(strings[2]);
                String urlString = strings[0];
                
                Resources resources = Resources.getSystem();
                float density = resources.getDisplayMetrics().density;
                
                // Handle data URLs
                if (urlString.startsWith("data:")) {
                    String base64 = urlString.substring(urlString.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                    
                    // Decode without any scaling first
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;
                    bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
                    
                    if (bitmap != null) {
                        // For data URLs, always scale to match device density
                        // The requested dimensions are in dp, convert to pixels
                        int targetPixelWidth = Math.round(requestedWidth * density);
                        int targetPixelHeight = Math.round(requestedHeight * density);
                        
                        // Always scale data URL images to the correct pixel size
                        bitmap = Bitmap.createScaledBitmap(bitmap, targetPixelWidth, targetPixelHeight, true);
                    }
                }
                // Handle file URLs
                else if (urlString.startsWith("file://")) {
                    bitmap = BitmapFactory.decodeFile(urlString.replace("file://", ""));
                }
                // Handle HTTP URLs
                else {
                    HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                }
                
                if (bitmap == null) {
                    return new ShapeDrawable();
                }
                
                // Create drawable
                BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                
                // Only set target density for non-data URLs
                // Data URLs have already been scaled to the correct pixel size
                if (!urlString.startsWith("data:")) {
                    drawable.setTargetDensity(resources.getDisplayMetrics());
                }
                
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
    }
}