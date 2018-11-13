package com.example.brandon.quicktrip.holders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.models.GroceryList;
import com.example.brandon.quicktrip.models.Item;


public class ItemViewHolder extends RecyclerView.ViewHolder {

    TextView itemName;

    public ItemViewHolder(View v) {
        super(v);
        itemName = v.findViewById(R.id.itemName);
    }

    public void populateItem(Context context, String userEmail, GroceryList groceryList, Item item) {
        String groceryListID = groceryList.getListID();
        String userListName = groceryList.getListName();
        String userItemName = item.getItemName();
        Log.d("USERITEMNAME", userItemName);
        itemName.setText(userItemName);

//        itemView.setOnClickListener(new View.OnClickListener() {
//            //Move item to another section
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), GroceryListActivity.class);
//                intent.putExtra("GroceryList", groceryList);
//                v.getContext().startActivity(intent);
//            }
//        });

//        itemView.setOnLongClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Edit Shopping List Name");
//
//                EditText editText = new EditText(context);
//                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//                editText.setText(userListName);
//                editText.setSelection(editText.getText().length());
//                editText.setHint("Type a name");
//                editText.setHintTextColor(Color.GRAY);
//                builder.setView(editText);
//
//                FirebaseFirestore rootref = FirebaseFirestore.getInstance();
//                Map<String, Object> map = new HashMap<>();
//
//                builder.setPositiveButton("Update", (dialogInterface, i) -> {
//                    String newGroceryListName = editText.getText().toString().trim();
//                    map.put("listName", newGroceryListName);
//                    rootref.collection("grocerylists").document(userEmail).collection("userLists")
//                            .document(groceryListID).update(map);
//                });
//
//                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
//
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
//        });
    }
}
