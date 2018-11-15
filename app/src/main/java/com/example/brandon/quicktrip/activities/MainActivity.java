package com.example.brandon.quicktrip.activities;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.holders.GroceryListViewHolder;
import com.example.brandon.quicktrip.models.GroceryList;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private String userEmail, userName;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef;
    private FirebaseAuth.AuthStateListener authStateListener;
    private CollectionReference userShoppingListsRef;
    private FirestoreRecyclerAdapter<GroceryList, GroceryListViewHolder> firestoreRecyclerAdapter;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Grocery Lists");

        context = this;

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
            Toast.makeText(this, "Welcome " + userName, Toast.LENGTH_LONG).show();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Create Shopping List");

            EditText editText = new EditText(MainActivity.this);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setHint("Type a name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            builder.setPositiveButton("Create", (dialogInterface, i) -> {
                String shoppingListName = editText.getText().toString().trim();
                addShoppingList(shoppingListName);
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        userShoppingListsRef = rootRef.collection("grocerylists").document(userEmail).collection("userLists");

        RecyclerView recyclerView = findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView emptyView = findViewById(R.id.empty);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        Query query = userShoppingListsRef.orderBy("listName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<GroceryList> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<GroceryList>()
                .setQuery(query, GroceryList.class)
                .build();

        firestoreRecyclerAdapter =
                new FirestoreRecyclerAdapter<GroceryList, GroceryListViewHolder>(firestoreRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull GroceryListViewHolder holder, int position, @NonNull GroceryList model) {
                        holder.populateGroceryList(context, userEmail, model);
                    }

                    @Override
                    public GroceryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_grocery_lists, parent, false);
                        return new GroceryListViewHolder(view);
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
    }

    private void signOut() {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenId", FieldValue.delete());

        rootRef.collection("users").document(userEmail).update(map).addOnSuccessListener(aVoid -> {
            firebaseAuth.signOut();

            if (googleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(googleApiClient);
            }
        });
    }

    private void addShoppingList(String shoppingListName) {
        String shoppingListId = userShoppingListsRef.document().getId();
        GroceryList shoppingListModel = new GroceryList(shoppingListId, shoppingListName, userName);
        userShoppingListsRef.document(shoppingListId).set(shoppingListModel).addOnSuccessListener(aVoid -> Log.d("TAG", "Shopping List successfully created!"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        firebaseAuth.addAuthStateListener(authStateListener);
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_button:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
