package com.example.brandon.quicktrip.holders;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.models.GroceryList;
import com.example.brandon.quicktrip.models.Item;
import com.example.brandon.quicktrip.models.NotificationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;


public class ItemViewHolder extends RecyclerView.ViewHolder {

    TextView itemName;
    TextView createdBy;

    public ItemViewHolder(View v) {
        super(v);
        itemName = v.findViewById(R.id.itemName);
        createdBy = v.findViewById(R.id.createdBy);
    }

    public void populateItem(Context context, View groceryListView, String userEmail, String userName, GroceryList groceryList, Item item) {
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
            Map<String, Object> map = new HashMap<>();

            if(show) {
                map.put("show", false);
//                Toast.makeText(context, "Checked off", Toast.LENGTH_SHORT).show();
                Toast toast = new Toast(context);
                ImageView newview = new ImageView(context);
                newview.setImageResource(R.drawable.checkmark);
                toast.setView(newview);
                toast.show();
            }
            else {
                map.put("show", true);
                Toast.makeText(context, "Added back to Grocery List", Toast.LENGTH_SHORT).show();
            }

            itemIDRef.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(show) {
                        rootref.collection("grocerylists").document(userEmail)
                                .collection("userLists").document(groceryListID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Map<String, Object> map = (Map<String, Object>) task.getResult().get("users");
                                String notificationMessage = userName + " has removed " + userItemName + " from " + groceryListName + "'s list.";
                                NotificationModel notificationModel = new NotificationModel(notificationMessage, userEmail);

                                if (map != null) {
                                    for(Map.Entry<String, Object> entry : map.entrySet()){
                                        String sharedUserEmail = entry.getKey();

                                        if(!sharedUserEmail.equals(userEmail)){
                                            rootref.collection("notifications").document(sharedUserEmail)
                                                    .collection("userNotifications").document()
                                                    .set(notificationModel);
                                        }
                                    }
                                }
                            }
                        });
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
                Toast.makeText(context, "Item has been updated!", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Delete", (dialogInterface, i) -> {
                itemIDRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(groceryListView, "Item deleted!", Snackbar.LENGTH_LONG).show();
//                        Toast.makeText(context, "Item deleted!", Toast.LENGTH_SHORT).show();
                        Toast toast = new Toast(context);
                        ImageView newview = new ImageView(context);
                        newview.setImageResource(R.drawable.xmark);
                        toast.setView(newview);
                        toast.show();
                    }
                });
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;

        });
    }
}
