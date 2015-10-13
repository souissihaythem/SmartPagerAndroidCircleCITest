package net.smartpager.android.holder.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.DefensiveURLSpan;

import org.apache.http.Header;

import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;

public abstract class ChatInboxItemHolder extends ChatItemHolder implements IHolderCursor {
	ImageView personImageView;
	RelativeLayout containerLayout;
	TextView messageCaptionTextView;
	TextView messageBodyTextView;
	TextView signatureTextView;
	TextView dateTextView;
	View lineView;
	TextView deliveredTextView;
	TextView readTextView;
	TextView acceptedTextView;
	
	LinearLayout btnPdfLinearLayout;
	Button btnPdf;

	LinearLayout btnLinearLayout;
	Button btnAccept;
	Button btnDecline;

	LinearLayout layResponseOptions;
	TextView txtResponseOption1;
	TextView txtResponseOption2;
	TextView txtResponseOption3;
	TextView txtResponseOption4;

	// SoundPlayer mPlayer;
	// Player mPlayer;

	PlayInfo mPlayInfo;
	Context mContext;
	
	List<String> pdfUrls;
	
	public abstract int getBubbleId();

	public abstract int getBorderColorID();

	private int mSpin = MessageTable.values().length;

	public ChatInboxItemHolder() {}

	@Override
	public ViewGroup inflateLayout(LayoutInflater inflater, Context context) {
		ViewGroup view = new RelativeLayout(context);
		mContext = context;
		// mPlayer = Player.createPlayer(context);
		inflater.inflate(R.layout.item_chat_list, view, true);
		
		pdfUrls = new ArrayList<String>();
		
		return view;
	}

	@Override
	public IHolderCursor createHolder(View view) {
		super.createHolder(view);
		personImageView = (ImageView) view.findViewById(R.id.item_chat_person_image_view);
		containerLayout = (RelativeLayout) view.findViewById(R.id.item_chat_message_container);
		messageCaptionTextView = (TextView) view.findViewById(R.id.item_chat_message_caption_text_view);
		messageBodyTextView = (TextView) view.findViewById(R.id.item_chat_message_body_text_view);
		signatureTextView = (TextView) view.findViewById(R.id.item_chat_signature_text_view);
		dateTextView = (TextView) view.findViewById(R.id.item_chat_date_text_view);
		lineView = view.findViewById(R.id.item_chat_line);
		deliveredTextView = (TextView) view.findViewById(R.id.item_chat_delivered_text_view);
		readTextView = (TextView) view.findViewById(R.id.item_chat_read_text_view);
		acceptedTextView = (TextView) view.findViewById(R.id.item_chat_accepted_text_view);
		
		// PDF Attachment section
		btnPdfLinearLayout = (LinearLayout) view.findViewById(R.id.itemchat_list_lin_lay_pdfbutton);
		btnPdf = (Button) view.findViewById(R.id.itemchat_btn_pdf_attachment);

		// Accept-Decline section
		btnLinearLayout = (LinearLayout) view.findViewById(R.id.itemchat_list_lin_lay_buttons);
		btnAccept = (Button) view.findViewById(R.id.itemchat_btn_accept);
		btnDecline = (Button) view.findViewById(R.id.itemchat_btn_decline);

		// Response Options section
		layResponseOptions = (LinearLayout) view.findViewById(R.id.item_chat_quick_resp_options_layout);

		// not needed - we generate them dynamically
		// txtResponseOption1 = (TextView) view.findViewById(R.id.item_chat_quick_resp_option_1);
		// txtResponseOption2 = (TextView) view.findViewById(R.id.item_chat_quick_resp_option_2);
		// txtResponseOption3 = (TextView) view.findViewById(R.id.item_chat_quick_resp_option_3);
		// txtResponseOption4 = (TextView) view.findViewById(R.id.item_chat_quick_resp_option_4);
		
		return this;
	}
	
	private void fixTextView(TextView tv) {
		try {
			SpannableString current=(SpannableString)tv.getText();
			URLSpan[] spans=
					current.getSpans(0, current.length(), URLSpan.class);

			for (URLSpan span : spans) {
				int start=current.getSpanStart(span);
				int end=current.getSpanEnd(span);

				current.removeSpan(span);
				current.setSpan(new DefensiveURLSpan(span.getURL(), mContext), start, end,
						0);
			}
		} catch (ClassCastException e) {
			Log.d(getClass().getName(), "ClassCastException: " + e);
		}
		
	}

	@Override
	public void preSetData(Bundle params) {
		super.preSetData(params);
	}

