package com.room.demo;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.room.demo.persistence.User;
import com.room.demo.persistence.UsersDatabase;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUsers();

    }

    private void getUsers(){
//        @SuppressLint("AutoDispose") Disposable subscribe = UsersDatabase.getInstance(this).userDao().getAllUsers()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<User>>() {
//                    @Override
//                    public void accept(List<User> user) throws Exception {
//                       System.out.println("users="+user);
//                    }
//                });
//        mDisposable.add(subscribe);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();
    }
}
