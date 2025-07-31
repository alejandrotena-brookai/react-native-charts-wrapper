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
                    
                    // Decode base64 with options for better quality
                    BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
                    decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    decodeOptions.inScaled = false;
                    decodeOptions.inDither = false;
                    
                    bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, decodeOptions);
                    
                    if (bitmap != null) {
                        // Check if scaling is needed
                        // Don't scale if the image is already close to the requested size
                        float scaleFactorX = (float) requestedWidth / bitmap.getWidth();
                        float scaleFactorY = (float) requestedHeight / bitmap.getHeight();
                        
                        // Only scale if the difference is significant (more than 30%)
                        if (Math.abs(scaleFactorX - 1.0f) > 0.3f || Math.abs(scaleFactorY - 1.0f) > 0.3f) {
                            // Use Bitmap.createScaledBitmap with filter=false for sharper scaling
                            // This might create some aliasing but will be less blurry
                            bitmap = Bitmap.createScaledBitmap(bitmap, requestedWidth, requestedHeight, false);
                        }
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
                
                // Set target density for URLs that haven't been manually scaled
                // Skip for data URLs (already scaled)
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
}a