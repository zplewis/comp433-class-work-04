import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.comp433classwork04.R;

import java.util.ArrayList;

public class CommentListAdapter extends ArrayAdapter<CommentItem> {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects
     */
    public CommentListAdapter(@NonNull Context context, int resource, ArrayList<CommentItem> objects) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        CommentItem currentItem = getItem(position);
        ImageView cImage = convertView.findViewById(R.id.photo);
        TextView cName = convertView.findViewById(R.id.name);
        TextView cComment = convertView.findViewById(R.id.comment);
        cImage.setImageResource(currentItem.photo);
        cName.setText(currentItem.name);
        cComment.setText(currentItem.comment);
        return convertView;
    }
}

