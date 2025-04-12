package com.example.licentaagain.mainpage;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ProblemViewModel extends ViewModel implements ProblemRepository.ProblemFetchCallback{
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final MutableLiveData<ProblemFilterState> filterState = new MutableLiveData<>(new ProblemFilterState());

    private final ProblemRepository problemRepository;

    public ProblemRepository getProblemRepository() {
        return problemRepository;
    }

    public ProblemViewModel() {
        problemRepository = new ProblemRepository();
    }

    //getters and setters
    public LiveData<ProblemFilterState> getFilterState() {
        return filterState;
    }

    public void updateFilterState(ProblemFilterState state) {
        filterState.setValue(state);
        applyFilter();
    }

    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;
    }

    public void setProblems(List<Problem> problems) {
        problemsLiveData.setValue(problems);
    }

    private void applyFilter() {
        Log.d("FilterCheck", "applyFilter() called with: " + filterState.getValue());

        if (filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NONE
                && filterState.getValue().getSelectedSectors().isEmpty()
                && filterState.getValue().getSelectedCategories().isEmpty()) {
            problemRepository.fetchAllProblems(this);
        }
        //doar sector
        else if (filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NONE
                && !filterState.getValue().getSelectedSectors().isEmpty()
                && filterState.getValue().getSelectedCategories().isEmpty()) {
            problemRepository.getBySector(filterState.getValue().getSelectedSectors(), this);
        }
        //doar ordine
        else if (filterState.getValue().getSelectedSectors().isEmpty()
                && filterState.getValue().getSelectedCategories().isEmpty()) {
            problemRepository.orderByAge(filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NEWEST, this);
        }
        //sector si ordine
        else if (filterState.getValue().getSelectedCategories().isEmpty() && !filterState.getValue().getSelectedSectors().isEmpty()) {
            problemRepository.getBySectorOrderByAge(filterState.getValue().getSelectedSectors(), filterState.getValue().getSortOrder(), this);
        }
        // doar categorii
        else if (filterState.getValue().getSelectedSectors().isEmpty()
                && !filterState.getValue().getSelectedCategories().isEmpty()
                && filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NONE) {
            problemRepository.getByCategory(filterState.getValue().getSelectedCategories(), this);
        }
        //categorie + sort order -> de aici de editat
        else if (filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NONE
                && !filterState.getValue().getSelectedSectors().isEmpty()
                && !filterState.getValue().getSelectedCategories().isEmpty()) {
            //getBySectorAndCategory(filterState.getValue().getSelectedSectors(), filterState.getValue().getSelectedCategories());
        }
        //categorie + sector fara ordine
        else if (filterState.getValue().getSortOrder() == ProblemFilterState.SortOrder.NONE
                && !filterState.getValue().getSelectedCategories().isEmpty()
                && !filterState.getValue().getSelectedSectors().isEmpty()){

        }
        // categorie, sector, ordine
        else if (!filterState.getValue().getSelectedSectors().isEmpty()
                && !filterState.getValue().getSelectedCategories().isEmpty()
                && filterState.getValue().getSortOrder() != ProblemFilterState.SortOrder.NONE) {
            //getBySectorCategoryOrderByAge(filterState.getValue().getSelectedSectors(),
        }
    }


    //aici va tb implementat cu Algolia, pt ca asa nu
    //gaseste cuvinte din INTERIORUL unui String

    //Firestore -> doesn't support OR operator
    public void searchDataTitleDescription(String searchText){
        CollectionReference ref = FirebaseFirestore.getInstance().collection("problems");
        List<Problem> searchedList = new ArrayList<>();

        Task<QuerySnapshot> titleQuery = ref.whereGreaterThanOrEqualTo("title", searchText)
                .whereLessThanOrEqualTo("title", searchText + "\uf8ff")
                .get();

        Task<QuerySnapshot> descriptionQuery = ref.whereGreaterThanOrEqualTo("description", searchText)
                .whereLessThanOrEqualTo("description", searchText + "\uf8ff")
                .get();

        Tasks.whenAllComplete(titleQuery, descriptionQuery).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (Task<?> individualTask : task.getResult()) {
                    if (individualTask.isSuccessful()) {
                        QuerySnapshot result = (QuerySnapshot) individualTask.getResult();
                        for (DocumentSnapshot doc : result.getDocuments()) {
                            Problem problem = doc.toObject(Problem.class);
                            if (!searchedList.contains(problem)) {
                                searchedList.add(problem);
                            }
                        }
                    }
                }
                setProblems(searchedList);

            } else {
                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
            }
        });
    }

    public void fetchAllProblems() {
        problemRepository.fetchAllProblems(this);
    }

    @Override
    public void onFetchComplete(List<Problem> problems) {
        problemsLiveData.setValue(problems);
    }
}