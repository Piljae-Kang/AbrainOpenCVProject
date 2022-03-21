package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class OcrFragment extends Fragment {

    ImageView imageView;
    Button button;
    Bitmap bitmap;
    TessBaseAPI tessBaseAPI;
    String datapath = "";
    Bitmap image;
    private final String[] mLanguageList = {"eng","kor"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_ocr, container, false);

        imageView = v.findViewById(R.id.imageView_ocr);
        button = v.findViewById(R.id.button_ocr);

        if(getArguments() != null){

         image = getArguments().getParcelable("bitmapImage");

         imageView.setImageBitmap(image);

        }


        //image = BitmapFactory.decodeResource(getResources(), R.drawable.sample1);

        //언어파일 경로
        Tesseract();



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //imageView.setImageResource(R.drawable.test1);

                Toast.makeText(getActivity(), "처리중 입니다.\n 잠시만 기다려 주세요", Toast.LENGTH_LONG).show();
                processImage(image);


            }
        });




        return v;

    }

    public void processImage(Bitmap bitmap) {
        String OCRresult = null;
        tessBaseAPI.setImage(bitmap);
        OCRresult = tessBaseAPI.getUTF8Text();

        //Toast.makeText(getActivity(), OCRresult, Toast.LENGTH_LONG).show();

        Bundle bundle = new Bundle();
        bundle.putString("Result", OCRresult);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        ResultFileOcr resultFileOcr = new ResultFileOcr();
        resultFileOcr.setArguments(bundle);
        transaction.replace(R.id.container, resultFileOcr);
        transaction.commit();
    }

    public void Tesseract() {
        //언어파일 경로
        datapath = getActivity().getFilesDir() + "/tesseract/";

        //트레이닝데이터가 카피되어 있는지 체크
        String lang = "";
        for (String Language : mLanguageList) {
            checkFile(new File(datapath + "tessdata/"), Language);
            lang += Language + "+";
        }
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(datapath, lang);
    }

    private void checkFile(File dir, String Language) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(Language);
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if (dir.exists()) {
            String datafilepath = datapath + "tessdata/" + Language + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(Language);
            }
        }
    }

    private void copyFiles(String Language) {
        try {
            String filepath = datapath + "/tessdata/" + Language + ".traineddata";
            AssetManager assetManager = getActivity().getAssets();
            InputStream instream = assetManager.open("tessdata/"+Language+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}