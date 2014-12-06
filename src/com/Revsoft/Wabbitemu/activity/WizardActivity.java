package com.Revsoft.Wabbitemu.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.fragment.BrowseFragment;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionActivity;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionEvent;
import com.Revsoft.Wabbitemu.utils.BrowseCallback;
import com.Revsoft.Wabbitemu.utils.ErrorUtils;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.OSDownloader;
import com.Revsoft.Wabbitemu.utils.SpinnerDropDownAdapter;
import com.Revsoft.Wabbitemu.utils.UserActivityTracker;
import com.google.android.gms.ads.AdView;

public class WizardActivity extends Activity implements BrowseCallback {

	private class OsTypeButtonClickListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			final RadioButton button = (RadioButton) v;
			if (!button.isChecked()) {
				return;
			}

			switch (button.getId()) {
			case R.id.browseOsRadio:
				mFinishButton.setText(R.string.next);
				break;
			case R.id.downloadOsRadio:
				mFinishButton.setText(R.string.finish);
				break;
			}
		}
	}

	private static final int BROWSE_CODE = 0;

	private static final int GETTING_STARTED_CHILD = 0;
	private static final int CALC_TYPE_CHILD = 1;
	private static final int OS_SELECTION_CHILD = 2;
	private static final int BROWSE_ROM_CHILD = 3;
	private static final int BROWSE_OS_CHILD = 4;

	private final AtomicBoolean mIsTransitioningPages = new AtomicBoolean();
	private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();
	private final AnimationListener mAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart(final Animation animation) {
			// no-op
		}

		@Override
		public void onAnimationRepeat(final Animation animation) {
			// no-op
		}

		@Override
		public void onAnimationEnd(final Animation animation) {
			mViewFlipper.postDelayed(new Runnable() {

				@Override
				public void run() {
					mIsTransitioningPages.set(false);
				}
			}, 250);
		}
	};

	private int mCalcType;
	private int mCalcModel;
	private InputStream mBootStream;
	private String mCreatedFilePath;
	private String mOsFilePath;
	private String mBootPagePath;
	private Button mFinishButton;
	private RadioButton mBrowseOsRadio;
	private ViewFlipper mViewFlipper;

	private OSDownloader mOsDownloader;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserActivityTracker.reportActivityStart(this);

		setContentView(R.layout.wizard);
		setTitle(R.string.gettingStartedTitle);

		final TextView osTerms = (TextView) findViewById(R.id.osTerms);
		osTerms.setMovementMethod(LinkMovementMethod.getInstance());

		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		final Button step1NextButton = (Button) findViewById(R.id.nextStep1Button);
		step1NextButton.setOnClickListener(getNextButtonStep1OnClick());
		final Button step2NextButton = (Button) findViewById(R.id.nextStep2Button);
		step2NextButton.setOnClickListener(getNextButtonStep2OnClick());
		mFinishButton = (Button) findViewById(R.id.finishStep3Button);
		mFinishButton.setOnClickListener(getFinishOnClick());
		final Button step2BackButton = (Button) findViewById(R.id.backStep2Button);
		step2BackButton.setOnClickListener(getBackButtonOnClick());
		final Button step3BackButton = (Button) findViewById(R.id.backStep3Button);
		step3BackButton.setOnClickListener(getBackButtonOnClick());
		final Button step4BackButton = (Button) findViewById(R.id.backStep4Button);
		step4BackButton.setOnClickListener(getBackButtonOnClick());
		final Button step5BackButton = (Button) findViewById(R.id.backStep5Button);
		step5BackButton.setOnClickListener(getBackButtonOnClick());
		final OnClickListener osTypeClickListener = new OsTypeButtonClickListener();
		mBrowseOsRadio = (RadioButton) findViewById(R.id.browseOsRadio);
		mBrowseOsRadio.setOnClickListener(osTypeClickListener);
		final RadioButton downloadOsRadio = (RadioButton) findViewById(R.id.downloadOsRadio);
		downloadOsRadio.setOnClickListener(osTypeClickListener);

		final AdView adView = (AdView) findViewById(R.id.adView1);
		final AdView adView2 = (AdView) findViewById(R.id.adView2);
		final AdView adView3 = (AdView) findViewById(R.id.adView3);
		AdUtils.loadAd(getResources(), adView);
		AdUtils.loadAd(getResources(), adView2);
		AdUtils.loadAd(getResources(), adView3);
	}

	@Override
	public void onStop() {
		super.onStop();
		mUserActivityTracker.reportActivityStart(this);

		if (mOsDownloader != null) {
			mOsDownloader.cancel(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wizard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.helpMenuItem:
			mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY, UserActionEvent.HELP);

			final AlertDialog.Builder builder = new AlertDialog.Builder(WizardActivity.this);
			builder.setMessage(R.string.aboutRomDescription).setTitle(R.string.aboutRomTitle)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int id) {
					dialog.dismiss();
				}
			});
			final AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case BROWSE_CODE:
			if (resultCode == RESULT_OK) {
				WizardActivity.this.setResult(RESULT_OK, data);
				WizardActivity.this.finish();
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mViewFlipper.getDisplayedChild() == 0) {
			super.onBackPressed();
		} else {
			getBackButtonOnClick().onClick(mViewFlipper.getCurrentView());
		}
	}

	private void launchBrowseRom() {
		final String extensions = "\\.(rom|sav)";
		final String description = getResources().getString(R.string.browseRomDescription);

		launchBrowse(extensions, description, R.id.browseRomFragment);
	}

	private void launchBrowseOs() {
		final RadioGroup group = (RadioGroup) findViewById(R.id.setupStep2RadioGroup);
		final String extensions;
		mCalcType = group.getCheckedRadioButtonId();
		switch (mCalcType) {
		case R.id.ti73Radio:
			extensions = "\\.73u";
			break;
		case R.id.ti84pcseRadio:
			extensions = "\\.8cu";
			break;
		default:
			extensions = "\\.8xu";
			break;
		}

		final String description = getResources().getString(R.string.browseOSDescription);
		launchBrowse(extensions, description, R.id.browseOsFragment);
	}

	private void launchBrowse(final String extensions, final String description, final int fragId) {
		final Bundle setupBundle = new Bundle();
		setupBundle.putString(IntentConstants.EXTENSION_EXTRA_REGEX, extensions);
		setupBundle.putString(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING, description);
		setupBundle.putInt(IntentConstants.RETURN_ID, mViewFlipper.getDisplayedChild());

		final BrowseFragment fragInfo = new BrowseFragment();
		fragInfo.setArguments(setupBundle);

		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(fragId, fragInfo);
		transaction.commit();
	}

	private OnClickListener getNextButtonStep1OnClick() {
		final RadioGroup group = (RadioGroup) findViewById(R.id.setupStep1RadioGroup);
		return new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (mIsTransitioningPages.getAndSet(true)) {
					return;
				}

				setTitle(R.string.calculatorTypeTitle);
				setNextAnimation();
				final int radioId = group.getCheckedRadioButtonId();
				switch (radioId) {
				case R.id.browseRomRadio:
					setTitle(R.string.browseRomTitle);
					mViewFlipper.setDisplayedChild(BROWSE_ROM_CHILD);
					launchBrowseRom();
					break;
				case R.id.createWizardRadio:
					setTitle(R.string.calculatorTypeTitle);
					mViewFlipper.showNext();
					break;
				}
			}
		};
	}

	private OnClickListener getNextButtonStep2OnClick() {
		final RadioGroup group = (RadioGroup) findViewById(R.id.setupStep2RadioGroup);
		return new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (mIsTransitioningPages.getAndSet(true)) {
					return;
				}

				setTitle(R.string.osSelectionTitle);
				setNextAnimation();

				final Spinner spinner = (Spinner) findViewById(R.id.osVersionSpinner);
				final List<String> items = new ArrayList<String>();

				mCalcType = group.getCheckedRadioButtonId();
				switch (mCalcType) {
				case R.id.ti73Radio:
					items.add("1.91");
					break;
				case R.id.ti83pRadio:
				case R.id.ti83pseRadio:
					items.add("1.19");
					break;
				case R.id.ti84pRadio:
				case R.id.ti84pseRadio:
					items.add("2.55 MP");
					items.add("2.43");
					break;
				case R.id.ti84pcseRadio:
					items.add("4.2");
					items.add("4.0");
					break;
				}

				final SpinnerAdapter adapter = new SpinnerDropDownAdapter(WizardActivity.this, items);
				spinner.setAdapter(adapter);
				spinner.setSelection(0);
				mViewFlipper.showNext();
			}
		};
	}

	private OnClickListener getFinishOnClick() {
		return new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (mIsTransitioningPages.getAndSet(true)) {
					return;
				}

				setNextAnimation();

				if (mBrowseOsRadio.isChecked()) {
					setTitle(R.string.browseOSTitle);
					mViewFlipper.setDisplayedChild(BROWSE_OS_CHILD);
					launchBrowseOs();
				} else {
					createRomDownloadOs();
				}
			}
		};
	}

	private void createRomCopyOs() {
		extractBootpage();

		mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY, UserActionEvent.BOOTFREE_ROM);
		final int error = CalcInterface.CreateRom(mOsFilePath, mBootPagePath, mCreatedFilePath, mCalcModel);

		if (error == 0) {
			finishSuccess(mCreatedFilePath);
		} else {
			finishRomError();
		}
	}

	private void extractBootpage() {
		final Resources resources = getResources();
		final File cache = getCacheDir();
		mCreatedFilePath = cache.getAbsolutePath() + "/";

		final File bootPagePath;
		try {
			bootPagePath = File.createTempFile("boot", ".hex", cache);
		} catch (final IOException e) {
			return;
		}

		switch (mCalcType) {
		case R.id.ti73Radio:
			mCreatedFilePath += resources.getString(R.string.ti73);
			mBootStream = resources.openRawResource(R.raw.bf73);
			mCalcModel = CalcInterface.TI_73;
			break;
		default:
		case R.id.ti83pRadio:
			mCreatedFilePath += resources.getString(R.string.ti83p);
			mBootStream = resources.openRawResource(R.raw.bf83pbe);
			mCalcModel = CalcInterface.TI_83P;
			break;
		case R.id.ti83pseRadio:
			mCreatedFilePath += resources.getString(R.string.ti83pse);
			mBootStream = resources.openRawResource(R.raw.bf83pse);
			mCalcModel = CalcInterface.TI_83PSE;
			break;
		case R.id.ti84pRadio:
			mCreatedFilePath += resources.getString(R.string.ti84p);
			mBootStream = resources.openRawResource(R.raw.bf84pbe);
			mCalcModel = CalcInterface.TI_84P;
			break;
		case R.id.ti84pseRadio:
			mCreatedFilePath += resources.getString(R.string.ti84pse);
			mBootStream = resources.openRawResource(R.raw.bf84pse);
			mCalcModel = CalcInterface.TI_84PSE;
			break;
		case R.id.ti84pcseRadio:
			mCreatedFilePath += resources.getString(R.string.ti84pcse);
			mBootStream = resources.openRawResource(R.raw.bf84pcse);
			mCalcModel = CalcInterface.TI_84PCSE;
			break;
		}

		mCreatedFilePath += ".rom";

		FileOutputStream outputStream = null;
		try {
			final byte[] buffer = new byte[4096];
			outputStream = new FileOutputStream(bootPagePath);
			while (mBootStream.read(buffer) != -1) {
				outputStream.write(buffer, 0, 4096);
			}
		} catch (final IOException e) {
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

		mBootPagePath = bootPagePath.getAbsolutePath();
	}

	private void createRomDownloadOs() {
		extractBootpage();

		final Spinner spinner = (Spinner) findViewById(R.id.osVersionSpinner);
		final int osVersion = spinner.getSelectedItemPosition();
		final File cache = getCacheDir();
		final File osDownloadPath;
		try {
			osDownloadPath = File.createTempFile("tios", ".8xu", cache);
		} catch (final IOException e) {
			return;
		}

		mOsFilePath = osDownloadPath.getAbsolutePath();
		mOsDownloader = new OSDownloader(this, mOsFilePath) {

			@Override
			protected void onPostExecute(final Boolean success) {
				super.onPostExecute(success);

				if (success) {
					final int error = CalcInterface.CreateRom(mOsFilePath, mBootPagePath, mCreatedFilePath, mCalcModel);
					if (error == 0) {
						finishSuccess(mCreatedFilePath);
					} else {
						finishRomError();
					}
				} else {
					finishOsError();
				}
			}
		};
		mOsDownloader.execute(mCalcModel, osVersion);
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
		ErrorUtils.showErrorDialog(this, R.string.errorOsDownloadDescription);
	}

	private void showRomError() {
		ErrorUtils.showErrorDialog(this, R.string.errorRomCreateDescription);
	}

	private OnClickListener getBackButtonOnClick() {
		return new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (mIsTransitioningPages.getAndSet(true)) {
					return;
				}

				setBackAnimation(mViewFlipper);

				switch (mViewFlipper.getDisplayedChild()) {
				case CALC_TYPE_CHILD:
					setTitle(R.string.gettingStartedTitle);
					break;
				case OS_SELECTION_CHILD:
					setTitle(R.string.calculatorTypeTitle);
					break;
				case BROWSE_ROM_CHILD:
					setTitle(R.string.gettingStartedTitle);
					mViewFlipper.setDisplayedChild(GETTING_STARTED_CHILD);
					return;
				case BROWSE_OS_CHILD:
					setTitle(R.string.osSelectionTitle);
					mViewFlipper.setDisplayedChild(OS_SELECTION_CHILD);
					return;
				}

				mViewFlipper.showPrevious();
			}
		};
	}

	private void setNextAnimation() {
		mViewFlipper.setOutAnimation(this, R.anim.out_to_left);
		mViewFlipper.setInAnimation(this, R.anim.in_from_right);
		setAnimationListeners();
	}

	private void setBackAnimation(final ViewFlipper flipper) {
		mViewFlipper.setOutAnimation(this, R.anim.out_to_right);
		mViewFlipper.setInAnimation(this, R.anim.in_from_left);
		setAnimationListeners();
	}

	private void setAnimationListeners() {
		mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);
		mViewFlipper.getOutAnimation().setAnimationListener(mAnimationListener);
	}

	@Override
	public void callback(final int returnId, final String fileName) {
		switch (returnId) {
		case BROWSE_OS_CHILD:
			mOsFilePath = fileName;
			createRomCopyOs();
			break;
		case BROWSE_ROM_CHILD:
			mUserActivityTracker.reportUserAction(UserActionActivity.WIZARD_ACTIVITY, UserActionEvent.HAVE_OWN_ROM);

			finishSuccess(fileName);
			break;
		}
	}
}
