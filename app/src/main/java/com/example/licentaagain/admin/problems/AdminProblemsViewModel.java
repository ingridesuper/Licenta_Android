package com.example.licentaagain.admin.problems;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.AdminRepository;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminProblemsViewModel extends ViewModel {
    private AdminRepository adminRepository;
    private MutableLiveData<List<Problem>> problems = new MutableLiveData<>();
    private final MutableLiveData<ProblemFilterState> filterState = new MutableLiveData<>(new ProblemFilterState());

    public LiveData<ProblemFilterState> getFilterState() {
        return filterState;
    }

    public void updateFilterState(ProblemFilterState state) {
        filterState.setValue(state);
        //applyFilter();
    }

    private ListenerRegistration problemListener;

    public AdminProblemsViewModel() { this.adminRepository = new AdminRepository(); }

    public LiveData<List<Problem>> getProblems(){ return problems; }

    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem){
        this.adminRepository.updateProblemWithoutPictureChange(problemId, newProblem, result -> {
            if(!result){
                Log.e("update error", "error");
            }
        });
    }

    public void startListening(){
        problemListener=adminRepository.listenToAllProblems(problems::postValue);
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
