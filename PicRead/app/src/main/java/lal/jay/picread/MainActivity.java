package lal.jay.picread;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    //Buttons..

    private Button captureButton;
    private Button detectButton;
    private Button clearButton;
    private Button shareButton;

    private ProgressBar progressBar;
    private TextView progressText;

//    private static String serverUrl = "http://35.200.202.208:5000/";

//    private static String serverUrl = "http://192.168.43.147:5000/";
//
//    private static String serverUrl;

    private static int portNo = 5000;

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

        captureButton = findViewById(R.id.btn_capture);
        detectButton = findViewById(R.id.btn_detect);
        clearButton = findViewById(R.id.btn_clear);
        shareButton = findViewById(R.id.btn_share);

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

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)captureButton.getLayoutParams();
            layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            captureButton.setLayoutParams(layoutParams);

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

        //Get ImageView Content as Bitmap!
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

//        String response = uploadImage(bitmap2String(bitmap));


        uploadImage(bitmap2String(bitmap,getImageQuality()));

//        if (response!=null) {
//            Log.d("Network", "Response received");
//
//            Bitmap resultImage = string2Bitmap(response);
//
//            imageView.setImageBitmap(resultImage);
//        }
//        else{
//            Log.d("Network","Error! Null response");
//        }
    }

    public void uploadImage(final String image)
    {

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        progressText = findViewById(R.id.progressTextView);
        progressText.setVisibility(View.VISIBLE);

        //Resolve server URL

        String serverUrl = getServerUrl();

        Log.d("Network","Sending request to Server: "+serverUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("Network", "Response :"+response.length());

                if(response==null || response == "" )
                    Log.d("Network","Empty Response!");

                Bitmap resultImage = string2Bitmap(response);

                Log.d("Network","Converted Response to Bitmap!");

                imageView.setImageBitmap(resultImage);

                progressBar.setVisibility(View.INVISIBLE);
                progressText.setVisibility(View.INVISIBLE);
                detectButton.setVisibility(View.INVISIBLE);
                captureButton.setVisibility(View.INVISIBLE);

                clearButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);

//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)captureButton.getLayoutParams();
//                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                captureButton.setLayoutParams(layoutParams);

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Network", "Erroneous Response!");

                        progressBar.setVisibility(View.INVISIBLE);
                        progressText.setVisibility(View.INVISIBLE);

                        Toast.makeText(getApplicationContext(),"Unable to reach Server, Check Network Connection",Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("name","default_name.jpg");
                params.put("image",image);
//                uploaded = true;
                //return super.getParams();
                return params;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);

    }

    public String bitmap2String(Bitmap bitmap,int imageQuality)
    {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,imageQuality,byteArrayOutputStream);

        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    private Bitmap string2Bitmap(String encodedImage)
    {
//        Log.d("OutputString",encodedImage);

        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }


    public void launchSettings(View view) {
        Log.d("onClick","Launching Settings Activity");
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private String getServerUrl()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverIPAddress = sharedPref.getString("pref_server_ip","");

        String serverUrl = "http://" + serverIPAddress + ":" + portNo + "/";

        //        Log.d("Preferences","Serever IP Address = "+serverIPAddress);

        return serverUrl;

    }

    private int getImageQuality()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPref.getString("pref_image_quality","");

        int imageQuality = Integer.parseInt(value);

        return imageQuality;
    }


    public void clearImage(View view) {
        //Get ImageView Content as Bitmap!
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        imageView.setImageDrawable(null);
        bitmap.recycle();

        shareButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)captureButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        captureButton.setLayoutParams(layoutParams);

        captureButton.setVisibility(View.VISIBLE);

    }

    public void shareImage(View view) {

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Object Detection", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Image"));

    }


}

