package org.noahy.q2android.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Q2AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        Q2Authenticator authenticator = new Q2Authenticator(this);
        return authenticator.getIBinder();
    }
}
