package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ResultFileOcr extends Fragment {

    TextView text;
    Button button_pdf;
    Button button_txt;
    String file_name;
    Dialog dialog;

    private static final String TAG = "webnautes" ;
    private File root;
    private AssetManager assetManager;
    private PDFont font;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_result_file_ocr, container, false);

        text = v.findViewById(R.id.text_file_ocr);

        Bundle bundle = getArguments();

        String str = bundle.getString("Result");
        text.setText(str);

        button_pdf = v.findViewById(R.id.button_pdf);
        button_txt = v.findViewById(R.id.button_txt);

        dialog = new Dialog(getActivity());       // Dialog 초기화
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog.setContentView(R.layout.dialoglayout);             // xml 레이아웃 파일과 연결

        button_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showDialog();

                // pdf 생성
                final SaveTask saveTask = new SaveTask();
                saveTask.execute();
            }
        });

        button_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = text.getText().toString();
                showDialog();
                writeFile(file_name, content);

            }
        });


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setup();
    }

    private void setup() {

        PDFBoxResourceLoader.init(getContext().getApplicationContext());
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        assetManager = getActivity().getAssets();

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    public String createPdf(){


        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);


        try{
            font = PDType0Font.load(document, assetManager.open("NanumBarunGothicLight.ttf"));
        }
        catch (IOException e){
            Log.e(TAG, "폰트를 읽을 수 없습니다.", e);
        }

        PDPageContentStream contentStream;

        try {
            contentStream = new PDPageContentStream( document, page, true, true);


            int text_width = 470;
            int text_left = 70;

            String textN = text.getText().toString();
            int fontSize = 17;
            float leading = 1.5f * fontSize;

            List<String> lines = new ArrayList<String>();
            int lastSpace = -1;
            for (String text : textN.split("\n"))
            {
                while (text.length() > 0) {
                    int spaceIndex = text.indexOf(' ', lastSpace + 1);
                    if (spaceIndex < 0)
                        spaceIndex = text.length();
                    String subString = text.substring(0, spaceIndex);
                    float size = fontSize * font.getStringWidth(subString) / 1000;
                    if (size > text_width) {
                        if (lastSpace < 0)
                            lastSpace = spaceIndex;
                        subString = text.substring(0, lastSpace);
                        lines.add(subString);
                        text = text.substring(lastSpace).trim();
                        lastSpace = -1;
                    } else if (spaceIndex == text.length()) {
                        lines.add(text);
                        text = "";
                    } else {
                        lastSpace = spaceIndex;
                    }
                }
            }

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(text_left, 70);

            for (String line: lines)
            {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
            }
            contentStream.endText();
            contentStream.close();

            String path = root.getAbsolutePath() + "/" + file_name +".pdf";

            document.save(path);
            document.close();

            return path;

        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while creating PDF", e);
        }

        return "error";


    }

    class SaveTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            String path = createPdf();
            return path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Toast.makeText(getActivity(), "잠시 기다리세요.", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);

            Toast.makeText(getActivity(), path+"에 PDF 파일로 저장했습니다.", Toast.LENGTH_LONG).show();
        }

    }

    public void showDialog(){

        Button confirm_btn = dialog.findViewById(R.id.confirm_button);
        EditText editText = dialog.findViewById(R.id.filename);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                file_name = editText.getText().toString();
                dialog.dismiss();
            }
        });

    }

    private void writeFile(String fileName, String msg) {
        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(fileName,
                    Context.MODE_PRIVATE));
            oStreamWriter.write(msg);
            oStreamWriter.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}