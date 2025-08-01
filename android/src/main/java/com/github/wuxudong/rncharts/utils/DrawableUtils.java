package com.github.wuxudong.rncharts.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Base64;
import android.content.res.AssetManager;

public class DrawableUtils {
    private static Context appContext;
    
    public static void setApplicationContext(Context context) {
        appContext = context.getApplicationContext();
    }
    
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
                
                // For release mode data URLs, apply proper scaling using device density
                if (appContext != null) {
                    DisplayMetrics metrics = appContext.getResources().getDisplayMetrics();
                    float density = metrics.density;
                    
                    // Scale dimensions based on device density to match iOS behavior
                    int scaledWidth = Math.round(width * density);
                    int scaledHeight = Math.round(height * density);
                    
                    x = Bitmap.createScaledBitmap(x, scaledWidth, scaledHeight, true);
                }
            }
            // Handle asset names (try to load from app assets first)
            else if (!urlString.startsWith("http") && !urlString.startsWith("file://") && appContext != null) {
                // Try to load from app's assets folder for asset names
                try {
                    AssetManager assetManager = appContext.getAssets();
                    // Try different potential paths for the asset
                    String[] assetPaths = {
                        "icons/" + urlString,
                        urlString,
                        "assets/" + urlString,
                        "images/" + urlString
                    };
                    
                    InputStream assetStream = null;
                    for (String assetPath : assetPaths) {
                        try {
                            assetStream = assetManager.open(assetPath);
                            break;
                        } catch (IOException ignored) {
                            // Try next path
                        }
                    }
                    
                    if (assetStream != null) {
                        x = BitmapFactory.decodeStream(assetStream);
                        assetStream.close();
                        
                        // Apply proper scaling for assets
                        DisplayMetrics metrics = appContext.getResources().getDisplayMetrics();
                        float density = metrics.density;
                        int scaledWidth = Math.round(width * density);
                        int scaledHeight = Math.round(height * density);
                        
                        x = Bitmap.createScaledBitmap(x, scaledWidth, scaledHeight, true);
                    } else {
                        // Fallback to HTTP if asset not found
                        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        x = BitmapFactory.decodeStream(input);
                    }
                } catch (Exception assetException) {
                    // Fallback to HTTP
                    HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    x = BitmapFactory.decodeStream(input);
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
