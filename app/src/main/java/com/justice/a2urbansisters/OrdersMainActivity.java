package com.justice.a2urbansisters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class OrdersMainActivity extends AppCompatActivity implements MainAdapter.ItemClicked, CustomDialogAdapter.ItemClicked {
    public static final String STOCKS = "stocks";
    public static final String PERSONAL_ORDERS = "personal_orders";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private MainAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpRecyclerView();
        setTitle("All appointments booked by clients");
        checkIfUserIsLoggedIn_andIsCustomerOrAdmin();
    }

    private void checkIfUserIsLoggedIn_andIsCustomerOrAdmin() {

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            checkIfIsCustomerOrAdmin();
        }


    }

    private void checkIfIsCustomerOrAdmin() {
        firebaseFirestore.collection(RegisterActivity.ADMIN_CUSTOMER).document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (!task.getResult().getBoolean("isAdmin")) {
                        startActivity(new Intent(OrdersMainActivity.this, StocksCustomerActivity.class));
                        finish();
                    }
                } else {
                    Toasty.error(OrdersMainActivity.this, "Error: " + task.getException().getMessage()).show();
                }
            }
        });


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
                startActivity(new Intent(this, OrdersMainActivity.class));
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
        Query query = firebaseFirestore.collection(STOCKS);
        FirestoreRecyclerOptions<PersonalOrder> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<PersonalOrder>().setQuery(query,
                new SnapshotParser<PersonalOrder>() {
                    @NonNull
                    @Override
                    public PersonalOrder parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        PersonalOrder personalOrder = snapshot.toObject(PersonalOrder.class);
                        return personalOrder;
                    }
                }).setLifecycleOwner(OrdersMainActivity.this).build();


        adapter = new MainAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }


    @Override
    public void approveAppointment(DocumentSnapshot document, final boolean approved) {

        Map<String, Object> map = new HashMap<>();
        map.put("delivered", approved);
        document.getReference().set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (approved) {
                        //         Toasty.success(AppointMentActivity.this, "Appointment Approved successfully").show();

                    } else {
                        Toasty.success(OrdersMainActivity.this, "Delivered").show();

                    }

                } else {
                    Toasty.error(OrdersMainActivity.this, "Error; " + task.getException().getMessage()).show();

                }
            }
        });

    }

    @Override
    public void itemClickedDocumentSnapshot(DocumentSnapshot document) {
        PersonalOrder personalOrder = document.toObject(PersonalOrder.class);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setBackground(getDrawable(R.drawable.recycler_view_dialog_bg));
        builder.setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = (View) inflater.inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Orders by " + personalOrder.getEmail());

        RecyclerView rv = (RecyclerView) dialogView.findViewById(R.id.rv);

        Query query = firebaseFirestore.collection(STOCKS).document(personalOrder.getId()).collection(PERSONAL_ORDERS);
        FirestoreRecyclerOptions<Stock> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Stock>().setQuery(query,
                new SnapshotParser<Stock>() {
                    @NonNull
                    @Override
                    public Stock parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        Stock stock = snapshot.toObject(Stock.class);
                        stock.setId(snapshot.getId());
                        return stock;
                    }
                }).setLifecycleOwner(OrdersMainActivity.this).build();

        final CustomDialogAdapter dialogAdapter = new CustomDialogAdapter(OrdersMainActivity.this, firestoreRecyclerOptions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(dialogAdapter);

///////////////set up swipe listener ///// so that supervisor can delete the appointment by scrolling of the screen//////////

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deleteAppointment(viewHolder.getAdapterPosition(), dialogAdapter);

            }
        }).attachToRecyclerView(rv);
////////////////////////////////
        AlertDialog dialog = builder.create();

        dialog.show();


///////////previous code///////
        /**
         *    MainActivity.documentSnapshot = document;
         *         startActivity(new Intent(this, MapsActivity.class));
         *         overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         */
    }

    private void deleteAppointment(int adapterPosition, CustomDialogAdapter dialogAdapter) {
        dialogAdapter.getSnapshots().getSnapshot(adapterPosition).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(OrdersMainActivity.this, "Appointment Deleted").show();

                } else {
                    Toasty.success(OrdersMainActivity.this, "Error: " + task.getException().getMessage()).show();

                }
            }
        });
    }


}