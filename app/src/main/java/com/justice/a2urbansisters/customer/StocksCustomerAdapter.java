package com.justice.a2urbansisters.customer;


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
import com.justice.a2urbansisters.R;
import com.justice.a2urbansisters.modal.Stock;

import de.hdodenhof.circleimageview.CircleImageView;

public class StocksCustomerAdapter extends FirestoreRecyclerAdapter<Stock, StocksCustomerAdapter.ViewHolder> {

    private Context context;
    private ItemClicked itemClicked;

    public StocksCustomerAdapter(Context context, @NonNull FirestoreRecyclerOptions<Stock> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Stock model) {
        holder.nameTxtView.setText(model.getName());
        holder.priceTxtView.setText(model.getPrice() + "");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(R.mipmap.ic_launcher_round);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getImageUrl()).into(holder.imageView);


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_customer, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView imageView;
        private TextView nameTxtView, priceTxtView;


        public ViewHolder(@NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.imageView);
            nameTxtView = v.findViewById(R.id.nameTextView);
            priceTxtView = v.findViewById(R.id.priceTxtView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClicked.itemClicked(getItem(getAdapterPosition()));

        }
    }

    public interface ItemClicked {

        void itemClicked(Stock stock);
    }
}
