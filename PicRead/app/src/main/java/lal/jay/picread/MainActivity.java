package lal.jay.picread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button detectButton;

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(getApplicationContext(),"App needs Camera Access",Toast.LENGTH_SHORT).show();

            if(grantResults[1]  != PackageManager.PERMISSION_GRANTED )
                Toast.makeText(getApplicationContext(),"App needs to Storage Access",Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        //For Android Marshmallow Onwards..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //If permission has not been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "App requires Camera Permission!", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION_RESULT);
            }
        }

        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private Uri file;

    public void takePicture(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        file = Uri.fromFile(getOutputMediaFile());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,file);

        startActivityForResult(cameraIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("IntentResult","requestCode = "+requestCode+", resultCode = "+resultCode);
        if(requestCode==1 && resultCode == RESULT_OK)
        {
            Log.d("IntentResult","Intent Call Successfull!");

            imageView.setImageURI(file);

            detectButton = findViewById(R.id.btn_detect);
            detectButton.setVisibility(View.VISIBLE);

        }
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"PicRead");

        if(!mediaStorageDir.exists())
        {
            if(!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator  + "IMG_" + timeStamp + ".jpg");

    }

    public void detectObjects(View view) {
        Log.d("OnClick","Detect Button Clicked...");
    }
}
