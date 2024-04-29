package com.l2h1.tamagotchi.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.l2h1.tamagotchi.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FriendsActivity extends AppCompatActivity {

    private static final String TAG = "bluetooth";
    private static final UUID APP_UUID =
            UUID.fromString("f1e9765e-8cf1-4566-b90f-242a508269cf");

    private BluetoothDevice deviceConnect;
    ConnectedThread connectedThread;
    private Context context;
    private Button sendID, retrieveID, letsSend;
    private BluetoothLeAdvertiser advertiser;
    private BluetoothLeScanner scanner;

    private boolean wantToSend = false;

    private final ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.v("bluetooth", "bluetooth enable");
                } else {
                    Log.v("bluetooth", "bluetooth not enable");
                    finish();
                }
            });

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  //not yet in use
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devShowDeviceInfo(device);
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.v(TAG, "device bounding : " + device.getName());
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "device paired !", Toast.LENGTH_SHORT).show();
                    if(wantToSend) {
                        startServer();
                    } else {
                        startConnect(device);
                    }
                }
            }
        }
    };

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v("bluetooth", "Advertising started successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e("bluetooth", "Advertising failed with error code: " + errorCode);
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            devShowDeviceInfo(device);
            boolean createBond = device.createBond();
            Log.v(TAG, "createBond : " + createBond);
            if(createBond) {
                deviceConnect = device;
                //Toast.makeText(context, deviceConnect + " Device paired ! " + device, Toast.LENGTH_SHORT).show();
                // research result
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("bluetooth", "Scan failed with error code: " + errorCode);
        }
    };

    private BluetoothAdapter bluetoothAdapter;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.v("bluetooth", "bluetooth enable : " + bluetoothAdapter.isEnabled());
        ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);

        if (!bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(FriendsActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("bluetooth", "application doesn't have 'Manifest.permission.BLUETOOTH_CONNECT' granted");
            } else {
                askEnableBluetooth();
            }
        } else {
            Toast.makeText(this, "Bluetooth already on", Toast.LENGTH_SHORT).show();
        }


        sendID = findViewById(R.id.send);
        retrieveID = findViewById(R.id.retrieve);
        letsSend = findViewById(R.id.lets_send);

        letsSend.setOnClickListener(e -> {
            if(deviceConnect != null) {
                sendMessage();
            }
        });

        sendID.setOnClickListener(e -> {
            wantToSend = false;
            startScanningForFriends();
        });

        retrieveID.setOnClickListener(e -> {
            wantToSend = true;
            startAdvertising();
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.context = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
            if(connectedThread != null) connectedThread.cancel();
        } catch (IllegalArgumentException e) {
            Log.e("bluetooth", "receiver not registered");
        }
    }

    private void devShowDeviceInfo(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.v("bluetooth", "found device : " + device.getName() + " | " + device.getAlias() + " | " + device.getAddress());
        } else {
            Log.v("bluetooth", "found device : " + device.getName() + " | " + device.getAddress());
        }
    }

    private void askEnableBluetooth() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        someActivityResultLauncher.launch(enableBluetooth);
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            if (advertiser != null) {
                AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                        .setConnectable(true)
                        .setTimeout(3000)
                        .build();

                ParcelUuid parcelUuid = new ParcelUuid(APP_UUID);
                AdvertiseData data = new AdvertiseData.Builder()
                        .setIncludeDeviceName(false)
                        .addServiceUuid(parcelUuid)
                        .build();

                advertiser.startAdvertising(settings, data, advertiseCallback);
            } else {
                Log.e("bluetooth", "BluetoothLeAdvertiser is null");
            }
        } else {
            Log.e("bluetooth", "Bluetooth is not enabled");
        }
    }

    @SuppressLint("MissingPermission")
    private void startScanningForFriends() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) {
                ParcelUuid parcelUuid = new ParcelUuid(APP_UUID);
                ScanFilter scanFilter = new ScanFilter.Builder()
                        .setServiceUuid(parcelUuid)
                        .build();

                List<ScanFilter> filters = new ArrayList<>();
                filters.add(scanFilter);

                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //foreground
                        .build();

                scanner.startScan(filters, settings, scanCallback);
            } else {
                Log.e("bluetooth", "BluetoothLeScanner is null");
            }
        } else {
            Log.e("bluetooth", "Bluetooth is not enabled");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("perms", Arrays.toString(permissions) + " -> " + Arrays.toString(grantResults) + " / requestCode : " + requestCode);
        if (grantResults.length != 0) {
            if (requestCode == 1) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted for CONNECT", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    askEnableBluetooth();
                } else {
                    Toast.makeText(this, "Permission denied for CONNECT", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if(requestCode == 2) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted for SCAN", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, 3);
                } else {
                    Toast.makeText(this, "Permission denied for SCAN", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if(requestCode == 3) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted for ADVERTISE", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied for ADVERTISE", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }






    private class ConnectThread extends Thread {
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            deviceConnect = device;
        }

        @SuppressLint("MissingPermission") //should work without cause it's already checked...
        public void run() {
            Log.v(TAG, "START RUNNING ConnectThread - starting...");

            BluetoothSocket tmp = null;
            try {
                tmp = deviceConnect.createRfcommSocketToServiceRecord(APP_UUID);
                Log.v(TAG, "INSTANTIATE ConnectThread - setting up client using : " + APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "ERROR ConnectThread - (Bluetooth not available/no permissions) IOException : " + e.getMessage());
            }
            socket = tmp;

            try {
                Log.v(TAG, "INIT ConnectThread - attempting to connect ...");
                socket.connect(); //blocking call
                Log.v(TAG, "-> CONNECTED ConnectThread - connected to remote device");
            } catch (IOException e) {
                try {
                    socket.close();
                    Log.e(TAG, "ERROR ConnectThread - close socket");
                } catch (IOException i) {
                    Log.e(TAG, "ERROR ConnectThread - BluetoothSocket close failed : " + i.getMessage());
                }
                Log.e(TAG, "ERROR ConnectThread - can't connect to " + APP_UUID);
            }


            connected(socket);
            Log.v(TAG, "END ConnectThread - exit...");
        }

        public void cancel() { //TODO: call this onDestroy()/onPause()
            try {
                Log.v(TAG, "CANCEL ConnectThread - canceling...");
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "ERROR ConnectThread - BluetoothSocket close failed : " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket bluetoothSocket) {
        Log.v(TAG, "::: CONNECTED - try performing transmissions");

        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.v(TAG, "INSTANTIATE ConnectedThread - getting BluetoothSocket input/output streams");
            } catch (IOException e) {
                Log.e(TAG, "ERROR ConnectedThread - (get BluetoothSocket streams failed) IOException : " + e.getMessage());
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            Log.v(TAG, "START RUNNING ConnectedThread - starting...");
            byte[] buffer = new byte[1024];
            int bytes;
            //sendMessage();

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.v(TAG, "READ ConnectedThread - InputStream read : " + incomingMessage);

                    runOnUiThread(() -> Toast.makeText(context, incomingMessage, Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    Log.e(TAG, "ERROR ConnectedThread - (read) IOException : " + e.getMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.v(TAG, "WRITE ConnectedThread - writing '" + text + "'...");
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "ERROR ConnectedThread - (write) IOException : " + e.getMessage());
            }
        }

        public void cancel() { //TODO: call this onDestroy()/onPause()
            Log.v(TAG, "CANCEL ConnectedThread - canceling...");
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "ERROR ConnectedThread - BluetoothSocket close failed : " + e.getMessage());
            }
        }
    }


    public void sendMessage() {
        //connectedThread.write("hello !".getBytes(Charset.defaultCharset()));
        connectedThread.write(FirebaseAuth.getInstance().getCurrentUser().getUid().getBytes(Charset.defaultCharset()));
    }


    public void startServer() {
        AcceptThread accept = new AcceptThread();
        accept.start();
    }

    public void startConnect(BluetoothDevice device) {
        ConnectThread connect = new ConnectThread(device);
        connect.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private static final String SDP_SERVICE_NAME = "tamagotchi";

        @SuppressLint("MissingPermission") //should work without cause it's already checked...
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SDP_SERVICE_NAME, APP_UUID);
                Log.v(TAG, "INSTANTIATE AcceptThread - setting up server using : "
                        + APP_UUID + " | with service name for SDP record : " + SDP_SERVICE_NAME);
            } catch (IOException e) {
                Log.e(TAG, "ERROR AcceptThread - (Bluetooth not available/no permissions/channel in use) IOException : " + e.getMessage());
            }
            serverSocket = tmp;
        }

        public void run() {
            Log.v(TAG, "START RUNNING AcceptThread - starting...");
            BluetoothSocket socket = null;
            try {
                Log.v(TAG, "INIT - RFCOM server socket waiting for acceptation...");
                socket = serverSocket.accept(); //blocking call
                Log.v(TAG, "-> ACCEPTED - RFCOM server socket accepted connection !");
            } catch (IOException e) {
                Log.e(TAG, "ERROR AcceptThread - (server timeout/aborted) IOException : " + e.getMessage());
            }

            if (socket != null) {
                connected(socket);
            }
            Log.v(TAG, "END AcceptThread - exit...");
        }

        public void cancel() { //TODO: call this onDestroy()/onPause()
            Log.v(TAG, "CANCEL AcceptThread - canceling...");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ERROR AcceptThread - ServerSocket close failed : " + e.getMessage());
            }
        }


    }
}