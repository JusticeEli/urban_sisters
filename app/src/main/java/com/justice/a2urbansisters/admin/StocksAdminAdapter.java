package com.justice.a2urbansisters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.Stock;

import de.hdodenhof.circleimageview.CircleImageView;

public class StocksAdminAdapter extends FirestoreRecyclerAdapter<Stock, StocksAdminAdapter.ViewHolder> {

    private Context context;
    private ItemClicked itemClicked;


    public StocksAdminAdapter(Context context, @NonNull FirestoreRecyclerOptions<Stock> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Stock model) {
        holder.nameTxtView.setText(model.getName());
        holder.priceTxtView.setText(model.getPrice() + "");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.ic_launcher_round);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getImageUrl()).into(holder.imageView);

        setOnClickListeners(holder, position);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_admin, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    private void setOnClickListeners(ViewHolder holder, final int position) {
        holder.deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClicked.deleteStock(getSnapshots().getSnapshot(position), position);
            }
        });

        holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClicked.editStock(getSnapshots().getSnapshot(position));
            }
        });


    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageView;
        private TextView nameTxtView, priceTxtView, deleteTxtView, editTxtView;


        public ViewHolder(@NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.imageView);
            nameTxtView = v.findViewById(R.id.nameTextView);
            priceTxtView = v.findViewById(R.id.priceTxtView);
            deleteTxtView = v.findViewById(R.id.deleteTxtView);
            editTxtView = v.findViewById(R.id.editTxtView);
        }
    }

    public interface ItemClicked {
        void deleteStock(DocumentSnapshot documentSnapshot, int position);

        void editStock(DocumentSnapshot documentSnapshot);

    }
}
