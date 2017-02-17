package calebpaul.quietpocket.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import calebpaul.quietpocket.R;

public class QueryActivity extends AppCompatActivity {

    //TODO - Validate form w snackbar feedback

    private static final String TAG = QueryActivity.class.getSimpleName();

    @Bind(R.id.queryEditText) TextView mQueryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);


    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String queryString = mQueryEditText.getText().toString();
                Intent intent = new Intent(QueryActivity.this, MainActivity.class);

                intent.putExtra("query", queryString);

                startActivity(intent);
                Toast.makeText(QueryActivity.this, "Enter Pressed", Toast.LENGTH_SHORT).show();
                return true;
            }
        return false;
    }
}
