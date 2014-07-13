package com.Revsoft.Wabbitemu.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.os.FileObserver;

public class RecursiveFileObserver extends FileObserver {

	public static int CHANGES_ONLY = CLOSE_WRITE | MOVE_SELF | MOVED_FROM;

	List<SingleFileObserver> mObservers;
	String mPath;
	int mMask;

	public RecursiveFileObserver(final String path) {
		this(path, ALL_EVENTS);
	}

	public RecursiveFileObserver(final String path, final int mask) {
		super(path, mask);
		mPath = path;
		mMask = mask;
	}

	@Override
	public void startWatching() {
		if (mObservers != null) {
			return;
		}

		mObservers = new ArrayList<SingleFileObserver>();
		final Stack<String> stack = new Stack<String>();
		stack.push(mPath);

		while (!stack.empty()) {
			final String parent = stack.pop();
			mObservers.add(new SingleFileObserver(parent, mMask));
			final File path = new File(parent);
			final File[] files = path.listFiles();
			if (files == null) {
				continue;
			}
			for (int i = 0; i < files.length; ++i) {
				if (files[i].isDirectory() && !files[i].getName().equals(".")
						&& !files[i].getName().equals("..")) {
					stack.push(files[i].getPath());
				}
			}
		}
		for (int i = 0; i < mObservers.size(); i++) {
			mObservers.get(i).startWatching();
		}
	}

	@Override
	public void stopWatching() {
		if (mObservers == null) {
			return;
		}

		for (int i = 0; i < mObservers.size(); ++i) {
			mObservers.get(i).stopWatching();
		}

		mObservers.clear();
		mObservers = null;
	}

	@Override
	public void onEvent(final int event, final String path) {

	}

	private class SingleFileObserver extends FileObserver {
		private final String mPath;

		public SingleFileObserver(final String path, final int mask) {
			super(path, mask);
			mPath = path;
		}

		@Override
		public void onEvent(final int event, final String path) {
			final String newPath = mPath + "/" + path;
			RecursiveFileObserver.this.onEvent(event, newPath);
		}

	}
}
