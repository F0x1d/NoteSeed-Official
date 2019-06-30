package com.f0x1d.notes.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.f0x1d.notes.R;
import com.f0x1d.notes.fragment.editing.NoteEdit;
import com.f0x1d.notes.fragment.settings.translations.TranslationsEditor;
import com.f0x1d.notes.fragment.settings.translations.TranslationsFragment;
import com.f0x1d.notes.utils.Logger;
import com.f0x1d.notes.utils.UselessUtils;
import com.f0x1d.notes.utils.dialogs.ShowAlertDialog;
import com.f0x1d.notes.utils.translations.IncorrectTranslationError;
import com.f0x1d.notes.utils.translations.Translation;
import com.f0x1d.notes.utils.translations.Translations;
import com.f0x1d.notes.view.theming.ItemCardView;
import com.f0x1d.notes.view.theming.MyTextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TranslationsAdapter extends RecyclerView.Adapter<TranslationsAdapter.TranslationViewHolder> {

    private List<Translation> translations;
    private TranslationsFragment fragment;

    public TranslationsAdapter(List<Translation> translations, TranslationsFragment fragment){
        this.translations = translations;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public TranslationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TranslationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_translation, parent, false));
        else
            return new TranslationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_translation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TranslationViewHolder holder, int position) {
        if (UselessUtils.ifCustomTheme()) {
            holder.cardView.setCardBackgroundColor(Color.BLACK);
        }

        holder.text.setupCompoundDrawables();

        if (position == 0){
            holder.text.setText(Translations.getString("add_translation"));
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] items = {Translations.getString("import_db"), Translations.getString("create")};

                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    fragment.openFile("*/*", 228, fragment.getContext());
                                    break;
                                case 1:
                                    UselessUtils.replace(TranslationsEditor.newInstance(null), "translations_edit");
                                    break;
                            }
                        }
                    });
                    ShowAlertDialog.show(builder.create());
                }
            });
        } else if (position == 1){
            Translation translation = translations.get(position);
            holder.text.setText(translation.name);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Translations.setDefaultTranslation();
                    fragment.restart();
                }
            });
        } else {
            Translation translation = translations.get(position);
            if (translation == null)
                return;

            holder.text.setText(translation.name);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (File file : Translations.getAvailableTranslations()){
                        if (file.getName().equals(translation.name)) {
                            try {
                                Translations.setCurrentTranslation(file);
                                fragment.restart();
                            } catch (IncorrectTranslationError incorrectTranslationError) {
                                Toast.makeText(fragment.getContext(), incorrectTranslationError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                Logger.log(incorrectTranslationError);
                            }
                        }
                    }
                }
            });
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                    String[] items = {Translations.getString("delete"), Translations.getString("change"), Translations.getString("export")};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    if (Translations.deleteTranslation(translation.file)){
                                        translations.remove(position);
                                        notifyDataSetChanged();
                                        fragment.restart();
                                    } else {
                                        translations.remove(position);
                                        notifyDataSetChanged();
                                    }
                                    break;
                                case 1:
                                    UselessUtils.replace(TranslationsEditor.newInstance(translation.file.getAbsolutePath()), "translations_edit");
                                    break;
                                case 2:
                                    File dst = new File(Environment.getExternalStorageDirectory() + "/Notes/translations");
                                    if (!dst.exists())
                                        dst.mkdirs();
                                    try {
                                        NoteEdit.copy(new FileInputStream(translation.file), new File(dst, translation.name));
                                        Toast.makeText(fragment.getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        Logger.log(e);
                                    }
                                    break;
                            }
                        }
                    });
                    ShowAlertDialog.show(builder.create());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return translations.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    public class TranslationViewHolder extends RecyclerView.ViewHolder {

        public ItemCardView cardView;
        public MyTextView text;

        public TranslationViewHolder(@NonNull View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}