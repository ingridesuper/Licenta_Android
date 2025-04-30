package com.example.licentaagain.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminProblemsViewModel extends ViewModel {
    private ProblemRepository problemRepository;
    private MutableLiveData<List<Problem>> problems = new MutableLiveData<>();
    private final MutableLiveData<ProblemFilterState> filterState = new MutableLiveData<>(new ProblemFilterState());

    private ListenerRegistration problemListener;

    public AdminProblemsViewModel() { this.problemRepository = new ProblemRepository(); }

    public LiveData<List<Problem>> getProblems(){ return problems; }

    public void startListening(){
        problemListener=problemRepository.listenToAllProblemsAdmin(problems::postValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(problemListener!=null){
            problemListener.remove();
            problemListener=null;
        }
    }

    public void stopListening(){
        if(problemListener!=null){
            problemListener.remove();
            problemListener=null;
        }
    }
}
