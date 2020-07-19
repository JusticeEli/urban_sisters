package com.justice.a2urbansisters.admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.SetOptions;
import com.justice.a2urbansisters.AddEditStockActivity;
import com.justice.a2urbansisters.Constants;
import com.justice.a2urbansisters.CustomDialogAdapter;
import com.justice.a2urbansisters.login_register.LoginActivity;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.customer.StocksCustomerActivity;
import com.justice.a2urbansisters.modal.PersonalOrder;
import com.justice.a2urbansisters.modal.Stock;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.justice.a2urbansisters.Constants.PERSONAL_ORDERS;
import static com.justice.a2urbansisters.Constants.STOCKS;


public class OrdersAdminMainActivity extends AppCompatActivity implements OrdersAdminMainAdapter.ItemClicked, CustomDialogAdapter.ItemClicked {
    ////widgets
    private RecyclerView recyclerView;
    private Button addButton;
    private ProgressDialog progressDialog;
    /////////firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private OrdersAdminMainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_admin_main);
        initWidgets();
        setOnClickListeners();
        setUpRecyclerView();
        setUpSwipeListener();
        setTitle("Orders");
        checkIfUserIsLoggedIn_andIsCustomerOrAdmin();
    }

    private void setUpSwipeListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getReference().delete().addOnCompleteListener(null);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setOnClickListeners() {

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.documentSnapshot = null;
                Intent intent = new Intent(OrdersAdminMainActivity.this, AddEditStockActivity.class);
                startActivity(intent);

            }
        });

    }

    private void initWidgets() {
        addButton = findViewById(R.id.addBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login user in");
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);

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
        recyclerView.setVisibility(View.INVISIBLE);
        addButton.setVisibility(View.INVISIBLE);

        progressDialog.show();
        firebaseFirestore.collection(Constants.ADMIN_CUSTOMER).document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (!task.getResult().getBoolean("isAdmin")) {
                        Constants.isAdmin = false;

                        startActivity(new Intent(OrdersAdminMainActivity.this, StocksCustomerActivity.class));
                        finish();
                    }
                } else {
                    Toasty.error(OrdersAdminMainActivity.this, "Error: " + task.getException().getMessage()).show();
                }
                recyclerView.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
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
                }).setLifecycleOwner(OrdersAdminMainActivity.this).build();


        adapter = new OrdersAdminMainAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }


    @Override
    public void approveDelivery(DocumentSnapshot document, final boolean approved) {

        Map<String, Object> map = new HashMap<>();
        map.put("delivered", approved);
        document.getReference().set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (approved) {
                        //         Toasty.success(AppointMentActivity.this, "Appointment Approved successfully").show();

                    } else {
                        //      Toasty.success(OrdersMainActivity.this, "Delivered").show();

                    }

                } else {
                    Toasty.error(OrdersAdminMainActivity.this, "Error; " + task.getException().getMessage()).show();

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

        RecyclerView rv = dialogView.findViewById(R.id.rv);

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
                }).setLifecycleOwner(OrdersAdminMainActivity.this).build();

        final CustomDialogAdapter dialogAdapter = new CustomDialogAdapter(OrdersAdminMainActivity.this, firestoreRecyclerOptions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(dialogAdapter);

///////////////set up swipe listener ///// so that admin can delete the order by scrolling of the screen//////////

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deleteOrder(viewHolder.getAdapterPosition(), dialogAdapter);

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

    private void deleteOrder(int adapterPosition, CustomDialogAdapter dialogAdapter) {
        dialogAdapter.getSnapshots().getSnapshot(adapterPosition).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(OrdersAdminMainActivity.this, "Order Deleted").show();

                } else {
                    Toasty.success(OrdersAdminMainActivity.this, "Error: " + task.getException().getMessage()).show();

                }
            }
        });
    }


}