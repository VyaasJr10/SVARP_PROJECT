package com.example.svarp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.Set;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.ViewHolder> {

    private final String[] internalNames;   // English names used for engine mapping
    private final String[] displayNames;    // Names shown on screen (English or Hindi)
    private final int[] icons;
    private final OnSelectionChanged callback;
    private final Set<Integer> selectedPositions = new HashSet<>();

    // Constructor with separate internal and display names (for multilingual support)
    public SymptomAdapter(String[] internalNames,
                          String[] displayNames,
                          int[] icons,
                          OnSelectionChanged callback) {
        this.internalNames = internalNames;
        this.displayNames = displayNames;
        this.icons = icons;
        this.callback = callback;
    }

    // Original constructor (English only) — keeps backward compatibility
    public SymptomAdapter(String[] symptoms,
                          int[] icons,
                          OnSelectionChanged callback) {
        this.internalNames = symptoms;
        this.displayNames = symptoms;
        this.icons = icons;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtSymptom.setText(displayNames[position]); // show display name
        holder.imgSymptom.setImageResource(icons[position]);

        boolean isSelected = selectedPositions.contains(position);
        holder.bindSelection(isSelected);

        holder.card.setOnClickListener(v -> {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position);
            } else {
                selectedPositions.add(position);
            }
            callback.onChanged(selectedPositions.size());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return internalNames.length;
    }

    // Returns English internal names for engine mapping — always works regardless of display language
    public String[] getSelectedSymptoms() {
        String[] selected = new String[selectedPositions.size()];
        int i = 0;
        for (int pos : selectedPositions) {
            selected[i++] = internalNames[pos];
        }
        return selected;
    }

    public interface OnSelectionChanged {
        void onChanged(int selectedCount);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        ImageView imgSymptom;
        TextView txtSymptom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            imgSymptom = itemView.findViewById(R.id.imgSymptom);
            txtSymptom = itemView.findViewById(R.id.txtSymptom);
        }

        void bindSelection(boolean selected) {
            if (selected) {
                card.setStrokeColor(
                        ContextCompat.getColor(card.getContext(), R.color.blue_600)
                );
                card.setCardBackgroundColor(
                        ContextCompat.getColor(card.getContext(), R.color.blue_100)
                );
            } else {
                card.setStrokeColor(
                        ContextCompat.getColor(card.getContext(), R.color.gray_border)
                );
                card.setCardBackgroundColor(
                        ContextCompat.getColor(card.getContext(), R.color.white)
                );
            }
        }
    }
}