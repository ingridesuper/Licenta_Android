package com.example.licentaagain.repositories;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ProblemRepository {
    private final FirebaseFirestore db;

    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem, Consumer<Boolean> callback) {
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("title", newProblem.getTitle());
        updatedFields.put("description", newProblem.getDescription());
        updatedFields.put("sector", newProblem.getSector());
        updatedFields.put("categorieProblema", newProblem.getCategorieProblema());
        updatedFields.put("latitude", newProblem.getLatitude());
        updatedFields.put("longitude", newProblem.getLongitude());
        updatedFields.put("address", newProblem.getAddress());
        db.collection("problems")
                .document(problemId)
                .update(updatedFields)
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        callback.accept(true);
                    }
                    else {
                        callback.accept(false);
                    }
                });
    }

    public interface ProblemFetchCallback {
        void onFetchComplete(List<Problem> problems);
    }


    public ProblemRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void fetchAllProblems(ProblemFetchCallback callback) {
        db.collection("problems").get()
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
                                    problem.getString("categorieProblema"),
                                    (List<String>) problem.get("imageUrls")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                });
    }

    public void orderByAge(boolean byNewest, ProblemFetchCallback callback){
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
                callback.onFetchComplete(orderedProblems);
            } else {
                Log.e("FirestoreOrder", "Error ordering by createDate", task.getException());
            }
        });
    }

    public void getBySector(List<Sector> selectedSectors, ProblemFetchCallback callback){
        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        db.collection("problems")
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
                        callback.onFetchComplete(fetchedProblems);
                    }
                    else {
                        Log.i("fetch sector", "error");
                    }
                });
    }

    public void getByCategory(List<CategorieProblema> selectedCategories, ProblemFetchCallback callback) {
        List<String> categorieProblemaString=new ArrayList<>();
        for(CategorieProblema categorieProblema:selectedCategories){
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        FirebaseFirestore.getInstance().collection("problems")
                .whereIn("categorieProblema", categorieProblemaString)
                .get()
                .addOnCompleteListener(task->{
                    if (task.isSuccessful()){
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                    else {
                        Log.i("fetch categorie", "error");
                    }
                });
    }

    public void getBySectorOrderByAge(List<Sector> selectedSectors, ProblemFilterState.SortOrder sortOrder,  ProblemFetchCallback callback) {
        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("problems").whereIn("sector", sectorNumbers);

        if (sortOrder == ProblemFilterState.SortOrder.NEWEST) {
            query = query.orderBy("createDate", Query.Direction.DESCENDING);
        } else if (sortOrder == ProblemFilterState.SortOrder.OLDEST) {
            query = query.orderBy("createDate", Query.Direction.ASCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Problem> fetchedProblems = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Problem problem = doc.toObject(Problem.class);
                    problem.setId(doc.getId());
                    fetchedProblems.add(problem);
                }
                callback.onFetchComplete(fetchedProblems);
            } else {
                Log.e("getBySectorOrderByAge", "Error fetching problems", task.getException());
            }
        });
    }

    public void getByCategoryOrderByAge(List<CategorieProblema> selectedCategories, ProblemFilterState.SortOrder sortOrder, ProblemFetchCallback callback){
        List<String> categorieProblemaString=new ArrayList<>();
        for(CategorieProblema categorieProblema:selectedCategories){
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        Query query=db.collection("problems").whereIn("categorieProblema", categorieProblemaString);

        if (sortOrder == ProblemFilterState.SortOrder.NEWEST) {
            query = query.orderBy("createDate", Query.Direction.DESCENDING);
        } else if (sortOrder == ProblemFilterState.SortOrder.OLDEST) {
            query = query.orderBy("createDate", Query.Direction.ASCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Problem> fetchedProblems = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Problem problem = doc.toObject(Problem.class);
                    problem.setId(doc.getId());
                    fetchedProblems.add(problem);
                }
                callback.onFetchComplete(fetchedProblems);
            } else {
                Log.e("getByCategoryOrderByAge", "Error fetching problems", task.getException());
            }
        });

    }

    public void getByCategorySectorUnsorted(List<CategorieProblema> selectedCategories, List<Sector> selectedSectors, ProblemFetchCallback callback){
        List<String> categorieProblemaString=new ArrayList<>();
        for(CategorieProblema categorieProblema:selectedCategories){
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        db.collection("problems")
                .whereIn("categorieProblema", categorieProblemaString)
                .whereIn("sector", sectorNumbers)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                    else {
                        Log.e("getByCategorySector", "Error fetching problems", task.getException());
                    }
                });
    }

    public void getByCategorySectorOrderByAge(List<CategorieProblema> selectedCategories, List<Sector> selectedSectors, ProblemFilterState.SortOrder sortOrder,ProblemFetchCallback callback){
        List<String> categorieProblemaString=new ArrayList<>();
        for(CategorieProblema categorieProblema:selectedCategories){
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        Query query=db.collection("problems")
                .whereIn("categorieProblema", categorieProblemaString)
                .whereIn("sector", sectorNumbers);

        if(sortOrder.equals(ProblemFilterState.SortOrder.NEWEST)){
            query = query.orderBy("createDate", Query.Direction.DESCENDING);
        }
        else if(sortOrder.equals(ProblemFilterState.SortOrder.OLDEST)){
            query = query.orderBy("createDate", Query.Direction.ASCENDING);
        }
        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<Problem> fetchedProblems = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Problem problem = doc.toObject(Problem.class);
                    problem.setId(doc.getId());
                    fetchedProblems.add(problem);
                }
                callback.onFetchComplete(fetchedProblems);
            }
            else {
                Log.e("getByCategorySectorOrderByAge", "Error fetching problems", task.getException());
            }
        });
    }

    //!!!
    // aici va tb implementat cu Algolia, pt ca asa nu
    //gaseste cuvinte din INTERIORUL unui String

    //Firestore -> doesn't support OR operator
    public void searchDataTitleDescription(String searchText, ProblemFetchCallback callback){
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
                            problem.setId(doc.getId());
                            if (!searchedList.contains(problem)) { //based on id
                                searchedList.add(problem);
                            }
                        }
                    }
                }
                callback.onFetchComplete(searchedList);

            } else {
                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
            }
        });
    }

    public void fetchAllProblemsByUser(String uid, ProblemFetchCallback callback){
        db.collection("problems").whereEqualTo("authorUid", uid).get()
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
                                    problem.getString("categorieProblema"),
                                    (List<String>) problem.get("imageUrls")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                });
    }

    public void deleteProblem(Problem problem, Consumer<Boolean> callback){
        db.collection("problems")
                .document(problem.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        //eventual aici voi adauga un Toast si logica de stergere de semnaturi asociate (cascade)
                        //acum nu e cazul
                        //si also notificari catre semnatari
                        //stergem pozele din storage
                        deletePicturesOfProblemFromStorage(problem, callback);
                    }
                    else {
                        callback.accept(false);
                    }
                });
    }

    private void deletePicturesOfProblemFromStorage(Problem problem, Consumer<Boolean> callback) {
        List<String> imageUrls = problem.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            callback.accept(true);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        AtomicInteger remaining = new AtomicInteger(imageUrls.size());
        AtomicBoolean hadError = new AtomicBoolean(false);

        for (String url : imageUrls) {
            StorageReference photoRef = storage.getReferenceFromUrl(url);
            photoRef.delete()
                    .addOnSuccessListener(unused -> {
                        if (remaining.decrementAndGet() == 0) {
                            callback.accept(!hadError.get());
                        }
                    })
                    .addOnFailureListener(e -> {
                        hadError.set(true);
                        if (remaining.decrementAndGet() == 0) {
                            callback.accept(false);
                        }
                    });
        }
    }
}
