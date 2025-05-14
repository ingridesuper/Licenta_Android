package com.example.licentaagain.repositories;

import android.net.Uri;
import android.util.Log;

import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ProblemRepository {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;


    public interface ProblemFetchCallback {
        void onFetchComplete(List<Problem> problems);
    }

    public interface ProblemCreationCallback {
        void onSuccess();

        void onFailure(Exception e);
    }


    public ProblemRepository() {
        this.db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void fetchAllProblemsGatheringSignatures(ProblemFetchCallback callback){
        db.collection("problems").whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot problem : task.getResult()) {
                            Problem newProblem = new Problem(
                                    problem.getString("address"),
                                    problem.getString("authorUid"),
                                    problem.getString("description"),
                                    problem.getDouble("latitude"),
                                    problem.getDouble("longitude"),
                                    problem.getDouble("sector").intValue(),
                                    problem.getString("title"),
                                    problem.getString("categorieProblema"),
                                    (List<String>) problem.get("imageUrls"),
                                    StareProblema.fromString(problem.getString("stareProblema")),
                                    problem.getString("facebookGroupLink")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                });
    }

    //in curs strangere semnaturi->
    public void orderByAge(boolean byNewest, ProblemFetchCallback callback) {
        Query query;

        if (byNewest) {
            query = db.collection("problems").whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare()).orderBy("createDate", Query.Direction.DESCENDING);
        } else {
            query = db.collection("problems").whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare()).orderBy("createDate", Query.Direction.ASCENDING);
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

    public void getBySector(List<Sector> selectedSectors, ProblemFetchCallback callback) {
        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        db.collection("problems")
                .whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare())
                .whereIn("sector", sectorNumbers)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    } else {
                        Log.i("fetch sector", "error");
                    }
                });
    }

    public void getByCategory(List<CategorieProblema> selectedCategories, ProblemFetchCallback callback) {
        List<String> categorieProblemaString = new ArrayList<>();
        for (CategorieProblema categorieProblema : selectedCategories) {
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        FirebaseFirestore.getInstance().collection("problems")
                .whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare())
                .whereIn("categorieProblema", categorieProblemaString)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    } else {
                        Log.i("fetch categorie", "error");
                    }
                });
    }

    public void getBySectorOrderByAge(List<Sector> selectedSectors, ProblemFilterState.SortOrder sortOrder, ProblemFetchCallback callback) {
        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("problems").whereIn("sector", sectorNumbers).whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare());

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

    public void getByCategoryOrderByAge(List<CategorieProblema> selectedCategories, ProblemFilterState.SortOrder sortOrder, ProblemFetchCallback callback) {
        List<String> categorieProblemaString = new ArrayList<>();
        for (CategorieProblema categorieProblema : selectedCategories) {
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        Query query = db.collection("problems").whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare()).whereIn("categorieProblema", categorieProblemaString);

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

    public void getByCategorySectorUnsorted(List<CategorieProblema> selectedCategories, List<Sector> selectedSectors, ProblemFetchCallback callback) {
        List<String> categorieProblemaString = new ArrayList<>();
        for (CategorieProblema categorieProblema : selectedCategories) {
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        db.collection("problems")
                .whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare())
                .whereIn("categorieProblema", categorieProblemaString)
                .whereIn("sector", sectorNumbers)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Problem problem = doc.toObject(Problem.class);
                            problem.setId(doc.getId());
                            fetchedProblems.add(problem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    } else {
                        Log.e("getByCategorySector", "Error fetching problems", task.getException());
                    }
                });
    }

    public void getByCategorySectorOrderByAge(List<CategorieProblema> selectedCategories, List<Sector> selectedSectors, ProblemFilterState.SortOrder sortOrder, ProblemFetchCallback callback) {
        List<String> categorieProblemaString = new ArrayList<>();
        for (CategorieProblema categorieProblema : selectedCategories) {
            categorieProblemaString.add(categorieProblema.getCategorie());
        }

        List<Integer> sectorNumbers = new ArrayList<>();
        for (Sector sector : selectedSectors) {
            sectorNumbers.add(sector.getNumar());
        }

        Query query = db.collection("problems")
                .whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare())
                .whereIn("categorieProblema", categorieProblemaString)
                .whereIn("sector", sectorNumbers);

        if (sortOrder.equals(ProblemFilterState.SortOrder.NEWEST)) {
            query = query.orderBy("createDate", Query.Direction.DESCENDING);
        } else if (sortOrder.equals(ProblemFilterState.SortOrder.OLDEST)) {
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
                Log.e("getByCategorySectorOrderByAge", "Error fetching problems", task.getException());
            }
        });
    }

    //!!!
    // aici va tb implementat cu Algolia, pt ca asa nu
    //gaseste cuvinte din INTERIORUL unui String

    //Firestore -> doesn't support OR operator
    public void searchDataTitleDescription(String searchText, ProblemFetchCallback callback) {
//        CollectionReference ref = FirebaseFirestore.getInstance().collection("problems");
//        List<Problem> searchedList = new ArrayList<>();
//
//        Task<QuerySnapshot> titleQuery = ref.whereGreaterThanOrEqualTo("title", searchText)
//                .whereLessThanOrEqualTo("title", searchText + "\uf8ff")
//                .get();
//
//        Task<QuerySnapshot> descriptionQuery = ref.whereGreaterThanOrEqualTo("description", searchText)
//                .whereLessThanOrEqualTo("description", searchText + "\uf8ff")
//                .get();
//
//        Tasks.whenAllComplete(titleQuery, descriptionQuery).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (Task<?> individualTask : task.getResult()) {
//                    if (individualTask.isSuccessful()) {
//                        QuerySnapshot result = (QuerySnapshot) individualTask.getResult();
//                        for (DocumentSnapshot doc : result.getDocuments()) {
//                            Problem problem = doc.toObject(Problem.class);
//                            problem.setId(doc.getId());
//                            if(problem.getStareProblema().equals(StareProblema.CURS_STRANGERE_SEMNATURI.getStare())){
//                                if (!searchedList.contains(problem)) { //based on id
//                                    searchedList.add(problem);
//                                }
//                            }
//                        }
//                    }
//                }
//                callback.onFetchComplete(searchedList);
//
//            } else {
//                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
//            }
//        });

        db.collection("problems").whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot problem : task.getResult()) {
                            if(problem.getString("title").toLowerCase().contains(searchText.toLowerCase()) || problem.getString("description").toLowerCase().contains(searchText.toLowerCase())) {
                                Problem newProblem = new Problem(
                                        problem.getString("address"),
                                        problem.getString("authorUid"),
                                        problem.getString("description"),
                                        problem.getDouble("latitude"),
                                        problem.getDouble("longitude"),
                                        problem.getDouble("sector").intValue(),
                                        problem.getString("title"),
                                        problem.getString("categorieProblema"),
                                        (List<String>) problem.get("imageUrls"),
                                        StareProblema.fromString(problem.getString("stareProblema")),
                                        problem.getString("facebookGroupLink")
                                );
                                newProblem.setId(problem.getId());
                                fetchedProblems.add(newProblem);
                            }
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                });

    }

    //<-in curs strangere semnaturi

    public void fetchAllProblemsByUser(String uid, ProblemFetchCallback callback) {
        db.collection("problems").whereEqualTo("authorUid", uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Problem> fetchedProblems = new ArrayList<>();
                        for (QueryDocumentSnapshot problem : task.getResult()) {
                            Problem newProblem = new Problem(
                                    problem.getString("address"),
                                    problem.getString("authorUid"),
                                    problem.getString("description"),
                                    problem.getDouble("latitude"),
                                    problem.getDouble("longitude"),
                                    problem.getDouble("sector").intValue(),
                                    problem.getString("title"),
                                    problem.getString("categorieProblema"),
                                    (List<String>) problem.get("imageUrls"),
                                    StareProblema.fromString(problem.getString("stareProblema")),
                                    problem.getString("facebookGroupLink")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                        callback.onFetchComplete(fetchedProblems);
                    }
                });
    }

    public void deleteProblem(Problem problem, Consumer<Boolean> callback) {
        db.collection("problems")
                .document(problem.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deletePicturesOfProblemFromStorage(problem, callback);
                        deleteSignaturesOfProblem(problem, callback);
                    } else {
                        callback.accept(false);
                    }
                });
    }

    private void deleteSignaturesOfProblem(Problem problem, Consumer<Boolean> callback) {
        db.collection("problem_signatures")
                .whereEqualTo("problemId", problem.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int totalSignatures = queryDocumentSnapshots.size();
                        final int[] deletedSignatures = {0};
                        final boolean[] deletionFailed = {false};

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("problem_signatures").document(doc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedSignatures[0]++;
                                        if (deletedSignatures[0] == totalSignatures && !deletionFailed[0]) {
                                            callback.accept(true);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!deletionFailed[0]) {
                                            deletionFailed[0] = true;
                                            callback.accept(false);
                                        }
                                    });
                        }
                    } else {
                        callback.accept(true); // No signatures to delete
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching signatures", e);
                    callback.accept(false); // Error fetching signatures
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

    public void updateProblemWithPictureChange(
            Problem oldProblem,
            Problem newProblem,
            List<Uri> newLocalUris,
            List<String> existingRemoteUrls,
            Consumer<Boolean> callback) {

        deletePicturesOfProblemFromStorage(oldProblem, result -> {
            if (!result) {
                Log.e("updateProblemWithPictureChange", "Error deleting old images");
                callback.accept(false);
                return;
            }

            Map<String, Object> updatedFields = new HashMap<>();
            updatedFields.put("title", newProblem.getTitle());
            updatedFields.put("description", newProblem.getDescription());
            updatedFields.put("sector", newProblem.getSector());
            updatedFields.put("categorieProblema", newProblem.getCategorieProblema());
            updatedFields.put("latitude", newProblem.getLatitude());
            updatedFields.put("longitude", newProblem.getLongitude());
            updatedFields.put("address", newProblem.getAddress());

            updateProblemWithoutPictureChange(oldProblem.getId(), newProblem, updateResult -> {
                if (!updateResult) {
                    Log.e("updateProblemWithPictureChange", "Error updating fields");
                    callback.accept(false);
                    return;
                }

                uploadImages(oldProblem.getId(), newLocalUris, downloadLinks -> {
                    List<String> allImageUrls = new ArrayList<>();
                    allImageUrls.addAll(existingRemoteUrls);  // Keep existing images
                    allImageUrls.addAll(downloadLinks);       // Add new ones

                    db.collection("problems")
                            .document(oldProblem.getId())
                            .update("imageUrls", allImageUrls)
                            .addOnSuccessListener(aVoid -> callback.accept(true))
                            .addOnFailureListener(e -> {
                                Log.e("updateProblemWithPictureChange", "Failed to update imageUrls", e);
                                callback.accept(false);
                            });

                }, e -> {
                    Log.e("updateProblemWithPictureChange", "Image upload error: " + e.getMessage());
                    callback.accept(false);
                });
            });
        });
    }


    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem, Consumer<Boolean> callback) {
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("title", newProblem.getTitle());
        updatedFields.put("description", newProblem.getDescription());
        updatedFields.put("sector", newProblem.getSector());
        updatedFields.put("categorieProblema", newProblem.getCategorieProblema());
        updatedFields.put("latitude", newProblem.getLatitude());
        updatedFields.put("longitude", newProblem.getLongitude());
        updatedFields.put("address", newProblem.getAddress());
        updatedFields.put("facebookGroupLink", newProblem.getFacebookGroupLink());
        db.collection("problems")
                .document(problemId)
                .update(updatedFields)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.accept(true);
                    } else {
                        callback.accept(false);
                    }
                });
    }

    public void addProblem(Problem problem, List<Uri> imageUris, ProblemCreationCallback callback) {
        db.collection("problems")
                .add(problem)
                .addOnSuccessListener(documentReference -> {
                    String problemId = documentReference.getId();
                    documentReference.update("createDate", FieldValue.serverTimestamp());

                    if (imageUris.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }

                    uploadImages(problemId, imageUris, newImages -> {
                        db.collection("problems")
                                .document(problemId)
                                .update("imageUrls", newImages)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> {
                                    callback.onFailure(e);
                                });
                    }, e -> {
                        callback.onFailure(e);
                    });

                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    private void uploadImages(String problemId, List<Uri> imageUris, OnImagesUploadedListener onSuccess, OnFailureListener onFailure) {
        List<Task<Uri>> uploadTasks = new ArrayList<>();
        List<String> downloadUrls = new ArrayList<>();

        for (Uri uri : imageUris) {
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = storage.getReference("problems/" + problemId + "/images/" + filename);

            Task<Uri> task = ref.putFile(uri)
                    .continueWithTask(t -> {
                        if (!t.isSuccessful()) throw t.getException();
                        return ref.getDownloadUrl();
                    });

            uploadTasks.add(task);
        }

        Tasks.whenAllSuccess(uploadTasks)
                .addOnSuccessListener(results -> {
                    for (Object result : results) {
                        if (result instanceof Uri) {
                            downloadUrls.add(result.toString());
                        }
                    }
                    onSuccess.onUploaded(downloadUrls);
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(e);
                    Log.e("FirebaseStorage", "Error uploading image: " + e.getMessage());

                });
    }

    private interface OnImagesUploadedListener {
        void onUploaded(List<String> imageUrls);
    }

    private interface OnFailureListener {
        void onFailure(Exception e);
    }

    public void numberOfProblemsReporteddByUser(String userId, Consumer<Integer> callback) {
        db.collection("problems")
                .whereEqualTo("authorUid", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    callback.accept(count);
                })
                .addOnFailureListener(e -> {
                    callback.accept(0);
                    Log.e("Firestore", "Error getting signed problems", e);
                });
    }


    public void updateStareProblema(String problemId, StareProblema newStare){
        db.collection("problems")
                .document(problemId)
                .update("stareProblema", newStare.getStare());
    }

    public void updateStareProblemaWithCallback(String problemId, StareProblema newStare, Consumer<Boolean> callback){
        db.collection("problems")
                .document(problemId)
                .update("stareProblema", newStare.getStare())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        callback.accept(true);
                    }
                    else {
                        callback.accept(false);
                    }
                });
    }

    public ListenerRegistration listenToProblemsByUserGatheringSignatures(String uid, Consumer<List<Problem>> callback) {
        return db.collection("problems")
                .whereEqualTo("authorUid", uid)
                .whereEqualTo("stareProblema", StareProblema.CURS_STRANGERE_SEMNATURI.getStare())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("FirestoreListen", "Listen failed.", e);
                        return;
                    }

                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }

                    callback.accept(problems);
                });
    }

    public ListenerRegistration listenToProblemsByUserSent(String uid, Consumer<List<Problem>> callback) {
        return db.collection("problems")
                .whereEqualTo("authorUid", uid)
                .whereIn("stareProblema", Arrays.asList(
                        StareProblema.AWAITING_RESOLVATION.getStare(),
                        StareProblema.AWAITING_RESPONSE.getStare(),
                        StareProblema.UNSATISFACTORY.getStare()
                ))
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("FirestoreListen", "Listen failed.", e);
                        return;
                    }

                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }

                    callback.accept(problems);
                });
    }

    public ListenerRegistration listenToSolvedProblemsOfUser(String uid, Consumer<List<Problem>> callback) {
        return db.collection("problems")
                .whereEqualTo("authorUid", uid)
                .whereEqualTo("stareProblema", StareProblema.SOLVED.getStare())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Listen failed.", e);
                        return;
                    }

                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }

                    callback.accept(problems);
                });
    }

    public ListenerRegistration listenToSignedProblemsOfUser(String uid, Consumer<List<Problem>> callback) {
        Query query = db.collection("problem_signatures")
                .whereEqualTo("userId", uid);

        ListenerRegistration listenerRegistration = query.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("listenToSignedProblems", "Error listening for signed problems", error);
                return;
            }

            if (snapshot == null || snapshot.isEmpty()) {
                callback.accept(new ArrayList<>());
                return;
            }

            List<Task<DocumentSnapshot>> problemTasks = new ArrayList<>();

            for (QueryDocumentSnapshot signature : snapshot) {
                String problemId = signature.getString("problemId");

                Task<DocumentSnapshot> problemTask = db.collection("problems")
                        .document(problemId)
                        .get();

                problemTasks.add(problemTask);
            }

            Tasks.whenAllComplete(problemTasks).addOnCompleteListener(tasks -> {
                List<Problem> fetchedProblems = new ArrayList<>();

                for (Task<DocumentSnapshot> taskItem : problemTasks) {
                    if (taskItem.isSuccessful()) {
                        DocumentSnapshot problem = taskItem.getResult();
                        if (problem.exists()) {
                            Problem newProblem = new Problem(
                                    problem.getString("address"),
                                    problem.getString("authorUid"),
                                    problem.getString("description"),
                                    problem.getDouble("latitude"),
                                    problem.getDouble("longitude"),
                                    problem.getDouble("sector").intValue(),
                                    problem.getString("title"),
                                    problem.getString("categorieProblema"),
                                    (List<String>) problem.get("imageUrls"),
                                    StareProblema.fromString(problem.getString("stareProblema")),
                                    problem.getString("facebookGroupLink")
                            );
                            newProblem.setId(problem.getId());
                            fetchedProblems.add(newProblem);
                        }
                    } else {
                        Log.e("listenToSignedProblems", "Error fetching problem details", taskItem.getException());
                    }
                }

                callback.accept(fetchedProblems);
            });
        });

        return listenerRegistration;
    }

    public ListenerRegistration listenToCountReportedProblemsByUser(String uid, Consumer<Integer> callback){
        return db.collection("problems").whereEqualTo("authorUid", uid)
                .addSnapshotListener((snapshots, e)->{
                    if (e != null) {
                        Log.e("Firestore", "Listen failed.", e);
                        return;
                    }
                    assert snapshots != null;
                    callback.accept(snapshots.size());
                });
    }

    public ListenerRegistration listenToAllProblemsAdmin(Consumer<List<Problem>> callback){
        return db.collection("problems")
                .addSnapshotListener((snapshots, e)->{
                    if (e != null) {
                        Log.e("Firestore", "Listen failed.", e);
                        return;
                    }
                    assert snapshots!=null;
                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }
                    callback.accept(problems);

                });
    }

    public ListenerRegistration listenToAllProblemsByUser(String uid, Consumer<List<Problem>> callback){
        return db.collection("problems")
                .whereEqualTo("authorUid", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Listen failed.", e);
                        return;
                    }

                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }

                    callback.accept(problems);
                });
    }

}