package com.example.parkapp.fragments_owners;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.parkapp.R;

public class HowToParkDemoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_how_to_park_demo, container, false);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        VideoView videoView = (VideoView) getView().findViewById(R.id.videoView);
        // or  (ImageView) view.findViewById(R.id.foo);
        videoView.setVideoPath("android.resource://" + "com.example.parkapp" + "/" + R.raw.how_to_park_demo_compressed);
        videoView.start();

        MediaController mediaController = new MediaController(this.getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }
}