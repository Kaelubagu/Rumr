package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatroomActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private List<String> messages;
    private ChatAdapter adapter;

    private int roomId;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        editText     = findViewById(R.id.editTextMessage);
        sendButton   = findViewById(R.id.buttonSend);

        messages = new ArrayList<>();
        adapter  = new ChatAdapter(messages);
        //getting user and room info
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 9999);
        roomId = intent.getIntExtra("roomId", 1);


        // stacks from the bottom
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);

        recyclerView.setAdapter(adapter);


        //make a field list with all the chatrooms inside of it.
        //when you select one of the chatroom fields the data inside of recyclerViewMessages


        // send button
        sendButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) return;
            messages.add(text);
            adapter.notifyItemInserted(messages.size() - 1);

            recyclerView.scrollToPosition(messages.size() - 1);
            editText.setText("");                // clear input
            Log.d("SEND_MESSAGE", "roomId: " + roomId + " senderId: " + userId + " text: " + text);
            sendMessageAsync(roomId, userId, text);
        });

        // (Optionally handle IME “Send” on keyboard)
        editText.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendButton.performClick();
                return true;
            }
            return false;
        });
    }

    // simple adapter for a list of strings (chatgpt)
    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        private final List<String> data;
        ChatAdapter(List<String> d) { data = d; }
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int i) {
            h.message.setText(data.get(i));
        }
        @Override
        public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView message;
            VH(View itemView) {
                super(itemView);
                message = itemView.findViewById(R.id.textViewMessage);
            }
        }
    }
    private void sendMessageAsync(int roomId, int senderId, String message) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:3000/sendMessage");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                // Form-encoded string
                String formData = "roomId=" + URLEncoder.encode(String.valueOf(roomId), "UTF-8") +
                        "&senderId=" + URLEncoder.encode(String.valueOf(senderId), "UTF-8") +
                        "&message=" + URLEncoder.encode(message, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = formData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                // Optional: force the connection to complete
                int responseCode = conn.getResponseCode();
                Log.d("SEND_MESSAGE", "Response code: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
