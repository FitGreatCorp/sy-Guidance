package com.fitgreat.airfacerobot.commonproblem.node;

import com.chad.library.adapter.base.entity.node.BaseNode;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class CommonProblemSecondNode extends BaseNode {

    private String mProblemAnswer;
    private String mEnProblemAnswer;

    public CommonProblemSecondNode(String problemAnswer, String problemEnAnswer) {
        this.mProblemAnswer = problemAnswer;
        this.mEnProblemAnswer = problemEnAnswer;
    }

    public String getProblemAnswer() {
        return mProblemAnswer;
    }

    public String getProblemEnAnswer() {
        return mEnProblemAnswer;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
