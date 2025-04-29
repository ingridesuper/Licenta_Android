package com.example.licentaagain.account.signed_problems;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class SignaturesOfUserViewModel extends ViewModel {

    private MutableLiveData<List<Problem>> problemsLiveData = new MutableLiveData<>();
    private final ProblemRepository problemRepository;
    private ListenerRegistration signaturesListener;

    public SignaturesOfUserViewModel() {
        problemRepository = new ProblemRepository();
    }

    public ProblemRepository getProblemRepository() {
        return problemRepository;
    }

    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;
    }

    public void setProblems(List<Problem> problems) {
        this.problemsLiveData.setValue(problems);
    }

    // Start listening to signed problems in real-time
    public void startListening(String uid) {
        signaturesListener = problemRepository.listenToSignedProblemsOfUser(uid, problems -> {
            problemsLiveData.setValue(problems);
        });
    }

    // Stop the listener when it's no longer needed
    public void stopListening() {
        if (signaturesListener != null) {
            signaturesListener.remove();
            signaturesListener = null;
        }
    }

    // Unsign a problem - no need to refetch, listener will handle updates
    public void unSignProblem(Problem problem, String uid) {
        new ProblemSignatureRepository().removeSignature(problem.getId(), uid, result -> {
            if (result) {
                // No need to refetch. The listener will update the problemsLiveData automatically.
                Log.i("SignaturesOfUserViewModel", "Problem successfully unsigned.");
            } else {
                Log.e("SignaturesOfUserViewModel", "Failed to unsign problem.");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening(); // Stop the listener when the ViewModel is cleared
    }
}
