package com.example.rumrapp;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class ChatroomActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private List<String> messages;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        editText     = findViewById(R.id.editTextMessage);
        sendButton   = findViewById(R.id.buttonSend);

        messages = new ArrayList<>();
        adapter  = new ChatAdapter(messages);

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
}
