package com.example.brandon.quicktrip.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.brandon.quicktrip.R;
import com.example.brandon.quicktrip.fragments.GroceryListFragment;
import com.example.brandon.quicktrip.fragments.QuickAddFragment;
import com.example.brandon.quicktrip.models.GroceryList;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryListActivity extends AppCompatActivity {

    private String userEmail;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GroceryList groceryList;
    private String groceryListID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent intent = new Intent(GroceryListActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groceryList = (GroceryList) getIntent().getSerializableExtra("GroceryList");
        String groceryListName = groceryList.getListName();
        groceryListID = groceryList.getListID();
        setTitle(groceryListName);

        ViewPager viewPager = findViewById(R.id.view_pager);
        populateViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
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

    private void shareGroceryList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroceryListActivity.this);
        builder.setTitle("Share Grocery List!");
        builder.setMessage("Please insert your friend's email!");

        EditText editText = new EditText(GroceryListActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setHint("Enter email");
        editText.setHintTextColor(Color.GRAY);
        builder.setView(editText);

        builder.setPositiveButton("Add", (dialogInterface, i) -> {
            String friendEmail = editText.getText().toString().trim();
            rootRef.collection("grocerylists").document(friendEmail)
                    .collection("userLists").document(groceryListID)
                    .set(groceryList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Map<String, Object> users = new HashMap<>();
                    Map<String, Object> map = new HashMap<>();
                    map.put(userEmail, true);
                    map.put(friendEmail, true);
                    users.put("users", map);
                    rootRef.collection("grocerylists").document(userEmail)
                            .collection("userLists").document(groceryListID)
                            .update(users);
                    rootRef.collection("grocerylists").document(friendEmail)
                            .collection("userLists").document(groceryListID)
                            .update(users);
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grocery_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                this.finish();
                return true;

            case R.id.add_friend:
                shareGroceryList();
                return true;

            case R.id.sign_out_button:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> titleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @NonNull
        @Override
        public CharSequence getPageTitle(int i) {
            return titleList.get(i);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titleList.add(title);
        }
    }

    public GroceryList getGroceryList() {
        return groceryList;
    }

    private void populateViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        //Show items to users
        GroceryListFragment showGroceryListFragment = new GroceryListFragment();
        Bundle showBundle = new Bundle();
        showBundle.putBoolean("show", true);
        showGroceryListFragment.setArguments(showBundle);
        viewPagerAdapter.addFragment(showGroceryListFragment, "Grocery List");

        //Do not show items to users
        GroceryListFragment noShowGroceryListFragment = new GroceryListFragment();
        Bundle noShowBundle = new Bundle();
        noShowBundle.putBoolean("show", false);
        noShowGroceryListFragment.setArguments(noShowBundle);
        viewPagerAdapter.addFragment(noShowGroceryListFragment, "Checked Off");

        //Quick Add feature that we will implement
//        QuickAddFragment showQuickAddFragment = new QuickAddFragment();
//        Bundle showBundle2 = new Bundle();
//        showBundle.putBoolean("show", false);
//        showQuickAddFragment.setArguments(showBundle2);
        viewPagerAdapter.addFragment(new QuickAddFragment(), "Quick Add");

        viewPager.setAdapter(viewPagerAdapter);
    }
}
