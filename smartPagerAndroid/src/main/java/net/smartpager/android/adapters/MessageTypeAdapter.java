package net.smartpager.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.model2.MessageType;

import java.util.ArrayList;


/**
 * Created by haythemsouissi on 15/08/15.
 */
public class MessageTypeAdapter extends ArrayAdapter<MessageType> {

    public MessageTypeAdapter(Context context, ArrayList<MessageType> items) {
        super(context, R.layout.item_message_type, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MessageType item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message_type, parent, false);
        }

        TextView message_type_name = (TextView) convertView.findViewById(R.id.message_type_name);
        message_type_name.setText(item.getName());

        return convertView;
    }
}
