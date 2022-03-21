package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class NameCardScan extends Fragment {

    Button button_galery;
    Button button_camera;
    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_name_card_scan, container, false);

        button_galery = v.findViewById(R.id.NC_button_galery);
        button_camera = v.findViewById(R.id.NC_button_camera);
        textView = v.findViewById(R.id.NC_textview);

        String str = "when you want to take picture from galary\nPress \"파일에서 가져오기\" button\n\n"
                + "When you want to take by camera\nPress \"카메라 촬영\" button";

        textView.setText(str);

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                NcCameraFragment ncCameraFragment = new NcCameraFragment();
                transaction.replace(R.id.container, ncCameraFragment);
                transaction.commit();

            }
        });

        button_galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        return v;
    }
}