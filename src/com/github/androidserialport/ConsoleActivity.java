package com.github.androidserialport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConsoleActivity extends Activity implements OnClickListener {

    private StringBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private Button sendButton;
    private EditText textToSend;
    private TextView incomingText;

    private class StringBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String string = intent.getStringExtra(ReadingService.EXTRA_STRING);
            incomingText.append(string);

            final Layout layout = incomingText.getLayout();
            if (layout != null) {
                int scrollDelta = layout.getLineBottom(incomingText.getLineCount() - 1) - incomingText.getScrollY()
                        - incomingText.getHeight();
                if (scrollDelta > 0) {
                    incomingText.scrollBy(0, scrollDelta);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        sendButton = (Button) findViewById(R.id.buttonSend);

        textToSend = (EditText) findViewById(R.id.editTextOutgoing);
        incomingText = (TextView) findViewById(R.id.textViewIncoming);
        incomingText.setMovementMethod(new ScrollingMovementMethod());
        incomingText.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", incomingText.getText());
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);
                return true;
            }
        });
        sendButton.setOnClickListener(this);
        receiver = new StringBroadcastReceiver();
        intentFilter = new IntentFilter(ReadingService.ACTION_READ_STRING);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(sendButton)) {
            sendString(textToSend.getText().toString());
        } else {
            // TODO
        }
    }

    private void sendString(String string) {
        Intent intent = new Intent(WritingService.ACTION_SEND_STRING);
        intent.putExtra(WritingService.EXTRA_STRING, string);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_console, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, SerialPreferenceActivity.class));
            break;

        default:
            break;
        }
        return true;
    }
}
