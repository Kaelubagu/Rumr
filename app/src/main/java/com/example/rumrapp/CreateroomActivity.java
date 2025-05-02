package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateroomActivity extends AppCompatActivity {

    private Spinner spinnerRooms;
    private Button joinBtn, createBtn;
    private EditText newRoomEdit;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> roomsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createroom2);

        spinnerRooms = findViewById(R.id.spinnerRooms);
        joinBtn      = findViewById(R.id.buttonJoinRoom);
        createBtn    = findViewById(R.id.buttonCreateRoom);
        newRoomEdit  = findViewById(R.id.editTextNewRoom);

        // 1) set up spinner adapter
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roomsList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRooms.setAdapter(adapter);

        // 2) load rooms from server
        loadRooms();

        // 3) join selected room
        joinBtn.setOnClickListener(v -> {
            String selectedRoom = (String) spinnerRooms.getSelectedItem();
            Intent intent = new Intent(this, ChatroomActivity.class);
            intent.putExtra("roomName", selectedRoom);
            startActivity(intent);
        });

        // 4) create & refresh
        createBtn.setOnClickListener(v -> {
            String roomName = newRoomEdit.getText().toString().trim();
            if (!roomName.isEmpty()) {
                createRoomAsync(roomName);
                newRoomEdit.setText("");
                // small delay to let server register new room, then reload list
                new Handler(Looper.getMainLooper())
                        .postDelayed(this::loadRooms, 500);
            }
        });
    }

    /** Fetch current rooms and populate spinner */
    private void loadRooms() {
        fetchRoomsAsync(rooms -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                roomsList.clear();
                roomsList.addAll(rooms);
                adapter.notifyDataSetChanged();
            });
        });
    }

    /** Synchronous GET to /getRooms */
    private ArrayList<String> getRooms() {
        try {
            URL url = new URL(getString(R.string.url_root) + "/getRooms");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                resp.append(line);
            }
            in.close();

            JSONArray arr = new JSONArray(resp.toString());
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(obj.getString("Room_Name"));
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Async wrapper around getRooms() */
    private void fetchRoomsAsync(RoomCallback callback) {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        exe.execute(() -> {
            ArrayList<String> rooms = getRooms();
            callback.onRoomsReceived(rooms);
        });
    }

    /** Async POST to /createRoom */
    private void createRoomAsync(String roomName) {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        exe.execute(() -> {
            try {
                URL url = new URL(getString(R.string.url_root) + "/createRoom");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );
                conn.setDoOutput(true);

                String formData = "roomName=" +
                        URLEncoder.encode(roomName, "UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(formData.getBytes("utf-8"));
                }
                conn.getResponseCode(); // fire & forget

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Simple callback interface */
    public interface RoomCallback {
        void onRoomsReceived(ArrayList<String> rooms);
    }
}
