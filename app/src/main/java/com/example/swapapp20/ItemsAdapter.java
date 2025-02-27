package com.example.swapapp20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private List<ClothingItem> items;
    private final OnItemLikeListener likeListener;

    public interface OnItemLikeListener {
        void onItemLike(ClothingItem item);
    }

    public ItemsAdapter(List<ClothingItem> items, OnItemLikeListener likeListener) {
        this.items = items;
        this.likeListener = likeListener;
    }

    public void updateItems(List<ClothingItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clothing, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClothingItem item = items.get(position);

        // Load image if URL exists
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.likes)
                    .into(holder.itemPhoto);
        }

        // Set description
        holder.itemDescription.setText(item.getDescription());

        // Set like button state
        holder.likeButton.setImageResource(item.isLiked() ? R.drawable.likes : R.drawable.likes);

        // Handle like button click
        holder.likeButton.setOnClickListener(v -> {
            if (!item.isLiked()) {
                item.setLiked(true);
                holder.likeButton.setImageResource(R.drawable.likes);
                likeListener.onItemLike(item);
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemPhoto;
        TextView itemDescription;
        ImageButton likeButton;

        ViewHolder(View view) {
            super(view);
            itemPhoto = view.findViewById(R.id.itemPhoto);
            itemDescription = view.findViewById(R.id.description);
            likeButton = view.findViewById(R.id.likeButton);
        }
    }
}