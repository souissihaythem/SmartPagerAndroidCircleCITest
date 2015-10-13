package net.smartpager.android.fragment.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FragmentDialogTag;

/**
 * Created by dmitriy on 3/13/14.
 */
public class EditYesNoFragmentDialog extends DialogFragment implements android.content.DialogInterface.OnClickListener {
    private OnDialogDoneListener mDialogDoneListener;

    public static EditYesNoFragmentDialog newInstance(String title, String message, String hint, boolean cancelable) {
        EditYesNoFragmentDialog fragmentDialog = newInstance(title, message, hint, cancelable, null);
        return fragmentDialog;
    }

    public static EditYesNoFragmentDialog newInstance(String title, String message, String hint, boolean cancelable, OnDialogDoneListener listener) {
        EditYesNoFragmentDialog fragmentDialog = new EditYesNoFragmentDialog();
        fragmentDialog.mDialogDoneListener = listener;
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.dialogTitle.name(), title);
        bundle.putString(BundleKey.dialogMessage.name(), message);
        bundle.putString(BundleKey.hint.name(), hint);
        fragmentDialog.setArguments(bundle);
        fragmentDialog.setCancelable(cancelable);
        return fragmentDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        if (mDialogDoneListener == null && activity instanceof OnDialogDoneListener) {
            mDialogDoneListener = (OnDialogDoneListener) activity;
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View rootView = inflater.inflate(R.layout.dialog_edit_yes_no, container, false);
        Bundle settings = getArguments();
        TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_edit_yes_no_title);
        titleTextView.setText( settings.getString(BundleKey.dialogTitle.name()));

        String msg = settings.getString(BundleKey.dialogMessage.name());
        TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_edit_yes_no_message);
        int visibility = (TextUtils.isEmpty(msg) ? View.GONE : View.VISIBLE);
        messageTextView.setVisibility(visibility);
        if(!TextUtils.isEmpty(msg))
           messageTextView.setText(msg);

        final EditText editText = (EditText) rootView.findViewById(R.id.dialog_edit_yes_no_text_field);
        if(settings.containsKey(BundleKey.hint.name()))
            editText.setHint(settings.getString(BundleKey.hint.name()));

        Button btnPositive = (Button) rootView.findViewById(R.id.dialog_edit_yes_no_ok_button);
        if(settings.containsKey(BundleKey.positiveBtnText.name()))
            btnPositive.setText(settings.getString(BundleKey.positiveBtnText.name()));
        btnPositive.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editText.getText()))
                {
                    Toast.makeText(SmartPagerApplication.getInstance(), R.string.error_empty_answer, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mDialogDoneListener != null) {
                    mDialogDoneListener.onDialogDone( FragmentDialogTag.valueOf(getTag()), editText.getText().toString());
                }
                dismiss();
            }
        });

        Button btnNegative = (Button) rootView.findViewById(R.id.dialog_edit_yes_no_no_button);
        if(settings.containsKey(BundleKey.negativeBtnText.name()))
            btnNegative.setText(settings.getString(BundleKey.negativeBtnText.name()));
        btnNegative.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogDoneListener != null) {
                    mDialogDoneListener.onDialogNo(FragmentDialogTag.valueOf(getTag()), "");
                }
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onClick(DialogInterface arg0, int arg1) {
        dismiss();
    }

    public static class Builder {
        private Bundle m_settings;
        private EditYesNoFragmentDialog m_dialog = new EditYesNoFragmentDialog();

        public Builder(OnDialogDoneListener listener)
        {
            m_settings = new Bundle();
            m_dialog = new EditYesNoFragmentDialog();
            m_dialog.setArguments(m_settings);
            m_dialog.mDialogDoneListener = listener;
        }

        public Builder setCancelable(boolean value)
        {
            m_dialog.setCancelable(value);
            return this;
        }

        public Builder setTitle(int resID)
        {
            return setTitle(SmartPagerApplication.getInstance().getString(resID));
        }

        public Builder setTitle(String title)
        {
            m_settings.putString(BundleKey.dialogTitle.name(), title);
            return this;
        }

        public Builder setMessage(int resID)
        {
            return setMessage(SmartPagerApplication.getInstance().getString(resID));
        }

        public Builder setMessage(String message)
        {
            m_settings.putString(BundleKey.dialogMessage.name(), message);
            return this;
        }

        public Builder setHint(int resID)
        {
            return setHint(SmartPagerApplication.getInstance().getString(resID));
        }

        public Builder setHint(String hint)
        {
            m_settings.putString(BundleKey.hint.name(), hint);
            return this;
        }

        public Builder setPositiveButtonText(int resID)
        {
            return setPositiveButtonText(SmartPagerApplication.getInstance().getString(resID));
        }

        public Builder setPositiveButtonText(String text)
        {
            m_settings.putString(BundleKey.positiveBtnText.name(), text);
            return this;
        }

        public Builder setNegativeButtonText(int resID)
        {
            return setNegativeButtonText(SmartPagerApplication.getInstance().getString(resID));
        }

        public Builder setNegativeButtonText(String text)
        {
            m_settings.putString(BundleKey.negativeBtnText.name(), text);
            return this;
        }

        public void show(FragmentManager fragmentManager, FragmentDialogTag tag)
        {
            m_dialog.show(fragmentManager, tag.name());
        }
    }
}
