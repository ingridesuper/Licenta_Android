package com.example.licentaagain.problem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.account.TopProfileFragment;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.custom_adapters.SearchUserAdapter;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.example.licentaagain.repositories.UserRepository;
import com.example.licentaagain.view_models.SearchedUserViewModel;
import com.example.licentaagain.view_models.SemnatariViewModel;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProblemOfCurrentUserDetailsFragment extends Fragment implements OnMapReadyCallback {
    private Problem problem;
    private GoogleMap myMap;
    private Button btnClose, btnOpenInGoogleMaps;
    private MaterialButton btnTakeAction;
    private SemnatariViewModel viewModel;
    SearchUserAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }
        viewModel=new ViewModelProvider(requireActivity()).get(SemnatariViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_problem_of_current_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillUiWithProblemData(view);
        subscribeButtonsToEvents();
        setUpMapFragment(view);
    }

    private void setUpMapFragment(View view) {
        ScrollView mScrollView = view.findViewById(R.id.scrollView);
        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.i("map found", "Map found and initialized");
            mapFragment.setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));
        } else {
            Log.e("map not found", "Map fragment not found");
        }
    }

    private void subscribeButtonsToEvents() {
        subscribeBtnCloseToEvent();
        subscribeOpenInGoogleMaps();
        subscribeBtnTakeActionToEvent();
    }

    private void subscribeBtnTakeActionToEvent() {
        btnTakeAction.setOnClickListener(v -> {
            new UserRepository().getUsersWhoSignedProblem(problem.getId(), semnatari -> {
                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                documentReference.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String currentUserFullName = document.getString("name") + " " + document.getString("surname");
                            String currentUserSector = "Sectorul " + document.getLong("sector").intValue();
                            String userEmail = document.getString("email");

                            StringBuilder prompt = new StringBuilder();
                            prompt.append("Scrie un email oficial în limba română pentru autorități, ");
                            prompt.append("în care o persoană numită ").append(currentUserFullName)
                                    .append(" din ").append(currentUserSector).append(", dorește să sesizeze următoarea problemă: ");
                            prompt.append(problem.getTitle()).append(".\n");
                            prompt.append("Descriere: ").append(problem.getDescription()).append("\n");
                            prompt.append("Adresa: ").append(problem.getAddress()).append("\n\n");
                            prompt.append("Persoana cere un număr de înregistrare și vrea să primească răspuns la adresa ").append(userEmail).append(".\n");
                            prompt.append("Include și o listă de susținători cu nume și email.\n\n");

                            for (User user : semnatari) {
                                prompt.append("- ").append(user.getName()).append(" ")
                                        .append(user.getSurname()).append(": ")
                                        .append(user.getEmail()).append("\n");
                            }

                            generateEmailWithAI(prompt.toString(), new com.squareup.okhttp.Callback() {
                                @Override
                                public void onFailure(Request request, IOException e) {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Eroare la generarea emailului.", Toast.LENGTH_SHORT).show()
                                    );
                                }

                                @Override
                                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        String errorMessage = "Error: " + response.code() + " - " + response.message();
                                        Log.e("OpenAI API", errorMessage);
                                        requireActivity().runOnUiThread(() -> {
                                            Toast.makeText(getContext(), "Eroare API OpenAI: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        });
                                        return;
                                    }


                                    String responseData = response.body().string();
                                    try {
                                        JSONObject json = new JSONObject(responseData);
                                        String generatedEmail = json.getJSONArray("choices")
                                                .getJSONObject(0)
                                                .getJSONObject("message")
                                                .getString("content");

                                        requireActivity().runOnUiThread(() -> {
                                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                            emailIntent.setData(Uri.parse("mailto:"));
                                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesizare: " + problem.getTitle());
                                            emailIntent.putExtra(Intent.EXTRA_TEXT, generatedEmail);

                                            try {
                                                startActivity(Intent.createChooser(emailIntent, "Trimite email cu..."));
                                            } catch (ActivityNotFoundException e) {
                                                Toast.makeText(getContext(), "Nu s-a găsit o aplicație de email.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    }
                });
            });
        });
    }

    public void generateEmailWithAI(String prompt, com.squareup.okhttp.Callback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            messages.put(userMessage);
            json.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json.toString().getBytes(StandardCharsets.UTF_8));

        String apiKey = getResources().getString(R.string.open_ai_api_key);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }



    private void subscribeOpenInGoogleMaps() {
        btnOpenInGoogleMaps.setOnClickListener(v->{
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", problem.getLatitude(), problem.getLongitude(), problem.getLatitude(), problem.getLongitude(), problem.getTitle()+" - "+problem.getAddress());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeBtnCloseToEvent() {
        btnClose.setOnClickListener(v->{
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, new TopProfileFragment())
                        .commit();
            }
        });
    }

    private void fillUiWithProblemData(View view) {
        TextView tvProblemTitle= view.findViewById(R.id.tvProblemTitle);
        TextView tvProblemDescription=view.findViewById(R.id.tvProblemDescription);
        TextView tvProblemCategory=view.findViewById(R.id.tvProblemCategory);
        TextView tvProblemAddressSector=view.findViewById(R.id.tvProblemAddressSector);
        RecyclerView recyclerViewPictures=view.findViewById(R.id.recyclerViewPictures);
        TextView tvNrSemnatariHeading=view.findViewById(R.id.tvNrSemnatariHeading);
        btnClose=view.findViewById(R.id.btnClose);
        btnOpenInGoogleMaps=view.findViewById(R.id.btnOpenInGoogleMaps);
        btnTakeAction=view.findViewById(R.id.btnTakeAction);

        tvProblemTitle.setText(problem.getTitle());
        tvProblemDescription.setText(problem.getDescription());
        tvProblemCategory.setText("Categorie: "+problem.getCategorieProblema());
        tvProblemAddressSector.setText(problem.getAddress()+", Sectorul "+problem.getSector());

        new ProblemSignatureRepository().numberSignaturesOfProblem(problem.getId(), result-> tvNrSemnatariHeading.setText("Semnatari ("+result.toString()+"): "));

        List<String> problemImageUrls=problem.getImageUrls();
        ImageAdapterProblemDetails adapter=new ImageAdapterProblemDetails(getContext(), problemImageUrls);
        recyclerViewPictures.setAdapter(adapter);

        fillUpSemnatariRecyclerView(view);
    }

    private void fillUpSemnatariRecyclerView(View view) {
        RecyclerView rvSemnatariList=view.findViewById(R.id.rvSemnatariList);
        rvSemnatariList.setNestedScrollingEnabled(false);
        adapter=new SearchUserAdapter(new ArrayList<>());
        rvSemnatariList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvSemnatariList.setAdapter(adapter);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.updateData(users);
            Log.i("fetchedUsers", String.valueOf(users.size())+": "+users.toString());
        });
        viewModel.getSemnatariOfProblema(problem.getId());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng location = new LatLng(problem.getLatitude(), problem.getLongitude());
        myMap.addMarker(new MarkerOptions().position(location).title(problem.getTitle()));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        UiSettings uiSettings= myMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        Log.i("ProblemDetailsFragment", "Map is ready");
    }
}