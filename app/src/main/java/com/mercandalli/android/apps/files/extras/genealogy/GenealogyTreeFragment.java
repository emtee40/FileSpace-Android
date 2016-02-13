/**
 * This file is part of FileSpace for Android, an app for managing your server (files, talks...).
 * <p/>
 * Copyright (c) 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 * <p/>
 * LICENSE:
 * <p/>
 * FileSpace for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p/>
 * FileSpace for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 */
package com.mercandalli.android.apps.files.extras.genealogy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mercandalli.android.apps.files.R;
import com.mercandalli.android.apps.files.common.fragment.FabFragment;
import com.mercandalli.android.apps.files.common.listener.IPostExecuteListener;
import com.mercandalli.android.apps.files.common.net.TaskGet;
import com.mercandalli.android.apps.files.main.Constants;
import com.mercandalli.android.apps.files.main.network.NetUtils;
import com.mercandalli.android.apps.files.common.util.StringPair;
import com.mercandalli.android.apps.files.common.util.StringUtils;
import com.mercandalli.android.apps.files.common.view.divider.DividerItemDecoration;
import com.mercandalli.android.apps.files.main.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan on 28/08/2015.
 */
public class GenealogyTreeFragment extends FabFragment {

    private static final String RESULT = "result";
    private View rootView;

    private ModelGenealogyPerson genealogyPerson = null;
    private boolean requestReady = true;

    private EditText et_user, et_user_description, et_father, et_mother, et_father_description, et_mother_description;
    private Switch switch_brothers_sisters_marriages;
    private TextView tv_brothers_sisters_marriages;

    private List<ModelGenealogyPerson> list;
    private RecyclerView recyclerView;
    private AdapterModelGenealogyUser mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static GenealogyTreeFragment newInstance() {
        return new GenealogyTreeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_genealogy_tree, container, false);

        this.et_user = (EditText) this.rootView.findViewById(R.id.user);
        this.et_user_description = (EditText) this.rootView.findViewById(R.id.user_description);
        this.et_father = (EditText) this.rootView.findViewById(R.id.et_father);
        this.et_father_description = (EditText) this.rootView.findViewById(R.id.et_father_description);
        this.et_mother = (EditText) this.rootView.findViewById(R.id.et_mother);
        this.et_mother_description = (EditText) this.rootView.findViewById(R.id.et_mother_description);

