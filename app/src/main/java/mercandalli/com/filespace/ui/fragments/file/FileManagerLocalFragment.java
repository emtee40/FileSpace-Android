/**
 * This file is part of Jarvis for Android, an app for managing your server (files, talks...).
 *
 * Copyright (c) 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 *
 * LICENSE:
 *
 * Jarvis for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Jarvis for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 */
package mercandalli.com.filespace.ui.fragments.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mercandalli.com.filespace.R;
import mercandalli.com.filespace.config.Const;
import mercandalli.com.filespace.listeners.IListener;
import mercandalli.com.filespace.listeners.IModelFileListener;
import mercandalli.com.filespace.listeners.IPostExecuteListener;
import mercandalli.com.filespace.listeners.IStringListener;
import mercandalli.com.filespace.models.ModelFile;
import mercandalli.com.filespace.net.TaskPost;
import mercandalli.com.filespace.ui.activities.Application;
import mercandalli.com.filespace.ui.adapters.AdapterGridModelFile;
import mercandalli.com.filespace.ui.adapters.AdapterModelFile;
import mercandalli.com.filespace.ui.fragments.FabFragment;
import mercandalli.com.filespace.ui.views.DividerItemDecoration;
import mercandalli.com.filespace.utils.FileUtils;
import mercandalli.com.filespace.utils.StringPair;

public class FileManagerLocalFragment extends FabFragment {
	
	private RecyclerView listView;
    private GridView gridView;
    private RecyclerView.LayoutManager mLayoutManager;
	private ArrayList<ModelFile> files;
	private ProgressBar circularProgressBar;
	private File currentDirectory;
	private TextView message;
	private SwipeRefreshLayout swipeRefreshLayout, swipeRefreshLayoutGrid;

    private List<ModelFile> filesToCut = new ArrayList<>();
    private List<ModelFile> filesToCopy = new ArrayList<>();

    private int sortMode = Const.SORT_DATE_MODIFICATION;

