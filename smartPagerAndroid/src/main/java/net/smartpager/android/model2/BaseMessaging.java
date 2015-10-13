package net.smartpager.android.model2;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haythemsouissi on 13/09/15.
 */
public class BaseMessaging {

    @Expose
    private Boolean success;
    @Expose
    private int errorCode;
    @Expose
    private String errorMessage;
    @Expose
    private List<MessageType> messageTypes = new ArrayList<MessageType>();

    /**
     *
     * @return
     * The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     *
     * @return
     * The errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     *
     * @param errorCode
     * The errorCode
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     *
     * @return
     * The errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *
     * @param errorMessage
     * The errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     *
     * @return
     * The messageTypes
     */
    public List<MessageType> getMessageTypes() {
        return messageTypes;
    }

    /**
     *
     * @param messageTypes
     * The messageTypes
     */
    public void setMessageTypes(List<MessageType> messageTypes) {
        this.messageTypes = messageTypes;
    }

}