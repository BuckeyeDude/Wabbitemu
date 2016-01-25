package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.ViewAnimator;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionActivity;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionEvent;
import com.Revsoft.Wabbitemu.utils.ErrorUtils;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.OSDownloader;
import com.Revsoft.Wabbitemu.utils.UserActivityTracker;
import com.Revsoft.Wabbitemu.utils.ViewUtils;
import com.Revsoft.Wabbitemu.wizard.OnWizardFinishedListener;
import com.Revsoft.Wabbitemu.wizard.WizardController;
import com.Revsoft.Wabbitemu.wizard.controller.BrowseOsPageController;
import com.Revsoft.Wabbitemu.wizard.controller.BrowseRomPageController;
import com.Revsoft.Wabbitemu.wizard.controller.CalcModelPageController;
import com.Revsoft.Wabbitemu.wizard.controller.LandingPageController;
import com.Revsoft.Wabbitemu.wizard.controller.OsDownloadPageController;
import com.Revsoft.Wabbitemu.wizard.controller.OsPageController;
import com.Revsoft.Wabbitemu.wizard.data.FinishWizardData;
import com.Revsoft.Wabbitemu.wizard.view.BrowseOsPageView;
import com.Revsoft.Wabbitemu.wizard.view.BrowseRomPageView;
import com.Revsoft.Wabbitemu.wizard.view.LandingPageView;
import com.Revsoft.Wabbitemu.wizard.view.ModelPageView;
import com.Revsoft.Wabbitemu.wizard.view.OsDownloadPageView;
import com.Revsoft.Wabbitemu.wizard.view.OsPageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WizardActivity extends Activity {

	private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();

	private WizardController mWizardController;
	private String mCreatedFilePath;
	private boolean mIsWizardFinishing;

	private OSDownloader mOsDownloader;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserActivityTracker.initializeIfNecessary(getApplicationContext());
		mUserActivityTracker.reportActivityStart(this);

		setContentView(R.layout.wizard);

		final ViewAnimator viewAnimator = ViewUtils.findViewById(this, R.id.viewFlipper, ViewAnimator.class);
		final ViewGroup navContainer = ViewUtils.findViewById(this, R.id.navContainer, ViewGroup.class);
		mWizardController = new WizardController(this, viewAnimator, navContainer, new OnWizardFinishedListener() {

			@Override
			public void onWizardFinishedListener(Object finalData) {
				if (mIsWizardFinishing) {
					return;
				}
				mIsWizardFinishing = true;

				final FinishWizardData finishInfo = (FinishWizardData) finalData;
				if (finishInfo == null) {
					ErrorUtils.showErrorDialog(WizardActivity.this, R.string.errorRomImage);
					return;
				}

				final int calcModel = finishInfo.getCalcModel();
				mUserActivityTracker.reportBreadCrumb("User finished wizard. Model: %s", calcModel);

				if (finishInfo.shouldDownloadOs()) {
					mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY,
							UserActionEvent.BOOTFREE_ROM);
					tryDownloadAndCreateRom(calcModel, finishInfo.getDownloadCode());
				} else if (calcModel == CalcInterface.NO_CALC) {
					mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY,
							UserActionEvent.HAVE_OWN_ROM);
					finishSuccess(finishInfo.getFilePath());
				} else {
					mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY,
							UserActionEvent.BOOTFREE_ROM);
					createRomCopyOs(calcModel, finishInfo.getFilePath());
				}
			}
		});

		final LandingPageView landingPageView = ViewUtils.findViewById(this, R.id.landing_page, LandingPageView.class);
		mWizardController.registerView(R.id.landing_page, new LandingPageController(landingPageView));

		final ModelPageView modelPageView = ViewUtils.findViewById(this, R.id.model_page, ModelPageView.class);
		mWizardController.registerView(R.id.model_page, new CalcModelPageController(modelPageView));

		final OsPageView osPageView = ViewUtils.findViewById(this, R.id.os_page, OsPageView.class);
		mWizardController.registerView(R.id.os_page, new OsPageController(osPageView));

		final OsDownloadPageView osDownloadPageView = ViewUtils.findViewById(this, R.id.os_download_page,
				OsDownloadPageView.class);
		mWizardController.registerView(R.id.os_download_page, new OsDownloadPageController(osDownloadPageView));

		final BrowseOsPageView browseOsPageView = ViewUtils.findViewById(this, R.id.browse_os_page,
				BrowseOsPageView.class);
		mWizardController.registerView(R.id.browse_os_page, new BrowseOsPageController(browseOsPageView,
				getFragmentManager()));

		final BrowseRomPageView browseRomPageView = ViewUtils.findViewById(this, R.id.browse_rom_page,
				BrowseRomPageView.class);
		mWizardController.registerView(R.id.browse_rom_page, new BrowseRomPageController(browseRomPageView,
				getFragmentManager()));
	}

	@Override
	protected void onPause() {
		super.onPause();

		cancelDownloadTask();
	}

	@Override
	public void onStop() {
		super.onStop();
		mUserActivityTracker.reportActivityStop(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		cancelDownloadTask();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.wizard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.helpMenuItem:
			mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY, UserActionEvent.HELP);

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final AlertDialog dialog = builder.setMessage(R.string.aboutRomDescription)
					.setTitle(R.string.aboutRomTitle)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							dialog.dismiss();
						}
					})
					.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (!mWizardController.movePreviousPage()) {
			super.onBackPressed();
		}
	}

	private void tryDownloadAndCreateRom(int calcModel, String downloadCode) {
		if (mOsDownloader != null && mOsDownloader.getStatus() == Status.RUNNING) {
			throw new IllegalStateException("Invalid state, download running");
		}

		if (!isOnline()) {
			final AlertDialog dialog = new AlertDialog.Builder(WizardActivity.this)
					.setMessage(getResources().getString(R.string.noNetwork))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							mIsWizardFinishing = false;
							dialog.dismiss();
						}
					})
					.create();
			dialog.show();
			return;
		}

		createRomDownloadOs(calcModel, downloadCode);
	}

	private boolean isOnline() {
		final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	private void createRomCopyOs(int calcModel, String osFilePath) {
		final String bootPagePath = extractBootpage(calcModel);
		if (bootPagePath == null) {
			finishRomError();
			return;
		}

		final int error = CalcInterface.CreateRom(osFilePath, bootPagePath, mCreatedFilePath, calcModel);

		mUserActivityTracker.reportBreadCrumb("Creating ROM given OS: %s model: %s error: %s",
				osFilePath, calcModel, error);
		if (error == 0) {
			finishSuccess(mCreatedFilePath);
		} else {
			finishRomError();
		}
	}

	private String extractBootpage(int calcModel) {
		final Resources resources = getResources();
		final File cache = getCacheDir();
		mCreatedFilePath = cache.getAbsolutePath() + "/";

		final File bootPagePath;
		try {
			bootPagePath = File.createTempFile("boot", ".hex", cache);
		} catch (final IOException e) {
			mUserActivityTracker.reportBreadCrumb("Error extracting bootpage %s", e);
			return null;
		}

		final InputStream bootStream;
		switch (calcModel) {
		case CalcInterface.TI_73:
			mCreatedFilePath += resources.getString(R.string.ti73);
			bootStream = resources.openRawResource(R.raw.bf73);
			break;
		default:
		case CalcInterface.TI_83P:
			mCreatedFilePath += resources.getString(R.string.ti83p);
			bootStream = resources.openRawResource(R.raw.bf83pbe);
			break;
		case CalcInterface.TI_83PSE:
			mCreatedFilePath += resources.getString(R.string.ti83pse);
			bootStream = resources.openRawResource(R.raw.bf83pse);
			break;
		case CalcInterface.TI_84P:
			mCreatedFilePath += resources.getString(R.string.ti84p);
			bootStream = resources.openRawResource(R.raw.bf84pbe);
			break;
		case CalcInterface.TI_84PSE:
			mCreatedFilePath += resources.getString(R.string.ti84pse);
			bootStream = resources.openRawResource(R.raw.bf84pse);
			break;
		case CalcInterface.TI_84PCSE:
			mCreatedFilePath += resources.getString(R.string.ti84pcse);
			bootStream = resources.openRawResource(R.raw.bf84pcse);
			break;
		}

		mCreatedFilePath += ".rom";

		FileOutputStream outputStream = null;
		try {
			final byte[] buffer = new byte[4096];
			outputStream = new FileOutputStream(bootPagePath);
			while (bootStream.read(buffer) != -1) {
				outputStream.write(buffer, 0, 4096);
			}
		} catch (final IOException e) {
			mUserActivityTracker.reportBreadCrumb("Error writing bootpage %s", e);
			finishRomError();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (final IOException e) {
				finishRomError();
			}
		}

		return bootPagePath.getAbsolutePath();
	}

	private void createRomDownloadOs(final int calcModel, final String downloadCode) {
		final String bootPagePath = extractBootpage(calcModel);
		if (bootPagePath == null) {
			finishRomError();
			return;
		}

		final Spinner spinner = (Spinner) findViewById(R.id.osVersionSpinner);
		final int osVersion = spinner.getSelectedItemPosition();
		final File cache = getCacheDir();
		final File osDownloadPath;
		try {
			osDownloadPath = File.createTempFile("tios", ".8xu", cache);
		} catch (final IOException e) {
			mUserActivityTracker.reportBreadCrumb("Error creating OS temp file: %s", e);
			return;
		}

		final String osFilePath = osDownloadPath.getAbsolutePath();
		mOsDownloader = new OSDownloader(this, osFilePath, calcModel, osVersion, downloadCode) {

			@Override
			protected void onPostExecute(final Boolean success) {
				super.onPostExecute(success);
				createRom(success, osFilePath, bootPagePath, calcModel);
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();

				mIsWizardFinishing = false;
			}
		};
		mOsDownloader.execute();
	}

	private void createRom(Boolean success, String osFilePath, String bootPagePath, int calcModel) {
		if (success) {
            final int error = CalcInterface.CreateRom(osFilePath, bootPagePath, mCreatedFilePath, calcModel);
            mUserActivityTracker.reportBreadCrumb("Creating ROM type: %s error: %s", calcModel, error);
            if (error == 0) {
                finishSuccess(mCreatedFilePath);
            } else {
                finishRomError();
            }
        } else {
            finishOsError();
        }
	}

	private void finishOsError() {
		showOsError();
	}

	private void finishRomError() {
		showRomError();
	}

	private void finishSuccess(String fileName) {
		final Intent resultIntent = new Intent();
		resultIntent.putExtra(IntentConstants.FILENAME_EXTRA_STRING, fileName);
		setResult(RESULT_OK, resultIntent);
		finish();
	}

	private void showOsError() {
		mIsWizardFinishing = false;
		ErrorUtils.showErrorDialog(this, R.string.errorOsDownloadDescription);
	}

	private void showRomError() {
		mIsWizardFinishing = false;
		ErrorUtils.showErrorDialog(this, R.string.errorRomCreateDescription);
	}

	private void cancelDownloadTask() {
		if (mOsDownloader != null) {
			mOsDownloader.cancel(true);
			mOsDownloader = null;
		}
	}
}
