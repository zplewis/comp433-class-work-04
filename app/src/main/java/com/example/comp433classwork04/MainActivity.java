package com.example.comp433classwork04;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "classwork04";

    ArrayList<String> commentsFromGemini;

    String geminiResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        getCommentsFromGemini();



        ArrayList<CommentItem> data  = getAllDrawables();

        CommentListAdapter adapter = new CommentListAdapter(this, R.layout.list_item, data);

        // once you have the adapter, if you want to add items to the ArrayList, notify of the
        // change. Why is this required, or what is the benefit?
//        data.add(new CommentItem(R.drawable.dwayne_johnson, "Dwayne Johnson", "Some comment goes here!"));
//        adapter.notifyDataSetChanged();

        ListView lv = findViewById(R.id.mylist);
        lv.setAdapter(adapter);


//        ArrayList<CommentItem> data = new ArrayList<>();
//        data.add(new CommentItem(R.drawable.taylor_swift, "Taylor Swift", "Wow what a lovely photo!"));
//        data.add(new CommentItem(R.drawable.dwayne_johnson, "Dwayne Johnson", "Nice picture!"));
//        data.add(new CommentItem(R.drawable.emma_stone, "Emma Stone", "Awesome, great"));
//        CommentListAdapter adapter = new CommentListAdapter(this, R.layout.list_item, data);
//        ListView lv = findViewById(R.id.mylist);
//        lv.setAdapter(adapter);
    }

    public static String getTitleCase(String word) {

        // Split words and capitalize each one
        word = word.replace("_", " ");

        String[] words = word.split("\\s+");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String current = words[i];
            if (current.isEmpty()) { continue; }

            sb.append(Character.toUpperCase(current.charAt(0)));
            if (current.length() > 1) {
                sb.append(current.substring(1).toLowerCase());
            }

            if (i < words.length - 1) {
                sb.append(" ");
            }
        } // end of for

        return sb.toString();
    }

    public void getCommentsFromGemini() {

        try {

            String generatedComment = "default comment";

            String geminiPrompt = "Generate a 5-10 word comment  as if someone is commenting on their " +
                    "own nice photo.";

            String json = "{\"contents\": " +
                    "[{\"parts\": " +
                    "[{\"text\": " +
                    "\"" + geminiPrompt + "\"" +
                    "}]}]}";

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            Request r = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                    .addHeader("x-goog-api-key", BuildConfig.API_KEY)
                    .post(body)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();

            okHttpClient.newCall(r).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    if (response.body() != null) {
                        String newComment = response.body().string();
                        Log.v("classwork04", newComment);
                        geminiResponse = newComment;
                    }
                }
            });

        } catch (Exception e) {
            Log.v("classwork04", e.getMessage());
        }

        // THIS is synchronous versus how we were given it in class
//        try (Response response = okHttpClient.newCall(r).execute()) {
//
//            if (!response.isSuccessful()) {
//                throw new IOException("Could not get comment from Gemini.");
//            }
//
//            return response.body() != null ? response.body().string() : generatedComment;
//        } catch (IOException e) {
//            Log.e("classwork04", e.getMessage());
//        }

//        okHttpClient.newCall(r).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {}
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//
//                if (response.body() != null) {
//                    String newComment = response.body().string();
//                    Log.v("classwork04", newComment);
//                    generatedComment[0] = newComment;
//                }
//            }
//        });
//
//        return generatedComment[0];
    }


    /**
     *
     */
    public static ArrayList<CommentItem> getAllDrawables() {

        ArrayList<CommentItem> comments = new ArrayList<>();

        // get all fields (resource names) from R.drawable
        Field[] fields = R.drawable.class.getFields();

        for (Field field : fields) {
            try {
                String name = field.getName();

                // skip this drawable if the name starts with "ic_launcher" which was not
                // created by me
                if (name.startsWith("ic_launcher")) {
                    continue;
                }

                String properName = getTitleCase(name);

                // Get the resource Id so that you can get the drawable itself
                int resourceId = field.getInt(null);

                // Good to know but not needed for this code
                // Drawable drawable = ContextCompat.getDrawable(context, resourceId);

                comments.add(new CommentItem(resourceId, properName, "some comment"));

                Log.v(TAG, "added commentItem for '" + properName + "'.");

            } catch (Exception e) {
                Log.e(TAG, "Error getting drawables: " + field.getName(), e);
            }
        } // end of for

        return comments;
    }
}