package com.example.licentaagain.customarrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.licentaagain.R;
import com.example.licentaagain.models.Problem;

import java.util.List;

public class ProblemCardAdapter extends ArrayAdapter<Problem> {

    private Context context;
    private List<Problem> problemList;
    private int resourceId;
    private LayoutInflater layoutInflater;


    public ProblemCardAdapter(@NonNull Context context, int resource, @NonNull List<Problem> objects, LayoutInflater layoutInflater) {
        super(context, resource, objects);
        this.context=context;
        this.resourceId=resource;
        this.problemList=objects;
        this.layoutInflater = layoutInflater;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view=layoutInflater.inflate(resourceId, parent, false);
        Problem problem=problemList.get(position);

        TextView tvTitle=view.findViewById(R.id.tvTitle);
        tvTitle.setText(problem.getTitle());

        return view;
    }
}
