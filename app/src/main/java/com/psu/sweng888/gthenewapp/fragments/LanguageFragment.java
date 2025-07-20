package com.psu.sweng888.gthenewapp.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.psu.sweng888.gthenewapp.R;
import java.util.Locale;

public class LanguageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language, container, false);
        Button englishBtn = view.findViewById(R.id.language_english_btn);
        Button spanishBtn = view.findViewById(R.id.language_spanish_btn);
        englishBtn.setOnClickListener(v -> setLocale("en"));
        spanishBtn.setOnClickListener(v -> setLocale("es"));
        return view;
    }
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        getActivity().recreate();
    }
} 