package com.fitgreat.airfacerobot.commonproblem.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemFirstNode;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemSecondNode;
import com.fitgreat.airfacerobot.commonproblem.provider.CommonProblemNodeFirstProvider;
import com.fitgreat.airfacerobot.commonproblem.provider.CommonProblemNodeSecondProvider;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommonProblemNodeAdapter extends BaseNodeAdapter {
    private Context mContext;

    public CommonProblemNodeAdapter(Context context) {
        super();
        this.mContext = context;
        addNodeProvider(new CommonProblemNodeFirstProvider(mContext));
        addNodeProvider(new CommonProblemNodeSecondProvider());
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> list, int i) {
        BaseNode baseNode = list.get(i);
        if (baseNode instanceof CommonProblemFirstNode) {
            return 1;
        } else if (baseNode instanceof CommonProblemSecondNode) {
            return 2;
        }
        return -1;
    }

    public static final int EXPAND_COLLAPSE_PAYLOAD = 110;
}
