package com.example.brandon.quicktrip.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.models.GroceryList;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GroceryListViewHolder extends RecyclerView.ViewHolder {

    TextView listName;
    TextView createdBy;
    TextView date;

    public GroceryListViewHolder(View v) {
        super(v);
        listName = v.findViewById(R.id.listName);
        createdBy = v.findViewById(R.id.createdBy);
        date = v.findViewById(R.id.date);
    }

    public void populateGroceryList(Context context, String userEmail, GroceryList groceryList) {
        String groceryListID = groceryList.getListID();
        String userListName = groceryList.getListName();
        String userCreatedBy = groceryList.getCreatedBy();
        Date userDate = groceryList.getDate();

        Log.d("populate", userListName);
        Log.d("populate", userCreatedBy);
        Log.d("populateGroceryList", "This is being called");
        listName.setText(userListName);
        createdBy.setText(userCreatedBy);
        if(userDate != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
            String userListCreationDate = dateFormat.format(userDate);
            date.setText(userListCreationDate);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GroceryListActivity.class);
                intent.putExtra("GroceryList", groceryList);
                v.getContext().startActivity(intent);
            }
        });

        itemView.setOnLongClickListener(view-> {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Shopping List Name");

                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setText(userListName);
                editText.setSelection(editText.getText().length());
                editText.setHint("Type a name");
                editText.setHintTextColor(Color.GRAY);
                builder.setView(editText);

                FirebaseFirestore rootref = FirebaseFirestore.getInstance();
                Map<String, Object> map = new HashMap<>();

                builder.setPositiveButton("Update", (dialogInterface, i) -> {
                    String newGroceryListName = editText.getText().toString().trim();
                    map.put("listName", newGroceryListName);
                    rootref.collection("grocerylists").document(userEmail).collection("userLists")
                            .document(groceryListID).update(map);
                    Toast.makeText(context, "Edited Grocery List Name", Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            });
    }


}
