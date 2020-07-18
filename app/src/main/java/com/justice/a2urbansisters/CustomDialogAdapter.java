package com.justice.a2urbansisters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class CustomDialogAdapter extends FirestoreRecyclerAdapter<Stock, CustomDialogAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;


    public CustomDialogAdapter(Context context, @NonNull FirestoreRecyclerOptions<Stock> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Stock model) {

        Glide.with(context).load(model.getImageUrl()).into(holder.imageView);

        holder.nameTxtView.setText(model.getName());
        holder.priceTxtView.setText(model.getPrice()+"");
        holder.deliveredCheckBox.setChecked(model.isDelivered());
        if (model.isDelivered()) {
            holder.deliveredCheckBox.setText("delivered");

        } else {
            holder.deliveredCheckBox.setText("not delivered");

        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_original, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private TextView nameTxtView, priceTxtView;
        private CheckBox deliveredCheckBox;
        private ImageView imageView;

        public ViewHolder(@NonNull View v) {
            super(v);
            nameTxtView = v.findViewById(R.id.nameTxtView);
            priceTxtView = v.findViewById(R.id.priceTxtView);
            deliveredCheckBox = v.findViewById(R.id.deliveredCheckBox);
            imageView = v.findViewById(R.id.imageView);
             deliveredCheckBox.setOnCheckedChangeListener(this);
        }



        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            itemClicked.approveDelivery(getSnapshots().getSnapshot(getAdapterPosition()), isChecked);
            if (isChecked) {
                deliveredCheckBox.setText("delivered");

            } else {
                deliveredCheckBox.setText("not delivered");

            }
        }


    }

    public interface ItemClicked {

        void approveDelivery(DocumentSnapshot document, boolean approved);

    }
}
