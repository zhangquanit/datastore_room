/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.room.demo;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.room.demo.persistence.User;
import com.room.demo.persistence.UsersDatabase;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


/**
 * Main screen of the app. Displays a user name and gives the option to update the user name.
 */
public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    private TextView mUserName;

    private EditText mUserNameInput;

    private Button mUpdateButton;

    private ViewModelFactory mViewModelFactory;

    private UserViewModel mViewModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserName = findViewById(R.id.user_name);
        mUserNameInput = findViewById(R.id.user_name_input);
        mUpdateButton = findViewById(R.id.update_user);

        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(UserViewModel.class);
        mUpdateButton.setOnClickListener(v -> updateUserName());

//        startActivity(new Intent(this,MainActivity.class));
//        finish();
        test();
        getUserList();

        findViewById(R.id.update_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<User> users = UsersDatabase.getInstance(UserActivity.this).userDao().getUsers();
                System.out.println("插入前 users: " + users);
                User user = new User("张三");
                if (!users.isEmpty()) {
                    user.setId(users.get(0).getId());
                }
                user.v2="v2";
                user.v3="v3";
                UsersDatabase.getInstance(UserActivity.this).userDao().insertUser(user);
                users = UsersDatabase.getInstance(UserActivity.this).userDao().getUsers();
                System.out.println("插入后 users: " + users);
            }
        });

    }

    @SuppressLint({"AutoDispose", "CheckResult"})
    private void test() {
        Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(FlowableEmitter<Object> emitter) throws Exception {
                emitter.onNext(new Object());
            }
        }, BackpressureStrategy.LATEST)
                .observeOn(Schedulers.io())
                .map(new Function<Object, TempData>() {
                    @Override
                    public TempData apply(Object o) throws Exception {
                        System.out.println("map1");
                        return new TempData();
                    }
                })
                .filter(new Predicate<TempData>() {
                    @Override
                    public boolean test(TempData tempData) throws Exception {
                        System.out.println("test");
                        return tempData.data != null;
                    }
                })
                .map(new Function<TempData, String>() {
                    @Override
                    public String apply(TempData tempData) throws Exception {
                        System.out.println("map2");
                        return tempData.data;
                    }
                })
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("onSubscribe");
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("onNext s=" + s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("onError");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
    }

    private class TempData {
        public String data;
    }

    @SuppressLint("AutoDispose")
    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        mDisposable.add(mViewModel.getUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String userName) throws Exception {
                        System.out.println("onNext userName=" + userName);
                        mUserName.setText(userName);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("Unable to update username throwable=" + throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("onComplete");
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // clear all the subscriptions
        mDisposable.clear();
    }

    @SuppressLint("AutoDispose")
    private void updateUserName() {
        String userName = mUserNameInput.getText().toString();
        // Disable the update button until the user name update has been done
        mUpdateButton.setEnabled(false);
        // Subscribe to updating the user name.
        // Re-enable the button once the user name has been updated
        mDisposable.add(mViewModel.updateUserName(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> mUpdateButton.setEnabled(true),
                        throwable -> Log.e(TAG, "Unable to update username", throwable)));
    }

    //-------------------------------

    @SuppressLint("AutoDispose")
    private void getUserList() {
        List<User> users = UsersDatabase.getInstance(this).userDao().getUsers();
        System.out.println("users=" + users);
        UsersDatabase.getInstance(this).userDao().getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Exception {
                        System.out.println("getAllUsers  users=" + users);
                    }
                });

        if (users.isEmpty()) return;
        User user = users.get(0);
        user.password = "1111";
        user.isLogin = true;
        UsersDatabase.getInstance(this).userDao().insertUser(user);


    }

}
