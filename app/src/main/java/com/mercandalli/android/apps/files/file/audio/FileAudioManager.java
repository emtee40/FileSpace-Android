package com.mercandalli.android.apps.files.file.audio;

import android.content.Context;
import android.text.Spanned;

import com.mercandalli.android.apps.files.file.FileModel;

import java.io.File;
import java.util.List;

/**
 * The {@link FileModel} Manager abstract class.
 */
public abstract class FileAudioManager {

    /**
     * Get all the {@link FileAudioModel} in the device.
     */
    public abstract void getAllLocalMusic(final int sortMode, final String search);

    /**
     * Get all the {@link FileAudioModel} in a folder.
     */
    public abstract void getLocalMusic(final FileModel fileModelDirectParent, final int sortMode, final String search);

    /**
     * Get all local folders that contain music.
     */
    public abstract void getLocalMusicFolders(final int sortMode, final String search);

    /**
     * Edit the metadata.
     */
    public abstract boolean setFileAudioMetaData(
            final File fileAudio,
            final String newTitle,
            final String newArtist,
            final String newAlbum);

    /**
     * Edit the metadata.
     */
    @SuppressWarnings("unused")
    public abstract boolean setFileAudioMetaData(
            final FileAudioModel fileAudio,
            final String newTitle,
            final String newArtist,
            final String newAlbum);

    /**
     * Get the {@link FileAudioModel} overview.
     */
    public abstract Spanned toSpanned(final Context context, final FileAudioModel fileAudioModel);

    public abstract boolean registerAllLocalMusicListener(GetAllLocalMusicListener getAllLocalMusicListener);

    public abstract boolean unregisterAllLocalMusicListener(GetAllLocalMusicListener getAllLocalMusicListener);

    public abstract boolean registerLocalMusicFoldersListener(GetLocalMusicFoldersListener getLocalImageFoldersListener);

    public abstract boolean unregisterLocalMusicFoldersListener(GetLocalMusicFoldersListener getLocalImageFoldersListener);

    public abstract boolean registerLocalMusicListener(GetLocalMusicListener getLocalImageListener);

    public abstract boolean unregisterLocalMusicListener(GetLocalMusicListener getLocalImageListener);

    public abstract boolean registerOnMusicUpdateListener(MusicsChangeListener musicsChangeListener);

    public abstract boolean unregisterOnMusicUpdateListener(MusicsChangeListener musicsChangeListener);

    /**
     * Class used to count.
     * See {@link #getLocalMusicFolders(int, String)}.
     * http://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java
     * Used to count with a map.
     */
    protected class MutableInt {
        int value = 1; // note that we start at 1 since we're counting

        public void increment() {
            ++value;
        }
    }

    interface GetAllLocalMusicListener {

        /**
         * Called when the call of {@link #getAllLocalMusic(int, String)} succeeded.
         *
         * @param fileModels the {@link List} of result.
         */
        void onAllLocalMusicSucceeded(List<FileAudioModel> fileModels);

        void onAllLocalMusicFailed();
    }

    interface GetLocalMusicFoldersListener {

        /**
         * Called when the call of {@link #getLocalMusicFolders(int, String)} succeeded.
         *
         * @param fileModels the {@link List} of result.
         */
        void onLocalMusicFoldersSucceeded(List<FileModel> fileModels);

        void onLocalMusicFoldersFailed();
    }

    interface GetLocalMusicListener {

        /**
         * Called when the call of {@link #getLocalMusic(FileModel, int, String)} succeeded.
         *
         * @param fileModels the {@link List} of result.
         */
        void onLocalMusicSucceeded(List<FileAudioModel> fileModels);

        void onLocalMusicFailed();
    }

    interface MusicsChangeListener {

        /**
         * At least one music on the device change.
         */
        void onMusicsContentChange();
    }
}
