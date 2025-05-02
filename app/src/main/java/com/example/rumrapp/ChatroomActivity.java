package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatroomActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;

    private ImageButton backButton;
    private ArrayList<Message> messages;
    private ChatAdapter adapter;

    private int roomId;
    private String roomName;
    private int userId;

    private TextView chatroomChecker;

    private Handler fetchHandler;
    private Runnable fetchRunnable;
    private static final long FETCH_INTERVAL_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        editText     = findViewById(R.id.editTextMessage);
        sendButton   = findViewById(R.id.buttonSend);
        backButton = findViewById(R.id.backArrow);
        chatroomChecker  = findViewById(R.id.chatroomNamePlate);

        messages = new ArrayList<>();

        Intent intent = getIntent();
        userId   = intent.getIntExtra("userId", 9999);
        roomId   = intent.getIntExtra("roomId", 1);
        roomName = intent.getStringExtra("roomName");

        chatroomChecker.setText(String.valueOf(roomName));

        adapter  = new ChatAdapter(messages,userId);


        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        fetchHandler = new Handler(Looper.getMainLooper());
        fetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchMessagesAsync(roomId, new MessageCallback() {
                    @Override
                    public void onMessagesReceived(ArrayList<Message> serverMessages) {
                        runOnUiThread(() -> {
                            messages.clear();
                            messages.addAll(serverMessages);
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        });
                    }
                });
                fetchHandler.postDelayed(this, FETCH_INTERVAL_MS);
            }
        };
        fetchHandler.postDelayed(fetchRunnable, FETCH_INTERVAL_MS);


        backButton.setOnClickListener(v -> {
            finish(); // go back to previous activity
        });
        sendButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) return;
            editText.setText("");
            sendMessageAsync(roomId, userId, text);
            fetchMessagesAsync(roomId, new MessageCallback() {
                @Override
                public void onMessagesReceived(ArrayList<Message> serverMessages) {
                    runOnUiThread(() -> {
                        messages.clear();
                        messages.addAll(serverMessages);
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });
                }
            });
        });

        editText.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendButton.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchHandler.removeCallbacks(fetchRunnable);
    }

    private void sendMessageAsync(int roomId, int senderId, String message) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(getString(R.string.url_root) + "/sendMessage");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                String formData = "roomId=" + URLEncoder.encode(String.valueOf(roomId), "UTF-8") +
                        "&senderId=" + URLEncoder.encode(String.valueOf(senderId), "UTF-8") +
                        "&message="  + URLEncoder.encode(message, "UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = formData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                conn.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ArrayList<Message> getMessages(int roomId) {
        try {
            URL url = new URL(getString(R.string.url_root) + "/getMessages/" + roomId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            JSONArray jsonArray = new JSONArray(response.toString());
            ArrayList<Message> messages = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Message msg = new Message();
                msg.senderId = obj.getInt("Sender_ID");
                msg.content  = obj.getString("Message");
                messages.add(msg);
            }
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fetchMessagesAsync(int roomId, MessageCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            ArrayList<Message> result = getMessages(roomId);
            handler.post(() -> callback.onMessagesReceived(result));
        });
    }

    public interface MessageCallback {
        void onMessagesReceived(ArrayList<Message> messages);
    }


private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final ArrayList<Message> data;
    private final int userId;

    // View type constants
    private static final int VIEW_TYPE_SELF = 0;
    private static final int VIEW_TYPE_OTHER = 1;

    ChatAdapter(ArrayList<Message> d, int userId) {
        this.data = d;
        this.userId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        return (data.get(position).senderId == userId)
                ? VIEW_TYPE_SELF
                : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_TYPE_SELF)
                ? R.layout.item_message_self
                : R.layout.item_message_other;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Message msg = data.get(i);
        h.message.setText(msg.content);
        h.sender.setText("User " + msg.senderId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView message;
        TextView sender;

        VH(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.textViewMessage);
            sender = itemView.findViewById(R.id.textViewSender);
        }
    }
}

}
