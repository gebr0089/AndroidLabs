package algonquin.cst2335.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String TAG = "CatSlideshow";
    private ImageView catImageView;
    private ProgressBar catProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        catImageView = findViewById(R.id.catImageView);
        catProgressBar = findViewById(R.id.catProgressBar);

        // Start background image fetcher task
        CatImages task = new CatImages();
        task.execute();
    }

    private class CatImages extends AsyncTask<String, Integer, String> {
        private Bitmap currentCatBitmap = null;
        private boolean isNewImageReady = false;

        @Override
        protected String doInBackground(String... strings) {
            while (true) {
                try {
                    // 1. Query random cat JSON
                    URL apiUrl = new URL("https://cataas.com/cat?json=true");
                    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
                    String imageId = jsonResponse.optString("_id", jsonResponse.optString("id", "cat_" + System.currentTimeMillis()));
                    String relativeUrl = jsonResponse.optString("url", "/cat/" + imageId);
                    String fullImageUrl = relativeUrl.startsWith("http") ? relativeUrl : "https://cataas.com" + relativeUrl;

                    String localFileName = imageId + ".png";
                    File localFile = new File(getFilesDir(), localFileName);

                    // 2. Check if image file exists locally
                    if (localFile.exists() && localFile.length() > 0) {
                        Log.i(TAG, "Cache Hit: Loading local image: " + localFileName);
                        FileInputStream fis = openFileInput(localFileName);
                        currentCatBitmap = BitmapFactory.decodeStream(fis);
                        fis.close();
                    } else {
                        Log.i(TAG, "Cache Miss: Downloading new image from " + fullImageUrl);
                        URL imageUrl = new URL(fullImageUrl);
                        HttpURLConnection imgConn = (HttpURLConnection) imageUrl.openConnection();
                        imgConn.setDoInput(true);
                        imgConn.connect();

                        InputStream input = imgConn.getInputStream();
                        currentCatBitmap = BitmapFactory.decodeStream(input);
                        input.close();

                        // Save image to internal storage
                        if (currentCatBitmap != null) {
                            FileOutputStream fos = openFileOutput(localFileName, Context.MODE_PRIVATE);
                            currentCatBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.flush();
                            fos.close();
                            Log.i(TAG, "Saved image to storage as: " + localFileName);
                        }
                    }

                    // Signal UI that a new image is ready
                    isNewImageReady = true;
                    publishProgress(0);

                    // 3. Update progress bar countdown
                    for (int i = 0; i <= 100; i++) {
                        publishProgress(i);
                        Thread.sleep(35);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage(), e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (isNewImageReady && currentCatBitmap != null) {
                catImageView.setImageBitmap(currentCatBitmap);
                isNewImageReady = false;
            }

            if (values.length > 0) {
                catProgressBar.setProgress(values[0]);
            }
        }
    }
}