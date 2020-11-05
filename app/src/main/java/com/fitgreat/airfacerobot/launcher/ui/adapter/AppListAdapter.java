package com.fitgreat.airfacerobot.launcher.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.AppInfo;
import com.fitgreat.archmvp.base.util.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 应用列表数据适配器<p>
 *
 * @author zixuefei
 * @since 2020/4/28 0028 18:14
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppHolder> {
    private List<AppInfo> appInfoList;
    private Context context;

    public AppListAdapter(List<AppInfo> appInfoList, Context context) {
        this.appInfoList = appInfoList;
        this.context = context;
    }

    @NonNull
    @Override
    public AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false);
        return new AppHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull AppHolder holder, int position) {
        AppInfo appInfo = appInfoList.get(position);
        holder.appIcon.setImageDrawable(appInfo.getIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.itemView.setOnClickListener((View view) -> {
            startApp(appInfo.getPackageName(), appInfo.getActivityName());
        });
    }

    private void startApp(String packageName, String className) {
        LogUtils.d("AppListActivity", "start app :" + packageName + " activity:" + className);
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("AppListActivity", "start app error:" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return appInfoList != null ? appInfoList.size() : 0;
    }


    public class AppHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.app_info_icon)
        ImageView appIcon;
        @BindView(R.id.app_info_name)
        TextView appName;

        public AppHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
