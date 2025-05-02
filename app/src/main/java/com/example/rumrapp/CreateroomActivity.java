package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.example.myapp.models.Room;
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
import android.util.Log;
public class CreateroomActivity extends AppCompatActivity {

    private Spinner spinnerRooms;
    private Button joinBtn, createBtn;
    private EditText newRoomEdit;
    private ArrayAdapter<Room> adapter;
    private ArrayList<Room> roomsList = new ArrayList<>();

    private int roomId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createroom2);
        Intent prevIntent = getIntent();
        userId = prevIntent.getIntExtra("userId", 9999);
        spinnerRooms = findViewById(R.id.spinnerRooms);
        joinBtn      = findViewById(R.id.buttonJoinRoom);
        createBtn    = findViewById(R.id.buttonCreateRoom);
        newRoomEdit  = findViewById(R.id.editTextNewRoom);

        // 1) set up spinner adapter
        spinnerRooms = findViewById(R.id.spinnerRooms); // replace with your actual spinner ID
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
            Room selectedRoom = (Room) spinnerRooms.getSelectedItem();
            String selectedRoomName = selectedRoom.getName();
            int selectedRoomId = Integer.parseInt(selectedRoom.getId()); // Assuming ID is a stringified int
            roomId = selectedRoomId;
            Log.d("selectedRoomId",String.valueOf(roomId));
            Intent intent = new Intent(this, ChatroomActivity.class);
            intent.putExtra("roomName", selectedRoomName);
            intent.putExtra("roomId", roomId);
            intent.putExtra("userId", userId);
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
    private ArrayList<Room> getRooms() {
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
            ArrayList<Room> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("Room_Name");
                String id = obj.getString("Room_ID");  // <-- Make sure this key matches the JSON
                list.add(new Room(id, name));
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
            ArrayList<Room> rooms = getRooms();
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

                String formData = "roomName=" + URLEncoder.encode(roomName, "UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(formData.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.d("CREATE_ROOM", "Response code: " + responseCode);

                // Show toast on main thread if creation succeeded (e.g., HTTP 200 or 201)
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(CreateroomActivity.this, "Room created!", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Optional: show error toast on failure
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(CreateroomActivity.this, "Failed to create room", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    /** Simple callback interface */
    public interface RoomCallback {
        void onRoomsReceived(ArrayList<Room> rooms);
    }
}