	@Override
	public void setData(Cursor cursor) {
		super.setData(cursor);
		// TODO END
		lineView.setVisibility(View.GONE);
		deliveredTextView.setVisibility(View.GONE);
		readTextView.setVisibility(View.GONE);
		acceptedTextView.setVisibility(View.GONE);

		String currentSmartPagerID = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
		messageBodyTextView.setText(cursor.getString(MessageTable.text.ordinal()));
		Linkify.addLinks(messageBodyTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
		fixTextView(messageBodyTextView);
		//containerLayout.setBackgroundResource(getBubbleId());

		int idxContactId = MessageTable.values().length + ContactTable.id.ordinal(); // Look for SQL query for more
		// details
		final String contactId = cursor.getString(idxContactId);

		String displayName;

		// displayName = cursor.getString(mSpin + ContactTable.firstName.ordinal()) + " "
		// + cursor.getString(mSpin + ContactTable.lastName.ordinal());
		String firstName = "";
		String lastName = "";
		if (!cursor.isNull(mSpin + ContactTable.firstName.ordinal())) {
			firstName = cursor.getString(mSpin + ContactTable.firstName.ordinal());
		}
		if (!cursor.isNull(mSpin + ContactTable.lastName.ordinal())) {
			lastName = cursor.getString(mSpin + ContactTable.lastName.ordinal());
		}
		if (TextUtils.isEmpty(lastName) && TextUtils.isEmpty(firstName)) {
			displayName = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
		} else {
			displayName = String.format("%s %s", cursor.getString(mSpin + ContactTable.firstName.ordinal()),
					cursor.getString(mSpin + ContactTable.lastName.ordinal()));
		}
		signatureTextView.setText(displayName);

		boolean isShowPhoto = true;
		if (!cursor.isFirst()) {
			if (cursor.moveToPrevious()) {

				if (currentSmartPagerID.equalsIgnoreCase(cursor.getString(MessageTable.fromSmartPagerId.ordinal()))
						&& cursor.getInt(MessageTable.messageType.ordinal()) != MessageType.sent.ordinal()) {
					isShowPhoto = false;
				}
				cursor.moveToNext();
			}
		}
		if (isShowPhoto) {
			personImageView.setVisibility(View.VISIBLE);
			personImageView.setBackgroundResource(getBorderColorID());
			String imageUri = cursor.getString(mSpin + ContactTable.photoUrl.ordinal());
			SmartPagerApplication.getInstance().getImageLoader()
					.displayImage(imageUri, personImageView, R.drawable.chat_avatar_no_image);
			personImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mChatItemClickListener != null && !TextUtils.isEmpty(contactId)) {
						mChatItemClickListener.onContactClick(contactId);
					}
				}
			});

		} else {
			personImageView.setVisibility(View.GONE);
		}
		
		long milliseconds = cursor.getLong(MessageTable.timeSent.ordinal());
		if (milliseconds == Constants.DEFAULT_DATE.getTime()) {
			dateTextView.setText("");
		} else {
			dateTextView.setText(DateTimeUtils.format(milliseconds));
		}
		
		setupPdfAttachmentSection(cursor);

		setupAcceptDeclineSection(cursor);

		setupResponseOptionsSection(cursor);
	}

	private void setupResponseOptionsSection(Cursor cursor) {

		String respOptions = cursor.getString(cursor.getColumnIndex(MessageTable.responseTemplate.name()));
		String currentStatus = cursor.getString(MessageTable.status.ordinal());
		boolean isReplied = currentStatus.equalsIgnoreCase(MessageStatus.Replied.name());

		if (!TextUtils.isEmpty(respOptions) && !isReplied) {

			layResponseOptions.setVisibility(View.VISIBLE);

			respOptions = respOptions.substring(0, respOptions.length() - 1).substring(1);
			final String[] responses = respOptions.split("\",\"");

			// if (layResponseOptions.getChildCount() != responses.length) {
			layResponseOptions.removeAllViewsInLayout();
			// }

			for (int i = 0; i < responses.length; i++) {
				final int idx = i;
				TextView txtView = new TextView(mContext);
				setTextViewStyle(txtView);
				responses[idx] = responses[idx].replace("\\\"", "\"");
				txtView.setText(responses[idx]);
				txtView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						mChatItemClickListener.onResponseOptionClick(responses[idx]);
					}
				});
				layResponseOptions.addView(txtView);
			}

		} else {

			layResponseOptions.setVisibility(View.GONE);
		}
	}

	private void setTextViewStyle(TextView txtView) {

		txtView.setBackgroundResource(R.drawable.selector_gray_page_button);
		txtView.setPadding(5, 5, 5, 5);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		llp.setMargins(5, 5, 5, 5);
		txtView.setLayoutParams(llp);
		txtView.setTextAppearance(mContext, android.R.attr.textAppearanceSmall);
		txtView.setTextColor(mContext.getResources().getColor(R.color.border_contact_status));
	}
	
	private void setupPdfAttachmentSection(Cursor cursor) {
        final String threadID = cursor.getString(cursor.getColumnIndex("threadID"));
		String pdfUrlString = cursor.getString(cursor.getColumnIndex("pdfUrls"));
		boolean showPDFButton = false;
		if (pdfUrlString.length() > 0) {
			pdfUrls = Arrays.asList(pdfUrlString.split(","));
			showPDFButton = true;

			if (pdfUrls.size() > 0) {
				if (pdfUrls.size() == 1) {
					btnPdf.setText("1 PDF Attachment");
				} else {
					btnPdf.setText(pdfUrls.size() + " PDF Attachments");
				}
			}
			
			final String[] labels = new String[pdfUrls.size()];
			
			for (int x = 0; x < pdfUrls.size(); x++) {
				labels[x] = "Attachment #" + (x + 1);
			}
			
			btnPdf.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				    builder.setTitle("Attachments")
				           .setItems(labels, new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int which) {
				            	   String url = pdfUrls.get(which);
                                    downloadPDF(threadID, url);

//								   Intent i = new Intent(mContext, PDFActivity.class);
//								   i.putExtra("url", url);
//								   mContext.startActivity(i);
				           }
				    });
				    builder.show();
					
				}
			});
		}
		
		if (!showPDFButton) {
			btnPdfLinearLayout.setVisibility(View.GONE);
		}
	}

	private void setupAcceptDeclineSection(Cursor cursor) {

//		int reqAcceptance = cursor.getInt(MessageTable.requestAcceptance.ordinal());
		int reqAcceptanceShow = cursor.getInt(MessageTable.requestAcceptanceShow.ordinal());
		String currentStatus = cursor.getString(MessageTable.status.ordinal());

		boolean isRejected = currentStatus.equalsIgnoreCase(MessageStatus.Rejected.name());
		boolean isAccepted = currentStatus.equalsIgnoreCase(MessageStatus.Accepted.name());

		if (reqAcceptanceShow==1) {

			btnLinearLayout.setVisibility(View.VISIBLE);

			btnAccept.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mChatItemClickListener.onAcceptAcceptanceReq(mMessageId);
					btnLinearLayout.setVisibility(View.GONE);
				}
			});

			btnDecline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mChatItemClickListener.onRejectAcceptanceReq(mMessageId);
					btnLinearLayout.setVisibility(View.GONE);
				}
			});
		} else {
			btnLinearLayout.setVisibility(View.GONE);

			if (isRejected || isAccepted) {
				long lastUpdateMilliseconds = cursor.getLong(MessageTable.lastUpdate.ordinal());
				lineView.setVisibility(View.VISIBLE);
				acceptedTextView.setVisibility(View.VISIBLE);
				StringBuilder builder = new StringBuilder();
				builder.append("<b><i>");
				builder.append(currentStatus).append(" by Me ");
				builder.append(DateTimeUtils.format(lastUpdateMilliseconds));
				builder.append("</i></b>");

				acceptedTextView.setText(Html.fromHtml(builder.toString()));
			}
		}
	}

	@Override
	public void setListeners(Object... listeners) {
		super.setListeners(listeners);

	}

    private void downloadPDF(String threadID, String url) {
        final String fileName = threadID + "-" + SmartPagerApplication.getInstance().md5(url) + ".pdf";
        final String filePath = mContext.getExternalFilesDir(null) + "/" + fileName;
        System.out.println("**** filePath: " + filePath);
        final File output = new File(filePath);
        System.out.println("**** output.getAbsolutePath: " + output.getAbsolutePath());

        if (output.exists()) {
            openPDF(output);
        } else {
            final ProgressDialog pd = new ProgressDialog(mContext);
            pd.setMessage("Downloading Attachment");
            pd.setIndeterminate(false);
            pd.setMax(100);
            pd.show();

            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new FileAsyncHttpResponseHandler(mContext) {
                @Override
                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                    pd.dismiss();
                }

                @Override
                public void onSuccess(int i, Header[] headers, File file) {

                    System.out.println("**** file.getAbsolutePath: " + file.getAbsolutePath());
                    try {
                        copyFile(file, output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("**** file.renameTo.getAbsolutePath: " + file.getAbsolutePath());
                    System.out.println("**** output.getAbsolutePath: " + output.getAbsolutePath());
                    openPDF(output);
                    pd.dismiss();
                }
            });
        }


    }

    private void openPDF(File output) {
        System.out.println("**** openPDF");
        System.out.println("**** output.getAbsolutePath: " + output.getAbsolutePath());
        Intent aI = new Intent(Intent.ACTION_VIEW);
        aI.setDataAndType(Uri.fromFile(output), "application/pdf");
        aI.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
			mContext.startActivity(aI);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mContext, "No application available to open PDF files.", Toast.LENGTH_LONG).show();
		}
    }

    public static void copyFile(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyFile(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


}
