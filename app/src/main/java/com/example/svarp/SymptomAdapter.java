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

    private final String[] symptoms;
    private final int[] icons;
    private final OnSelectionChanged callback;
    private final Set<Integer> selectedPositions = new HashSet<>();

    public SymptomAdapter(String[] symptoms,
                          int[] icons,
                          OnSelectionChanged callback) {
        this.symptoms = symptoms;
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

        holder.txtSymptom.setText(symptoms[position]);
        holder.imgSymptom.setImageResource(icons[position]);

        boolean isSelected = selectedPositions.contains(position);
        holder.bindSelection(isSelected);

        holder.card.setOnClickListener(v -> {

            if (isSelected) {
                selectedPositions.remove(position); // deselect
            } else {
                selectedPositions.add(position);    // select
            }

            callback.onChanged(selectedPositions.size());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return symptoms.length;
    }

    public String[] getSelectedSymptoms() {
        String[] selected = new String[selectedPositions.size()];
        int i = 0;
        for (int pos : selectedPositions) {
            selected[i++] = symptoms[pos];
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
