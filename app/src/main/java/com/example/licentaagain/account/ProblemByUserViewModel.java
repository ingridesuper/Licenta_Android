package com.example.licentaagain.account;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;

import java.util.List;
import java.util.function.Consumer;

public class ProblemByUserViewModel extends ViewModel implements ProblemRepository.ProblemFetchCallback {
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final ProblemRepository problemRepository;

    public ProblemByUserViewModel() {
        problemRepository = new ProblemRepository();
    }

    public ProblemRepository getProblemRepository() {
        return problemRepository;
    }

    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;
    }

    public void setProblems(List<Problem> problemsLiveData) {
        this.problemsLiveData.setValue(problemsLiveData);
    }

    public void fetchAllProblemsByUser(String uid) {
        problemRepository.fetchAllProblemsByUser(uid, this);
    }

    @Override
    public void onFetchComplete(List<Problem> problems) {
        problemsLiveData.setValue(problems);
        Log.i("problemsByUser", String.valueOf(problems.size()));
    }

    public void deleteProblem(Problem problem){
        problemRepository.deleteProblem(problem, result->{
            if(result){
                Log.i("deleteProblem", "Success");
            }
            else {
                Log.e("deleteProblem", "Fail");
            }
        });
    }
}
