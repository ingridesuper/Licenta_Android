package com.example.licentaagain.mainpage;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
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
import java.util.List;
import java.util.function.Consumer;

public class ProblemViewModel extends ViewModel {
    private MutableLiveData<List<Problem>> problemsLiveData=new MutableLiveData<>();
    private final MutableLiveData<ProblemFilterState> filterState = new MutableLiveData<>(new ProblemFilterState());

    //getters and setters
    public LiveData<ProblemFilterState> getFilterState() {
        return filterState;
    }

    public void updateFilterState(ProblemFilterState state) {
        filterState.setValue(state);
        applyFilter(state);
    }

    public LiveData<List<Problem>> getProblems() {
        return problemsLiveData;
    }

    public void setProblems(List<Problem> problems) {
        problemsLiveData.setValue(problems);
    }

    private void applyFilter(ProblemFilterState state) {
        //orderByAge(state.getSortOrder()==ProblemFilterState.SortOrder.NEWEST);
        getBySector(filterState.getValue().getSelectedSectors());
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
                resetProblemFilterState();

            } else {
                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
            }
        });
    }

    private void resetProblemFilterState() { //resetam si asta! ca sa nu arate ce A FOST selectat, nu ce e selectat in prezent
        ProblemFilterState state=new ProblemFilterState();
        updateFilterState(state);
    }

    public void fetchAllProblems() {
        FirebaseFirestore.getInstance().collection("problems").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot problem : task.getResult()) {
                            Problem newProblem=new Problem(
                                    problem.getString("address"),
                                    problem.getString("authorUid"),
                                    problem.getString("description"),
                                    problem.getDouble("latitude"),
                                    problem.getDouble("longitude"),
                                    problem.getDouble("sector").intValue(),
                                    problem.getString("title"),
                                    problem.getString("categorieProblema")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                        setProblems(fetchedProblems);
                        resetProblemFilterState();
                    }
                });
    }

    public void orderByAge(boolean byNewest){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query;

        if (byNewest) {
            query = db.collection("problems").orderBy("createDate", Query.Direction.DESCENDING);
        } else {
            query = db.collection("problems").orderBy("createDate", Query.Direction.ASCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Problem> orderedProblems = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Problem problem = doc.toObject(Problem.class);
                    problem.setId(doc.getId());
                    orderedProblems.add(problem);
                }
                setProblems(orderedProblems);
            } else {
                Log.e("FirestoreOrder", "Error ordering by createDate", task.getException());
            }
        });
    }


    public void getBySector(List<Sector> selectedSectors){
        if(selectedSectors.isEmpty()){
            return;
        }
        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        FirebaseFirestore.getInstance().collection("problems")
                .whereIn("sector", sectorNumbers)
                .get()
                .addOnCompleteListener(task->{
                    if (task.isSuccessful()){
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        setProblems(fetchedProblems);
                    }
                    else {
                        Log.i("fetch sector", "error");
                    }
                });
    }

}