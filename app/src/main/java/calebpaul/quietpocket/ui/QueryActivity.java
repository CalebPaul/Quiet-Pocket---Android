package calebpaul.quietpocket.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import calebpaul.quietpocket.R;

public class QueryActivity extends AppCompatActivity {

    //TODO - Validate form w snackbar feedback

    private static final String TAG = QueryActivity.class.getSimpleName();

    @Bind(R.id.queryEditText) EditText mQueryEditText;
    @Bind(R.id.titleTextView) TextView mTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);

        Typeface ptSans = Typeface.createFromAsset(getAssets(), "fonts/PTSans.ttf");
        mTitleTextView.setTypeface(ptSans);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String queryString = mQueryEditText.getText().toString();
            Intent intent = new Intent(QueryActivity.this, MainActivity.class);
            intent.putExtra("query", queryString);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
