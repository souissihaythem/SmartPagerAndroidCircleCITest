package net.smartpager.android.model2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haythemsouissi on 13/09/15.
 */
public class MessageType {

    @Expose
    private Integer id;
    @Expose
    private Integer account;
    @SerializedName("system_type")
    @Expose
    private Object systemType;
    @SerializedName("is_default")
    @Expose
    private Boolean isDefault;
    @Expose
    private String name;
    @Expose
    private Boolean enabled;
    @SerializedName("icon_class")
    @Expose
    private Object iconClass;
    @SerializedName("icon_color")
    @Expose
    private Object iconColor;
    @SerializedName("alerting_policy")
    @Expose
    private Object alertingPolicy;
    @SerializedName("attachments_allowed")
    @Expose
    private Boolean attachmentsAllowed;
    @SerializedName("response_options_allowed")
    @Expose
    private Boolean responseOptionsAllowed;
    @SerializedName("requires_confirm")
    @Expose
    private Boolean requiresConfirm;
    @SerializedName("apply_escalations")
    @Expose
    private Boolean applyEscalations;
    @SerializedName("ignore_user_status")
    @Expose
    private Boolean ignoreUserStatus;
    @SerializedName("allow_custom_subject")
    @Expose
    private Boolean allowCustomSubject;
    @SerializedName("toggle_requires_confirm")
    @Expose
    private Boolean toggleRequiresConfirm;
    @SerializedName("disable_replies")
    @Expose
    private Boolean disableReplies;
    @SerializedName("response_options")
    @Expose
    private List<String> responseOptions = new ArrayList<String>();
    @SerializedName("show_callback_number")
    @Expose
    private Boolean showCallbackNumber;
    @SerializedName("require_callback_number")
    @Expose
    private Boolean requireCallbackNumber;
    @SerializedName("body_label")
    @Expose
    private String bodyLabel;
    @SerializedName("body_text")
    @Expose
    private String bodyText;
    @Expose
    private List<Field> fields = new ArrayList<Field>();

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The account
     */
    public Integer getAccount() {
        return account;
    }

    /**
     *
     * @param account
     * The account
     */
    public void setAccount(Integer account) {
        this.account = account;
    }

    /**
     *
     * @return
     * The systemType
     */
    public Object getSystemType() {
        return systemType;
    }

    /**
     *
     * @param systemType
     * The system_type
     */
    public void setSystemType(Object systemType) {
        this.systemType = systemType;
    }

    /**
     *
     * @return
     * The isDefault
     */
    public Boolean getIsDefault() {
        return isDefault;
    }

    /**
     *
     * @param isDefault
     * The is_default
     */
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled
     * The enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     * @return
     * The iconClass
     */
    public Object getIconClass() {
        return iconClass;
    }

    /**
     *
     * @param iconClass
     * The icon_class
     */
    public void setIconClass(Object iconClass) {
        this.iconClass = iconClass;
    }

    /**
     *
     * @return
     * The iconColor
     */
    public Object getIconColor() {
        return iconColor;
    }

    /**
     *
     * @param iconColor
     * The icon_color
     */
    public void setIconColor(Object iconColor) {
        this.iconColor = iconColor;
    }

    /**
     *
     * @return
     * The alertingPolicy
     */
    public Object getAlertingPolicy() {
        return alertingPolicy;
    }

    /**
     *
     * @param alertingPolicy
     * The alerting_policy
     */
    public void setAlertingPolicy(Object alertingPolicy) {
        this.alertingPolicy = alertingPolicy;
    }

    /**
     *
     * @return
     * The attachmentsAllowed
     */
    public Boolean getAttachmentsAllowed() {
        return attachmentsAllowed;
    }

    /**
     *
     * @param attachmentsAllowed
     * The attachments_allowed
     */
    public void setAttachmentsAllowed(Boolean attachmentsAllowed) {
        this.attachmentsAllowed = attachmentsAllowed;
    }

    /**
     *
     * @return
     * The responseOptionsAllowed
     */
    public Boolean getResponseOptionsAllowed() {
        return responseOptionsAllowed;
    }

