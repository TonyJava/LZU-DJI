package cn.edu.lzu.oss.lzu_dji;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    MobileFlyRobot mfr;

    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mfr = new MobileFlyRobot();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DJIApplication.getAircraftInstance() != null) {
                    mfr.startUp(DJIApplication.getAircraftInstance().getFlightController());
                    t.start();
                }
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DJIApplication.getAircraftInstance() != null) {
                    try {
                        //测试
                        mfr.initFC();
                        mfr.ahead(10,2);
                        mfr.left(5,2.5f);
                        mfr.rise(10,3);
                        mfr.turnRight(90,20);
                        mfr.startFC();
                        //测试
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.currentThread().sleep(10);
                    } catch (InterruptedException e) {

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mfr.printfInfo((TextView) MainActivity.this.findViewById(R.id.textView));
                        }
                    });
                }
            }
        });
    }
}
