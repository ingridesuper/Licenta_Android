package com.example.licentaagain.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;

import java.util.List;

public class SolvedProblemsByCurrentUserViewModel extends ViewModel implements ProblemRepository.ProblemFetchCallback {
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final ProblemRepository problemRepository;

    public SolvedProblemsByCurrentUserViewModel() {
        this.problemRepository = new ProblemRepository();
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

    public void fetchProblems(String uid) {
        problemRepository.getSolvedProblemsOfUser(uid, this);
    }

    @Override
    public void onFetchComplete(List<Problem> problems) {
        problemsLiveData.setValue(problems);
    }

    public void updateStareProblema(Problem problem, StareProblema newStare){
        problemRepository.updateStareProblemaWithCallback(problem.getId(), newStare, result->{
            if(result){
                this.fetchProblems(problem.getAuthorUid());
            }
        });
    }
}
