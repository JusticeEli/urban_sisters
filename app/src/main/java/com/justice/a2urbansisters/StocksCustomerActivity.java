package com.justice.a2urbansisters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import es.dmoral.toasty.Toasty;

public class StocksCustomerActivity extends AppCompatActivity implements StocksCustomerAdapter.ItemClicked {
    private StocksAdminAdapter adapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks);
        setTitle("Stocks List");
        initwidgets();
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        Query query = firebaseFirestore.collection(OrdersMainActivity.STOCKS);
        FirestoreRecyclerOptions<Stock> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Stock>().setQuery(query, new SnapshotParser<Stock>() {
            @NonNull
            @Override
            public Stock parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Stock stock = snapshot.toObject(Stock.class);
                stock.setId(snapshot.getId());
                return stock;
            }
        }).setLifecycleOwner(StocksCustomerActivity.this).build();


        adapter = new StocksAdminAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.orderItem:
                startActivity(new Intent(this, OrdersCustomerActivity.class));
                break;
            case R.id.stockItem:
                startActivity(new Intent(this, StocksCustomerActivity.class));
                break;
            case R.id.logoutItem:
                logoutUser();
                break;

        }
        return true;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

    }


    private void initwidgets() {
        recyclerView = findViewById(R.id.recyclerView);
    }


    @Override
    public void itemClicked(Stock stock) {

        firebaseFirestore.collection(OrdersMainActivity.STOCKS).document(FirebaseAuth.getInstance().getUid()).collection(OrdersMainActivity.PERSONAL_ORDERS).add(stock).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toasty.success(StocksCustomerActivity.this, "Item Added successfully").show();
                } else {
                    Toasty.error(StocksCustomerActivity.this, "Error: " + task.getException().getMessage()).show();
                }
            }
        });

    }
}