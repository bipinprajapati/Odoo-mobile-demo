package com.odoo.core.account;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.OdooActivity;
import com.serpentcs.saltracker.R;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.config.FirstLaunchConfig;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.auth.OdooAuthenticator;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooInstancesSelectorDialog;
import com.odoo.core.support.OdooUserLoginSelectorDialog;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OResource;
import com.odoo.datas.OConstants;

import java.util.ArrayList;
import java.util.List;

import Utils.Util;
import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.OdooInstance;
import odoo.listeners.IDatabaseListListener;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooInstanceListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class OdooLogin extends AppCompatActivity implements View.OnClickListener,
        View.OnFocusChangeListener, OdooInstancesSelectorDialog.OnInstanceSelectListener,
        OdooUserLoginSelectorDialog.IUserLoginSelectListener, IOdooConnectionListener, IOdooLoginCallback {

    private EditText edtUsername, edtPassword, edtSelfHosted;
    private Boolean mCreateAccountRequest = false;
    private Boolean mSelfHostedURL = true;
    private Boolean mConnectedToServer = false;
    private Boolean mAutoLogin = false;
    private Boolean mRequestedForAccount = false;
    private AccountCreater accountCreator = null;
    private Spinner databaseSpinner = null;
    private List<String> databases = new ArrayList<>();
    private TextView mLoginProcessStatus = null;
    private TextView mTermsCondition;
    private App mApp;
    private Odoo mOdoo;
    private odoo.helper.OUser mUser;

    String TAG1 = OdooLogin.class.getName();
    String TAG2 = OdooLogin.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_login);
        Util.getStackTrace(TAG1, TAG2);
        mApp = (App) getApplicationContext();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Util.getStackTrace(TAG1, TAG2);
            if (extras.containsKey(OdooAuthenticator.KEY_NEW_ACCOUNT_REQUEST))
                mCreateAccountRequest = true;
            if (extras.containsKey(OdooActivity.KEY_ACCOUNT_REQUEST)) {
                mRequestedForAccount = true;
                setResult(RESULT_CANCELED);
            }
        }
        if (!mCreateAccountRequest) {
            Util.getStackTrace(TAG1, TAG2);
            Log.v("OdooAccountManager.anyActiveUser-------", OdooAccountManager.anyActiveUser(this) + "");
            Log.v("OdooAccountManager.hasAnyAccount----", OdooAccountManager.hasAnyAccount(this)+"");
            if (OdooAccountManager.anyActiveUser(this)) {
                Util.getStackTrace(TAG1, TAG2);
                startOdooActivity();
                return;
            } else if (OdooAccountManager.hasAnyAccount(this)) {
                Util.getStackTrace(TAG1, TAG2);
                onRequestAccountSelect();
            }
        }
        init();
    }

    private void init() {
        Util.getStackTrace(TAG1, TAG2);
        mLoginProcessStatus = (TextView) findViewById(R.id.login_process_status);
        mTermsCondition = (TextView) findViewById(R.id.termsCondition);
        mTermsCondition.setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.create_account).setOnClickListener(this);
        findViewById(R.id.txvAddSelfHosted).setOnClickListener(this);
        edtSelfHosted = (EditText) findViewById(R.id.edtSelfHostedURL);
        edtSelfHosted.setOnFocusChangeListener(this);
    }

    private void startOdooActivity() {
        Util.getStackTrace(TAG1, TAG2);
        startActivity(new Intent(this, OdooActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.getStackTrace(TAG1, TAG2);
        getMenuInflater().inflate(R.menu.menu_base_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.getStackTrace(TAG1, TAG2);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Util.getStackTrace(TAG1, TAG2);
        switch (v.getId()) {
            case R.id.txvAddSelfHosted:
                Util.getStackTrace(TAG1, TAG2);
                toggleSelfHostedURL();
                break;
            case R.id.btnLogin:
                Util.getStackTrace(TAG1, TAG2);
                loginUser();
                break;
            case R.id.forgot_password:
                Util.getStackTrace(TAG1, TAG2);
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_RESET_PASSWORD);
                break;
            case R.id.create_account:
                Util.getStackTrace(TAG1, TAG2);
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_SIGN_UP);
                break;
        }
    }

    private void toggleSelfHostedURL() {
        Util.getStackTrace(TAG1, TAG2);
        TextView txvAddSelfHosted = (TextView) findViewById(R.id.txvAddSelfHosted);
        if (!mSelfHostedURL) {
            Util.getStackTrace(TAG1, TAG2);
            mSelfHostedURL = true;
            findViewById(R.id.layoutSelfHosted).setVisibility(View.VISIBLE);
            edtSelfHosted.setOnFocusChangeListener(this);
            edtSelfHosted.requestFocus();
            txvAddSelfHosted.setText(R.string.label_login_with_odoo);
        } else {
            Util.getStackTrace(TAG1, TAG2);
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
            findViewById(R.id.layoutSelfHosted).setVisibility(View.GONE);
            mSelfHostedURL = false;
            txvAddSelfHosted.setText(R.string.label_add_self_hosted_url);
            edtSelfHosted.setText("");
        }
    }

    @Override
    public void onFocusChange(final View v, final boolean hasFocus) {
        Util.getStackTrace(TAG1, TAG2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSelfHostedURL && v.getId() == R.id.edtSelfHostedURL && !hasFocus) {
                    Util.getStackTrace(TAG1, TAG2);
                    if (!TextUtils.isEmpty(edtSelfHosted.getText())
                            && validateURL(edtSelfHosted.getText().toString())) {
                        edtSelfHosted.setError(null);
                        if (mAutoLogin) {
                            Util.getStackTrace(TAG1, TAG2);
                            findViewById(R.id.controls).setVisibility(View.GONE);
                            findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
                            mLoginProcessStatus.setText(OResource.string(OdooLogin.this,
                                    R.string.status_connecting_to_server));
                        }
                        findViewById(R.id.imgValidURL).setVisibility(View.GONE);
                        findViewById(R.id.serverURLCheckProgress).setVisibility(View.VISIBLE);
                        findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
                        findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
                        String test_url = createServerURL(edtSelfHosted.getText().toString());
                        Log.v("", "Testing URL :" + test_url);
                        try {
                            Odoo.createInstance(OdooLogin.this, test_url).setOnConnect(OdooLogin.this);
                        } catch (OdooVersionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 500);
    }

    private boolean validateURL(String url) {
        Util.getStackTrace(TAG1, TAG2);
        return (url.contains("."));
    }

    private String createServerURL(String server_url) {
        Util.getStackTrace(TAG1, TAG2);
        StringBuilder serverURL = new StringBuilder();
        if (!server_url.contains("http://") && !server_url.contains("https://")) {
            Util.getStackTrace(TAG1, TAG2);
            serverURL.append("http://");
        }
        serverURL.append(server_url);
        return serverURL.toString();
    }

    // User Login
    private void loginUser() {
        Util.getStackTrace(TAG1, TAG2);
        Log.v("", "LoginUser()");
        String serverURL = createServerURL((mSelfHostedURL) ? edtSelfHosted.getText().toString() :
                OConstants.URL_ODOO);
        String databaseName;
        edtUsername = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        if (mSelfHostedURL) {
            Util.getStackTrace(TAG1, TAG2);
            edtSelfHosted.setError(null);
            if (TextUtils.isEmpty(edtSelfHosted.getText())) {
                Util.getStackTrace(TAG1, TAG2);
                edtSelfHosted.setError(OResource.string(this, R.string.error_provide_server_url));
                edtSelfHosted.requestFocus();
                return;
            }
            if (databaseSpinner != null && databases.size() > 1 && databaseSpinner.getSelectedItemPosition() == 0) {
                Util.getStackTrace(TAG1, TAG2);
                Toast.makeText(this, OResource.string(this, R.string.label_select_database), Toast.LENGTH_LONG).show();
                return;
            }

        }
        edtUsername.setError(null);
        edtPassword.setError(null);
        if (TextUtils.isEmpty(edtUsername.getText())) {
            Util.getStackTrace(TAG1, TAG2);
            edtUsername.setError(OResource.string(this, R.string.error_provide_username));
            edtUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(edtPassword.getText())) {
            Util.getStackTrace(TAG1, TAG2);
            edtPassword.setError(OResource.string(this, R.string.error_provide_password));
            edtPassword.requestFocus();
            return;
        }
        findViewById(R.id.controls).setVisibility(View.GONE);
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        mLoginProcessStatus.setText(OResource.string(OdooLogin.this,
                R.string.status_connecting_to_server));
        if (mConnectedToServer) {
            Util.getStackTrace(TAG1, TAG2);
            databaseName = databases.get(0);
            if (databaseSpinner != null) {
                Util.getStackTrace(TAG1, TAG2);
                databaseName = databases.get(databaseSpinner.getSelectedItemPosition());
            }
            mAutoLogin = false;
            loginProcess(null, serverURL, databaseName);
        } else {
            Util.getStackTrace(TAG1, TAG2);
            mAutoLogin = true;
            Log.v("", "Testing URL: " + serverURL);
            try {
                Odoo.createInstance(OdooLogin.this, serverURL).setOnConnect(OdooLogin.this);
            } catch (OdooVersionException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatabases() {
        if (databases.size() > 1) {
            Util.getStackTrace(TAG1, TAG2);
            //findViewById(R.id.layoutBorderDB).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutDatabase).setVisibility(View.VISIBLE);
            databaseSpinner = (Spinner) findViewById(R.id.spinnerDatabaseList);
            databases.add(0, OResource.string(this, R.string.label_select_database));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, databases);
            databaseSpinner.setAdapter(adapter);
        } else {
            Util.getStackTrace(TAG1, TAG2);
            databaseSpinner = null;
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserSelected(OUser user) {
        Util.getStackTrace(TAG1, TAG2);
        OdooAccountManager.login(this, user.getAndroidName());
        startOdooActivity();
    }

    @Override
    public void onRequestAccountSelect() {
        Util.getStackTrace(TAG1, TAG2);
        OdooUserLoginSelectorDialog dialog = new OdooUserLoginSelectorDialog(this);
        dialog.setUserLoginSelectListener(this);
        dialog.show();
    }

    @Override
    public void onNewAccountRequest() {
        Util.getStackTrace(TAG1, TAG2);
        init();
    }


    @Override
    public void onConnect(Odoo odoo) {
        Util.getStackTrace(TAG1, TAG2);
        Log.v("Odoo", "Connected to server.");
        mOdoo = odoo;
        databases.clear();
        findViewById(R.id.serverURLCheckProgress).setVisibility(View.GONE);
        edtSelfHosted.setError(null);
        mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_connected_to_server));
        mOdoo.getDatabaseList(new IDatabaseListListener() {
            @Override
            public void onDatabasesLoad(List<String> strings) {
                databases.addAll(strings);
                showDatabases();
                mConnectedToServer = true;
                findViewById(R.id.imgValidURL).setVisibility(View.VISIBLE);
                if (mAutoLogin) {
                    loginUser();
                }
            }
        });
    }

    @Override
    public void onError(OdooError error) {
        // Some error occurred
        Util.getStackTrace(TAG1, TAG2);
        if (error.getResponseCode() == Odoo.ErrorCode.InvalidURL.get() ||
                error.getResponseCode() == -1) {
            Util.getStackTrace(TAG1, TAG2);
            edtSelfHosted.setError(OResource.string(OdooLogin.this, R.string.error_invalid_odoo_url));
            edtSelfHosted.requestFocus();
        }
        canceledInstanceSelect();
    }

    @Override
    public void onCancelSelect() {
        Util.getStackTrace(TAG1, TAG2);
    }

    @Override
    public void canceledInstanceSelect() {
        Util.getStackTrace(TAG1, TAG2);
        findViewById(R.id.controls).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        findViewById(R.id.serverURLCheckProgress).setVisibility(View.VISIBLE);
    }

    @Override
    public void instanceSelected(OdooInstance instance) {
        Util.getStackTrace(TAG1, TAG2);
        // Logging in to instance
        loginProcess(instance, null, null);
    }

    private void loginProcess(final OdooInstance instance, String url, final String database) {
        Util.getStackTrace(TAG1, TAG2);
        Log.v("", "LoginProcess");
        final String username = edtUsername.getText().toString();
        final String password = edtPassword.getText().toString();
        if (instance == null && url.equals(OConstants.URL_ODOO)) {
            Util.getStackTrace(TAG1, TAG2);
            // OAuth Login or Odoo.com Login
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_getting_instances));
            mOdoo.authenticate(username, password, database, new IOdooLoginCallback() {
                @Override
                public void onLoginSuccess(Odoo odoo, odoo.helper.OUser oUser) {
                    Util.getStackTrace(TAG1, TAG2);
                    mOdoo = odoo;
                    mUser = oUser;
                    mOdoo.getSaasInstances(new IOdooInstanceListener() {
                        @Override
                        public void onInstancesLoad(List<OdooInstance> odooInstances) {
                            OdooInstance oInstance = new OdooInstance();
                            oInstance.setCompanyName(OConstants.ODOO_COMPANY_NAME);
                            oInstance.setUrl(OConstants.URL_ODOO);
                            oInstance.setDbName(database);
                            odooInstances.add(0, oInstance);
                            if (odooInstances.size() > 1) {
                                OdooInstancesSelectorDialog instancesSelectorDialog =
                                        new OdooInstancesSelectorDialog(OdooLogin.this);
                                instancesSelectorDialog.setInstances(odooInstances);
                                instancesSelectorDialog.setOnInstanceSelectListener(OdooLogin.this);
                                instancesSelectorDialog.showDialog();
                            } else {
                                //Loggin in to odoo.com (default instance)
                                loginProcess(oInstance, oInstance.getUrl(), database);
                            }
                        }
                    });
                }

                @Override
                public void onLoginFail(OdooError error) {
                    Util.getStackTrace(TAG1, TAG2);
                    loginFail(error);
                }
            });
        } else if (instance == null) {
            Util.getStackTrace(TAG1, TAG2);
            Log.v("", "Processing Self Hosted Server Login");
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_logging_in));
            mOdoo.authenticate(username, password, database, this);
        } else {
            Util.getStackTrace(TAG1, TAG2);
            // Instance login
            Log.v("", "Processing Odoo Instance Login");
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this,
                    R.string.status_logging_in_with_instance));
            new AsyncTask<Void, Void, odoo.helper.OUser>() {

                @Override
                protected odoo.helper.OUser doInBackground(Void... params) {
                    // Need to execute in background task.
                    return mOdoo.oAuthLogin(instance, username, password);
                }

                @Override
                protected void onPostExecute(odoo.helper.OUser oUser) {
                    super.onPostExecute(oUser);
                    onLoginSuccess(mOdoo, oUser);
                }
            }.execute();
        }
    }

    @Override
    public void onLoginSuccess(Odoo odoo, odoo.helper.OUser oUser) {
        Util.getStackTrace(TAG1, TAG2);
        mApp.setOdoo(odoo, oUser);
        mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_login_success));
        mOdoo = odoo;
        if (accountCreator != null) {
            Util.getStackTrace(TAG1, TAG2);
            accountCreator.cancel(true);
        }
        accountCreator = new AccountCreater();
        OUser user = new OUser();
        user.setFromBundle(oUser.getAsBundle());
        accountCreator.execute(user);
    }

    @Override
    public void onLoginFail(OdooError error) {
        Util.getStackTrace(TAG1, TAG2);
        loginFail(error);
    }

    private void loginFail(OdooError error) {
        Util.getStackTrace(TAG1, TAG2);
        findViewById(R.id.controls).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        edtUsername.setError(OResource.string(this, R.string.error_invalid_username_or_password));
    }

    private class AccountCreater extends AsyncTask<OUser, Void, Boolean> {

        private OUser mUser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Util.getStackTrace(TAG1, TAG2);
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_creating_account));
        }

        @Override
        protected Boolean doInBackground(OUser... params) {
            Util.getStackTrace(TAG1, TAG2);
            mUser = params[0];
            if (OdooAccountManager.createAccount(OdooLogin.this, mUser)) {
                Util.getStackTrace(TAG1, TAG2);
                mUser = OdooAccountManager.getDetails(OdooLogin.this, mUser.getAndroidName());
                OdooAccountManager.login(OdooLogin.this, mUser.getAndroidName());
                FirstLaunchConfig.onFirstLaunch(OdooLogin.this, mUser);
                try {
                    // Syncing company details
                    ODataRow company_details = new ODataRow();
                    company_details.put("id", mUser.getCompanyId());
                    ResCompany company = new ResCompany(OdooLogin.this, mUser);
                    company.quickCreateRecord(company_details);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            Util.getStackTrace(TAG1, TAG2);
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_redirecting));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mRequestedForAccount)
                        startOdooActivity();
                    else {
                        Intent intent = new Intent();
                        intent.putExtra(OdooActivity.KEY_NEW_USER_NAME, mUser.getAndroidName());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }, 1500);
        }
    }
}