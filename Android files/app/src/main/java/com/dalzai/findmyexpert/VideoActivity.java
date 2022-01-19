package com.dalzai.findmyexpert;

import static android.content.ContentValues.TAG;

import static com.dalzai.findmyexpert.SearchActivity.REQUEST_AUTHORIZATION;

import com.google.android.youtube.player.YouTubeBaseActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener
{

    String api_key = "AIzaSyBEPVvXxNKEe0o9Bwh_VI152uVdgAGEHAo";
    YouTubePlayerFragment ytPlayer;
    String videoID;
    String videoTitle;
    String videoChannel;
    TextView VideoTitle;
    TextView VideoChannel;
    ArrayList <String> arrayList = new ArrayList<>();
    SharedPreferences sharedPrefs;
    YouTube mService = null;
    List<CaptionListResponse> searchResultList;
    CaptionListResponse searchResponse;
    private static final String[] SCOPES = { "https://www.googleapis.com/auth/youtube.force-ssl" };
    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoID = getIntent().getSerializableExtra("VideoID").toString();
        videoTitle = getIntent().getSerializableExtra("VideoTitle").toString();
        videoChannel = getIntent().getSerializableExtra("VideoChannel").toString();
        arrayList.add(videoID);
        arrayList.add(videoTitle);
        ytPlayer = (YouTubePlayerFragment)getFragmentManager().findFragmentById(R.id.video_player);
        VideoTitle = findViewById(R.id.video_title);
        VideoChannel = findViewById(R.id.video_channel);
        VideoChannel.setText(videoChannel);
        VideoTitle.setText(videoTitle);
        ytPlayer.initialize(api_key, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.cueVideo(videoID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            getYouTubePlayerProvider().initialize(api_key, this);
        }
    }
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView)findViewById(R.id.video_player);
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void saveVideo(View view) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(videoID, json);
        editor.apply();

        String show = sharedPrefs.getString(videoID, null);
        //Type type = new TypeToken<ArrayList<String>>() {}.getType();
        //ArrayList<String> newstring = gson.fromJson(show, type);
        Log.e("Here", sharedPrefs.getAll().toString());
    }

    public void getTranscript(View view) throws IOException {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        Log.e("Tag", sharedPrefs.getString(PREF_ACCOUNT_NAME, null));
        mCredential.setSelectedAccountName(sharedPrefs
                .getString(PREF_ACCOUNT_NAME, null));
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Find My Expert")
                .build();
        new MakeRequestTask(mService, videoID).execute();
    }

    private List<CaptionListResponse> getDataFromApi(YouTube service, String videoID) throws IOException {
        YouTube.Captions.List searchquery = service.captions().list("snippet", videoID);
        searchquery.setKey(getResources().getString(R.string.api_key));
        FileOutputStream output = openFileOutput("file:///android_asset/transcripts/"+videoTitle+".txt", Context.MODE_PRIVATE);
        try{
            searchResponse = searchquery.execute();
            String id = searchResponse.getItems().get(0).getId();
            YouTube.Captions.Download request = service.captions()
                    .download(id);
            request.getMediaHttpDownloader();
            request.executeMediaAndDownloadTo(output);
        }
        catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        }
        //searchResultList= searchResponse.getItems();
        return searchResultList;
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<CaptionListResponse>> {

        public MakeRequestTask(YouTube mService, String videoID) throws IOException {
        }


        @Override
        protected List<CaptionListResponse> doInBackground(Void... voids) {
            try {
                searchResultList = getDataFromApi(mService, videoID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResultList;
        }

    }
}
