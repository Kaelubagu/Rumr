package com.example.rumrapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        fetchMessagesAsync(1, new MessageCallback() {
            @Override
            public void onMessagesReceived(ArrayList<Message> messages) {
                // UI STUFF HERE, messages variable is ur info
            }
        });
    }
    private ArrayList<Message> getMessages(int roomId) {
        try {
            URL url = new URL("http://10.0.2.2:3000/getMessages/1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String messagesJson = response.toString();
            Log.d("API_RESPONSE", messagesJson);
            JSONArray jsonArray = new JSONArray(messagesJson);
            ArrayList<Message> messages = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Message msg = new Message();
                msg.senderId = obj.getInt("Sender_ID");
                msg.content = obj.getString("Message");
                messages.add(msg);
            }
            System.out.println("messages: " + messages.toString());
            return messages;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null; //mad with no return statement, null return probably bad
    }
    public interface MessageCallback {
        void onMessagesReceived(ArrayList<Message> messages);
    }
    private void fetchMessagesAsync(int roomId, MessageCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            ArrayList<Message> messages = getMessages(roomId);
            handler.post(() -> {
                callback.onMessagesReceived(messages);
            });
        });
    }


}