package com.justice.a2urbansisters.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.a2urbansisters.AddEditStockActivity;
import com.justice.a2urbansisters.Constants;
import com.justice.a2urbansisters.login_register.LoginActivity;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.Stock;

import es.dmoral.toasty.Toasty;

import static com.justice.a2urbansisters.Constants.ALL_STOCKS;

public class StocksAdminActivity extends AppCompatActivity implements StocksAdminAdapter.ItemClicked {
    private StocksAdminAdapter adapter;
    private RecyclerView recyclerView;
    private Button addButton;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_admin);
        setTitle("Stocks List");
        initwidgets();
        setOnClickListeners();
        setUpRecyclerView();
        setSwipeListenerForItems();

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
        }).setLifecycleOwner(StocksAdminActivity.this).build();


        adapter = new StocksAdminAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.orderItem:
                startActivity(new Intent(this, OrdersAdminMainActivity.class));
                break;
            case R.id.stockItem:
                startActivity(new Intent(this, StocksAdminActivity.class));
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


    private void setOnClickListeners() {

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.documentSnapshot = null;
                Intent intent = new Intent(StocksAdminActivity.this, AddEditStockActivity.class);
                startActivity(intent);

            }
        });

    }


    private void setSwipeListenerForItems() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deleteStock(adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()), viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);
    }

    private void initwidgets() {
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addBtn);

    }

    @Override
    public void deleteStock(final DocumentSnapshot documentSnapshot, final int position) {
        new MaterialAlertDialogBuilder(this).setBackground(getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.notifyItemChanged(position);
            }
        }).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStockConfirmed(documentSnapshot, position);
            }
        }).show();
    }

    private void deleteStockConfirmed(DocumentSnapshot documentSnapshot, final int position) {

        ///////delete photo///////

        FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("imageUrl")).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(StocksAdminActivity.this, "photo Deletion Success").show();
                } else {
                    Toasty.error(StocksAdminActivity.this, "Error :" + task.getException().getMessage()).show();

                }
            }
        });

        /////////////delete document//////////
        documentSnapshot.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(StocksAdminActivity.this, " Data Deletion Success").show();
                } else {
                    Toasty.error(StocksAdminActivity.this, "Error:  " + task.getException().getMessage()).show();

                }

            }
        });
    }

    @Override
    public void editStock(DocumentSnapshot documentSnapshot) {
        Constants.documentSnapshot = documentSnapshot;
        startActivity(new Intent(this, AddEditStockActivity.class));
    }
}