    /**
     *
     * @param responseOptionsAllowed
     * The response_options_allowed
     */
    public void setResponseOptionsAllowed(Boolean responseOptionsAllowed) {
        this.responseOptionsAllowed = responseOptionsAllowed;
    }

    /**
     *
     * @return
     * The requiresConfirm
     */
    public Boolean getRequiresConfirm() {
        return requiresConfirm;
    }

    /**
     *
     * @param requiresConfirm
     * The requires_confirm
     */
    public void setRequiresConfirm(Boolean requiresConfirm) {
        this.requiresConfirm = requiresConfirm;
    }

    /**
     *
     * @return
     * The applyEscalations
     */
    public Boolean getApplyEscalations() {
        return applyEscalations;
    }

    /**
     *
     * @param applyEscalations
     * The apply_escalations
     */
    public void setApplyEscalations(Boolean applyEscalations) {
        this.applyEscalations = applyEscalations;
    }

    /**
     *
     * @return
     * The ignoreUserStatus
     */
    public Boolean getIgnoreUserStatus() {
        return ignoreUserStatus;
    }

    /**
     *
     * @param ignoreUserStatus
     * The ignore_user_status
     */
    public void setIgnoreUserStatus(Boolean ignoreUserStatus) {
        this.ignoreUserStatus = ignoreUserStatus;
    }

    /**
     *
     * @return
     * The allowCustomSubject
     */
    public Boolean getAllowCustomSubject() {
        return allowCustomSubject;
    }

    /**
     *
     * @param allowCustomSubject
     * The allow_custom_subject
     */
    public void setAllowCustomSubject(Boolean allowCustomSubject) {
        this.allowCustomSubject = allowCustomSubject;
    }

    /**
     *
     * @return
     * The toggleRequiresConfirm
     */
    public Boolean getToggleRequiresConfirm() {
        return toggleRequiresConfirm;
    }

    /**
     *
     * @param toggleRequiresConfirm
     * The toggle_requires_confirm
     */
    public void setToggleRequiresConfirm(Boolean toggleRequiresConfirm) {
        this.toggleRequiresConfirm = toggleRequiresConfirm;
    }

    /**
     *
     * @return
     * The disableReplies
     */
    public Boolean getDisableReplies() {
        return disableReplies;
    }

    /**
     *
     * @param disableReplies
     * The disable_replies
     */
    public void setDisableReplies(Boolean disableReplies) {
        this.disableReplies = disableReplies;
    }

    /**
     *
     * @return
     * The responseOptions
     */
    public List<String> getResponseOptions() {
        return responseOptions;
    }

    /**
     *
     * @param responseOptions
     * The response_options
     */
    public void setResponseOptions(List<String> responseOptions) {
        this.responseOptions = responseOptions;
    }

    /**
     *
     * @return
     * The showCallbackNumber
     */
    public Boolean getShowCallbackNumber() {
        return showCallbackNumber;
    }

    /**
     *
     * @param showCallbackNumber
     * The show_callback_number
     */
    public void setShowCallbackNumber(Boolean showCallbackNumber) {
        this.showCallbackNumber = showCallbackNumber;
    }

    /**
     *
     * @return
     * The requireCallbackNumber
     */
    public Boolean getRequireCallbackNumber() {
        return requireCallbackNumber;
    }

    /**
     *
     * @param requireCallbackNumber
     * The require_callback_number
     */
    public void setRequireCallbackNumber(Boolean requireCallbackNumber) {
        this.requireCallbackNumber = requireCallbackNumber;
    }

    /**
     *
     * @return
     * The bodyLabel
     */
    public String getBodyLabel() {
        return bodyLabel;
    }

    /**
     *
     * @param bodyLabel
     * The body_label
     */
    public void setBodyLabel(String bodyLabel) {
        this.bodyLabel = bodyLabel;
    }

    /**
     *
     * @return
     * The bodyText
     */
    public String getBodyText() {
        return bodyText;
    }

    /**
     *
     * @param bodyText
     * The body_text
     */
    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    /**
     *
     * @return
     * The fields
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     *
     * @param fields
     * The fields
     */
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
