package mit.samaritans.em.utils;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import mit.samaritans.em.R;


public class BottomSheetFragment extends BottomSheetDialogFragment {
    private TextView textClose;
    private Toolbar toolbar;
    private LinearLayout linearLayout;
    private NavigationView navigationView;

    public BottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_bottom, container, false);
        textClose = view.findViewById(R.id.text_close);
        linearLayout = view.findViewById(R.id.linear_layout);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setFitsSystemWindows(true);


        navigationView = view.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav01:
                        Toast.makeText(getContext(),"Error",Toast.LENGTH_LONG).show();
                        item.setChecked(true);
                        break;
                }
                return false;
            }
        });

        if (toolbar.isScrollbarFadingEnabled()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        textClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
            }
        });

        return view;
    }

}

