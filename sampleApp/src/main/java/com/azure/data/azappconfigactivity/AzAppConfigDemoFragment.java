package com.azure.data.azappconfigactivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.azure.core.exception.HttpResponseException;
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
        Button setBtn = rootView.findViewById(R.id.setBtn);
        setBtn.setOnClickListener(this);
        //
        Button getBtn = rootView.findViewById(R.id.getBtn);
        getBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View buttonView) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String conString = preference.getString("az_conf_connection", "<unset>");
        String serviceUrl = preference.getString("az_conf_endpoint", "<unset>");
        //
        if (conString == "<unset>" || serviceUrl == "<unset>") {
            TextView textView = buttonView.getRootView().findViewById(R.id.setResponseTxt);
            textView.setText("Az config connection string is not set in preference.");
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
            switch (buttonView.getId()) {
                case R.id.setBtn:
                    onSetButtonClick(client, buttonView);
                    return;
                case R.id.getBtn:
                    onGetButtonClick(client, buttonView);
                    return;
            }
        }
    }

    private void  onSetButtonClick(ConfigurationClient client, View buttonView) {
        EditText key = buttonView.getRootView().findViewById(R.id.setConfigKey);
        EditText value = buttonView.getRootView().findViewById(R.id.setConfigValue);
        //
        String keyString = key.getText().toString();
        String valueString = value.getText().toString();
        //
        if (keyString == null || keyString == "" || valueString == null || valueString == "") {
            TextView textView = buttonView.getRootView().findViewById(R.id.setResponseTxt);
            textView.setText("key and value required");
            return;
        }
        //
        DisposableObserver<ConfigurationSetting> disposable = Observable.defer((Callable<ObservableSource<ConfigurationSetting>>) () -> Observable.fromCallable(() -> client.addSetting(keyString, valueString)))
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<ConfigurationSetting>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                HttpResponseException responseException = (HttpResponseException) e;
                Log.w("Undeliverable exception", responseException);
            }

            @Override
            public void onNext(ConfigurationSetting result) {
                TextView textView = buttonView.getRootView().findViewById(R.id.setResponseTxt);
                textView.setText(result.value());
            }
        });
        disposables.add(disposable);
    }

    private void  onGetButtonClick(ConfigurationClient client, View buttonView) {
        EditText key = buttonView.getRootView().findViewById(R.id.getConfigKey);
        //
        String keyString = key.getText().toString();
        //
        if (keyString == null || keyString == "") {
            TextView textView = buttonView.getRootView().findViewById(R.id.getResponseTxt);
            textView.setText("key required");
            return;
        }
        //
        DisposableObserver<ConfigurationSetting> disposable = Observable.defer((Callable<ObservableSource<ConfigurationSetting>>) () -> Observable.fromCallable(() -> {
            ConfigurationSetting setting = new ConfigurationSetting().key(keyString);
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
                HttpResponseException responseException = (HttpResponseException) e;
                Log.w("Undeliverable exception", responseException);
            }

            @Override
            public void onNext(ConfigurationSetting result) {
                TextView textView = buttonView.getRootView().findViewById(R.id.getResponseTxt);
                textView.setText(result.value());
            }
        });
        disposables.add(disposable);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
