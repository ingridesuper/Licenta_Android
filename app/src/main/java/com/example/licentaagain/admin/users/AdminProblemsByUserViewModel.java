package com.example.licentaagain.admin.users;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.AdminRepository;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminProblemsByUserViewModel extends ViewModel {
    private final MutableLiveData<List<Problem>> problems=new MutableLiveData<>();
    private final MutableLiveData<Integer> problemsCount=new MutableLiveData<>();
    private final ProblemRepository problemRepository;
    private ListenerRegistration problemsListener;
    private ListenerRegistration countListener;

    public AdminProblemsByUserViewModel() {
        this.problemRepository=new ProblemRepository();
    }

    public LiveData<List<Problem>> getProblems() { return problems; }

    public LiveData<Integer> getProblemsCount() { return problemsCount; }

    public void startListening(String uid){
        problemsListener=problemRepository.listenToAllProblemsByUser(uid, problems::postValue);
        countListener=problemRepository.listenToCountReportedProblemsByUser(uid, problemsCount::postValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(problemsListener!=null){
            problemsListener.remove();
            problemsListener=null;
        }
        if (countListener != null) {
            countListener.remove();
            countListener = null;
        }
    }

    public void stopListening() {
        if(problemsListener!=null){
            problemsListener.remove();
            problemsListener=null;
        }
        if (countListener != null) {
            countListener.remove();
            countListener = null;
        }
    }



}
