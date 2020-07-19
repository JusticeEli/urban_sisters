package com.justice.a2urbansisters.customer;

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
import com.justice.a2urbansisters.login_register.LoginActivity;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.PersonalOrder;
import com.justice.a2urbansisters.modal.Stock;

import es.dmoral.toasty.Toasty;

import static com.justice.a2urbansisters.Constants.ALL_STOCKS;
import static com.justice.a2urbansisters.Constants.PERSONAL_ORDERS;
import static com.justice.a2urbansisters.Constants.STOCKS;

public class StocksCustomerActivity extends AppCompatActivity implements StocksCustomerAdapter.ItemClicked {
    private StocksCustomerAdapter adapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_customer);
        setTitle("Stocks List");
        initwidgets();
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        Query query = firebaseFirestore.collection(ALL_STOCKS);
        FirestoreRecyclerOptions<Stock> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Stock>().setQuery(query, new SnapshotParser<Stock>() {
            @NonNull
            @Override
            public Stock parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Stock stock = snapshot.toObject(Stock.class);
                stock.setId(snapshot.getId());
                return stock;
            }
        }).setLifecycleOwner(StocksCustomerActivity.this).build();


        adapter = new StocksCustomerAdapter(this, firestoreRecyclerOptions);
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
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
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
        PersonalOrder personalOrder = new PersonalOrder();
        personalOrder.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        personalOrder.setId(FirebaseAuth.getInstance().getUid());

        firebaseFirestore.collection(STOCKS).document(FirebaseAuth.getInstance().getUid()).set(personalOrder).addOnCompleteListener(null);

        firebaseFirestore.collection(STOCKS).document(FirebaseAuth.getInstance().getUid()).collection(PERSONAL_ORDERS).add(stock).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
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