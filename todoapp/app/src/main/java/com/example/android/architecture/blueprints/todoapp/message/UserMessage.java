package com.example.android.architecture.blueprints.todoapp.message;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.architecture.blueprints.todoapp.R;


/**
 * As an enum, this value can be passed around the app to indicate various states.
 * If you want to display it to the user you can put it inside a dialog (it implements
 * parcelable). Call getString() for the human readable text.
 */
public enum UserMessage implements Parcelable {

    ERROR_MISC(R.string.msg_error_misc),
    ERROR_NETWORK(R.string.msg_error_network),
    ERROR_SERVER(R.string.msg_error_server),
    ERROR_CLIENT(R.string.msg_error_client),
    ERROR_SESSION_TIMED_OUT(R.string.msg_error_session_timeout),
    ERROR_BUSY(R.string.msg_error_busy),
    ERROR_NOT_FOUND(R.string.msg_error_not_found),
    ERROR_CANCELLED(R.string.msg_error_cancelled);

    private String message;
    private int messageResId;

    UserMessage(int messageResId) {
        this.messageResId = messageResId;
    }

    public String getString(Resources resources) {

        if (message == null) {
            message = getString(messageResId, resources);
        }

        return message;
    }

    private String getString(int resId, Resources resources) {
        return resources.getString(resId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<UserMessage> CREATOR = new Creator<UserMessage>() {
        @Override
        public UserMessage createFromParcel(final Parcel source) {
            return UserMessage.values()[source.readInt()];
        }

        @Override
        public UserMessage[] newArray(final int size) {
            return new UserMessage[size];
        }
    };

}
