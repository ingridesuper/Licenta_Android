package com.example.licentaagain.view_models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.repositories.ProblemSignatureRepository;

import java.util.List;

public class ProblemBySelectedUserViewModel extends ViewModel implements ProblemRepository.ProblemFetchCallback {
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final ProblemRepository problemRepository;

    public ProblemBySelectedUserViewModel() {
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

    public void fetchAllProblemsByUser(String uid) {
        problemRepository.fetchAllProblemsByUser(uid, this);
    }


    @Override
    public void onFetchComplete(List<Problem> problems) {
        problemsLiveData.setValue(problems);
        Log.i("problemsByUser", String.valueOf(problems.size()));
    }


    private final MutableLiveData<Integer> signedProblemCountLiveData = new MutableLiveData<>();

    public LiveData<Integer> getSignedProblemCount() {
        return signedProblemCountLiveData;
    }

    public void fetchSignedProblemCountByUser(String userId) {
        problemRepository.numberOfProblemsReporteddByUser(userId, count -> {
            signedProblemCountLiveData.postValue(count);
            Log.i("signedCount", "Signed problems: " + count);
        });
    }


}
