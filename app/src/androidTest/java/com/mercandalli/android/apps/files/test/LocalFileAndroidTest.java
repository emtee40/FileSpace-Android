package com.mercandalli.android.apps.files.test;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.EditText;

import com.mercandalli.android.apps.files.R;
import com.mercandalli.android.apps.files.core.view.RecyclerViewUtils;
import com.mercandalli.android.apps.files.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static com.mercandalli.android.apps.files.core.TestApp.resetApp;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.clickWaitNewWindow;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.clickWaitNewWindowContainsText;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.finish;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.getActivity;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.getCurrentDateString;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.getDevice;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.getResources;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.swipeDownById;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.swipeUpById;
import static com.mercandalli.android.apps.files.core.UiAutomatorLib.takeScreenShot;
import static com.mercandalli.android.apps.files.core.view.DrawerLayoutUtils.actionCloseDrawer;

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class LocalFileAndroidTest {

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            resetApp(InstrumentationRegistry.getTargetContext());
        }
    };

    @Before
    public void closeNavDrawer() {
        onView(ViewMatchers.withId(R.id.activity_main_drawer_layout)).perform(actionCloseDrawer());
    }

    @Test
    public void openFolderAndGoUp() throws UiObjectNotFoundException {
        final Activity activity = getActivity();

        takeScreenShot(activity, "openFolderAndGoUp-start");

        // Select the first file
        RecyclerViewUtils.clickRecyclerAt(R.id.fragment_file_files_recycler_view, 0);
        takeScreenShot(activity, "openFolderAndGoUp-1");

        // Go up.
        clickWaitNewWindow(R.id.fragment_file_fab_2);
        takeScreenShot(activity, "openFolderAndGoUp-2");

        // Swipe up.
        swipeUpById(R.id.fragment_file_files_recycler_view, 20);
        takeScreenShot(activity, "openFolderAndGoUp-3");

        // Swipe up.
        swipeDownById(R.id.fragment_file_files_recycler_view, 20);
        takeScreenShot(activity, "openFolderAndGoUp-4");

        // Select the second file
        getDevice().findObject(new UiSelector()
                .resourceId(getResources().getResourceName(R.id.fragment_file_files_recycler_view)))
                .getChild(new UiSelector().clickable(true).index(2))
                .clickAndWaitForNewWindow(400);

        takeScreenShot(activity, "openFolderAndGoUp-end");
        finish(MainActivity.class);
    }

    @Test
    public void createAndDeleteFolder() throws UiObjectNotFoundException {
        takeScreenShot("createAndDeleteFolder-start");

        final Activity activity = getActivity();
        final String folderName = "_" + getCurrentDateString();
        takeScreenShot(activity, "createAndDeleteFolder-1");

        // Create folder.
        clickWaitNewWindow(R.id.fragment_file_fab_1);
        clickWaitNewWindow("android:id/text1");
        getDevice().findObject(new UiSelector().className(EditText.class)).setText(folderName);
        clickWaitNewWindow("android:id/button1");
        takeScreenShot(activity, "createAndDeleteFolder-2");

        // Delete
        getDevice().findObject(new UiSelector().descriptionContains("overflow#" + folderName)).click();
        clickWaitNewWindowContainsText(R.string.delete);
        clickWaitNewWindow("android:id/button1");

        takeScreenShot("createAndDeleteFolder-end");
        finish(MainActivity.class);
    }

    @Test
    public void scrollLocalFilesAndClick() {
        takeScreenShot("scrollLocalFilesAndClick-start");

        // Find obj.
        final ViewInteraction viewInteraction = onView(ViewMatchers.withId(R.id.fragment_file_files_recycler_view));

        // Scroll recyclerView.
        viewInteraction.perform(swipeUp());
        takeScreenShot("scrollLocalFilesAndClick-1");
        viewInteraction.perform(swipeDown());
        takeScreenShot("scrollLocalFilesAndClick-2");

        onView(ViewMatchers.withId(R.id.fragment_file_files_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        takeScreenShot("scrollLocalFilesAndClick-end");
        finish(MainActivity.class);
    }
}