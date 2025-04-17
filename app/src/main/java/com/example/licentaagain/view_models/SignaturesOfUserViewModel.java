package com.example.licentaagain.view_models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.repositories.ProblemSignatureRepository;

import java.util.ArrayList;
import java.util.List;

public class SignaturesOfUserViewModel extends ViewModel implements ProblemRepository.ProblemFetchCallback {

    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final ProblemRepository problemRepository;
    public SignaturesOfUserViewModel() {
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
    @Override
    public void onFetchComplete(List<Problem> problems) {
        problemsLiveData.setValue(problems);
        Log.i("problemsByUser", String.valueOf(problems.size()));
    }

    public void fetchSignedProblemsOfUser(String uid){
        problemRepository.fetchSignedProblemsOfUser(uid, this);
    }

    public void unSignProblem(Problem problem, String uid){
        new ProblemSignatureRepository().removeSignature(problem.getId(), uid, result -> {
            if (result) {
                this.problemsLiveData.getValue().remove(problem);
            }
        });
    }

}
