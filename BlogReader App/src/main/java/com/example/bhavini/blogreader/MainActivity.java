package com.example.bhavini.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ListActivity {

    public static final int NO_POSTS = 10;
    public static final String TAG = MainActivity.class.getSimpleName();

    protected JSONObject mBlogData;
    protected ProgressBar progressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);
            getBlogPoststask gtbp = new getBlogPoststask();
            gtbp.execute();
        }
        else
         Toast.makeText(this, R.string.NetworkError,Toast.LENGTH_LONG).show();

        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,mBlogTitle );
        setListAdapter(adapter);
*/
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            JSONArray jsonPost = mBlogData.getJSONArray("posts");
            JSONObject jsonObject = jsonPost.getJSONObject(position);
            String blogUrl = jsonObject.getString("url");

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.setData(Uri.parse(blogUrl));
            startActivity(intent);

        } catch (JSONException e) {
            Log.e( TAG , " Exception Caught! " , e );
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= manager.getActiveNetworkInfo();

        Boolean isavailable = false;
        if (networkInfo!= null && networkInfo.isConnected())
        {
            isavailable = true;
        }

        return isavailable;
    }

    private void handleBlogResponse() {

        progressBar.setVisibility(View.INVISIBLE);

        if(mBlogData == null)
        {
            updateDisplayForError();
        }
        else
        {
            try {
                    JSONArray jsonPosts= mBlogData.getJSONArray("posts");
                   ArrayList<HashMap<String, String>> blogPosts =
                           new ArrayList<HashMap<String, String>>();

                    for(int i=0; i<jsonPosts.length(); i++)
                    {
                        JSONObject post = jsonPosts.getJSONObject(i);
                        String title= post.getString(KEY_TITLE);
                        title= Html.fromHtml(title).toString();

                        String author= post.getString(KEY_AUTHOR);
                        author = Html.fromHtml(author).toString();

                        HashMap<String, String> blogPost = new HashMap<String, String>();
                        blogPost.put(KEY_TITLE,title);
                        blogPost.put(KEY_AUTHOR,author);

                        blogPosts.add(blogPost);
                    }

                String[] from = { KEY_TITLE , KEY_AUTHOR };
                int[] to = {android.R.id.text1,  android.R.id.text2};

                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, from, to);
                setListAdapter(adapter);
            }
            catch(JSONException e)
            {
                Log.e(TAG,e.getMessage());
            }
        }

    }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//inner class * factory methods
        builder.setTitle("OOPS! Sorry");
        builder.setMessage(("There was an error getting data from web"));
        builder.setPositiveButton("ok",null);
        AlertDialog dialog = builder.create();
        dialog.show();


        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    private class getBlogPoststask extends AsyncTask<Object,Void,JSONObject>
    {
        @Override
        protected JSONObject doInBackground(Object... params) {
            int responseCode=-1;
            JSONObject jsonResponse=null;
            try {
                URL blogURL = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NO_POSTS);
                HttpURLConnection connect = (HttpURLConnection) blogURL.openConnection();
                connect.connect();
                responseCode = connect.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    InputStream inputStream = connect.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);

                   /*
                    int contentLength = connect.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData =  new String(charArray);
                    */


                    int nextCharacter; // read() returns an int, we cast it to char later
                    String responseData = "";

                    while(true) { // Infinite loop, can only be stopped by a "break" statement
                        nextCharacter = reader.read(); // read() without parameters returns one character
                        if(nextCharacter == -1) // A return value of -1 means that we reached the end
                            break;
                        responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                    }
                    jsonResponse = new JSONObject(responseData);
                }
                else
                {
                    Log.e(TAG,"Unsuccessful HTTP Response Code : "+responseCode);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Exception caught: ",e);
            }catch(IOException e) {
                Log.e(TAG,"IOException caught: "+e);
            }
            catch(Exception e) {
                Log.e(TAG,"Exception caught: "+e);
            }

       //  Toast.makeText(MainActivity, ""+responseCode, Toast.LENGTH_LONG).show();

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mBlogData = jsonObject;
            handleBlogResponse();
        }
    }

}
