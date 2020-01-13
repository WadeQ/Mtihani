package com.wadektech.mtihanirevise.utils;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This class extends MutableLiveData. It only calls setValue and postValue
 * after a server response
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean mPending=new AtomicBoolean(false);
    @MainThread
    public void observe(LifecycleOwner owner,final Observer<T> observer){
        if (hasActiveObservers()){
           // Timber.d("multiple observers registered but only one will be notified of changes");
        }
        //observe the internal mutablelivedata
        super.observe(owner, t -> {
            if (mPending.compareAndSet(true,false)){
                observer.onChanged(t);
            }
        });
    }
    @MainThread
    public void setValue(@Nullable T t){
        mPending.set(true);
        super.setValue(t);
    }

    @MainThread
    public void call(){
        setValue(null);
    }

    public void postValue(@Nullable T t){
        mPending.set(true);
        super.postValue(t);
    }
}
