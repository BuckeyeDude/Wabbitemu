package com.Revsoft.Wabbitemu.wizard.controller;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.activity.WabbitemuActivity;
import com.Revsoft.Wabbitemu.calc.CalcModel;
import com.Revsoft.Wabbitemu.extract.MsiDatabase;
import com.Revsoft.Wabbitemu.extract.MsiDatabase.CItem;
import com.Revsoft.Wabbitemu.extract.MsiHandler;
import com.Revsoft.Wabbitemu.utils.UserActivityTracker;
import com.Revsoft.Wabbitemu.wizard.WizardNavigationController;
import com.Revsoft.Wabbitemu.wizard.WizardPageController;
import com.Revsoft.Wabbitemu.wizard.data.FinishWizardData;
import com.Revsoft.Wabbitemu.wizard.data.OSDownloadData;
import com.Revsoft.Wabbitemu.wizard.view.ChooseOsPageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.CookieManager;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dorkbox.cabParser.CabException;
import dorkbox.cabParser.CabParser;
import dorkbox.cabParser.CabStreamSaver;
import dorkbox.cabParser.structure.CabFileEntry;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChooseOsPageController implements WizardPageController {

    private final ChooseOsPageView mView;

    private CalcModel mCalcModel;
    private String mAuthUrl;
    private FindOSDownloadsAsyncTask mLoadOsPageTask;
    private int mNextPage;
    private WizardNavigationController mNavController;
    private boolean mIsFinished;
    private boolean mIsFinalPage;
    private boolean mHasNextPage;
    private String mDownloadedOsPath;

    public ChooseOsPageController(@Nonnull ChooseOsPageView osPageView) {
        mView = osPageView;
    }

    @Override
    public void configureButtons(@Nonnull WizardNavigationController navController) {
        mNavController = navController;
        navController.hideNextButton();
        if (mIsFinished) {
            mNavController.finishWizard();
        }
    }

    @Override
    public boolean hasPreviousPage() {
        return true;
    }

    @Override
    public boolean hasNextPage() {
        return mHasNextPage;
    }

    @Override
    public boolean isFinalPage() {
        return mIsFinalPage;
    }

    @Override
    public int getNextPage() {
        return mNextPage;
    }

    @Override
    public int getPreviousPage() {
        return R.id.model_page;
    }

    @Override
    public void onHiding() {
        if (mLoadOsPageTask != null) {
            mLoadOsPageTask.cancel(true);
            mLoadOsPageTask = null;
        }
    }

    @Override
    public void onShowing(Object previousData) {
        mCalcModel = (CalcModel) previousData;
        mHasNextPage = false;
        mIsFinalPage = false;
        mIsFinished = false;
        mView.getMessage().setText(R.string.long_loading);
        mView.getLoadingSpinner().setVisibility(View.VISIBLE);
        mLoadOsPageTask = new FindOSDownloadsAsyncTask();
        mLoadOsPageTask.execute();
    }

    @Override
    public int getTitleId() {
        return R.string.osLoadingTitle;
    }

    @Override
    public Object getControllerData() {
        return mIsFinalPage ?
                new FinishWizardData(mCalcModel, mDownloadedOsPath, false) :
                new OSDownloadData(mCalcModel, mAuthUrl);
    }

    private enum NextAction {
        LOAD_MSI,
        LOAD_AUTHENTICATED_OS_PAGE,
        LOAD_UNAUTHENTICATED_OS_PAGE,
        ERROR
    }

    private static class NextOsAction {
        public final NextAction mNextAction;
        public final String mData;

        private NextOsAction(@Nonnull NextAction nextAction, @Nullable String data) {
            mNextAction = nextAction;
            mData = data;
        }
    }

    private class FindOSDownloadsAsyncTask extends AsyncTask<Void, Void, NextOsAction> {

        @Override
        protected NextOsAction doInBackground(Void... params) {
            final NextOsAction osAction = tryLoadOsPage();
            if (osAction != null) {
                return osAction;
            }
            final NextOsAction msiAction = tryLoadMsi();
            if (msiAction != null) {
                return msiAction;
            }

            return new NextOsAction(NextAction.ERROR, null);
        }

        private ByteArrayOutputStream outputStream;

        private NextOsAction tryLoadMsi() {
            final Pattern extensionPattern;
            final String extension;
            switch (mCalcModel) {
                case TI_73:
                    extensionPattern = Pattern.compile(".*\\.73u", Pattern.CASE_INSENSITIVE);
                    extension = ".73u";
                    break;
                case TI_83P:
                case TI_83PSE:
                    extensionPattern = Pattern.compile(".*83Plus.*\\.8xu", Pattern.CASE_INSENSITIVE);
                    extension = ".8xu";
                    break;
                case TI_84P:
                case TI_84PSE:
                    extensionPattern = Pattern.compile(".*84Plus.*\\.8xu", Pattern.CASE_INSENSITIVE);
                    extension = ".8xu";
                    break;
                case TI_84PCSE:
                    extensionPattern = Pattern.compile(".*\\.8cu", Pattern.CASE_INSENSITIVE);
                    extension = ".8cu";
                    break;
                default:
                    return null;
            }

            final CookieManager cookieManager = new CookieManager();
            final OkHttpClient connection = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();
            final String msiLink = tryLoadMsiPage(connection);
            if (msiLink == null) {
                return null;
            }

            final Request request = new Request.Builder()
                    .url(msiLink)
                    .addHeader("User-Agent", OsDownloadPageController.USER_AGENT)
                    .build();
            RandomAccessFile randomAccessFile = null;
            try {
                final Response response = connection.newCall(request).execute();
                final File msiFile = new File(WabbitemuActivity.sBestCacheDir, "msiFile.msi");
                final FileOutputStream fileOutputStream = new FileOutputStream(msiFile);
                fileOutputStream.write(response.body().bytes());
                fileOutputStream.close();

                randomAccessFile = new RandomAccessFile(msiFile, "r");
                final MsiDatabase msiDatabase = new MsiDatabase();
                msiDatabase.open(randomAccessFile);
                final MsiHandler msiHandler = new MsiHandler(msiDatabase);
                int i = 0;
                outputStream = null;
                for (final CItem item : msiDatabase.Items) {
                    if (item.getRealName().endsWith(".cab")) {
                        final byte[] test = msiHandler.GetStream(randomAccessFile, item, i);
                        final ByteArrayInputStream stream = new ByteArrayInputStream(test);
                        final CabParser cabParser = new CabParser(stream, new MyCabStreamSaver(extensionPattern));
                        cabParser.extractStream();
                        final File osFile = new File(WabbitemuActivity.sBestCacheDir, "osFile" + extension);
                        if (outputStream == null) {
                            return null;
                        }

                        final FileOutputStream osStream = new FileOutputStream(osFile);
                        osStream.write(outputStream.toByteArray());
                        osStream.close();
                        outputStream.close();
                        return new NextOsAction(NextAction.LOAD_MSI, osFile.getAbsolutePath());
                    }
                    i++;
                }
            } catch (IOException | CabException e) {
                UserActivityTracker.getInstance().reportBreadCrumb("Exception loading msi " + e);
            } finally {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        Log.w("Wabbitemu", "Failed to close file " + e);
                    }
                }
            }
            return null;
        }

        @Nullable
        private String tryLoadMsiPage(OkHttpClient connection) {
            final Document document;
            try {
                final Request pageRequest = new Request.Builder()
                        .url("https://epsstore.ti.com/OA_HTML/csksxvm.jsp;jsessionid=b401c39d98b4886b458efc7dd5d8327db3bc7777671e65db5db506a2e6bafa8c.e34TbNuKax4RaO0Mah0LaxaTchyRe0?jfn=ZGC7FD5432DD1749EE35764C594E5B43B3511EE256D94188614786F910B87AE331265643E242F68AFA6CE2579F26775AF7EC&lepopus=bE7LmZ2FxS3jD0l7eyTp1L9xkc&lepopus_pses=ZG6B09CB0A3807E876346D50579677E97CB0AB13CC596FF029053030558FF7479CA28505CDAD2053EE19E6BE618AC72AA757DCFBE5884A6B21&oas=eFTq1K0o_gpUMQl6PJojPw..&nSetId=130494&nBrowseCategoryId=10464&cskViewSolSourcePage=cskmbasicsrch.jsp%3FcategoryId%3D10464%26fRange%3Dnull%26fStartRow%3D0%26fSortBy%3D2%26fSortByOrder%3D1")
                        .addHeader("User-Agent", OsDownloadPageController.USER_AGENT)
                        .build();
                final Response pageResponse = connection.newCall(pageRequest).execute();
                document = Jsoup.parse(pageResponse.body().string());
            } catch (IOException e) {
                UserActivityTracker.getInstance().reportBreadCrumb("Exception loading msi page " + e);
                return null;
            }
            final Elements elements = document.select("#rightcol a");
            if (elements.isEmpty()) {
                return null;
            }

            final Element element = elements.iterator().next();
            return "https://epsstore.ti.com//OA_HTML/" + element.attr("href");
        }

        @Nullable
        private NextOsAction tryLoadOsPage() {
            final String urlString = getOsPageUrl();
            if (urlString == null) {
                return null;
            }
            final OkHttpClient connection = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", OsDownloadPageController.USER_AGENT)
                    .build();

            try {
                final Response response = connection.newCall(request).execute();
                final Document document = Jsoup.parse(response.body().string());
                final Elements elements = document.select(".column-downloaditem");
                for (Element element : elements) {
                    final Elements linkChildren = element.select("a");
                    for (Element linkChild : linkChildren) {
                        final String href = linkChild.attr("href");
                        if (href == null) {
                            continue;
                        }
                        if (href.toLowerCase().endsWith("8xu") || href.toLowerCase().endsWith("8cu")) {
                            final boolean isProtected = element.classNames().contains("protected-download");
                            final NextAction nextAction = (isProtected ?
                                    NextAction.LOAD_AUTHENTICATED_OS_PAGE :
                                    NextAction.LOAD_UNAUTHENTICATED_OS_PAGE);
                            return new NextOsAction(nextAction, href);
                        }
                    }
                }
            } catch (IOException e) {
                UserActivityTracker.getInstance().reportBreadCrumb("Failed to download os page " + e);
            }
            return null;
        }

        @Nullable
        private String getOsPageUrl() {
            switch (mCalcModel) {
                case TI_73:
                    return "https://education.ti.com/en/us/software/details/en/956CE30854A74767893104FCDF195B76/73ti73exploreroperatingsystem";
                case TI_84P:
                case TI_84PSE:
                    return "https://education.ti.com/en/us/software/details/en/B7DADA7FD4AA40CE9D7911B004B8C460/ti84plusoperatingsystem";
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(NextOsAction action) {
            final NextAction nextAction = action.mNextAction;
            switch (nextAction) {
                case LOAD_MSI:
                    mIsFinalPage = true;
                    mDownloadedOsPath = action.mData;
                    break;
                case LOAD_AUTHENTICATED_OS_PAGE:
                case LOAD_UNAUTHENTICATED_OS_PAGE:
                    mIsFinalPage = false;
                    final String authUrl = action.mData;
                    mAuthUrl = (authUrl != null && authUrl.startsWith("/") ? "https://education.ti.com" + authUrl : authUrl);
                    mNextPage = nextAction == NextAction.LOAD_AUTHENTICATED_OS_PAGE ? R.id.os_download_page : R.id.os_page;
                    mHasNextPage = true;
                    break;
                case ERROR:
                    mNavController.hideNextButton();
                    mView.getLoadingSpinner().setVisibility(View.GONE);
                    mView.getMessage().setText(R.string.errorWebPageDownloadError);
                    break;
            }

            // May happen if the async task finishes before the animation
            if (mNavController == null) {
                mIsFinished = true;
            } else {
                mNavController.finishWizard();
            }
        }

        private class MyCabStreamSaver implements CabStreamSaver {

            private final Pattern mExtension;

            public MyCabStreamSaver(Pattern extension) {
                mExtension = extension;
            }

            @Override
            public OutputStream openOutputStream(CabFileEntry cabFileEntry) {
                final String name = cabFileEntry.getName();
                if (!mExtension.matcher(name).matches() || outputStream != null) {
                    return null;
                }
                outputStream = new ByteArrayOutputStream((int) cabFileEntry.getSize());
                return outputStream;
            }

            @Override
            public void closeOutputStream(OutputStream outputStream, CabFileEntry cabFileEntry) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ignored) {

                    }
                }
            }

            @Override
            public boolean saveReservedAreaData(byte[] bytes, int i) {
                return false;
            }
        }
    }
}
