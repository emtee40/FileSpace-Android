package com.mercandalli.android.apps.files.file.audio;

import android.app.Application;

import com.mercandalli.android.apps.files.file.FileManager;
import com.mercandalli.android.apps.files.file.local.provider.FileLocalProviderManager;

/**
 * Created by Jonathan on 02/04/2016.
 */
public class FileAudioModuleTest extends FileAudioModule {

    @Override
    FileAudioManager provideFileAudioManager(
            final Application application,
            final FileLocalProviderManager fileLocalProviderManager,
            final FileManager fileManager) {
        return new FileAudioManagerTest(application, fileLocalProviderManager, fileManager);
    }

    @Override
    FileAudioPlayer provideFileAudioPlayer(Application application) {
        return new FileAudioPlayer(application);
    }
}
