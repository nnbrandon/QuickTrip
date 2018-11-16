package com.example.brandon.quicktrip.holders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.models.GroceryList;
import com.example.brandon.quicktrip.models.Item;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class ItemViewHolder extends RecyclerView.ViewHolder {

    TextView itemName;
    TextView createdBy;

    public ItemViewHolder(View v) {
        super(v);
        itemName = v.findViewById(R.id.itemName);
        createdBy = v.findViewById(R.id.createdBy);
    }

    public void populateItem(Context context, View groceryListView, String userEmail, GroceryList groceryList, Item item) {
        String groceryListID = groceryList.getListID();
        String groceryListName = groceryList.getListName();
        String itemID = item.getItemID();
        String userItemName = item.getItemName();
        String userCreatedBy = item.getCreatedBy();
        Boolean show = item.getShow();

        itemName.setText(userItemName);
        createdBy.setText(userCreatedBy);

        FirebaseFirestore rootref = FirebaseFirestore.getInstance();
        DocumentReference itemIDRef = rootref.collection("items").document(groceryListID)
                .collection("groceryListItems").document(itemID);

        itemView.setOnClickListener(view-> {
            //Move item to History section
            Map<String, Object> map = new HashMap<>();

            if(show) {
                map.put("show", false);
            }
            else {
                map.put("show", true);
            }

            itemIDRef.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(show) {
                        //Send Notification
                    }
                }
            });

        });

        itemView.setOnLongClickListener(view-> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit Item");

            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setText(userItemName);
            editText.setSelection(editText.getText().length());
            editText.setHint("Type a name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            builder.setPositiveButton("Update", (dialogInterface, i) -> {
                String newItemName = editText.getText().toString().trim();
                Map<String, Object> map = new HashMap<>();
                map.put("itemName", newItemName);
                itemIDRef.update(map);
            });

            builder.setNegativeButton("Delete", (dialogInterface, i) -> {
                itemIDRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(groceryListView, "Item deleted!", Snackbar.LENGTH_LONG).show();
                    }
                });
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;

        });
    }
}
