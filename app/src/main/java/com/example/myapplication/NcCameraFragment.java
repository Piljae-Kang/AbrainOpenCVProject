package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class NcCameraFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    ImageButton imageButton;
    PreviewView previewVieww;
    private ImageCapture imageCapture;
    Bitmap bitmap;

    Uri pictureUri;
    final static int TAKE_PICTURE = 1;

    Box box;

    public static int LEFT, TOP, RIGHT, BOTTOM;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nc_camera, container, false);


        imageButton = v.findViewById(R.id.imageButton);
        previewVieww = v.findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        draw_preview_rectangle();




        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ViewManager)box.getParent()).removeView(box);

                capturePhoto();
            }
        });



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) { //데이터를 가지고 있는지 확인
            final Bundle extras = data.getExtras();
            Bitmap croped_bitmap = null;
            if (extras != null) {
                croped_bitmap = extras.getParcelable("data"); //크롭한 이미지 가져오기

            } // 임시 파일 삭제





        Bundle bundle = new Bundle();
        bundle.putParcelable("bitmapImage", croped_bitmap);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        OcrFragment ocrFragment = new OcrFragment();
        ocrFragment.setArguments(bundle);
        transaction.replace(R.id.container, ocrFragment);
        transaction.commit();

        }

    }

    private Executor getExecutor() {

        return ContextCompat.getMainExecutor(getContext());
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        // camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewVieww.createSurfaceProvider());

        // image capture use case
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

    }


    private void capturePhoto(){

        imageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {

                Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();

                bitmap = imageProxyToBitmap(image);

                bitmap = RotateBitmap(bitmap, 90);

                pictureUri = getImageUri(getContext(), bitmap);

                int num = getOrientation(pictureUri);

                Toast.makeText(getContext(), ""+num,Toast.LENGTH_LONG).show();

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(pictureUri, "image/*");
                intent.putExtra("outputX", 200); //크롭한 이미지 x축 크기
                intent.putExtra("outputY", 200); //크롭한 이미지 y축 크기
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);


                startActivityForResult(intent, TAKE_PICTURE);


                image.close();

                super.onCaptureSuccess(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);

                Toast.makeText(getActivity(), "fail", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private Bitmap imageProxyToBitmap(ImageProxy image)
    {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



    public class Box extends View {
        private Paint paint = new Paint();

        Box(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // Override the onDraw() Method
            super.onDraw(canvas);

            int color_white = R.color.white;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getResources().getColor(color_white));
            paint.setStrokeWidth(4);

            // center coordinates of canvas
            int x = getWidth()/2;
            int y = getHeight()/2;

            // Top left and Bottom right coordinates of rectangle
            int x_topLeft = x-500;
            int y_topLeft = y-300;
            int x_bottomRight = x+500;
            int y_bottomRight = y+300;

            LEFT = x_topLeft;
            RIGHT = x_bottomRight;
            TOP = y_topLeft;
            BOTTOM = y_bottomRight;

            //draw guide box
            canvas.drawRect(LEFT, TOP, RIGHT, BOTTOM, paint);

        }
    }

    private void draw_preview_rectangle() {

        box = new Box(getContext());
        getActivity().addContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public int getOrientation(Uri selectedImage) {
        int orientation = 0;
        final String[] projection = new String[]{MediaStore.Images.Media.ORIENTATION};
        final Cursor cursor = getActivity().getContentResolver().query(selectedImage, projection, null, null, null);
        if(cursor != null) {
            final int orientationColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
            if(cursor.moveToFirst()) {
                orientation = cursor.isNull(orientationColumnIndex) ? 0 : cursor.getInt(orientationColumnIndex);
            }
            cursor.close();
        }
        return orientation;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }







}