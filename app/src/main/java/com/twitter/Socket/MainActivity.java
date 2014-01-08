package com.twitter.Socket;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;

import com.google.gson.Gson;
import com.twitter.Socket.Class.TvMessage;
import com.twitter.Socket.Class.TvProgramChange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends ActionBarActivity {

    private EditText txtEdit;
    private Socket socket;
    private BufferedReader socketreader;
    private BufferedWriter socketWriter;
    private Handler mHandler;
    Gson gson = new Gson();
    private volatile boolean connected = false;
    Queue<TvMessage> readQueue = new ConcurrentLinkedQueue<TvMessage>();
    Queue<TvMessage> writeQueue = new ConcurrentLinkedQueue<TvMessage>();

    private void writeToWriteQ(TvMessage msg)
    {
        synchronized (writeQueue) {
            writeQueue.add(msg);
        }
    }

    private TvMessage readFromWriteQ() {
        synchronized (writeQueue) {
            if(!writeQueue.isEmpty()) return readQueue.remove();
        }
        return null;
    }

    private void createConnection(String hostName) {
        try {
            if(socket == null)
            {
                final InetAddress serverAddr = InetAddress.getByName(hostName);
                InetSocketAddress address = new InetSocketAddress(serverAddr, 8675);
                socket = new Socket();
                socket.connect(address, 5000);
                socketreader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                socketWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
            }
            connected = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    readFromSocket();
                }
            } ).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFromSocket() {
        String s;
        while(connected) {
            try {
                s = socketreader.readLine();
                TvMessage tvMsg = null;
                if(s != null)
                {
                    tvMsg = gson.fromJson(s, TvMessage.class);
                    Message complete = mHandler.obtainMessage(0, tvMsg);
                    complete.sendToTarget();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                connected = false;
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        txtEdit = (EditText) findViewById(R.id.editText);
        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message inputMessage) {
                TvMessage tvMsg = (TvMessage)inputMessage.obj;
                if(tvMsg.Message.equals(TvProgramChange.MESSAGE)) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("TMS_ID: " + tvMsg.ProgramChangeInfo.Id);
                    sb.append("\n");
                    sb.append("Title: " + tvMsg.ProgramChangeInfo.Title);
                    sb.append("\n");
                    sb.append("SubTitle: " + tvMsg.ProgramChangeInfo.SubTitle);
                    sb.append("\n");
                    sb.append("Description: " + tvMsg.ProgramChangeInfo.Description);
                    sb.append("\n");
                    sb.append("Start: " + tvMsg.ProgramChangeInfo.StartTime);
                    sb.append("\n");
                    sb.append("End: " + tvMsg.ProgramChangeInfo.EndTime);
                    sb.append("\n");
                    txtEdit.setText(sb.toString());
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                createConnection("172.25.252.64");
            }
        } ).start();
    }

    @Override
    protected void onDestroy() {
        connected = false;
        if(socket != null) {
            try{
            socket.close();
            } catch (IOException ex) {

            } finally {
                socket = null;
            }
        }
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
