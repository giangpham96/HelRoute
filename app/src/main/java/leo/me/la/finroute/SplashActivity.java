package leo.me.la.finroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import leo.me.la.finroute.getRoutes.GetRoutesActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, GetRoutesActivity.class);
        startActivity(intent);
        finish();
    }
}
