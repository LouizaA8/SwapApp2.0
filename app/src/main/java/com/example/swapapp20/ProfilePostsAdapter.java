package com.example.swapapp20;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder> {
    private static final String TAG = "ProfilePostsAdapter";
    private final Context context;
    private final List<Post> posts;

    public ProfilePostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);

        // Hide all elements except the post image
        TextView userName = view.findViewById(R.id.userName);
        TextView userLocation = view.findViewById(R.id.userLocation);
        Button swapButton = view.findViewById(R.id.swapButton);
        CircleImageView userProfileImage = view.findViewById(R.id.userProfileImage);
        TextView caption = view.findViewById(R.id.caption);

        if (userName != null) userName.setVisibility(View.GONE);
        if (userLocation != null) userLocation.setVisibility(View.GONE);
        if (swapButton != null) swapButton.setVisibility(View.GONE);
        if (userProfileImage != null) userProfileImage.setVisibility(View.GONE);
        if (caption != null) caption.setVisibility(View.GONE);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        // Load only the post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getImageUrl())
                    .into(holder.postImage);
        } else {
            holder.postImage.setImageResource(R.drawable.profile);
        }

        // Set click listener to view the full post as in HomeFragment
        holder.itemView.setOnClickListener(v -> openPostDetail(post));
    }

    // Method to open post detail view
    private void openPostDetail(Post post) {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;

            if (post == null || post.getPostId() == null) {
                Log.e(TAG, "Cannot open post detail: post or postId is null");
                return;
            }

            Log.d(TAG, "Opening post detail for post ID: " + post.getPostId());

            // Create a bundle to pass the post ID
            Bundle bundle = new Bundle();
            bundle.putString("postId", post.getPostId());

            // Create post detail fragment
            PostDetailFragment postDetailFragment = new PostDetailFragment();
            postDetailFragment.setArguments(bundle);

            // Replace current fragment with post detail fragment
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, postDetailFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e(TAG, "Context is not a FragmentActivity");
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }
}