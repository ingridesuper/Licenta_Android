package com.example.licentaagain.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SearchedUserViewModel extends ViewModel implements UserRepository.UserFetchCallback {

    private MutableLiveData<List<User>> usersLiveData=new MutableLiveData<>();
    private final UserRepository userRepository;
    public SearchedUserViewModel(){ userRepository=new UserRepository();}
    public LiveData<List<User>> getUsers() {
        return usersLiveData;
    }


    public void searchUserByEmailOrNameSurname(String searchText){
        userRepository.searchUserByEmailOrNameSurname(searchText, FirebaseAuth.getInstance().getCurrentUser().getUid(),this);
    }
    public void setUsers(List<User> users) {
        usersLiveData.setValue(users);
    }


    @Override
    public void onFetchComplete(List<User> users) {
        usersLiveData.setValue(users);
    }
}
