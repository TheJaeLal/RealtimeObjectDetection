package lal.jay.smartlens;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launch(View v) {

        Log.i("launch","Launch Button Clicked");
        Toast.makeText(getApplicationContext(), "Launching", Toast.LENGTH_SHORT).show();

    }
}
