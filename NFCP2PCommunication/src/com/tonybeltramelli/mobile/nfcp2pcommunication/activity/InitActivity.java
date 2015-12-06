package com.tonybeltramelli.mobile.nfcp2pcommunication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.tonybeltramelli.mobile.nfcp2pcommunication.R;
import com.tonybeltramelli.mobile.nfcp2pcommunication.utils.NFCUtils;

import java.util.List;

/**
 * Created by KPRASAN6 on 10/31/2015.
 */
public class InitActivity extends Activity {
    private NfcAdapter _nfcAdapter;
    private PendingIntent _pendingIntent;
    private IntentFilter[] _intentFilters;

    Boolean action;
    Boolean Accessing = Boolean.FALSE;
    Boolean Sharing = Boolean.TRUE;

    private String accessKey = "0000";

    private String ownerKey = "abc1";
    private String sharedKey = "0000";
    private final String _MIME_TYPE = "text/plain";
    private Button shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        shareButton = (Button) findViewById(R.id.share);
        shareButton.setEnabled(false);
        _init();
    }

    private void _init()
    {
        _nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (_nfcAdapter == null)
        {
            Toast.makeText(this, "This device does not support NFC.", Toast.LENGTH_LONG).show();
            return;
        }

        if (_nfcAdapter.isEnabled())
        {
            _pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try
            {
                ndefDetected.addDataType(_MIME_TYPE);
            } catch (IntentFilter.MalformedMimeTypeException e)
            {
                Log.e(this.toString(), e.getMessage());
            }
            _intentFilters = new IntentFilter[] { ndefDetected };
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _enableNdefExchangeMode(sharedKey);
    }

    private void _enableNdefExchangeMode(String stringMessage)
    {
        NdefMessage message = NFCUtils.getNewMessage(_MIME_TYPE, stringMessage.getBytes());
        _nfcAdapter.setNdefPushMessage(message, this);
        _nfcAdapter.enableForegroundDispatch(this, _pendingIntent, _intentFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            List<String> msgs = NFCUtils.getStringsFromNfcIntent(intent);
            sharedKey = msgs.get(0);
            Toast.makeText(this, "Access received", Toast.LENGTH_LONG).show();
        }
    }

    public void onShareRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.shareByNFC:
                if (checked) {
                    shareButton.setEnabled(false);
                    action = Sharing;
                    _enableNdefExchangeMode(ownerKey);
                }
                break;
            case R.id.shareByNet:
                if (checked) {
                    shareButton.setEnabled(true);
                }
                break;
        }
    }

    public void shareKey(View view) {
        //
    }

    public void onAccessRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        action = Accessing;

        switch(view.getId()) {
            case R.id.accessUrCar:
                if (checked) {
                    _enableNdefExchangeMode(ownerKey);
                }
                break;
            case R.id.accessSharedCar:
                if (checked) {
                    accessKey = sharedKey;
                    _enableNdefExchangeMode(sharedKey);
                }
                break;
        }
    }

    public void accessKey(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.tapAgainstCar);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
