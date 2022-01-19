package com.dalzai.findmyexpert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.request.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Response;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.CommentListResponse;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SearchActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    int counter = 0;
    EditText search;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY, "https://www.googleapis.com/auth/youtube.force-ssl" };
    GoogleAccountCredential mCredential;
    String searchword;
    YouTube mService = null;
    long limit = 10;
    List<SearchResult> searchResultList;
    ArrayList<VideoModel> results = new ArrayList<>();;
    RecyclerView recyclerView;
    VideoAdapter videoAdapter;
    LinearLayoutManager linearLayoutManager;
    Iterator<SearchResult> iterator;
    SearchListResponse searchResponse;
    ArrayList<String> searches;
    SharedPreferences settings;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        search = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.search_results);
        String[] searchstrings = getResources().getStringArray(R.array.search_history);
        searches = new ArrayList<>(Arrays.asList(searchstrings));
        search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        try {
            getResultsFromApi();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        /*try {
            tflite = new TensorFlowLite(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] input = new String[1];
        input[0] = "Great stuff";
        Float[] output = new Float[1];
        tflite.run(input, output);
        Log.e("Here", String.valueOf(output[0]));
        try {
            getResultsFromApi();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }*/
    }

    public void back(View view) {
    }

    /*private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("findmyexpert.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }*/

    private void getResultsFromApi() throws IOException, GeneralSecurityException {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(this,
                    "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Find My Expert")
                    .build();
            search.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH)
                    {
                        searchword = search.getText().toString();
                        try {
                            new MakeRequestTask(mService, searchword).execute();
                            showResults(searchResultList);
                            new MakeCommentsRequestTask(mService, results).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() throws IOException, GeneralSecurityException {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.e("Error",
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    try {
                        getResultsFromApi();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putString(PREF_ACCOUNT_NAME, getPreferences(Context.MODE_PRIVATE)
                                .getString(PREF_ACCOUNT_NAME, null));
                        edit.commit();
                        mCredential.setSelectedAccountName(accountName);
                        try {
                            getResultsFromApi();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    try {
                        getResultsFromApi();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(SearchActivity.this, connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private List<SearchResult> getDataFromApi(YouTube service, String search) throws IOException {
        YouTube.Search.List searchquery = service.search().list("id,snippet");
        searchquery.setKey(getResources().getString(R.string.api_key));
        searchquery.setQ(search);
        searchquery.setType("video");
        searchquery.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/medium/url,snippet/channelTitle)");
        searchquery.setMaxResults(limit);
        try{
            searchResponse = searchquery.execute();
        }
        catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        }
        searchResultList= searchResponse.getItems();
        return searchResultList;
    }

    private void showResults(List<SearchResult> searchResultList) throws IOException {
        if (searchResultList != null) {
            iterator = searchResultList.iterator();
            while(iterator.hasNext())
            {
                SearchResult singleVideo = iterator.next();
                ResourceId rId = singleVideo.getId();
                Log.e("Tag", String.valueOf(searchword));
                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getMedium();
                    results.add(new VideoModel(rId.getVideoId(), singleVideo.getSnippet().getTitle(), thumbnail.getUrl(), singleVideo.getSnippet().getChannelTitle()));
                }
            }
            videoAdapter = new VideoAdapter(this, results);
            linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(videoAdapter);
            videoAdapter.notifyDataSetChanged();
        }
    }

    private CommentThreadListResponse getCommentList(ArrayList<VideoModel> results, YouTube service) throws IOException {
        CommentThreadListResponse response = null;
        String videoId;
        YouTube.CommentThreads.List request = service.commentThreads()
                .list("id");
        for(int i = 0; i<results.size(); i++)
        {
            response = request.setMaxResults(20L)
                    .setVideoId(results.get(i).getVideo_id())
                    .execute();
           for(int j = 0; j<response.getPageInfo().getTotalResults(); j++)
           {
               videoId = response.getItems().get(j).getId();
               getComments(videoId, service);
           }
            String url = "http://localhost:8501/vv1/models/find_my_expert_model:predict";
            JsonObjectReqest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Tag", response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
            'signature_name'=> "serving_default", 'instances'
        }
        return response;
    }

    private String getComments(String commentId, YouTube service) throws IOException {
        CommentListResponse response = null;
        String comment;
        YouTube.Comments.List request = service.comments()
                .list("id, snippet");
        response = request.setId(commentId)
                .setMaxResults(1L)
                .execute();
        comment = response.getItems().get(0).getSnippet().getTextOriginal();
        return comment;
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<SearchResult>>{

        public MakeRequestTask(YouTube mService, String searchword) throws IOException {
        }


        @Override
        protected List<SearchResult> doInBackground(Void... voids) {
            try {
                searchResultList = getDataFromApi(mService, searchword);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResultList;
        }
    }

    private class MakeCommentsRequestTask extends AsyncTask<Void, Void, CommentThreadListResponse>
    {
        CommentThreadListResponse response;
        public MakeCommentsRequestTask(YouTube mService, ArrayList<VideoModel> results) throws IOException {
        }

        @Override
        protected CommentThreadListResponse doInBackground(Void... voids) {
            try {
                response = getCommentList(results, mService);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}