        this.tv_brothers_sisters_marriages = (TextView) this.rootView.findViewById(R.id.tv_brothers_sisters_marriages);
        this.tv_brothers_sisters_marriages.setVisibility(View.INVISIBLE);
        this.tv_brothers_sisters_marriages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_brothers_sisters_marriages.setChecked(!switch_brothers_sisters_marriages.isChecked());
            }
        });
        this.switch_brothers_sisters_marriages = (Switch) this.rootView.findViewById(R.id.switch_brothers_sisters_marriages);
        this.switch_brothers_sisters_marriages.setVisibility(View.INVISIBLE);
        this.switch_brothers_sisters_marriages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tv_brothers_sisters_marriages.setText(isChecked ? "Marriages" : "Brothers & Sisters");
                refreshList();
            }
        });

        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.listView);
        this.recyclerView.setHasFixedSize(true);
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(mLayoutManager);
        this.recyclerView.setItemAnimator(/*new SlideInFromLeftItemAnimator(mRecyclerView)*/new DefaultItemAnimator());
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        refreshList();

        return rootView;
    }

    public void getChildren(int id_user) {
        List<StringPair> parameters = null;
        if (NetUtils.isInternetConnection(getContext()) && mApplicationCallback.isLogged()) {
            if (requestReady) {
                requestReady = false;
                new TaskGet(
                        getActivity(),
                        Constants.URL_DOMAIN + Config.routeGenealogyChildren + "/" + id_user,
                        new IPostExecuteListener() {
                            @Override
                            public void onPostExecute(JSONObject json, String body) {
                                requestReady = true;
                                List<ModelGenealogyPerson> listChildren = new ArrayList<>();
                                try {
                                    if (json != null) {
                                        if (json.has(RESULT)) {
                                            JSONArray array = json.getJSONArray(RESULT);
                                            for (int i = 0; i < array.length(); i++) {
                                                listChildren.add(new ModelGenealogyPerson(getActivity(), mApplicationCallback, array.getJSONObject(i)));
                                            }
                                        }
                                    } else {
                                        Toast.makeText(getContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Log.e(getClass().getName(), "Failed to convert Json", e);
                                }
                                if (listChildren.size() != 0) {
                                    genealogyPerson = listChildren.get(0);
                                    genealogyPerson.selected = true;
                                } else {
                                    Toast.makeText(getContext(), "No children", Toast.LENGTH_SHORT).show();
                                }
                                update();
                            }
                        },
                        parameters
                ).execute();
            } else {
                Toast.makeText(getContext(), getString(R.string.waiting_for_response), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public void changeUser(int id_user) {

        List<StringPair> parameters = null;
        if (NetUtils.isInternetConnection(getContext()) && mApplicationCallback.isLogged()) {
            if (requestReady) {
                requestReady = false;
                new TaskGet(
                        getActivity(),
                        Constants.URL_DOMAIN + Config.routeGenealogy + "/" + id_user,
                        new IPostExecuteListener() {
                            @Override
                            public void onPostExecute(JSONObject json, String body) {
                                requestReady = true;
                                try {
                                    if (json != null) {
                                        if (json.has(RESULT)) {
                                            GenealogyTreeFragment.this.genealogyPerson = new ModelGenealogyPerson(getActivity(), mApplicationCallback, json.getJSONObject(RESULT));
                                            GenealogyTreeFragment.this.genealogyPerson.selected = true;
                                        }
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.action_failed), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Log.e(getClass().getName(), "Failed to convert Json", e);
                                }
                                update();
                            }
                        },
                        parameters
                ).execute();
            } else {
                Toast.makeText(getContext(), R.string.waiting_for_response, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public void update() {
        this.et_user.setText("");
        this.et_user_description.setText("");
        this.et_father.setText("");
        this.et_mother.setText("");
        this.et_father_description.setText("");
        this.et_mother_description.setText("");

        if (genealogyPerson != null && genealogyPerson.selected) {
            this.et_user.setText(genealogyPerson.getAdapterTitle());
            this.et_user.setTextColor(Color.parseColor(genealogyPerson.is_man ? "#1976D2" : "#E91E63"));
            this.et_user.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (genealogyPerson != null) {
                        genealogyPerson.modify(new IPostExecuteListener() {
                            @Override
                            public void onPostExecute(JSONObject json, String body) {
                                update();
                            }
                        });
                    }
                    return false;
                }
            });
            this.et_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (genealogyPerson != null) {
                        getChildren(genealogyPerson.id);
                    }
                }
            });

            this.et_user_description.setText(genealogyPerson.getAdapterSubtitle() + (StringUtils.isNullOrEmpty(genealogyPerson.description) ? "" : ("\n" + genealogyPerson.description)));

            if (genealogyPerson.father != null) {
                this.et_father.setText(genealogyPerson.father.getAdapterTitle());
                this.et_father_description.setText(genealogyPerson.father.getAdapterSubtitle());
                this.et_father.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (genealogyPerson.father != null) {
                            genealogyPerson.father.modify(new IPostExecuteListener() {
                                @Override
                                public void onPostExecute(JSONObject json, String body) {
                                    update();
                                }
                            });
                        }
                        return false;
                    }
                });
                this.et_father.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (genealogyPerson.father != null) {
                            changeUser(genealogyPerson.id_father);
                        }
                    }
                });
            }
            if (genealogyPerson.mother != null) {
                this.et_mother.setText(genealogyPerson.mother.getAdapterTitle());
                this.et_mother_description.setText(genealogyPerson.mother.getAdapterSubtitle());
                this.et_mother.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (genealogyPerson.mother != null) {
                            genealogyPerson.mother.modify(new IPostExecuteListener() {
                                @Override
                                public void onPostExecute(JSONObject json, String body) {
                                    update();
                                }
                            });
                        }
                        return false;
                    }
                });
                this.et_mother.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (genealogyPerson.mother != null) {
                            changeUser(genealogyPerson.id_mother);
                        }
                    }
                });
            }
        }
        refreshList();
    }

    public void refreshList() {
        if (genealogyPerson != null) {
            if (switch_brothers_sisters_marriages.isChecked()) {
                this.list = genealogyPerson.getPartners();
            } else {
                this.list = genealogyPerson.getBrothersSisters();
            }
        } else {
            this.list = new ArrayList<>();
        }
        updateAdapter();
    }

    public void updateAdapter() {
        if (this.recyclerView != null && this.list != null && this.isAdded()) {

            this.mAdapter = new AdapterModelGenealogyUser(getActivity(), list, new IModelGenealogyUserListener() {
                @Override
                public void execute(final ModelGenealogyPerson modelGenealogyUser) {

                }
            }, true);
            this.recyclerView.setAdapter(mAdapter);

            this.mAdapter.setOnItemClickListener(new AdapterModelGenealogyUser.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    changeUser(list.get(position).id);
                }
            });

            this.mAdapter.setOnItemLongClickListener(new AdapterModelGenealogyUser.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(View view, int position) {
                    return false;
                }
            });
        }

        int listVisible = genealogyPerson == null ? View.INVISIBLE : View.VISIBLE;
        if (genealogyPerson != null && genealogyPerson.getPartners().size() == 0 && genealogyPerson.getBrothersSisters().size() == 0) {
            listVisible = View.INVISIBLE;
        }
        this.tv_brothers_sisters_marriages.setVisibility(listVisible);
        this.switch_brothers_sisters_marriages.setVisibility(listVisible);
    }

    @Override
    public boolean back() {
        return false;
    }

    @Override
    public void onFocus() {
        update();
    }

    public void select(ModelGenealogyPerson genealogyUser_) {
        genealogyPerson = genealogyUser_;
    }

    @Override
    public void onFabClick(int fab_id, FloatingActionButton fab) {

    }

    @Override
    public boolean isFabVisible(int fab_id) {
        return false;
    }

    @Override
    public int getFabImageResource(int fab_id) {
        return -1;
    }
}
