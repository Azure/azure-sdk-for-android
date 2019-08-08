package com.azure.data.azappconfigactivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AzAppConfigDemoFragment extends Fragment implements View.OnClickListener {
    private Button button;
    private TextView textView;
    private final CompositeDisposable disposables = new CompositeDisposable();
    //
    public static AzAppConfigDemoFragment newInstance() {
        return new AzAppConfigDemoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.az_app_config_demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        button = rootView.findViewById(R.id.button);
        textView = rootView.findViewById(R.id.textView);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View buttonView) {
        //
        EditText conStringInput = buttonView.getRootView().findViewById(R.id.conStringEditText);
        EditText serviceUrlInput = buttonView.getRootView().findViewById(R.id.serviceUrlEditText);
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
            disposables.add(disposable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
