package com.dkarakaya.songify;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class SearchFragment extends Fragment {

    private static String GOOGLE_YOUTUBE_API_KEY = "AIzaSyAdDix7i7a3an-gyXiquTV_14cIsr8-DZg";
    private static String GET_URL_ = "https://www.googleapis" +
            ".com/youtube/v3/search?part=snippet&maxResults=25&key=" + GOOGLE_YOUTUBE_API_KEY + "&q=";
    private static String GET_URL_VIDEO = "";

    private static String VIDEO_ID = "";
    private static String YOUTUBE_ = "https://www.youtube.com/watch?v=";
    private static String YOUTUBE_LINK = "";

    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();

    private LinearLayout mainLayout;
    private ProgressBar mainProgressBar;
    private TextView tvSearch;
    private EditText edtSearch;
    private ImageButton btnSearch;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        checkUserPermission();

        tvSearch = view.findViewById(R.id.tvSearch);
        tvSearch.setVisibility(View.VISIBLE);

        edtSearch = view.findViewById(R.id.edtSearch);
        mainLayout = view.findViewById(R.id.main_layout);
        mainProgressBar = view.findViewById(R.id.prgrBar);
        mainLayout.setVisibility(View.GONE);

        mList_videos = view.findViewById(R.id.mList_videos);
        mList_videos.setVisibility(View.GONE);

        btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String q = edtSearch.getText().toString().replace(" ", "+");
                GET_URL_VIDEO = GET_URL_ + q;
                mainLayout.setVisibility(View.VISIBLE);

                new RequestYoutubeAPI().execute();
            }
        });

//        initList(mListData);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //have the permission now.
//            getYoutubeDownloadUrl(youtubeLink);
        }
    }

    private void checkUserPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                .WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            return;
        }
//        getYoutubeDownloadUrl(youtubeLink);
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        mList_videos.setLayoutManager(new LinearLayoutManager(getActivity()));
        tvSearch.setVisibility(View.GONE);
        mainLayout.setVisibility(View.GONE);
        mList_videos.setVisibility(View.VISIBLE);
        adapter = new VideoPostAdapter(getActivity(), mListData, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                VIDEO_ID = item.getVideo_id();
                YOUTUBE_LINK = YOUTUBE_ + VIDEO_ID;
                getYoutubeDownloadUrl(YOUTUBE_LINK, item.getTitle());
            }
        });
        mList_videos.setAdapter(adapter);

    }

    private void getYoutubeDownloadUrl(final String youtubeLink, final String videoTitle) {
        new YouTubeExtractor(getActivity()) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                System.out.println("111111111111: " + videoTitle);
                System.out.println("111111111111: " + youtubeLink);
                if (ytFiles != null) {
                    // Iterate over itags
                    System.out.println("22222222222222222222222");
                    for (int i = 0, itag; i < ytFiles.size(); i++) {
                        itag = ytFiles.keyAt(i);
                        // ytFile represents one file with its url and meta data
                        YtFile ytFile = ytFiles.get(itag);
                        System.out.println("ITAG: " + itag);
                        // Just add videos in a decent format => height -1 = audio
                        if (ytFile.getFormat().getHeight() == -1) {
//                            addButtonToMainLayout(vMeta.getTitle(), ytFile);
                            System.out.println("3333333333333333333333333333");
                            String filename;
                            if (videoTitle.length() > 30) {
                                filename = videoTitle.substring(0, 30) + "." + ytFile.getFormat()
                                        .getExt();
                            } else {
                                filename = videoTitle + "." + ytFile.getFormat().getExt();
                            }
                            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                            downloadFromUrl(ytFile.getUrl(), videoTitle, filename);
                        }
                    }

                }
            }
        }.extract(youtubeLink, true, false);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request
                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/music",
                fileName);
        System.out.println("DownloadManager manager = (DownloadManager) getActivity()" +
                ".getSystemService(Context.DOWNLOAD_SERVICE);");
        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context
                .DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    //create an asynctask to get all the data from youtube
    private class RequestYoutubeAPI extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(GET_URL_VIDEO);
            Log.e("URL", GET_URL_VIDEO);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    mListData = parseVideoListFromResponse(jsonObject);
                    initList(mListData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
            ArrayList<YoutubeDataModel> mList = new ArrayList<>();

            if (jsonObject.has("items")) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        if (json.has("id")) {
                            JSONObject jsonID = json.getJSONObject("id");
                            String video_id = "";
                            if (jsonID.has("videoId")) {
                                video_id = jsonID.getString("videoId");
                            }
                            if (jsonID.has("kind")) {
                                if (jsonID.getString("kind").equals("youtube#video")) {
                                    YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                    JSONObject jsonSnippet = json.getJSONObject("snippet");
                                    String title = jsonSnippet.getString("title");
                                    String channelTitle = jsonSnippet.getString("channelTitle");
                                    String publishedAt = jsonSnippet.getString("publishedAt");
                                    String thumbnail = jsonSnippet.getJSONObject("thumbnails")
                                            .getJSONObject("medium").getString("url");

                                    youtubeObject.setTitle(title);
                                    youtubeObject.setChannelTitle(channelTitle);
                                    youtubeObject.setPublishedAt(publishedAt);
                                    youtubeObject.setThumbnail(thumbnail);
                                    youtubeObject.setVideo_id(video_id);
                                    mList.add(youtubeObject);

                                }
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return mList;
        }
    }

}