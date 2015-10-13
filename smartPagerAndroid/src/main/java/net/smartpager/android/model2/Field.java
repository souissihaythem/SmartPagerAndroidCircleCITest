package net.smartpager.android.model2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haythemsouissi on 13/09/15.
 */
public class Field {

    @Expose
    private Integer id;
    @Expose
    private Integer order;
    @SerializedName("field_type")
    @Expose
    private String fieldType;
    @Expose
    private String label;
    @Expose
    private String slug;
    @Expose
    private Boolean required;
    @SerializedName("read_only")
    @Expose
    private Boolean readOnly;
    @SerializedName("default_value")
    @Expose
    private String defaultValue;
    @SerializedName("is_callback")
    @Expose
    private Boolean isCallback;
    @Expose
    private List<Object> choices = new ArrayList<Object>();

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
     * The order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     *
     * @param order
     * The order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     *
     * @return
     * The fieldType
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     *
     * @param fieldType
     * The field_type
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     *
     * @return
     * The label
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     *
     * @param slug
     * The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     *
     * @return
     * The required
     */
    public Boolean getRequired() {
        return required;
    }

    /**
     *
     * @param required
     * The required
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     *
     * @return
     * The readOnly
     */
    public Boolean getReadOnly() {
        return readOnly;
    }

    /**
     *
     * @param readOnly
     * The read_only
     */
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     *
     * @return
     * The defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     * @param defaultValue
     * The default_value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     *
     * @return
     * The isCallback
     */
    public Boolean getIsCallback() {
        return isCallback;
    }

    /**
     *
     * @param isCallback
     * The is_callback
     */
    public void setIsCallback(Boolean isCallback) {
        this.isCallback = isCallback;
    }

    /**
     *
     * @return
     * The choices
     */
    public List<Object> getChoices() {
        return choices;
    }

    /**
     *
     * @param choices
     * The choices
     */
    public void setChoices(List<Object> choices) {
        this.choices = choices;
    }

}