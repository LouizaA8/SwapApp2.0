package com.example.swapapp20;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.PostViewHolder> {
    private static final String TAG = "ProfilePostsAdapter";
    private final Context context;
    private final List<Post> postList;
    private PostDetailFragment.OnPostClickListener onItemClickListener;

    public ProfilePostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        Log.d(TAG, "Adapter created with " + (postList != null ? postList.size() : 0) + " posts");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (position < 0 || position >= postList.size()) {
            Log.e(TAG, "Invalid position: " + position + ", list size: " + postList.size());
            return;
        }

        Post post = postList.get(position);
        if (post == null) {
            Log.e(TAG, "Post at position " + position + " is null");
            return;
        }

        // Log post information
        Log.d(TAG, "Binding post at position " + position +
                " with ID: " + (post.getPostId() != null ? post.getPostId() : "null"));

        // Load the post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.postImage);
        } else {
            holder.postImage.setImageResource(R.drawable.profile);
        }

        // Set explicit click listener on the image and the itemView itself
        View.OnClickListener clickListener = v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Item clicked at position: " + adapterPosition);

                // Show a toast for immediate feedback
                Toast.makeText(context, "Clicked post " + adapterPosition, Toast.LENGTH_SHORT).show();

                if (onItemClickListener != null) {
                    Log.d(TAG, "Calling onPostClick listener");
                    onItemClickListener.onPostClick(adapterPosition);
                } else {
                    Log.e(TAG, "Click listener is null");
                    Toast.makeText(context, "Error: Click listener not set", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Invalid adapter position");
            }
        };

        // Apply the click listener to both the itemView and the image
        holder.itemView.setOnClickListener(clickListener);
        holder.postImage.setOnClickListener(clickListener);

        // Make sure the views are clickable
        holder.itemView.setClickable(true);
        holder.itemView.setFocusable(true);
        holder.postImage.setClickable(true);
        holder.postImage.setFocusable(true);
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public void setOnItemClickListener(PostDetailFragment.OnPostClickListener listener) {
        Log.d(TAG, "Setting click listener: " + (listener != null ? "NOT null" : "null"));
        this.onItemClickListener = listener;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.gridPostImage);
        }
    }
}