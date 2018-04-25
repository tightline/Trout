package ryan.trout.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import ryan.trout.R;

public class Legend extends AppCompatActivity {

    TextView gauge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legend);

        gauge = (TextView) findViewById(R.id.textView);
        //gauge.setTextColor(Color.parseColor("Hue_Orange"));

        //        android:textColor="#ff009416"
        //        android:textColor="#ff070085"
        //        android:textColor="#ff7a8500"
        //        android:textColor="#ff8c8f00"
    }

}
