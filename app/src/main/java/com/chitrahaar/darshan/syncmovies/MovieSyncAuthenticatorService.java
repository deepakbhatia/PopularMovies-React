package com.chitrahaar.darshan.syncmovies;

/**
 * Created by obelix on 21/11/2016.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class MovieSyncAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private MovieSyncAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new MovieSyncAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

