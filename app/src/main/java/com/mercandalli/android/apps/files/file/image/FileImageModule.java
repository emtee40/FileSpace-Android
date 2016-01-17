package com.mercandalli.android.apps.files.file.image;

import android.app.Application;

import com.mercandalli.android.apps.files.file.FileManager;
import com.mercandalli.android.apps.files.file.audio.FileAudioManager;
import com.mercandalli.android.apps.files.file.audio.FileAudioManagerImpl;
import com.mercandalli.android.apps.files.file.audio.FileAudioPlayer;
import com.mercandalli.android.apps.files.main.FileAppComponent;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A Dagger module used by the {@link FileAppComponent}.
 */
@Module
public class FileImageModule {

    @Provides
    @Singleton
    FileImageManager provideFileImageManager(Application application) {
        return new FileImageManagerImpl(application);
    }
}