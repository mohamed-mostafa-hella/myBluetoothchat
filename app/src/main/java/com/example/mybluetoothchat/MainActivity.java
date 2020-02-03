package com.example.mybluetoothchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private TextView state , messegecome;
    private EditText messegefilde;

    private BluetoothAdapter bluetoothAdapter;
    private Intent onIntent;
    private IntentFilter intentFilter, modeIntIntentFilter;
    private Intent seenintent;


    private ArrayAdapter<String> adapter;
    private ArrayList<String> data;
    private List<BluetoothDevice> Mpaired_devices , Mdeccoverd_devices,alldevices;

    public static final int onrequestcode = 854;
    public static final int STATE_lISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAIL = 4;
    public static final int STATE_MESSAGE_RECIIVED = 5;

    public static final String APP_NAME = "mmh_bluetooth_chat_app";
    public static final UUID MY_UUID= UUID.fromString("f8550b0b-558e-4e6a-a9a0-cc7b45b3a8b6");


    SendReceve sendReceve;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.viewlist);
        state = findViewById(R.id.status);
        messegecome = findViewById(R.id.messagecome);
        messegefilde =findViewById(R.id.messagefilde);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        onIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        modeIntIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        seenintent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        Mpaired_devices=new ArrayList<>();
        Mdeccoverd_devices=new ArrayList<>();
        alldevices=new ArrayList<>();

        data = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);




        registerReceiver(recever, modeIntIntentFilter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Client client = new Client(alldevices.get(position));
                client.start();

                state.setText("connecting");
            }
        });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case STATE_lISTENING:
                    state.setText("listening");
                    break;
                case STATE_CONNECTING:
                    state.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    state.setText("Connected");
                    break;
                case STATE_CONNECTION_FAIL:
                    state.setText("CONNECTION FAIL");
                    break;
                case STATE_MESSAGE_RECIIVED:
                    byte[] readbuffer = (byte[]) msg.obj;
                    String tempmsg = new String(readbuffer , 0 , msg.arg1);
                    messegecome.setText(tempmsg);
                    break;
            }
            return true;
        }
    });


    BroadcastReceiver recever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                alldevices.add(device);
                String name = device.getName();
                String MAC = device.getAddress();
                data.add(name + "    -----    " + MAC);
                adapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        state.setText("SCAN_MODE_NONE");
                        Toast.makeText(context, "SCAN_MODE_NONE" + mode, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        state.setText("SCAN_MODE_CONNECTABLE");
                        Toast.makeText(context, "SCAN_MODE_CONNECTABLE" + mode, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        state.setText("SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        Toast.makeText(context, "SCAN_MODE_CONNECTABLE_DISCOVERABLE" + mode, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        state.setText("error");
                        Toast.makeText(context, "error" + mode, Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }
    };

    private void setDuration(int duration) {
        seenintent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration); // note that Zero main always on
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == onrequestcode) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setOn(View view) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(onIntent, onrequestcode);
            }
        } else {
            Toast.makeText(this, "this device not support bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    public void setOff(View view) {
        bluetoothAdapter.disable();
    }

    public void getPaired(View view) {
        Set<BluetoothDevice> pairdDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairdDevices) {
            Mpaired_devices.add(device);
            alldevices.add(device);

            String deviceName = device.getName();
            String deviceMAC = device.getAddress();
            data.add(deviceName + "    --- 11  --    " + deviceMAC);

            Toast.makeText(this, deviceName + "    -----    " + deviceMAC + data.size(), Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
    }

    public void getaAll(View view) {
        bluetoothAdapter.startDiscovery();
        registerReceiver(recever, intentFilter);
    }

    public void seen(View view) {
        setDuration(8);
        startActivity(seenintent);
    }

    public void listen(View view) {
        ServerClass serverClass = new ServerClass();
        serverClass.start();
    }

    public void send(View view) {
        String messge = messegefilde.getText().toString();
        sendReceve.write(messge.getBytes());
    }


    class ServerClass extends Thread {

        private BluetoothServerSocket serverSocket ;

        public ServerClass() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME , MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            while(socket == null){
                try {
                    Message msg = Message.obtain();
                    msg.what=STATE_CONNECTING;
                    handler.sendMessage(msg);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    Message msg = Message.obtain();
                    msg.what=STATE_CONNECTION_FAIL;
                    handler.sendMessage(msg);
                }
            }

            if(socket != null){
                Message msg = Message.obtain();
                msg.what=STATE_CONNECTED;
                handler.sendMessage(msg);

                sendReceve = new SendReceve(socket);
                sendReceve.start();
            }

        }
    }

    class Client extends Thread{
        private BluetoothSocket socket;
        private BluetoothDevice device;

        public Client(BluetoothDevice device) {
            this.device = device;
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                Message msg = Message.obtain();
                msg.what=STATE_CONNECTED;
                handler.sendMessage(msg);

                sendReceve = new SendReceve(socket);
                sendReceve.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message msg = Message.obtain();
                msg.what=STATE_CONNECTION_FAIL;
                handler.sendMessage(msg);

            }
        }
    }

    class SendReceve extends Thread{
        private BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceve(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;

            InputStream tempin=null;
            OutputStream temout=null;

            try {
                tempin = bluetoothSocket.getInputStream();
                temout = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempin;
            outputStream = temout;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            try {
                bytes = inputStream.read(buffer);

                handler.obtainMessage(STATE_MESSAGE_RECIIVED , bytes , -1 , buffer ).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}



