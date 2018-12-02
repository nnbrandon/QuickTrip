package com.example.brandon.quicktrip.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.holders.ItemViewHolder;
import com.example.brandon.quicktrip.models.GroceryList;
import com.example.brandon.quicktrip.models.Item;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickAddFragment extends ListFragment {

    private String groceryListID;
    private FirebaseFirestore rootref;
    private CollectionReference groceryListItemRef;
    private boolean show;
    private String userEmail;
    private String userName;
    private GoogleApiClient googleApiClient;
    private FirestoreRecyclerAdapter<Item, ItemViewHolder> firestoreRecyclerAdapter;

    String[] items = {"Milk", "Eggs", "Rice", "Cereal", "Bread", "Steak", "Juice", "Banana", "Broccoli", "Sausage", "Bacon", "Salmon", "Cheese",};
    int[] images = {
            R.drawable.milk,
            R.drawable.eggs,
            R.drawable.rice,
            R.drawable.cereal,
            R.drawable.bread,
            R.drawable.steak,
            R.drawable.juice,
            R.drawable.banana,
            R.drawable.broccoli,
            R.drawable.sausage,
            R.drawable.bacon,
            R.drawable.salmon,
            R.drawable.cheese};

    ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Check if user is authenticated
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
        }

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        GroceryList groceryList = ((GroceryListActivity) getActivity()).getGroceryList();
        groceryListID = groceryList.getListID();

        rootref = FirebaseFirestore.getInstance();
        groceryListItemRef = rootref.collection("items").document(groceryListID).collection("groceryListItems");

        Query query = groceryListItemRef.whereEqualTo("show", show).orderBy("itemName", Query.Direction.ASCENDING);


        HashMap<String, String> map = new HashMap<String, String>();

        //FILL
        for(int i=0; i<items.length; i++)
        {
            map = new HashMap<String, String>();
            map.put("Item", items[i]);
            map.put("Image", Integer.toString(images[i]));

            data.add(map);
        }

        //KEYS IN MAP
        String[] from = {"Item", "Image"};

        //IDS OF VIEWS
        int[] to = {R.id.itemTxt, R.id.itemImage};

        //ADAPTER
        adapter = new SimpleAdapter(getActivity(), data, R.layout.model, from, to);
        setListAdapter(adapter);


        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        //firestoreRecyclerAdapter.startListening();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addGroceryItem(data.get(position).get("Item"));
                Toast.makeText(getActivity(), "Added " + data.get(position).get("Item"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void addGroceryItem(String itemName) {
        String itemID = groceryListItemRef.document().getId();
        Item item = new Item(itemID, itemName, true, userName);
        Log.d("NEWITEM", "check it out " + item.getCreatedBy());
        groceryListItemRef.document(itemID).set(item);
    }



}
