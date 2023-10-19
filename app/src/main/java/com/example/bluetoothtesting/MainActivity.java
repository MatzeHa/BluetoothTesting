package com.example.bluetoothtesting;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define Buttons
        Button btnScan = findViewById(R.id.btn_scan);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnSendData = findViewById(R.id.btn_senddata);


        // Check if Smartphone has Bluetooth
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getBaseContext(), "Smartphone besitzt keinen Bluetooth-Adapter!",
                    Toast.LENGTH_LONG).show();
        } else {
            switchBTOn();
        }

        // check Permissions
        checkLocationPermission();

        // Button Scan
        BluetoothScanner btScan = new BluetoothScanner(this);
        btnScan.setOnClickListener(view -> btScan.startScanning());

        // Button Connect
        ConnectThread connectThread = new ConnectThread(bluetoothAdapter);
        btnConnect.setOnClickListener(view -> connectThread.run());

        // Button Send Data

        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

    // Function for Requesting Permissions
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return true;
        }
        return false;
    }



    // Function for activating Bluetooth
    public void switchBTOn() {
        // If BT is turned off, User has to turn on BT
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        checkBTOn.launch(enableBtIntent);
    }
    ActivityResultLauncher<Intent> checkBTOn = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    Toast.makeText(getBaseContext(),
                            "Bluetooth konnte nicht angeschaltet werden, " +
                                    "bitte manuell einschalten",
                            Toast.LENGTH_LONG).show();                    }
            });

    // On Destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
       // unregisterReceiver(receiver);
    }


    // Class for Scanning Bluetooth Devices
    private class BluetoothScanner extends Thread {
        private final BluetoothAdapter bluetoothAdapter;
        private final Context context;

        public BluetoothScanner(Context context) {
            this.context = context;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }


        public void startScanning() {
            // Registrieren Sie einen BroadcastReceiver, um Bluetooth-Geräte in der Nähe zu erkennen.
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(bluetoothReceiver, filter);
            Toast.makeText(getBaseContext(),
                    "Starte Scanvorgang",
                    Toast.LENGTH_LONG).show();
            // Starten Sie den Scanvorgang.
            bluetoothAdapter.startDiscovery();
        }

        private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();

                    System.out.println(deviceName + " " + deviceAddress);
                    if (deviceAddress.equals("00:1A:7A:01:18:23")) {
                        Toast.makeText(getBaseContext(),"DEVICE FOUND!",
                                Toast.LENGTH_SHORT).show();
                        Toast.makeText(getBaseContext(),deviceName + " " + deviceAddress,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        public void stopScanning() {
            // Beenden Sie den Bluetooth-Scan und deaktivieren Sie den BroadcastReceiver.
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
            context.unregisterReceiver(bluetoothReceiver);
            Toast.makeText(getBaseContext(),
                    "Scanvorgang Beendet",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Class for Connecting to Bluetooth-Server
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final BluetoothAdapter bluetoothAdapter;

        public ConnectThread(BluetoothAdapter btAdapter) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            bluetoothAdapter = btAdapter;
            mmDevice = bluetoothAdapter.getRemoteDevice("00:1A:7A:01:18:23");

            UUID uuid = UUID.fromString("8BDAD8F1-A8CE-AD14-AFF9-D8BBC1938454");

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                System.out.println("Socket's create() method failed" + e);
            }
            mmSocket = tmp;
        }

        public void run() {
            System.out.println("RUN");
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    System.out.println("Could not close the client socket" + closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);

            Toast.makeText(getBaseContext(), "YAY! CONNECTED!",
                    Toast.LENGTH_LONG).show();        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                System.out.println("Could not close the client socket" + e);
            }
        }
    }

}


