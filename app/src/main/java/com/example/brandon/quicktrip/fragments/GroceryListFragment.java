package com.example.brandon.quicktrip.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.activities.GroceryListActivity;
import com.example.brandon.quicktrip.holders.GroceryListViewHolder;
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

public class GroceryListFragment extends Fragment {

    private String groceryListID;
    private FirebaseFirestore rootref;
    private CollectionReference groceryListItemRef;
    private boolean show;
    private String userEmail;
    private String userName;
    private GoogleApiClient googleApiClient;
    private FirestoreRecyclerAdapter<Item, ItemViewHolder> firestoreRecyclerAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View groceryListView = inflater.inflate(R.layout.fragment_grocery_list, container, false);


        Bundle bundle = getArguments();
        show = bundle.getBoolean("show");

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

        FloatingActionButton fab = groceryListView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add Grocery Item");

            EditText editText = new EditText(getContext());
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setHint("Type a name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            builder.setPositiveButton("Create", (dialogInterface, i) -> {
                String itemName = editText.getText().toString().trim();
                addGroceryItem(itemName);
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        rootref = FirebaseFirestore.getInstance();
        groceryListItemRef = rootref.collection("items").document(groceryListID).collection("groceryListItems");

        RecyclerView recyclerView = groceryListView.findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView emptyView = groceryListView.findViewById(R.id.empty);
        ProgressBar progressBar = groceryListView.findViewById(R.id.progressBar);

        Query query = groceryListItemRef.whereEqualTo("show", show).orderBy("itemName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Item> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        firestoreRecyclerAdapter =
                new FirestoreRecyclerAdapter<Item, ItemViewHolder>(firestoreRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull Item model) {
                        holder.populateItem(getContext(), groceryListView, userEmail, userName, groceryList, model);
                    }

                    @Override
                    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery, parent, false);
                        return new ItemViewHolder(view);
                    }

                    @Override
                    public void onDataChanged() {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (getItemCount() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }
                };
        recyclerView.setAdapter(firestoreRecyclerAdapter);

        return groceryListView;
    }

    private void addGroceryItem(String itemName) {
        String itemID = groceryListItemRef.document().getId();
        Item item = new Item(itemID, itemName, true, userName);
        Log.d("NEWITEM", "check it out " + item.getCreatedBy());
        groceryListItemRef.document(itemID).set(item);
        Toast.makeText(getContext(), "Added " + itemName + " to your list", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
    }
}
