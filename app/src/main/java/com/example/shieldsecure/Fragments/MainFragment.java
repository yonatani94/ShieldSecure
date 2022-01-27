package com.example.shieldsecure.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.shieldsecure.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainFragment extends Fragment {
    public static final String TAG = "johny";
    public static final String Useless_Facts_API = "https://useless-facts.sameerkumar.website/api";
    // https://github.com/sameerkumar18/useless-facts-api
    protected View view;
    private TextView factLabel;

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_main, container, false);
        }
        initViews();
        generateJoke();

        return view;
    }

    private void initViews() {
        factLabel = view.findViewById(R.id.main_LBL_factOfTheDay);
        factLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateJoke();
            }
        });
    }

    /**
     * A method to read the jokes api and display on the page
     */
    private void generateJoke() {
        Log.d(TAG, "initJokes: ");
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Useless_Facts_API)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String initialResponse = response.body().string();
                Log.d(TAG, "onResponse: " + initialResponse);
                try {
                    JSONObject baseObject = new JSONObject(initialResponse);
                    String data = baseObject.getString("data");
                    final String fact = data + "\n" ;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            factLabel.setText(fact);
                            factLabel.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getMessage());
                }
            }
        });
    }
}