package com.example.licentaagain.user_page;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ProblemBySelectedUserViewModel extends ViewModel {
    private MutableLiveData<List<Problem>> problemsGatheringSignatures=new MutableLiveData<>();
    private MutableLiveData<List<Problem>> problemsSent=new MutableLiveData<>();
    private MutableLiveData<List<Problem>> problemsSolved=new MutableLiveData<>();

    private MutableLiveData<Integer> reportedProblemsCount=new MutableLiveData<>();
    private final ProblemRepository problemRepository;

    private ListenerRegistration gatheringListener;
    private ListenerRegistration sentListener;
    private ListenerRegistration solvedListener;
    private ListenerRegistration countListener;

    public ProblemBySelectedUserViewModel() {
        this.problemRepository = new ProblemRepository();
    }

    public LiveData<List<Problem>> getProblemsGatheringSignatures() { return problemsGatheringSignatures; }

    public LiveData<List<Problem>> getProblemsSent() {
        return problemsSent;
    }

    public LiveData<List<Problem>> getProblemsSolved() {
        return problemsSolved;
    }

    public LiveData<Integer> getReportedProblemsCount(){return reportedProblemsCount;}

    public void startListening(String uid) {
        gatheringListener = problemRepository.listenToProblemsByUserGatheringSignatures(uid, problemsGatheringSignatures::postValue);
        sentListener = problemRepository.listenToProblemsByUserSent(uid, problemsSent::postValue);
        solvedListener= problemRepository.listenToSolvedProblemsOfUser(uid, problemsSolved::postValue);
        countListener=problemRepository.listenToCountReportedProblemsByUser(uid, reportedProblemsCount::postValue);
    }

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
        if (solvedListener != null) {
            solvedListener.remove();
            solvedListener = null;
        }
        if (countListener != null) {
            countListener.remove();
            countListener = null;
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
        if (solvedListener != null) {
            solvedListener.remove();
            solvedListener = null;
        }
        if (countListener != null) {
            countListener.remove();
            countListener = null;
        }
    }


}
