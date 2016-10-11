package com.cloudmind.momi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.cloudmind.momi.databinding.ActivityControlBinding;
import com.cloudmind.momi.databinding.OptionsLayoutBinding;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.palaima.smoothbluetooth.Device;
import io.palaima.smoothbluetooth.SmoothBluetooth;

/**
 */
public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "MOMI";
    private ActivityControlBinding binding;
    private OptionsLayoutBinding optionsBinding;
    public ObservableField<String> speed = new ObservableField<>("Speed: 0");
    public ObservableField<String> turn = new ObservableField<>("Turn: 0");
    public ObservableField<String> sonarMid = new ObservableField<>("Sonar: 0");
    public ObservableField<String> drift = new ObservableField<>("Drift: 0");
    public ObservableBoolean isManual = new ObservableBoolean(true);
    public ObservableBoolean isFound = new ObservableBoolean(false);
    public ObservableBoolean isConnecting = new ObservableBoolean(false);
    public ObservableBoolean isConnected = new ObservableBoolean(false);
    public ObservableBoolean isScanning = new ObservableBoolean(false);

    private final static int OFFSET = 100;
    private int newSpeed, prevSpeed, newTurn, prevTurn, newDrift;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName;
    private static final String MOMI_MAC_ADDRESS = "B8:27:EB:02:13:4E";

    private SmoothBluetooth mSmoothBlueTooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_control);
        binding.setControls(this);

        optionsBinding = OptionsLayoutBinding.inflate(getLayoutInflater(), null, false);
        optionsBinding.setControl(this);

        setupControls();
        mSmoothBlueTooth = new SmoothBluetooth(this, mListener);
