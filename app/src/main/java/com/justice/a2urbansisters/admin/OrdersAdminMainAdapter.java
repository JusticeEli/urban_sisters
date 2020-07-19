package com.justice.a2urbansisters.admin;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.justice.a2urbansisters.Constants;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.PersonalOrder;

import es.dmoral.toasty.Toasty;

public class OrdersAdminMainAdapter extends FirestoreRecyclerAdapter<PersonalOrder, OrdersAdminMainAdapter.ViewHolder> {

    private Context context;
    private ItemClicked itemClicked;


    public OrdersAdminMainAdapter(Context context, @NonNull FirestoreRecyclerOptions<PersonalOrder> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final PersonalOrder model) {
        holder.emailTxtView.setText(model.getEmail());

        ///////getting number of appointments for each user//////////
        getSnapshots().getSnapshot(position).getReference().collection(Constants.PERSONAL_ORDERS).addSnapshotListener((AppCompatActivity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toasty.error(context, "Error: " + e.getMessage()).show();
                    return;
                }
                holder.numberOfAppointmentsTextView.setText(queryDocumentSnapshots.size() + "");
                if (queryDocumentSnapshots.size() == 0) {
                    getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(null);
                }

            }
        });


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_admin_main, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView emailTxtView, numberOfAppointmentsTextView;

        public ViewHolder(@NonNull View v) {
            super(v);
            emailTxtView = v.findViewById(R.id.emailTxtView);
            numberOfAppointmentsTextView = v.findViewById(R.id.numberOfAppointmentTxtView);

            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            itemClicked.itemClickedDocumentSnapshot(getSnapshots().getSnapshot(getAdapterPosition()));
        }

    }

    public interface ItemClicked {
        void itemClickedDocumentSnapshot(DocumentSnapshot document);


    }
}
