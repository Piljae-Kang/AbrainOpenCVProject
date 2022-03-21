package com.example.myapplication;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileScan extends Fragment {

    TextView text;
    Button button;
    File file;
    Uri uri;
    Bitmap bitmap;

    private final int MY_PERMISSIONS_REQUEST_CAMERA=1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult"); if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED )
        {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_file_scan, container, false);

        File sdcard = Environment.getExternalStorageDirectory();
        //file = new File(sdcard, "capture.jpg");

        button = v.findViewById(R.id.button);
        text = v.findViewById(R.id.textview);

        String str = "when you want to take picture from galary\nPress \"파일에서 가져오기\" button\n\n"
                + "When you want to take by camera\nPress \"카메라 촬영\" button";

        text.setText(str);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                capture();

            }
        });


        return v;
    }

    public void capture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        file = null;
        try { file = createImageFile();
        } catch (IOException ex) { }

        uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 101);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 101  && resultCode == Activity.RESULT_OK){
            //imageView.setImageURI(uri);
            // Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), uri));
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample1);

            //bitmap = RotateBitmap(bitmap, 90);


            Bundle bundle = new Bundle();
            bundle.putParcelable("bitmapImage", bitmap);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            OcrFragment ocrFragment = new OcrFragment();
            ocrFragment.setArguments(bundle);
            transaction.replace(R.id.container, ocrFragment);
            transaction.commit();

        }


    }

    private File createImageFile() throws IOException { // 파일이름을 세팅 및 저장경로 세팅
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_"; File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File StorageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName, ".jpg", storageDir );
        return image; }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



}