package com.example.swapapp20;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {
    private static final String TAG = "MatchesFragment";

    private RecyclerView recyclerViewMatches;
    private TextView emptyMatchesText;
    private MatchAdapter matchAdapter;
    private List<Match> matchesList;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public MatchesFragment() {
        // Required empty public constructor
    }

    public static MatchesFragment newInstance() {
        return new MatchesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        recyclerViewMatches = view.findViewById(R.id.recyclerViewMatches);
        emptyMatchesText = view.findViewById(R.id.emptyMatchesText);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        matchesList = new ArrayList<>();
        matchAdapter = new MatchAdapter(requireContext(), matchesList);

        recyclerViewMatches.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMatches.setAdapter(matchAdapter);

        loadMatches();
    }

    private void loadMatches() {
        if (auth.getCurrentUser() == null) return;


        String currentUserId = auth.getCurrentUser().getUid();

        // Query for matches where the current user is either user1 or user2
        firestore.collection("matches")
                .whereEqualTo("isActive", true)
                .whereArrayContains("users", currentUserId)  // Assuming you have a users array field in your match documents
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading matches", error);
                        Toast.makeText(getContext(), "Error loading matches", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        emptyMatchesText = requireView().findViewById(R.id.emptyMatchesText);
                        matchesList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Match match = doc.toObject(Match.class);
                            if (match != null) {
                                match.setId(doc.getId());
                                matchesList.add(match);
                            }
                        }

                        if (matchesList.isEmpty()) {
                            emptyMatchesText.setVisibility(View.VISIBLE);
                            recyclerViewMatches.setVisibility(View.GONE);
                        } else {
                            emptyMatchesText.setVisibility(View.GONE);
                            recyclerViewMatches.setVisibility(View.VISIBLE);
                        }
                        matchAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + matchesList.size() + " matches");
                    }
                });
    }
}