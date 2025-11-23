package com.example.comp433classwork04;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "classwork04";

    ArrayList<String> commentsFromGemini;

    String geminiResponse = "";

    ArrayList<CommentItem> data;

    CommentListAdapter adapter;

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




//        ArrayList<CommentItem> data = new ArrayList<>();
//        data.add(new CommentItem(R.drawable.taylor_swift, "Taylor Swift", "Wow what a lovely photo!"));
//        data.add(new CommentItem(R.drawable.dwayne_johnson, "Dwayne Johnson", "Nice picture!"));
//        data.add(new CommentItem(R.drawable.emma_stone, "Emma Stone", "Awesome, great"));
//        CommentListAdapter adapter = new CommentListAdapter(this, R.layout.list_item, data);
//        ListView lv = findViewById(R.id.mylist);
//        lv.setAdapter(adapter);
    }

    /**
     * Converts a string to title case, meaning that the first letter of each word is capitalized.
     * @param word
     * @return
     */
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

    /**
     * Returns a JSONArray from a JSONObject if it exists.
     * @param obj
     * @param name
     * @return
     */
    public JSONArray getJSONArray(JSONObject obj, String name) {
        if (obj == null || name == null || name.isEmpty() || !obj.has(name)) {
            return null;
        }

        JSONArray array = obj.optJSONArray(name);

        if (array == null || array.length() == 0) {
            return null;
        }

        return obj.optJSONArray(name);
    }

    /**
     * Parse the three comments from the "text" property of the Gemini response.
     * @param response
     * @return
     */
    public String[] parseCommentsFromGemini(String response) {
        if (response == null || response.isEmpty()) {
            return new String[0];
        }

        // make sure that the response contains a list of comments.
        int startIndex = response.indexOf("1.");
        if (startIndex == -1) {
            return new String[0];
        }

        // remove the text before the start index
        String cleaned = response.substring(startIndex).trim();

        // Split the text on newlines to get the individual comments
        String[] lines = cleaned.split("\\r?\\n");

        List<String> comments = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) { continue; }

            // If it a line that starts with a number, like 1., 2., or 3., then keep it
            if (line.matches("^[0-9]+\\..*")) {
                // The substring is used here to omit the "1." from the comment.
                comments.add(line.substring(line.indexOf(".") + 1).trim());
            }

            if (comments.size() == 3) {
                return comments.toArray(new String[0]);
            }
        }

        return new String[0];
    }

    /**
     * Retrieve the "text" property of the response from Gemini.
     * @param json
     * @return
     */
    public String extractGeminiText(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }

        try {
            JSONObject root = new JSONObject(json);

            JSONArray candidates = getJSONArray(root, "candidates");

            if (candidates == null) {
                return "";
            }

            JSONObject firstCandidate = candidates.optJSONObject(0);

            if (firstCandidate == null) {
                return "";
            }

            // "content" must be an object
            JSONObject content = firstCandidate.optJSONObject("content");

            if (content == null) {
                return "";
            }

            JSONArray parts = getJSONArray(content, "parts");
            if (parts == null) {
                return "";
            }

            JSONObject firstPart = parts.optJSONObject(0);
            if (firstPart == null) {
                return "";
            }

            return firstPart.optString("text", "");

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON from Gemini response.");
            return "";
        }
    }

    /**
     * Reaches out to Gemini to retrieve 3 comments. These 3 comments are then added to
     * an arraylist of CommentItem objects which are then displayed on the screen in a ListView.
     */
    public void getCommentsFromGemini() {

        try {

            String generatedComment = "default comment";

            TextView textview = findViewById(R.id.loadingTextView);


            String geminiPrompt = "Generate a 3 random 5-10 word comments as if someone is commenting on their " +
                    "own nice photo. Return as a numbered list of comments with a newline character between each one.";

            String json = "{\"contents\": " +
                    "[{\"parts\": " +
                    "[{\"text\": " +
                    "\"" + geminiPrompt + "\"" +
                    "}]}]}";

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            // Go to https://aistudio.google.com/app/api-keys to get your API key
            // Add the API key to local.properties with the key name "api.key"
            // For the URL for this request, you can get it by clicking "Copy cURL quickstart" when
            // viewing the API key details within the Google AI Studio website.
            // The free tier was sufficient for this to work.
            // The cURL quickstart is good for confirming whether the API key works.
            Request r = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                    .addHeader("X-goog-api-key", BuildConfig.API_KEY)
                    .post(body)
                    .build();

            // This is just to make sure that the API key is being loaded correctly
//            Log.v("TAG", "API_KEY: " + BuildConfig.API_KEY);

            OkHttpClient okHttpClient = new OkHttpClient();

            Log.v(TAG, "sending request to Gemini for 3 comments...");

            okHttpClient.newCall(r).enqueue(new Callback() {

                /**
                 * It is really important to log the error here if one occurs so that you can
                 * better troubleshoot what is going wrong.
                 * @param call
                 * @param e
                 */
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.v(TAG, "Failure happened when attempting to retrieve comments from Gemini...");
                    textview.setText("Failed to retrieve comments from Gemini.");
                    Log.v(TAG, e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    // You cannot call .string() on response.body() more than one because it is a stream.
                    ResponseBody body = response.body();
                    String json = "";

                    String textViewResult = "A null response was returned from Gemini.";

                    if (body != null) {
                        json = body.string();
                        body.close();

                        textViewResult = "Comments have been retrieved from Gemini.";

                        Log.v(TAG, "Response from Gemini: " + json);

                        // This is how to set the margins on a view.
                        ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) textview.getLayoutParams();
                        params.setMargins(0,0,0,0);
                    }

                    Log.v("TAG", textViewResult);
                    textview.setText(textViewResult);

                    // Now, parse the result to get the actual comments
                    String[] comments = parseCommentsFromGemini(extractGeminiText(json));

                    data = getAllDrawables(comments);

                    // The next code actually touches views and will have to use runOnUiThread
                    runOnUiThread(() -> {
                        // This makes sure that the activity is still active before continuing
                        if (isFinishing() || isDestroyed()) {
                           return;
                        }

                        adapter = new CommentListAdapter(MainActivity.this, R.layout.list_item, data);

                        // once you have the adapter, if you want to add items to the ArrayList, notify of the
                        // change. Why is this required, or what is the benefit?
                        // data.add(new CommentItem(R.drawable.dwayne_johnson, "Dwayne Johnson", "Some comment goes here!"));
                        // adapter.notifyDataSetChanged();

                        ListView lv = findViewById(R.id.mylist);
                        lv.setAdapter(adapter);

                    });

                }
            }); // end of the callback

        } catch (Exception e) {
            Log.v("classwork04", e.getMessage());
        }
    }


    /**
     * Loop through the drawables that contains the pictures and add comments.
     */
    public static ArrayList<CommentItem> getAllDrawables(String[] geminiComments) {

        ArrayList<CommentItem> commentItems = new ArrayList<>();

        if (geminiComments == null || geminiComments.length == 0) {
            Log.e(TAG, "No comments were returned from Gemini.");
            return commentItems;
        }

        // get all fields (resource names) from R.drawable
        Field[] fields = R.drawable.class.getFields();

        int commentIndex = 0;
        int numGeminiComments = geminiComments.length;

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

                // Adds the comment generated by Gemini to the new CommentItem object.
                commentItems.add(new CommentItem(resourceId, properName, geminiComments[commentIndex]));

                Log.v(TAG, "added commentItem for '" + properName + "'.");

                commentIndex++;

                if (commentIndex >= numGeminiComments) {
                    break;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error getting drawables: " + field.getName(), e);
            }
        } // end of for

        return commentItems;
    }
}