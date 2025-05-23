package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private TextView welcomeTxt, userIdTxt, boxTxt;
    private ImageView logoImg;
    private Button enterBtn;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        welcomeTxt = findViewById(R.id.textView2);
        userIdTxt   = findViewById(R.id.textView3);
        logoImg     = findViewById(R.id.imageView);
        boxTxt      = findViewById(R.id.textView);
        enterBtn    = findViewById(R.id.button);

        enterBtn.setOnClickListener(v -> animateOut());
        int roomId = 2; //this will be changed based on what room user is in


        createUserAsync(new UserCallback() {
            @Override
            public void onUserCreate(int userIdTmp) {
                //do whatever you want with user id
                Log.d("USER_ID", String.valueOf(userIdTmp));
                userIdTxt.setText(String.valueOf(userIdTmp));
                userId = userIdTmp;
            }
        });


    }
    // animation funnnn!
    private void animateOut() {
        // staggered fade & slide upward
        long duration = 300;
        welcomeTxt.animate()
                .alpha(0f)
                .translationYBy(-50)
                .setDuration(duration)
                .start();

        userIdTxt.animate()
                .alpha(0f)
                .translationYBy(-50)
                .setStartDelay(50)
                .setDuration(duration)
                .start();

        logoImg.animate()
                .alpha(0f)
                .scaleX(0.5f).scaleY(0.5f)
                .setStartDelay(100)
                .setDuration(duration)
                .start();

        boxTxt.animate()
                .alpha(0f)
                .translationYBy(100)
                .setStartDelay(150)
                .setDuration(duration)
                .withEndAction(() -> {

                    welcomeTxt.setVisibility(View.GONE);
                    userIdTxt.setVisibility(View.GONE);
                    logoImg.setVisibility(View.GONE);
                    boxTxt.setVisibility(View.GONE);

                    Intent go = new Intent(MainActivity.this, CreateroomActivity.class);
                    go.putExtra("userId",userId);
                    startActivity(go);
                    finish();
                })
                .start();

        enterBtn.animate()
                .alpha(0f)
                .setStartDelay(200)
                .setDuration(duration)
                .withEndAction(() -> enterBtn.setVisibility(View.GONE))
                .start();
    }

    private ArrayList<String> getRooms(){
        try{
            URL url = new URL(getString(R.string.url_root) +"/getRooms");
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
            String roomsJson = response.toString();
            Log.d("API_RESPONSE", roomsJson);
            JSONArray jsonArray = new JSONArray(roomsJson);
            ArrayList<String> rooms = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String room = obj.getString("Room_Name");
                rooms.add(room);
            }
            return rooms;


        }catch (Exception e){
            e.printStackTrace();
        }
        return null;   //mad with no return statement, null return probably bad
    }
    private int createUser(){
        try{
            Log.d("URL", getString(R.string.url_root) + "/createUser");
            URL url = new URL(getString(R.string.url_root) + "/createUser");
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
            String userJson = response.toString();
            Log.d("API_RESPONSE", userJson);

            JSONObject user = new JSONObject(userJson);
            int userId = user.getInt("UserId");
            return userId;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public interface RoomCallback{
        void onRoomsReceived(ArrayList<String> rooms);
    }

    public interface UserCallback {
        void onUserCreate(int userId);
    }




    private void createUserAsync(UserCallback callback){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            int userId = createUser();
            handler.post(() -> {
                callback.onUserCreate(userId);
            });
        });

    }

}