package cx.myhome.thisis.smbimageviewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences prefer = getSharedPreferences("config", MODE_PRIVATE);
        // final Editor editor = prefer.edit();
        // editor.putString("uri", "smb://<user>:<password>@<host>/foo/bar/");
        // editor.commit();
        final String uri = prefer.getString("uri", "smb://localhost/");

        final AsyncSmbAccess asyncTask = new AsyncSmbAccess(this);
        asyncTask.execute(uri);
    }
}
