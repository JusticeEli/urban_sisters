package com.justice.a2urbansisters.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.justice.a2urbansisters.login_register.LoginActivity;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.Stock;

import es.dmoral.toasty.Toasty;

import static com.justice.a2urbansisters.Constants.PERSONAL_ORDERS;
import static com.justice.a2urbansisters.Constants.STOCKS;

public class OrdersCustomerActivity extends AppCompatActivity implements OrderCustomerAdapter.ItemClicked {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private OrderCustomerAdapter adapter;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_customer);
        setUpRecyclerView();
        setUpSwipeListener();
        setTitle("Orders...");
    }

    private void setUpSwipeListener() {

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deleteOrder(adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()), viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);
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
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.logoutItem:
                logoutUser();
                break;

        }
        return true;
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            setUpRecyclerView();

        }


    }


    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = firebaseFirestore.collection(STOCKS).document(firebaseAuth.getUid()).collection(PERSONAL_ORDERS);
        FirestoreRecyclerOptions<Stock> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Stock>().setQuery(query,
                new SnapshotParser<Stock>() {
                    @NonNull
                    @Override
                    public Stock parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        Stock stock = snapshot.toObject(Stock.class);
                        return stock;
                    }
                }).setLifecycleOwner(OrdersCustomerActivity.this).build();


        adapter = new OrderCustomerAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }


    @Override
    public void deleteOrder(DocumentSnapshot document, int position) {
        document.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(OrdersCustomerActivity.this, "deletion Success").show();

                } else {
                    Toasty.error(OrdersCustomerActivity.this, "Error: " + task.getException().getMessage()).show();
                }
            }
        });
    }
}