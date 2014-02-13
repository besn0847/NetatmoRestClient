/*
 * Copyright 2013 Netatmo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netatmo.weatherstation.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.netatmo.weatherstation.api.NetatmoHttpClient;
import com.netatmo.weatherstation.api.NetatmoUtils;

/**
 * This is just an example of how you can extend NetatmoHttpClient.
 * Tokens are stored in the shared preferences of the app, but you can store them as you wish
 * as long as they are properly returned by the getters.
 * If you want to add your own '/getmeasure' requests, this is also the place to do it.
 */
public class SampleHttpClient extends NetatmoHttpClient {
    final String CLIENT_ID = "52ea7ef91b7759f96250c24b";
    final String CLIENT_SECRET = "CoONiS67jOcSIXQlNh3ZLqmReXLiU6TFixJ4hso4mhkgn";

    private Properties mSharedPrefs;
    private Preferences tokenStore;

    public SampleHttpClient() {
    	tokenStore = Preferences.userRoot().node(this.getClass().getName());
    }
    
    public SampleHttpClient(String fileName) throws FileNotFoundException, IOException {
        this();
        File propFile = new File(fileName);
        mSharedPrefs.load(new FileInputStream(propFile));
    }
    
    @Override
    protected String getClientId() {
        return CLIENT_ID;
    }

    @Override
    protected String getClientSecret() {
        return CLIENT_SECRET;
    }

    @Override
    public void storeTokens(String refreshToken, String accessToken, long expiresAt) {
    	tokenStore.put(NetatmoUtils.KEY_REFRESH_TOKEN, refreshToken);
        tokenStore.put(NetatmoUtils.KEY_ACCESS_TOKEN, accessToken);
        tokenStore.putLong(NetatmoUtils.KEY_EXPIRES_AT, expiresAt);
    }

    @Override
    public void clearTokens() {
        try {
			tokenStore.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
    }

    @Override
    public String getAccessToken() {
        return tokenStore.get(NetatmoUtils.KEY_ACCESS_TOKEN, null);
    }

    @Override
    public String getRefreshToken() {
        return tokenStore.get(NetatmoUtils.KEY_REFRESH_TOKEN, null);
    }

    @Override
    public long getExpiresAt() {
        return tokenStore.getLong(NetatmoUtils.KEY_EXPIRES_AT, 0);
    }
}