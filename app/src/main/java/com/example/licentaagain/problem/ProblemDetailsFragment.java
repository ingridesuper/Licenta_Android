package com.example.licentaagain.problem;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.UserRepository;

import java.util.List;


public class ProblemDetailsFragment extends Fragment {
    private Problem problem;


    public ProblemDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_problem_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }

        fillUiWithProblemData(view);
    }

    private void fillUiWithProblemData(@NonNull View view) {
        TextView tvHelloProblem= view.findViewById(R.id.tvProblemTitle);
        TextView tvProblemAuthor=view.findViewById(R.id.tvProblemAuthor);
        TextView tvProblemDescription=view.findViewById(R.id.tvProblemDescription);
        TextView tvProblemCategory=view.findViewById(R.id.tvProblemCategory);
        TextView tvProblemAddressSector=view.findViewById(R.id.tvProblemAddressSector);
        RecyclerView recyclerViewPictures=view.findViewById(R.id.recyclerViewPictures);

        tvHelloProblem.setText(problem.getTitle());
        new UserRepository().getUserNameSurnameBasedOnId(problem.getAuthorUid(), fullName->{
            tvProblemAuthor.setText("Reported by: "+fullName);
        });
        tvProblemDescription.setText(problem.getDescription());
        tvProblemCategory.setText(problem.getCategorieProblema());
        tvProblemAddressSector.setText(problem.getAddress()+", Sectorul "+problem.getSector());

        List<String> problemImageUrls=problem.getImageUrls();
        ImageAdapterProblemDetails adapter=new ImageAdapterProblemDetails(getContext(), problemImageUrls);
        recyclerViewPictures.setAdapter(adapter);
    }
}