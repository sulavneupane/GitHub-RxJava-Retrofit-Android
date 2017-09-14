package com.nepalicoders.githubrxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GitHubRepoAdapter mAdapter = new GitHubRepoAdapter();

    private Disposable mDisposable;

    @BindView(R.id.list_view_repos)
    ListView listView;
    @BindView(R.id.edit_text_username)
    EditText editTextUserName;
    @BindView(R.id.button_search)
    Button buttonSearch;

    Unbinder butterKnifeBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        butterKnifeBind = ButterKnife.bind(this);

        listView.setAdapter(mAdapter);

        RxView.clicks(buttonSearch).subscribe(aVoid -> {
            final String userName = editTextUserName.getText().toString();
            if (!TextUtils.isEmpty(userName)) {
                Log.d(TAG, aVoid.toString());
                getStarredRepos(userName);
            }
        });

        RxTextView.textChanges(editTextUserName).subscribe(str -> {
            Log.d(TAG, str.toString());
            // buttonSearch.setText(str);
        });

//        buttonSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String userName = editTextUserName.getText().toString();
//                if (!TextUtils.isEmpty(userName)) {
//                    getStarredRepos(userName);
//                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        butterKnifeBind.unbind();
        super.onDestroy();
    }

    private void getStarredRepos(String userName) {
        GitHubClient.getInstance()
                .getStarredRepos(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GitHubRepo>>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        Log.d(TAG, "In onSubscribe()");
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull List<GitHubRepo> gitHubRepos) {
                        Log.d(TAG, "In onNext()");
                        mAdapter.setGitHubRepos(gitHubRepos);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "In onError()");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "In onComplete()");
                    }
                });
    }
}
