/**
 * Personal Project : Control server
 *
 * MERCANDALLI Jonathan
 */

package com.mercandalli.jarvis.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mercandalli.jarvis.Application;
import com.mercandalli.jarvis.R;
import com.mercandalli.jarvis.adapter.AdapterModelFile;
import com.mercandalli.jarvis.dialog.DialogUpload;
import com.mercandalli.jarvis.listener.IListener;
import com.mercandalli.jarvis.listener.IModelFileListener;
import com.mercandalli.jarvis.listener.IPostExecuteListener;
import com.mercandalli.jarvis.model.ModelFile;
import com.mercandalli.jarvis.net.TaskGet;

public class FileManagerFragmentOnline extends Fragment {

	Application app;
	ListView listView;
	List<ModelFile> listModelFile;
	ProgressBar circulerProgressBar;
	TextView message;
	SwipeRefreshLayout swipeRefreshLayout;

	public FileManagerFragmentOnline(Application app) {
		this.app = app;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_filemanager_online,
				container, false);
		circulerProgressBar = (ProgressBar) rootView
				.findViewById(R.id.circulerProgressBar);
		message = (TextView) rootView.findViewById(R.id.message);
		listView = (ListView) rootView.findViewById(R.id.listView);

		refreshList();

		swipeRefreshLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshList();
			}
		});

		((ImageView) rootView.findViewById(R.id.circle))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						app.dialog = new DialogUpload(app,
								new IPostExecuteListener() {
									@Override
									public void execute(JSONObject json,
											String body) {
										if (json != null)
											refreshList();
									}
								});
					}
				});

		return rootView;
	}

	public void refreshList() {

		new TaskGet(app, this.app.config.getUrlServer()
				+ this.app.config.routeFile, new IPostExecuteListener() {
			@Override
			public void execute(JSONObject json, String body) {
				listModelFile = new ArrayList<ModelFile>();
				try {
					if (json != null) {
						if (json.has("result")) {
							JSONArray array = json.getJSONArray("result");
							for (int i = 0; i < array.length(); i++) {
								ModelFile modelFile = new ModelFile(app,
										array.getJSONObject(i));
								listModelFile.add(modelFile);
							}
							circulerProgressBar.setVisibility(View.INVISIBLE);
						}
					} else
						Toast.makeText(app,
								app.getString(R.string.action_failed),
								Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				updateAdapter();
			}
		}, null).execute();

	}

	public void updateAdapter() {
		if(listView!=null && listModelFile!=null) {
			
			if(listModelFile.size()==0) {
				message.setText(getString(R.string.no_file_server));
				message.setVisibility(View.VISIBLE);
			}
			else
				message.setVisibility(View.GONE);
			
			listView.setAdapter(new AdapterModelFile(app, R.layout.tab_file, listModelFile, null, new IModelFileListener() {
				@Override
				public void execute(final ModelFile modelFile) {
					final AlertDialog.Builder menuAleart = new AlertDialog.Builder(FileManagerFragmentOnline.this.app);
					final String[] menuList = { "Download", "Delete" };
					menuAleart.setTitle("Action");
					menuAleart.setItems(menuList,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog, int item) {
									switch (item) {
									case 0:
										modelFile.download(new IListener() {
											@Override
											public void execute() {
												Toast.makeText(app, "Download finished.", Toast.LENGTH_SHORT).show();
												FileManagerFragmentOnline.this.app.updateAdapters();
											}											
										});
										break;
									case 1:
										FileManagerFragmentOnline.this.app.alert("Download", "Delete file ?", "Yes", new IListener() {			
											@Override
											public void execute() {
												modelFile.delete(new IPostExecuteListener() {
													@Override
													public void execute(JSONObject json, String body) {
														FileManagerFragmentOnline.this.app.refreshAdapters();
													}													
												});
											}
										}, "No", null);
										break;
									}
								}
							});
					AlertDialog menuDrop = menuAleart.create();
					menuDrop.show();
					
				}				
			}));
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					listModelFile.get(position).executeOnline();
				}
			});
			
			swipeRefreshLayout.setRefreshing(false);
		}
	}
}