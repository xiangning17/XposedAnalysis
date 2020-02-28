package com.xiangning.methodtrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xiangning.sectionadapter.SectionAdapter;
import com.xiangning.sectionadapter.binder.GeneralViewHolder;
import com.xiangning.sectionadapter.binder.SimpleItemBinder;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private RecyclerView mRvTrackedList;

    private SectionAdapter.Section mSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mRvTrackedList = findViewById(R.id.rv_tracked_list);
        mRvTrackedList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvTrackedList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        SectionAdapter sectionAdapter = new SectionAdapter();
        mSection = sectionAdapter.register(TrackedMethod.class,
            new SimpleItemBinder<TrackedMethod>(R.layout.item_tracked_list, null) {
                @Override
                public void onBindViewHolder(GeneralViewHolder holder, final TrackedMethod item) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editTrackedMethod(item);
                        }
                    });

                    ((TextView) holder.get(R.id.tv_title)).setText(item.getPackageName());
                    ((TextView) holder.get(R.id.tv_detail)).setText(String.format("%s#%s", item.getClassName(), item.toMethodString()));
                }
            });

        mRvTrackedList.setAdapter(sectionAdapter);

        AndPermission.with(this)
            .runtime()
            .permission(new String[][]{Permission.Group.STORAGE})
            .onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    Log.e(TAG, "external storage permission denied!!!");
                }
            })
            .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSection.setItems(TrackedManager.getInstance(this).getTrackedList());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                editTrackedMethod(null);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * 编辑一条TrackedMethod的记录
     * @param method 为null时相当于新建
     */
    private void editTrackedMethod(TrackedMethod method) {
        Intent intent = new Intent(this, TrackedEditActivity.class);
        intent.putExtra(TrackedEditActivity.EXTRA_PARCELABLE_TRACKED_METHOD, method);
        startActivity(intent);
    }

}
