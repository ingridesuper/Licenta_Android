package com.example.licentaagain.admin.problems;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.AdminRepository;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminProblemsViewModel extends ViewModel {
    private AdminRepository adminRepository;
    private final MutableLiveData<List<Problem>> problems = new MutableLiveData<>();
    private final MutableLiveData<List<Problem>> allProblems = new MutableLiveData<>();
    private final MutableLiveData<ProblemFilterState> filterState = new MutableLiveData<>(new ProblemFilterState());

    private ListenerRegistration problemListener;

    public AdminProblemsViewModel() {
        this.adminRepository = new AdminRepository();
    }

    public LiveData<List<Problem>> getProblems() {
        return problems;
    }

    public LiveData<ProblemFilterState> getFilterState() {
        return filterState;
    }

    public void updateFilterState(ProblemFilterState state) {
        filterState.setValue(state);
    }

    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem) {
        this.adminRepository.updateProblemWithoutPictureChange(problemId, newProblem, result -> {
            if (!result) {
                Log.e("update error", "error");
            }
        });
    }

    public void startListening() {
        try {
            problemListener = adminRepository.listenToAllProblems(problemList -> {
                allProblems.setValue(problemList);
                problems.setValue(problemList); // Initially show all problems
            });
        } catch (Exception e) {
            Log.i("error try catch", "error try catch "+e.getMessage());
        }
    }

    public void stopListening() {
        if (problemListener != null) {
            problemListener.remove();
            problemListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }

    public void filterProblems(String query) {
        List<Problem> all = allProblems.getValue();
        if (all == null) return;

        if (query == null || query.trim().isEmpty()) {
            problems.setValue(all); // Reset filter
            return;
        }

        List<Problem> filtered = new ArrayList<>();
        for (Problem problem : all) {
            if (problem.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    problem.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(problem);
            }
        }
        problems.setValue(filtered);
    }
}
