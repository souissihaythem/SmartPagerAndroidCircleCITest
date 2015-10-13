package net.smartpager.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;


/**
 * Created by dmitriy on 1/23/14.
 */
public class OutgoingCallReceiver extends BroadcastReceiver {
    private static final String OUTGOING_CALL_ACTION = "net.smartpager.android.SET_OUTGOING_CALL_NUMBER";
    private static final String BUNDLE_FIELD_NUMBER = "outgoingNumber";
    private static final String BUNDLE_FIELD_MESSAGE_ID = "messageID";
    private static String m_currNumber;
    private static String m_currMessageID;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(OUTGOING_CALL_ACTION))
        {
            m_currNumber = intent.getExtras().getString(BUNDLE_FIELD_NUMBER);
            m_currMessageID = intent.getExtras().getString(BUNDLE_FIELD_MESSAGE_ID);
        }else
        {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (m_currNumber != null && m_currMessageID != null && m_currNumber.equals(phoneNumber)) {
                startMarkMessageCalled(context, m_currMessageID);
                m_currMessageID = null;
                m_currNumber = null;
            }
        }
    }

    private void startMarkMessageCalled (Context context,String messageID)
    {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), messageID);
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageCalled, extras);
    }

    public static Intent createOutgoingCallNumberAction (String number, String messageID)
    {
        Intent intent = new Intent();
        intent.setAction(OUTGOING_CALL_ACTION);
        intent.putExtra(BUNDLE_FIELD_NUMBER, number);
        intent.putExtra(BUNDLE_FIELD_MESSAGE_ID, messageID);
        return intent;
    }
}
