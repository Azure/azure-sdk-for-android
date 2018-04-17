package com.azure.core.network

import com.azure.core.util.ContextProvider
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Connectivity {

    companion object {

        private val onChanged = mutableListOf<()->Unit>()

        private var onChangedDisposable: Disposable? = null

        fun addOnChangedCallback( callback : () -> Unit) {
            synchronized(onChanged){
                val count = onChanged.size
                onChanged.add(callback)
                if (count==0){
                    onChangedDisposable = ReactiveNetwork.observeNetworkConnectivity(ContextProvider.appContext)
                        .subscribeOn(Schedulers.io())
                        .subscribe{
                            onChanged.forEach { it.invoke() }
                        }
                }
            }
        }

        fun removeOnChangedCallback( callback : () -> Unit){
            synchronized(onChanged){
                onChanged.remove(callback)
                val count = onChanged.size
                if (count==0){
                    onChangedDisposable?.dispose()
                    onChangedDisposable = null
                }
            }
        }

    }

}