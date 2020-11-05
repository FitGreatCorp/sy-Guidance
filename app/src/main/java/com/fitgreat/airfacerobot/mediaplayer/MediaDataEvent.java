package com.fitgreat.airfacerobot.mediaplayer;

import com.fitgreat.archmvp.base.event.BaseEvent;

/**
 * class des<p>
 *
 * @author dengj
 * @since 2020/4/15 16:12
 */
public class MediaDataEvent extends BaseEvent {

    private String container, blob, instructionId, status, F_Type, operationType, operationProcedureId;

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getF_Type() {
        return F_Type;
    }

    public void setF_Type(String f_Type) {
        F_Type = f_Type;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationProcedureId() {
        return operationProcedureId;
    }

    public void setOperationProcedureId(String operationProcedureId) {
        this.operationProcedureId = operationProcedureId;
    }
}
