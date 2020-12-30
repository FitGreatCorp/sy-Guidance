package com.fitgreat.airfacerobot.settings.view;

import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;


public interface SettingsView extends BaseView {
    void showWorkflowList(List<WorkflowEntity> workflowEntityList);
}
