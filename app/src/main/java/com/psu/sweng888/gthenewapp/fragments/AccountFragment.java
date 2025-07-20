package com.psu.sweng888.gthenewapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.psu.sweng888.gthenewapp.R;

public class AccountFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        ImageView icon = view.findViewById(R.id.account_icon);
        TextView emailText = view.findViewById(R.id.account_email);
        TextView uidText = view.findViewById(R.id.account_uid);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailText.setText("Email: " + user.getEmail());
            uidText.setText("UID: " + user.getUid());
        } else {
            emailText.setText("Not signed in");
            uidText.setText("");
        }
        icon.setImageResource(R.drawable.ic_account);
        return view;
    }
} 