package com.fitgreat.airfacerobot.launcher.ui.adapter;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.RecordInfo;
import com.fitgreat.archmvp.base.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecordAdapter extends BaseQuickAdapter<RecordInfo, BaseViewHolder> {
    private static final String TAG = "RecordAdapter";
    private List<RecordInfo> mData;

    public RecordAdapter(List<RecordInfo> data) {
        super(R.layout.item_record, data);
        this.mData = data;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, RecordInfo recordInfo) {
        if (recordInfo.getType() == 0) {
            baseViewHolder.setImageResource(R.id.image_record, R.mipmap.ic_record_robot);
        }
        if (recordInfo.getType() == 1) {
            baseViewHolder.setImageResource(R.id.image_record, R.mipmap.ic_record_doctor);
        }
        baseViewHolder.setText(R.id.content_record, recordInfo.getContent());
    }

    /**
     * 更新指令记录列表数据源
     */
    public void updateData(RecordInfo recordInfo) {
        LogUtils.json(TAG, "当前添加数据==>" + JSON.toJSONString(recordInfo));
//        if (!judgeHasData(recordInfo)) {
//
//        }
        if (mData.size() >= 4) {
            mData.remove(mData.size() - 1);
        }
        mData.add(0, recordInfo);
        LogUtils.json(TAG, "当前列表数据==>" + JSON.toJSONString(mData));
        notifyDataSetChanged();
    }

    private boolean judgeHasData(RecordInfo mRecordInfo) {
        boolean hasDataTag = false;
        List<RecordInfo> data = getData();
        for (RecordInfo recordInfo : data) {
            if (recordInfo.getContent().equals(mRecordInfo.getContent())) {
                hasDataTag = true;
                break;
            }
        }
        return hasDataTag;
    }
}
