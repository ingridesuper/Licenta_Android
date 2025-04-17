package com.example.licentaagain.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.ProblemSignature;

import java.util.List;

public class ProblemSignatureViewModel extends ViewModel {
    private MutableLiveData<List<ProblemSignature>> problemSignaturesLiveData=new MutableLiveData<>();

    public LiveData<List<ProblemSignature>> getProblemSignatures() {
        return problemSignaturesLiveData;
    }

    public void setProblemSignatures(List<ProblemSignature> problemSignatures) {
        problemSignaturesLiveData.setValue(problemSignatures);
    }
}
