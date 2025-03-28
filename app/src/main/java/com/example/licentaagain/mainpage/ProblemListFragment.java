package com.example.licentaagain.mainpage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licentaagain.R;
import com.example.licentaagain.customarrayadapter.ProblemCardAdapter;
import com.example.licentaagain.models.Problem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProblemListFragment extends Fragment {
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<Problem> problemList=new ArrayList<>();


    public ProblemListFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

    }

    private void fetchAllProblems(View view) {
        db.collection("problems").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("fetching", "success");
                            for (QueryDocumentSnapshot problem : task.getResult()) {
                                problemList.add(new Problem(problem.getString("address"), problem.getString("authorUid"), problem.getString("description"), problem.getDouble("latitude"), problem.getDouble("longitude"), problem.getDouble("sector").intValue(), problem.getString("title"), problem.getString("categorieProblema")));
                            }
                            Log.i("problems", problemList.toString());
                            updateUi(view);
                        } else {
                            Log.w("fetching", "Error fetching problems.", task.getException());
                        }
                    }
                });
    }

    private void updateUi(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvProblems);

        ProblemCardAdapter adapter = new ProblemCardAdapter(problemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_problem_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchAllProblems(view);

    }
}