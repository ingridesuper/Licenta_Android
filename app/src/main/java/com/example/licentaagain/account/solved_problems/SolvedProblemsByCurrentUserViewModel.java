package com.example.licentaagain.account.solved_problems;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class SolvedProblemsByCurrentUserViewModel extends ViewModel {
    private final MutableLiveData<List<Problem>> problemsLiveData = new MutableLiveData<>();
    private final ProblemRepository problemRepository;
    private ListenerRegistration solvedProblemsListener;

    public SolvedProblemsByCurrentUserViewModel() {
        this.problemRepository = new ProblemRepository();
    }

    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;
    }

    public void startListening(String uid) {
        stopListening();
        solvedProblemsListener = problemRepository.listenToSolvedProblemsOfUser(uid, problemsLiveData::postValue);
    }

    public void stopListening() {
        if (solvedProblemsListener != null) {
            solvedProblemsListener.remove();
            solvedProblemsListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }

    public void updateStareProblema(Problem problem, StareProblema newStare) {
        problemRepository.updateStareProblemaWithCallback(problem.getId(), newStare, result -> {
            if (!result) {
                Log.e("SolvedViewModel", "Failed to update problem status");
            }
        });
    }
}
