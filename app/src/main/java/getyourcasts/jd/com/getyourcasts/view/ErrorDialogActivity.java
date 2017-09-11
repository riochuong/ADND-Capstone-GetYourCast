package getyourcasts.jd.com.getyourcasts.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper;

public class ErrorDialogActivity extends Activity {

    public static final String MESSAGE_KEY  = "message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String msg = getIntent().getStringExtra(MESSAGE_KEY);
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(msg);
        dlgAlert.setPositiveButton(this.getResources().getString(R.string.ok_string),
                (dialog, which) -> {
                    ErrorDialogActivity.this.finish();
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