//        checkBlueTooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSmoothBlueTooth.stop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSmoothBlueTooth.tryConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.options_menu:
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Options")
                        .setView(optionsBinding.getRoot())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ((ViewGroup)optionsBinding.getRoot().getParent()).removeView(optionsBinding.getRoot());
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    mSmoothBlueTooth.tryConnection();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void setupControls() {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                newSpeed = seekBar.getProgress() - OFFSET;
                speed.set("Speed: " + newSpeed);
                sendData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
                seekBar.setProgress(OFFSET);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Lost focus, change position back to OFFSET
                seekBar.setProgress(OFFSET);
                sendData();
            }
        });

        binding.seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                newTurn = seekBar.getProgress() - OFFSET;
                turn.set("Turn: " + newTurn);
                sendData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(OFFSET);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Lost focus, change position back to OFFSET
                seekBar.setProgress(OFFSET);
                sendData();
            }
        });

        newDrift = SharedPreferencesHelper.get().getIntValue(this, Constants.PREF_DRIFT);
        drift.set("Drift: " + newDrift);
        optionsBinding.drift.setProgress(newDrift + OFFSET);

        optionsBinding.drift.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                newDrift = seekBar.getProgress() - OFFSET;
                drift.set("Drift: " + newDrift);
                SharedPreferencesHelper.get().saveInt(ControlActivity.this, newDrift, Constants.PREF_DRIFT);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        boolean prefManual = SharedPreferencesHelper.get().getBooleanValue(this, Constants.PREF_MANUAL_MODE);
        isManual.set(prefManual);
    }

    public void onConnectClick(View view){
        if (mSmoothBlueTooth.isBluetoothAvailable() && mSmoothBlueTooth.isBluetoothEnabled()){
            if (mSmoothBlueTooth.isConnected()){
                Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
            }
            else {
                mSmoothBlueTooth.tryConnection();
            }
        }
        else
        {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleManualMode(CompoundButton compoundButton, boolean b) {
        isManual.set(b);
        SharedPreferencesHelper.get().saveBoolean(this, b, Constants.PREF_MANUAL_MODE);
    }

        /**
         * Sends data to the robot in the form of speed,turn
         * Example: 50,0 (50% forward, no turn)
         * or 100,-25 (100% forward, left turn 25%)
         * or -33, 44 (33% backward, right turn 44%)
         */
    private void sendData(){
        if (prevSpeed != newSpeed || prevTurn != newTurn){
            prevSpeed = newSpeed;
            prevTurn = newTurn;
            String instruction = "";
            if (isManual.get()){
                instruction = getManualInstructions();
            }
            else {
                instruction = getAutomaticInstructions();
            }
            Log.i(TAG, "Instruction: " + instruction);
            if (mSmoothBlueTooth.isConnected()) {
                mSmoothBlueTooth.send(instruction.getBytes());
            }
        }
    }

    /**
     * Returns instructions in raw speed and turn to be handled by the robot
     */
    private String getAutomaticInstructions(){
        return "s" + newSpeed + ",t" + newTurn + ",";
    }

    /**
     * Returns instructions for each wheel, doing the calculation for the turn and taking
     * into account the drift
     * @return
     */
    private String getManualInstructions(){
        //Set wheel speed
        int leftWheel = newSpeed;
        int rightWheel = newSpeed;

        //Spinning in place
        if (newSpeed == 0 && newTurn != 0){

            int turnDirection = 0;
            if (newTurn < 0) {
                turnDirection = -1;
            } else if (newTurn > 0) {
                turnDirection = 1;
            }

            leftWheel = newTurn;
            rightWheel = -newTurn;

            if (newDrift > 0) {
                rightWheel = rightWheel + (turnDirection * newDrift);
            } else if (newDrift < 0) {
                leftWheel = leftWheel + (turnDirection * newDrift);
            }
        }
        else {
            //Moving
            int direction = 0;
            if (newSpeed < 0) {
                direction = -1;
            } else if (newSpeed > 0) {
                direction = 1;
            }

            //Apply drift to the turn
            int driftTurn = newTurn + newDrift;

            //Slow down the wheel the turn direction is in
            if (driftTurn > 0) {
                //Right turn
                rightWheel = rightWheel - (direction * driftTurn);
            } else if (driftTurn < 0) {
                //Left turn
                leftWheel = leftWheel + (direction * driftTurn);
            }
        }
        return  "l" + leftWheel + ",r" + rightWheel + ",";
    }

    private SmoothBluetooth.Listener mListener = new SmoothBluetooth.Listener() {

        @Override
        public void onBluetoothNotSupported() {
            Log.e(TAG, "onBluetoothNotSupported");
            Toast.makeText(ControlActivity.this, "Bluetooth unavailable", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBluetoothNotEnabled() {
            Log.e(TAG, "onBluetoothNotEnabled");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        @Override
        public void onConnecting(Device device) {
            Log.i(TAG, "onConnecting");
            Toast.makeText(ControlActivity.this, "Connecting with " + device.getName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnected(Device device) {
            isConnecting.set(false);
            isConnected.set(true);
            Toast.makeText(ControlActivity.this, "Connected with " + device.getName(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "onConnected");

            String hello = "Hello World";
            mSmoothBlueTooth.send(hello.getBytes(), false);
        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "onDisconnected");
            Toast.makeText(ControlActivity.this, "Disconnected ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionFailed(Device device) {
            Log.e(TAG, "onConnectionFailed");
            Toast.makeText(ControlActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
            isConnecting.set(false);
            isConnected.set(false);
        }

        @Override
        public void onDiscoveryStarted() {
            Log.e(TAG, "onDiscoveryStarted");
        }

        @Override
        public void onDiscoveryFinished() {
            Log.e(TAG, "onDiscoveryFinished");
        }

        @Override
        public void onNoDevicesFound() {
            Log.e(TAG, "onNoDevicesFound");
        }

        @Override
        public void onDevicesFound(List<Device> deviceList, SmoothBluetooth.ConnectionCallback connectionCallback) {
            Log.i(TAG, "onDevicesFound");
            Iterator<Device> iter = deviceList.iterator();
            while (iter.hasNext())
            {
                Device device = iter.next();
                Log.e(TAG, "Device = " + device.getName() + ":" + device.getAddress() + ", paired: " + device.isPaired());

                if (MOMI_MAC_ADDRESS.equals(device.getAddress()) && device.isPaired()
                        && isConnected.get() == false && isConnecting.get() == false){
                    isConnecting.set(true);
                    isFound.set(true);
                    isConnected.set(false);
                    Log.e(TAG, "Connect to : " + device.getName());
                    connectionCallback.connectTo(device);
                }
            }
        }

        @Override
        public void onDataReceived(int data){
            Log.e(TAG, "onDataReceived: " + data + ", char: " + (char)data);
        }
    };

}

