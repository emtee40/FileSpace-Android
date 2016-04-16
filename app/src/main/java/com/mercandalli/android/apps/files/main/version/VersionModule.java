package com.mercandalli.android.apps.files.main.version;

import android.app.Application;

import com.mercandalli.android.apps.files.main.FileAppComponent;
import com.mercandalli.android.apps.files.main.network.RetrofitUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A Dagger module used by the {@link FileAppComponent}.
 */
@Module
public class VersionModule {

    @Provides
    @Singleton
    VersionManager provideVersionManager(final Application application) {
        return new VersionManager(
                application,
                RetrofitUtils.getRetrofit().create(VersionApi.class));
    }
}