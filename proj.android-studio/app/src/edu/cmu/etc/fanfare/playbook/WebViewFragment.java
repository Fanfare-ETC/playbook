package edu.cmu.etc.fanfare.playbook;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebViewFragment extends PlaybookFragment {
    private static final String TAG = WebViewFragment.class.getSimpleName();
    private static final String WEB_VIEW_PACKAGE_NAME = "com.google.android.webview";
    private static final String WEB_VIEW_PACKAGE_NAME_ALT = "com.android.webview";

    // WebView 42 is the minimum that supports ES6 classes.
    private static final int MIN_WEB_VIEW_VERSION = 42;

    private boolean mWebViewIsCompatible = false;
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWebView = new WebView(getActivity());
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        checkWebViewVersion();

        if (mWebViewIsCompatible) {
            WebView.setWebContentsDebuggingEnabled(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        }

        return mWebView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    public WebView getWebView() {
        return mWebView;
    }

    public boolean isWebViewCompatible() {
        return mWebViewIsCompatible;
    }

    private Integer getWebViewMajorVersion() {
        PackageManager pm = getActivity().getPackageManager();
        PackageInfo info;
        try {
            info = pm.getPackageInfo(WEB_VIEW_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e1) {
            try {
                info = pm.getPackageInfo(WEB_VIEW_PACKAGE_NAME_ALT, 0);
            } catch (PackageManager.NameNotFoundException e2) {
                Log.e(TAG, "Android System WebView is not installed");
                return null;
            }
        }

        Log.d(TAG, "Android System WebView: version " + info.versionName);
        String[] parts = info.versionName.split("\\.");
        return Integer.parseInt(parts[0]);
    }

    private void checkWebViewVersion() {
        Integer version = getWebViewMajorVersion();
        if (version == null) {
            showWebViewNotInstalledDialog();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mWebViewIsCompatible = true;
        } else if (version >= MIN_WEB_VIEW_VERSION) {
            mWebViewIsCompatible = true;
        } else {
            showWebViewNeedsUpdateDialog();
        }
    }

    private void showWebViewNotInstalledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.web_view_not_installed)
                .setTitle(R.string.incompatible_device)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getActivity().finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showWebViewNeedsUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.web_view_needs_update)
                .setTitle(R.string.update_web_view)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + WEB_VIEW_PACKAGE_NAME)
                            ));
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" + WEB_VIEW_PACKAGE_NAME)
                            ));
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getActivity().finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class PlaybookWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            switch (cm.messageLevel()) {
                case DEBUG:
                case LOG:
                case TIP:
                    Log.d(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
                case WARNING:
                    Log.w(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
                case ERROR:
                    Log.e(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
            }
            return true;
        }
    }
}
