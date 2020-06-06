package ml.chiragkhandhar.retagged;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class GetOAuthToken extends AsyncTask<Void, Void, Void> {
    private Activity mActivity;
    private Account mAccount;
    private int mRequestCode;
    private String mScope;

    GetOAuthToken(Activity mActivity, Account mAccount, String mScope, int mRequestCode) {
        this.mActivity = mActivity;
        this.mAccount = mAccount;
        this.mRequestCode = mRequestCode;
        this.mScope = mScope;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            String token = fetchToken();
            if (token != null) {
                ((MainActivity) mActivity).onTokenReceived(token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String fetchToken() throws IOException {
        String accessToken;
        try {
            accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope);
            GoogleAuthUtil.clearToken(mActivity, accessToken);
            accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope);
            return accessToken;
        } catch (UserRecoverableAuthException userRecoverableException) {
            mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode);
        } catch (GoogleAuthException fatalException) {
            fatalException.printStackTrace();
        }
        return null;
    }
}
