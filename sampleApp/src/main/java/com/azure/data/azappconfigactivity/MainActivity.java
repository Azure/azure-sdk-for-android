package com.azure.data.azappconfigactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private TextView textView;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        //
        button.setOnClickListener(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public void onClick(View view) {
        //
        EditText conStringInput = findViewById(R.id.conStringEditText);
        EditText serviceUrlInput = findViewById(R.id.serviceUrlEditText);
        //
        String conString = conStringInput.getText().toString();
        String serviceUrl = serviceUrlInput.getText().toString();
        //
        if (conString == null || conString == "" || serviceUrl == null || serviceUrl == "") {
            // NOP
            return;
        } else {
            URL serviceEndpoint;
            try {
                serviceEndpoint = new URL(serviceUrl);
            } catch (MalformedURLException mue) {
                throw new RuntimeException(mue);
            }
            //
            ConfigurationClient client = new ConfigurationClient(serviceEndpoint, conString);
            //
            DisposableObserver<ConfigurationSetting> disposable = Observable.defer((Callable<ObservableSource<ConfigurationSetting>>) () -> Observable.fromCallable(() -> {
                ConfigurationSetting setting = new ConfigurationSetting().key("hello");
                return client.getSetting(setting);
            }))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableObserver<ConfigurationSetting>() {
                @Override
                public void onComplete() {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onNext(ConfigurationSetting result) {
                    textView.setText(result.value());
                }
            });
            //
            disposables.add(disposable);
        }
    }
}
