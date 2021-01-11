package com.fitgreat.airfacerobot.commonproblem.node;


import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class CommonProblemFirstNode extends BaseExpandNode {
    private List<BaseNode> childNode;
    private CommonProblemEntity mCommonProblemEntity;

    public CommonProblemFirstNode(List<BaseNode> childNode, CommonProblemEntity commonProblemEntity) {
        setExpanded(false);
        this.childNode = childNode;
        this.mCommonProblemEntity = commonProblemEntity;
    }

    public CommonProblemEntity getCommonProblemEntity() {
        return mCommonProblemEntity;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return childNode;
    }
}
