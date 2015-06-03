package com.otter.otterbenchmark;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtterBenchmark extends AppCompatActivity {
    private static final String ACTION_TEST_CODE = "com.otter.otterbenchmark.TEST_CODE";

    private static final String KEY_TITLE = "title";
    private static final String KEY_INTENT = "intent";

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPackageManager = getPackageManager();

        setContentView(R.layout.activity_otter_benchmark);
        ListView test_item_list = (ListView) findViewById(R.id.test_item_list);

        // Initial adapter.
        SimpleAdapter adapter = new SimpleAdapter(
                getApplicationContext(),
                getTestItemList(),
                android.R.layout.simple_list_item_1,
                new String[]{KEY_TITLE}, new int[]{android.R.id.text1});

        // Set adapter to ListView.
        test_item_list.setAdapter(adapter);
        test_item_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> activity =
                        (Map<String, Object>) parent.getItemAtPosition(position);

                Intent intent = (Intent) activity.get(KEY_INTENT);
                startActivity(intent);
            }
        });
    }

    private List<Map<String, Object>> getTestItemList() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        // Search test activities.
        Intent demoIntent = new Intent(ACTION_TEST_CODE);
        List<ResolveInfo> activities = mPackageManager.queryIntentActivities(demoIntent, 0);
        if (activities == null || activities.size() < 1) {
            return list;
        }

        // Add activity name and intent to list.
        for (ResolveInfo info : activities) {
            Map<String, Object> activity = new HashMap<String, Object>();
            activity.put(KEY_TITLE, getActivityTitle(info));
            activity.put(KEY_INTENT, getActivityIntent(info));
            list.add(activity);
        }

        Collections.sort(list, sTitleComparator);

        return list;
    }

    private String getActivityTitle(ResolveInfo info) {
        CharSequence labelSeq = info.loadLabel(mPackageManager);
        String lable = labelSeq != null ? labelSeq.toString() : info.activityInfo.name;

        // Remove the suffix "Test".
        int index = lable.lastIndexOf("Test");
        if (index > 0) {
            lable = lable.substring(0, index);
        }

        return lable;
    }

    private Intent getActivityIntent(ResolveInfo info) {
        Intent intent = new Intent();
        intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        return intent;
    }

    private static final Comparator<Map<String, Object>> sTitleComparator =
            new Comparator<Map<String, Object>>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            return collator.compare(map1.get(KEY_TITLE), map2.get(KEY_TITLE));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_otter_benchmark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
