package com.example.ocrminiproject;

import static android.Manifest.permission_group.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;

import java.util.concurrent.Executor;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitMap;
    private boolean hasReqPermsBeenCalled=false;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idIVLogo);
        resultTV = findViewById(R.id.idTVResultText);
        snapBtn = findViewById(R.id.idIVButton);
        detectBtn = findViewById(R.id.idIVButton2);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetectText();
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPerms() || hasReqPermsBeenCalled){
                    CaptureImage();
                }
                else{
                    ReqPerms();
                }
            }
        });
    }

    private boolean CheckPerms(){
        int camPerms = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return camPerms == PackageManager.PERMISSION_GRANTED;
    }

    private void ReqPerms(){
//        Toast.makeText(this, "ONO", Toast.LENGTH_LONG).show();
        int PERMCODE = 200;
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        hasReqPermsBeenCalled=true;
    }

    private void CaptureImage(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(takePicture.resolveActivity(getPackageManager())!=null){
        startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(grantResults.length>0){
//            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//            if(cameraPermission){
//                Toast.makeText(this, "Permissions Granted...", Toast.LENGTH_SHORT).show();
//                CaptureImage();
//            }
//            else{
//                Toast.makeText(this, "Permissions Denied, error code "+grantResults[0], Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK);{
            Bundle extras = data.getExtras();
            imageBitMap = (Bitmap) extras.get("data");

//            Bitmap bOutput;
//            float degrees = 90; //rotation degree
//            Matrix matrix = new Matrix();
//            matrix.setRotate(degrees);
//            bOutput = Bitmap.createBitmap(imageBitMap, 0, 0, imageBitMap.getWidth(), imageBitMap.getHeight(), matrix, true);

            captureIV.setImageBitmap(imageBitMap);
        }
    }

    private void DetectText(){
        InputImage image = InputImage.fromBitmap(imageBitMap, 90);
        TextRecognizerOptionsInterface textRecognizerOptionsInterface = new TextRecognizerOptionsInterface() {
            @Override
            public int getLoggingEventId() {
                return 0;
            }

            @Override
            public int getLoggingLanguageOption() {
                return 0;
            }

            @NonNull
            @Override
            public String getCreatorClass() {
                return null;
            }

            @NonNull
            @Override
            public String getLoggingLibraryName() {
                return null;
            }

            @NonNull
            @Override
            public String getModuleId() {
                return null;
            }

            @Nullable
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public boolean getIsThickClient() {
                return false;
            }
        };
        TextRecognizer recognizer = TextRecognition.getClient(textRecognizerOptionsInterface);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for(Text.Line line: block.getLines()){
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect linRect = line.getBoundingBox();
                        for(Text.Element element: line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultTV.setText(blockText);
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Failed to detect text from the given image"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}