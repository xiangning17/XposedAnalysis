package com.xiangning.methodtrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TrackedEditActivity extends AppCompatActivity {

    public static final String EXTRA_PARCELABLE_TRACKED_METHOD = "tracked_method";

    private TrackedMethod mTrackedMethod;
    private int mTrackedIndex;

    private ScrollView mScrollView;

    private AutoCompleteTextView mActvPkg;
    private AutoCompleteTextView mActvCls;
    private AutoCompleteTextView mActvMethod;

    private LinearLayout mContainerParamTypes;
    private Button mBtnAddParamType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackedMethod = getIntent().getParcelableExtra(EXTRA_PARCELABLE_TRACKED_METHOD);
        mTrackedIndex = TrackedManager.getInstance(this).getTrackedList().indexOf(mTrackedMethod);

        setContentView(R.layout.activity_tracked_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mScrollView = findViewById(R.id.scrollView);
        mActvPkg = findViewById(R.id.actv_pkg);
        mActvPkg.setThreshold(2);
        mActvPkg.setAdapter(new ArrayAdapter<String>(this, R.layout.item_auto_complete_drop_down, R.id.tv) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> apps = MethodSearchHelper.findPackages(TrackedEditActivity.this, constraint.toString());
                        results.count = apps.size();
                        results.values = apps;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results.count > 0) {
                            clear();
                            addAll((List<String>) results.values);
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }
                };
            }
        });


        mActvCls = findViewById(R.id.actv_cls);
        mActvCls.setAdapter(new ArrayAdapter<String>(this, R.layout.item_auto_complete_drop_down, R.id.tv) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> classes = MethodSearchHelper.findClasses(TrackedEditActivity.this,
                            mActvPkg.getText().toString(), constraint.toString());
                        results.count = classes.size();
                        results.values = classes;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results.count > 0) {
                            clear();
                            addAll((List<String>) results.values);
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }
                };
            }
        });

        mActvMethod = findViewById(R.id.actv_method);
        mActvMethod.setThreshold(1);
        mActvMethod.setAdapter(new ArrayAdapter<Method>(this, R.layout.item_auto_complete_drop_down, R.id.tv) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<Method> classes = MethodSearchHelper.findMethods(mActvPkg.getText().toString(),
                            mActvCls.getText().toString(),
                            constraint.toString());
                        results.count = classes.size();
                        results.values = classes;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results.count > 0) {
                            clear();
                            addAll((List<Method>) results.values);
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }
                };
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return handleView(super.getView(position, convertView, parent), position);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return handleView(super.getDropDownView(position, convertView, parent), position);
            }

            private View handleView(View view, int position) {
                final Method method = getItem(position);
                StringBuilder builder = new StringBuilder(method.getName());
                builder.append("(");
                if (method.getParameterTypes().length > 0) {
                    for (Class<?> parameterType : method.getParameterTypes()) {
                        builder.append(parameterType.getName()).append(", ");
                    }
                    builder.setLength(builder.length() - 2);
                }
                builder.append(")");

                TextView tv = view.findViewById(R.id.tv);
                tv.setText(builder);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActvMethod.setText(method.getName(), false);
                        notifyDataSetInvalidated();
                        removeAllParamTypeViews();
                        for (Class<?> parameterType : method.getParameterTypes()) {
                            addParamTypeView(parameterType.getName());
                        }
                    }
                });

                return view;
            }
        });

        mContainerParamTypes = findViewById(R.id.container_param_types);
        mBtnAddParamType = findViewById(R.id.btn_add_param_type);
        mBtnAddParamType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addParamTypeView(null);
            }
        });

        if (mTrackedMethod != null) {
            mActvPkg.setText(mTrackedMethod.getPackageName());
            mActvCls.setText(mTrackedMethod.getClassName());
            mActvMethod.setText(mTrackedMethod.getMethodName());
            if (mTrackedMethod.getMethodParamTypes() != null) {
                if (mTrackedMethod.getMethodParamTypes().length == 0) {
                    addParamTypeView(null);
                } else {
                    for (String type : mTrackedMethod.getMethodParamTypes()) {
                        addParamTypeView(type);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tracked_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_delete:
                TrackedManager.getInstance(this).removeTrackedMethod(mTrackedMethod);
                finish();
                break;
            case R.id.action_save:
                TrackedMethod trackedMethod = mTrackedMethod == null ? new TrackedMethod() : mTrackedMethod;
                trackedMethod.setPackageName(mActvPkg.getText().toString().trim());
                trackedMethod.setClassName(mActvCls.getText().toString().trim());
                trackedMethod.setMethodName(mActvMethod.getText().toString().trim());
                List<String> typeList = new ArrayList<>(mContainerParamTypes.getChildCount());
                for (int i = 0; i < mContainerParamTypes.getChildCount(); i++) {
                    AutoCompleteTextView tv = mContainerParamTypes.getChildAt(i).findViewById(R.id.avtv_type);
                    if (!TextUtils.isEmpty(tv.getText().toString().trim())) {
                        typeList.add(tv.getText().toString().trim());
                    }
                }
                trackedMethod.setMethodParamTypes(mContainerParamTypes.getChildCount() > 0 ? typeList.toArray(new String[0]) : null);
                if (mTrackedIndex != -1) {
                    TrackedManager.getInstance(this).updateTrackedMethod(mTrackedIndex, trackedMethod);
                } else {
                    TrackedManager.getInstance(this).addTrackedMethod(trackedMethod);
                }
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void addParamTypeView(String type) {
        final View view = LayoutInflater.from(this).inflate(R.layout.item_param_type, mContainerParamTypes, false);
        final AutoCompleteTextView tv = view.findViewById(R.id.avtv_type);
        tv.setText(type);
        view.findViewById(R.id.iv_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContainerParamTypes.removeView(view);
            }
        });
        view.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
                tv.requestFocus();
            }
        });
        mContainerParamTypes.addView(view);
    }

    private void removeAllParamTypeViews() {
        mContainerParamTypes.removeAllViews();
    }
}
