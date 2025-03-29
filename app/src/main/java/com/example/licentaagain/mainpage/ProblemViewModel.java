package com.example.licentaagain.mainpage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;

import java.util.List;

public class ProblemViewModel extends ViewModel {
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();


    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;}

    public void setProblems(List<Problem> problems) {
        problemsLiveData.setValue(problems);
    }
}