    public static FileManagerLocalFragment newInstance() {
        Bundle args = new Bundle();
        FileManagerLocalFragment fragment = new FileManagerLocalFragment();
        fragment.setArguments(args);
        return fragment;
    }

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.app = (Application) activity;
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_file_manager_files, container, false);
        this.circularProgressBar = (ProgressBar) rootView.findViewById(R.id.circularProgressBar);
        this.circularProgressBar.setVisibility(View.INVISIBLE);
        this.message = (TextView) rootView.findViewById(R.id.message);

        this.listView = (RecyclerView) rootView.findViewById(R.id.listView);
        this.listView.setHasFixedSize(true);
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.listView.setLayoutManager(mLayoutManager);
        this.listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        this.gridView = (GridView) rootView.findViewById(R.id.gridView);
        this.gridView.setVisibility(View.GONE);

        this.currentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+this.app.getConfig().getLocalFolderName());
		if(!currentDirectory.exists())
            currentDirectory.mkdir();

        this.swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshList();
			}
		});

        this.swipeRefreshLayoutGrid = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayoutGrid);
        this.swipeRefreshLayoutGrid.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        this.swipeRefreshLayoutGrid.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
		
		refreshList();

        this.app.invalidateOptionsMenu();

        return rootView;
    }

    public void refreshList() {
        refreshList(null);
    }
	
	public void refreshList(final String search) {
		if(currentDirectory==null)
			return;

        List<File> fs = Arrays.asList((search==null) ? currentDirectory.listFiles() : currentDirectory.listFiles(
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().contains(search.toLowerCase());
                }
            }
        ));

        if(sortMode == Const.SORT_ABC) {
            Collections.sort(fs, new Comparator<File>() {
                @Override
                public int compare(final File f1, final File f2) {
                    return String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName());
                }
            });
        }
        else if(sortMode == Const.SORT_SIZE) {
            Collections.sort(fs, new Comparator<File>() {
                @Override
                public int compare(final File f1, final File f2) {
                    return (new Long(f2.length())).compareTo(f1.length());
                }
            });
        }
        else {
            final Map<File, Long> staticLastModifiedTimes = new HashMap<>();
            for(File f : fs) {
                staticLastModifiedTimes.put(f, f.lastModified());
            }
            Collections.sort(fs, new Comparator<File>() {
                @Override
                public int compare(final File f1, final File f2) {
                    return staticLastModifiedTimes.get(f2).compareTo(staticLastModifiedTimes.get(f1));
                }
            });
        }

        files = new ArrayList<>();
        for (File file : fs) {
            ModelFile tmpModelFile = new ModelFile(app, file);
            if(sortMode == Const.SORT_SIZE)
                tmpModelFile.adapterTitleStart = FileUtils.humanReadableByteCount(tmpModelFile.size) + " - ";
            files.add(tmpModelFile);
        }
		
		updateAdapter();		
	}

	public void updateAdapter() {
		if(listView!=null && files!=null && isAdded()) {

            refreshFab();

			if(files.size()==0) {
				message.setText(getString(R.string.no_file_local_folder, ""+currentDirectory.getName()));
				message.setVisibility(View.VISIBLE);
			}
			else
				message.setVisibility(View.GONE);

            final AdapterModelFile adapter = new AdapterModelFile(app, files, new IModelFileListener() {
				@Override
				public void execute(final ModelFile modelFile) {
					final AlertDialog.Builder menuAlert = new AlertDialog.Builder(FileManagerLocalFragment.this.app);
					String[] menuList = { getString(R.string.open_as), getString(R.string.rename), getString(R.string.delete), getString(R.string.copy), getString(R.string.cut), getString(R.string.properties) };
                    if(app.isLogged())
                        menuList = new String[]{ getString(R.string.upload), getString(R.string.open_as), getString(R.string.rename), getString(R.string.delete), getString(R.string.copy), getString(R.string.cut), getString(R.string.properties) };
                    menuAlert.setTitle("Action");
                    menuAlert.setItems(menuList,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int item) {
                                    if(!app.isLogged())
                                        item+=1;
									switch (item) {
                                        case 0:
                                            if(modelFile.directory) {
                                                Toast.makeText(FileManagerLocalFragment.this.app, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                                FileManagerLocalFragment.this.app.alert(getString(R.string.upload), "Upload file " + modelFile.name, getString(R.string.upload), new IListener() {
                                                    @Override
                                                    public void execute() {
                                                        if(modelFile.getFile()!=null) {
                                                            List<StringPair> parameters = modelFile.getForUpload();
                                                            (new TaskPost(app, app.getConfig().getUrlServer()+app.getConfig().routeFile, new IPostExecuteListener() {
                                                                @Override
                                                                public void execute(JSONObject json, String body) {

                                                                }
                                                            }, parameters, modelFile.getFile())).execute();
                                                        }
                                                    }
                                                }, getString(R.string.cancel), null);
                                            break;
                                        case 1:
                                            modelFile.openLocalAs(FileManagerLocalFragment.this.app);
                                            break;
                                        case 2:
                                            FileManagerLocalFragment.this.app.prompt("Rename", "Rename " + (modelFile.directory ? "directory" : "file") + " " + modelFile.name + " ?", "Ok", new IStringListener() {
                                                @Override
                                                public void execute(String text) {
                                                    modelFile.rename(text, new IPostExecuteListener() {
                                                        @Override
                                                        public void execute(JSONObject json, String body) {
                                                            if(filesToCut != null && filesToCut.size() != 0) {
                                                                filesToCut.clear();
                                                                refreshFab();
                                                            }
                                                            if(filesToCopy != null && filesToCopy.size() != 0) {
                                                                filesToCopy.clear();
                                                                refreshFab();
                                                            }
                                                            FileManagerLocalFragment.this.app.refreshAdapters();
                                                        }
                                                    });
                                                }
                                            }, "Cancel", null, modelFile.getNameExt());
                                            break;
                                        case 3:
                                            FileManagerLocalFragment.this.app.alert("Delete", "Delete " + (modelFile.directory ? "directory" : "file") + " " + modelFile.name + " ?", "Yes", new IListener() {
                                                @Override
                                                public void execute() {
                                                    modelFile.delete(new IPostExecuteListener() {
                                                        @Override
                                                        public void execute(JSONObject json, String body) {
                                                            if(filesToCut != null && filesToCut.size() != 0) {
                                                                filesToCut.clear();
                                                                refreshFab();
                                                            }
                                                            if(filesToCopy != null && filesToCopy.size() != 0) {
                                                                filesToCopy.clear();
                                                                refreshFab();
                                                            }
                                                            FileManagerLocalFragment.this.app.refreshAdapters();
                                                        }
                                                    });
                                                }
                                            }, "No", null);
                                            break;
                                        case 4:
                                            FileManagerLocalFragment.this.filesToCopy.add(modelFile);
                                            Toast.makeText(app, "File ready to copy.", Toast.LENGTH_SHORT).show();
                                            refreshFab();
                                            break;
                                        case 5:
                                            FileManagerLocalFragment.this.filesToCut.add(modelFile);
                                            Toast.makeText(app, "File ready to cut.", Toast.LENGTH_SHORT).show();
                                            refreshFab();
                                            break;
                                        case 6:
                                            FileManagerLocalFragment.this.app.alert(
                                                    getString(R.string.properties) + " : " + modelFile.name,
                                                    modelFile.toSpanned(),
                                                    "OK",
                                                    null,
                                                    null,
                                                    null);
                                            break;
									}
								}
							});
					AlertDialog menuDrop = menuAlert.create();
					menuDrop.show();
				}				
			});

            listView.setAdapter(adapter);

            adapter.setOnItemClickListener(new AdapterModelFile.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if(hasItemSelected()) {
                        files.get(position).selected = !files.get(position).selected;
                        adapter.notifyItemChanged(position);
                    }
                    else if (files.get(position).directory) {
                        currentDirectory = new File(files.get(position).url);
                        refreshList();
                    } else
                        files.get(position).executeLocal(files, view);
                }
            });

            adapter.setOnItemLongClickListener(new AdapterModelFile.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(View view, int position) {
                    files.get(position).selected = !files.get(position).selected;
                    adapter.notifyItemChanged(position);
                    return true;
                }
            });


            if(FileManagerFragment.VIEW_MODE == Const.MODE_GRID) {
                this.gridView.setVisibility(View.VISIBLE);
                this.swipeRefreshLayoutGrid.setVisibility(View.VISIBLE);
                this.listView.setVisibility(View.GONE);
                this.swipeRefreshLayout.setVisibility(View.GONE);

                this.gridView.setAdapter(new AdapterGridModelFile(app, files));
                this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(hasItemSelected()) {
                            files.get(position).selected = !files.get(position).selected;
                            adapter.notifyItemChanged(position);
                        }
                        else if (files.get(position).directory) {
                            currentDirectory = new File(files.get(position).url);
                            refreshList();
                        } else
                            files.get(position).executeLocal(files, view);
                    }
                });
                this.gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if(position>=files.size())
                            return false;
                        final ModelFile modelFile = files.get(position);

                        final AlertDialog.Builder menuAlert = new AlertDialog.Builder(FileManagerLocalFragment.this.app);
                        String[] menuList = { getString(R.string.rename), getString(R.string.delete), getString(R.string.copy), getString(R.string.cut), getString(R.string.properties) };
                        if(app.isLogged())
                            menuList = new String[]{ getString(R.string.upload), getString(R.string.rename), getString(R.string.delete), getString(R.string.copy), getString(R.string.cut), getString(R.string.properties) };
                        menuAlert.setTitle("Action");
                        menuAlert.setItems(menuList,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        if(!app.isLogged())
                                            item--;
                                        switch (item) {
                                            case 0:
                                                if(modelFile.directory) {
                                                    Toast.makeText(FileManagerLocalFragment.this.app, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                    FileManagerLocalFragment.this.app.alert(getString(R.string.upload), "Upload file " + modelFile.name, getString(R.string.upload), new IListener() {
                                                        @Override
                                                        public void execute() {
                                                            if(modelFile.getFile()!=null) {
                                                                List<StringPair> parameters = modelFile.getForUpload();
                                                                (new TaskPost(app, app.getConfig().getUrlServer()+app.getConfig().routeFile, new IPostExecuteListener() {
                                                                    @Override
                                                                    public void execute(JSONObject json, String body) {

                                                                    }
                                                                }, parameters, modelFile.getFile())).execute();
                                                            }
                                                        }
                                                    }, getString(R.string.cancel), null);
                                                break;
                                            case 1:
                                                FileManagerLocalFragment.this.app.prompt("Rename", "Rename " + (modelFile.directory ? "directory" : "file") + " " + modelFile.name + " ?", "Ok", new IStringListener() {
                                                    @Override
                                                    public void execute(String text) {
                                                        modelFile.rename(text, new IPostExecuteListener() {
                                                            @Override
                                                            public void execute(JSONObject json, String body) {
                                                                if (filesToCut != null && filesToCut.size() != 0) {
                                                                    filesToCut.clear();
                                                                    refreshFab();
                                                                }
                                                                FileManagerLocalFragment.this.app.refreshAdapters();
                                                            }
                                                        });
                                                    }
                                                }, "Cancel", null, modelFile.getNameExt());
                                                break;
                                            case 2:
                                                FileManagerLocalFragment.this.app.alert("Delete", "Delete " + (modelFile.directory ? "directory" : "file") + " " + modelFile.name + " ?", "Yes", new IListener() {
                                                    @Override
                                                    public void execute() {
                                                        modelFile.delete(new IPostExecuteListener() {
                                                            @Override
                                                            public void execute(JSONObject json, String body) {
                                                                if (filesToCut != null && filesToCut.size() != 0) {
                                                                    filesToCut.clear();
                                                                    refreshFab();
                                                                }
                                                                FileManagerLocalFragment.this.app.refreshAdapters();
                                                            }
                                                        });
                                                    }
                                                }, "No", null);
                                                break;
                                            case 3:
                                                FileManagerLocalFragment.this.filesToCopy.add(modelFile);
                                                Toast.makeText(app, "File ready to copy.", Toast.LENGTH_SHORT).show();
                                                refreshFab();
                                                break;
                                            case 4:
                                                FileManagerLocalFragment.this.filesToCut.add(modelFile);
                                                Toast.makeText(app, "File ready to cut.", Toast.LENGTH_SHORT).show();
                                                refreshFab();
                                                break;
                                            case 5:
                                                FileManagerLocalFragment.this.app.alert(
                                                        getString(R.string.properties) + " : " + modelFile.name,
                                                        "Name : " + modelFile.name + "\nExtension : " + modelFile.type + "\nType : " + modelFile.type.getTitle() + "\nSize : " + FileUtils.humanReadableByteCount(modelFile.size),
                                                        "OK",
                                                        null,
                                                        null,
                                                        null);
                                                break;
                                        }
                                    }
                                });
                        AlertDialog menuDrop = menuAlert.create();
                        menuDrop.show();
                        return false;
                    }
                });
            }
            else {
                this.gridView.setVisibility(View.GONE);
                this.swipeRefreshLayoutGrid.setVisibility(View.GONE);
                this.listView.setVisibility(View.VISIBLE);
                this.swipeRefreshLayout.setVisibility(View.VISIBLE);
            }

			swipeRefreshLayout.setRefreshing(false);
			swipeRefreshLayoutGrid.setRefreshing(false);
		}
	}

    public boolean createFile(String path, String name) {
        int len = path.length();
        if (len < 1 || name.length() < 1)
            return false;
        if (path.charAt(len - 1) != '/')
            path += "/";
        if(!name.contains(".")) {
            if (new File(path + name).mkdir())
                return true;
        }
        else {
            try {
                if (new File(path + name).createNewFile())
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean back() {
        if(hasItemSelected()) {
            deselectAll();
            return true;
        }
        else if(!currentDirectory.getPath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+this.app.getConfig().getLocalFolderName())) {
            if(currentDirectory.getParent() != null) {
                FileManagerLocalFragment.this.currentDirectory = new File(currentDirectory.getParentFile().getPath());
                FileManagerLocalFragment.this.refreshList();
                return true;
            }
        }
        else if((filesToCopy != null && filesToCopy.size() != 0) || (filesToCut != null && filesToCut.size() != 0)) {
            if(filesToCopy != null)
                filesToCopy.clear();
            if(filesToCut != null)
                filesToCut.clear();
            refreshFab();
            return true;
        }
        return false;
    }

    public void goHome() {
        this.currentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + app.getConfig().getLocalFolderName());
        this.refreshList();
    }

    public boolean hasItemSelected() {
        for(ModelFile file:files)
            if(file.selected)
                return true;
        return false;
    }

    public void deselectAll() {
        for(ModelFile file:files)
            file.selected = false;
        updateAdapter();
    }

    @Override
    public void onFocus() { }

    @Override
    public void onFabClick(int fab_id, FloatingActionButton fab) {

        switch (fab_id) {
            case 0:
                if( (filesToCopy != null && filesToCopy.size() != 0) || (filesToCut != null && filesToCut.size() != 0)){
                    if(filesToCopy != null) {
                        for (ModelFile file : filesToCopy) {
                            file.copyFile(currentDirectory.getAbsolutePath() + File.separator);
                        }
                        filesToCopy.clear();
                    }
                    if(filesToCut != null) {
                        for (ModelFile file : filesToCut) {
                            file.renameLocalByPath(currentDirectory.getAbsolutePath() + File.separator + file.getNameExt());
                        }
                        filesToCut.clear();
                    }
                    refreshList();
                } else {
                    final AlertDialog.Builder menuAlert = new AlertDialog.Builder(FileManagerLocalFragment.this.app);
                    final String[] menuList = {"New Folder or File"};
                    menuAlert.setTitle("Action");
                    menuAlert.setItems(menuList,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    switch (item) {
                                        case 0:
                                            FileManagerLocalFragment.this.app.prompt("New Folder or File", "Choose a file name with ext or a folder name.", getString(R.string.ok), new IStringListener() {
                                                @Override
                                                public void execute(String text) {
                                                    createFile(currentDirectory.getPath() + File.separator, text);
                                                    refreshList();
                                                }
                                            }, getString(R.string.cancel), null, null, "Name");
                                            break;
                                    }
                                }
                            });
                    AlertDialog menuDrop = menuAlert.create();
                    menuDrop.show();
                }
                refreshFab();
                break;

            case 1:
                if(currentDirectory.getParent() != null) {
                    FileManagerLocalFragment.this.currentDirectory = new File(currentDirectory.getParentFile().getPath());
                    //Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+FileManagerFragmentLocal.this.app.getConfig().localFolderName);
                    FileManagerLocalFragment.this.refreshList();
                }
                break;
        }
    }

    @Override
    public boolean isFabVisible(int fab_id) {
        switch (fab_id) {
            case 0:
                return true;
            case 1:
                return this.currentDirectory != null && currentDirectory.getParent() != null;
        }
        return false;
    }

    @Override
    public Drawable getFabDrawable(int fab_id) {
        switch (fab_id) {
            case 0:
                if(filesToCopy != null && filesToCopy.size() != 0)
                    return app.getDrawable(R.drawable.ic_menu_paste_holo_dark);
                else if(filesToCut != null && filesToCut.size() != 0)
                    return app.getDrawable(R.drawable.ic_menu_paste_holo_dark);
                else
                    return app.getDrawable(R.drawable.add);
            case 1:
                return app.getDrawable(R.drawable.arrow_up);
        }
        return app.getDrawable(R.drawable.add);
    }

    public void setSort(int mode) {
        if(mode == Const.SORT_ABC ||
                mode == Const.SORT_DATE_MODIFICATION ||
                mode == Const.SORT_SIZE) {
            this.sortMode = mode;
            refreshList();
        }
    }
}
