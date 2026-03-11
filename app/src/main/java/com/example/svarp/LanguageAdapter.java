package com.example.svarp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    public static final String PREFS_NAME = "svarp_prefs";
    public static final String KEY_LANGUAGE = "selected_language";
    public static final String LANG_HINDI = "hindi";
    public static final String LANG_ENGLISH = "english";

    private final List<String> languages;
    private int selectedPosition = -1;

    public LanguageAdapter(List<String> languages) {
        this.languages = languages;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        String language = languages.get(position);
        holder.tvLanguage.setText(language);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.common_box2);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.common_box);
        }

        holder.imgFlag.setImageResource(R.drawable.india_flag);
        holder.imgFlag.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);

            Context context = holder.itemView.getContext();

            // Save language choice to SharedPreferences
            String langCode = getLangCode(language);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LANGUAGE, langCode).apply();

            Intent intent = new Intent(context, Main_Screen.class);
            context.startActivity(intent);

            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
    }

    // Maps display language name to a simple code
    private String getLangCode(String displayName) {
        if (displayName == null) return LANG_ENGLISH;
        if (displayName.trim().equals("हिंदी")
                || displayName.trim().equals("हिन्दी")
                || displayName.toLowerCase().trim().equals("hindi")) {
            return LANG_HINDI;
        }
        return LANG_ENGLISH;
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFlag, imgChevron;
        TextView tvLanguage;

        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFlag = itemView.findViewById(R.id.imgFlag);
            imgChevron = itemView.findViewById(R.id.imgChevron);
            tvLanguage = itemView.findViewById(R.id.tvLanguage);
        }
    }
}