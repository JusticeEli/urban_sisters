package com.justice.a2urbansisters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class OrderCustomerAdapter extends FirestoreRecyclerAdapter<Stock, OrderCustomerAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;


    public OrderCustomerAdapter(Context context, @NonNull FirestoreRecyclerOptions<Stock> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Stock model) {
        holder.nameTxtView.setText(model.getName());
        holder.priceTxtView.setText(model.getPrice() + "");
        holder.deliveredCheckBox.setChecked(model.isDelivered());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(R.mipmap.ic_launcher_round);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getImageUrl());

        if (model.isDelivered()) {
            holder.deliveredCheckBox.setText("is delivered");
        } else {
            holder.deliveredCheckBox.setText("not yet delivered");

        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView imageView;
        private TextView nameTxtView, priceTxtView, deleteTxtView;
        private CheckBox deliveredCheckBox;

        public ViewHolder(@NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.imageView);
            nameTxtView = v.findViewById(R.id.nameTextView);
            priceTxtView = v.findViewById(R.id.priceTxtView);
            deleteTxtView = v.findViewById(R.id.deleteTxtView);
            deliveredCheckBox = v.findViewById(R.id.deliveredCheckBox);
            deleteTxtView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClicked.deleteOrder(getSnapshots().getSnapshot(getAdapterPosition()), getAdapterPosition());
        }

    }

    public interface ItemClicked {
        void deleteOrder(DocumentSnapshot document, int position);


    }
}
