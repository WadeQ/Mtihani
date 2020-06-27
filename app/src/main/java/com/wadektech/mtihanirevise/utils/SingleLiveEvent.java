package com.wadektech.mtihanirevise.utils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;


/**
 * This class extends MutableLiveData. It only calls setValue and postValue
 * after a server response
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean mPending=new AtomicBoolean(false);
    @MainThread
    public void observe(LifecycleOwner owner,final Observer<? super T> observer){
        if (hasActiveObservers()){
           Timber.d("multiple observers registered but only one will be notified of changes");
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
