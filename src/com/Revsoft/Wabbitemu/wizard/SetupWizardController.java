package com.Revsoft.Wabbitemu.wizard;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewAnimator;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.UserActivityTracker;

public class SetupWizardController {

	private final Activity mActivity;
	private final ViewAnimator mViewFlipper;
	private final OnWizardFinishedListener mFinishedListener;
	private final Map<Integer, WizardPageController> mPageControllers = new HashMap<Integer, WizardPageController>();
	private final Map<Integer, Integer> mIdToPositionMap = new HashMap<Integer, Integer>();
	private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();

	private WizardPageController mCurrentController;
	private Object mPreviousData;

	public SetupWizardController(@NonNull Activity activity,
			@NonNull ViewAnimator viewFlipper,
			@NonNull OnWizardFinishedListener onFinishListener)
	{
		mActivity = activity;
		mViewFlipper = viewFlipper;
		mFinishedListener = onFinishListener;

		for (int i = 0; i < viewFlipper.getChildCount(); i++) {
			final int pageId= viewFlipper.getChildAt(i).getId();
			if (pageId == View.NO_ID) {
				throw new IllegalStateException("Child at index " + i + " missing id");
			}

			if (mIdToPositionMap.containsKey(pageId)) {
				throw new IllegalStateException("Duplicate page id " + pageId);
			}

			mIdToPositionMap.put(pageId, i);
		}
	}

	public void registerView(int pageId, @NonNull WizardPageController pageController) {
		mPageControllers.put(pageId, pageController);

		if (!mIdToPositionMap.containsKey(pageId)) {
			throw new IllegalStateException("View Id must be child of the view animator");
		}

		if (mCurrentController == null) {
			mCurrentController = pageController;
		}

		pageController.initialize(this);
	}

	public boolean moveNextPage() {
		if (mCurrentController.isFinalPage()) {
			mUserActivityTracker.reportBreadCrumb("Finishing final page");
			mFinishedListener.onWizardFinishedListener(mCurrentController.getControllerData());
			return true;
		}

		if (!mCurrentController.hasNextPage()) {
			return false;
		}

		setNextAnimation();
		final int nextPageId = mCurrentController.getNextPage();
		moveToPage(nextPageId);
		return true;
	}

	public boolean movePreviousPage() {
		if (!mCurrentController.hasPreviousPage()) {
			return false;
		}

		setBackAnimation();
		final int previousPageId = mCurrentController.getPreviousPage();
		moveToPage(previousPageId);
		return true;
	}

	private void moveToPage(final int nextPageId) {
		final WizardPageController lastController = mCurrentController;
		mCurrentController = mPageControllers.get(nextPageId);
		if (mCurrentController == null) {
			throw new IllegalStateException("Must have registered next page");
		}

		lastController.onHiding();
		mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);

		if (!mIdToPositionMap.containsKey(nextPageId)) {
			throw new IllegalStateException("Id is not registered " + nextPageId);
		}

		mUserActivityTracker.reportBreadCrumb("Moving to page %s from %s", mCurrentController, lastController);
		mPreviousData = lastController.getControllerData();
		mViewFlipper.setDisplayedChild(mIdToPositionMap.get(nextPageId));
		mCurrentController.onShowing(mPreviousData);
	}

	private void setNextAnimation() {
		mViewFlipper.setOutAnimation(mActivity, R.anim.out_to_left);
		mViewFlipper.setInAnimation(mActivity, R.anim.in_from_right);
	}

	private void setBackAnimation() {
		mViewFlipper.setOutAnimation(mActivity, R.anim.out_to_right);
		mViewFlipper.setInAnimation(mActivity, R.anim.in_from_left);
	}

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
			mActivity.setTitle(mCurrentController.getTitleId());
		}
	};
}
