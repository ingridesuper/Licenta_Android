package com.example.licentaagain.account.reported_problems;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ProblemsByCurrentUserViewModel extends ViewModel {
    private final MutableLiveData<List<Problem>> problemsGatheringSignatures = new MutableLiveData<>();
    private final MutableLiveData<List<Problem>> problemsSent = new MutableLiveData<>();
    private final ProblemRepository problemRepository;

    private ListenerRegistration gatheringListener; //refereinte la ascultatori firestore activi
    private ListenerRegistration sentListener;

    public ProblemsByCurrentUserViewModel() {
        problemRepository = new ProblemRepository();
    }

    public LiveData<List<Problem>> getProblemsGatheringSignatures() {
        return problemsGatheringSignatures;
    }

    public LiveData<List<Problem>> getProblemsSent() {
        return problemsSent;
    }

    /**
     * Start listening to real-time changes in the user's problems.
     */
    public void startListening(String uid) {
        gatheringListener = problemRepository.listenToProblemsByUserGatheringSignatures(uid, problemsGatheringSignatures::postValue);
        sentListener = problemRepository.listenToProblemsByUserSent(uid, problemsSent::postValue);
    }

    /**
     * Stop listening when the ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (gatheringListener != null) {
            gatheringListener.remove();
            gatheringListener = null;
        }
        if (sentListener != null) {
            sentListener.remove();
            sentListener = null;
        }
    }

    public void stopListening() {
        if (gatheringListener != null) {
            gatheringListener.remove();
            gatheringListener = null;
        }
        if (sentListener != null) {
            sentListener.remove();
            sentListener = null;
        }
    }

    public void deleteProblem(Problem problem) {
        problemRepository.deleteProblem(problem, result -> {
            if (result) {
                Log.i("deleteProblem", "Success");
            } else {
                Log.e("deleteProblem", "Fail");
            }
        });
    }

    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem) {
        problemRepository.updateProblemWithoutPictureChange(problemId, newProblem, result -> {
            if (!result) {
                Log.e("updateProblemNoPic", "Failed to update without picture");
            }
        });
    }

    public void updateProblemWithPictureChange(Problem oldProblem, Problem newProblem, List<Uri> newLocalUris, List<String> existingRemoteUrls) {
        problemRepository.updateProblemWithPictureChange(oldProblem, newProblem, newLocalUris, existingRemoteUrls, result -> {
            if (!result) {
                Log.e("updateProblemWithPic", "ERROR");
            }
        });
    }
}
