package com.rafaelkarlo.workmode.mainscreen.config;

import com.rafaelkarlo.workmode.mainscreen.view.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MainActivityModule.class)
public interface MainActivityComponent {
    void inject(MainActivity mainScreenActivity);
}
