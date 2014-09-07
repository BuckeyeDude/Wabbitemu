package com.Revsoft.Wabbitemu.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.os.Build;
import android.os.FileObserver;

public class FileUtils {

	private static final FileUtils INSTANCE = new FileUtils();

	public static FileUtils getInstance() {
		return INSTANCE;
	}

	private List<String> mFiles;
	private CountDownLatch mSearchLatch;
	private final List<FileObserver> mObservers;

	protected FileUtils() {
		final String regex = "\\.(rom|sav|([8|7][x|c|3|2|6|5][b|c|d|g|i|k|l|m|n|p|q|s|t|u|v|w|y|z]))$";

		mObservers = new ArrayList<FileObserver>();
		startFileSearch(regex);
	}

	public void startInitialSearch() {

	}

	private void startFileSearch(final String regex) {
		final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				mSearchLatch = new CountDownLatch(1);
			}

			@Override
			public Void doInBackground(final Void... params) {
				final String path;
				if (StorageUtils.hasExternalStorage()) {
					path = StorageUtils.getPrimaryStoragePath();
					mFiles = findValidFiles(regex, path);
					RecursiveFileObserver observer = getObserverForDir(path);
					mObservers.add(observer);
					observer.startWatching();

					final String extraStorage = System
							.getenv("SECONDARY_STORAGE");
					if (extraStorage != null && !"".equals(extraStorage)) {
						for (final String dir : extraStorage.split(":")) {
							mFiles.addAll(findValidFiles(regex, dir));
							observer = getObserverForDir(dir);
							mObservers.add(observer);
							observer.startWatching();
						}
					}
				} else {
					mFiles = new ArrayList<String>();
				}

				mSearchLatch.countDown();
				return null;
			}

			@Override
			protected void onPostExecute(final Void arg) {
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			asyncTask.execute();
		}
	}

	private RecursiveFileObserver getObserverForDir(final String observerPath) {
		return new RecursiveFileObserver(observerPath, FileObserver.CREATE
				| FileObserver.DELETE) {

			@Override
			public void onEvent(final int event, final String path) {
				switch (event) {
				case FileObserver.CREATE:
					final File file = new File(path);
					if (file.isFile()) {
						mFiles.add(file.getAbsolutePath());
					}
					break;
				case FileObserver.DELETE:
					for (int i = 0; i < mFiles.size(); i++) {
						if (mFiles.get(i).equals(path)) {
							mFiles.remove(i);
							break;
						}
					}
					break;
				}
			}
		};
	}

	public List<String> getValidFiles(final String extensionsRegex) {
		try {
			mSearchLatch.await();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return new ArrayList<String>();
		}

		final List<String> validFiles = new ArrayList<String>();
		for (final String file : mFiles) {
			if (isValidFile(extensionsRegex, file)) {
				validFiles.add(file);
			}
		}

		return validFiles;
	}

	private List<String> findValidFiles(final String extensionsRegex, final String dir) {
		final List<File> searchedFiles = new ArrayList<File>();
		final List<String> validFiles = new ArrayList<String>();

		final File rootDir = new File(dir);
		final File[] rootDirArray = rootDir.listFiles();
		if (rootDirArray == null) {
			return validFiles;
		}

		final List<File> filesToSearch = new ArrayList<File>();
		filesToSearch.addAll(Arrays.asList(rootDirArray));

		while (!filesToSearch.isEmpty()) {
			final File file = filesToSearch.remove(0);
			searchedFiles.add(file);

			if (file.isDirectory()) {
				final File[] subDirArray = file.listFiles(new FileFilter() {

					@Override
					public boolean accept(final File pathname) {
						if (pathname.isDirectory()) {
							return true;
						}

						return isValidFile(extensionsRegex, pathname.getName());
					}
				});
				if (subDirArray == null) {
					continue;
				}

				for (final File subFile : subDirArray) {
					if (!searchedFiles.contains(subFile)) {
						filesToSearch.add(subFile);
					}
				}
			} else if (file.isFile()) {
				final boolean isValid = isValidFile(extensionsRegex, file.getName());
				if (isValid) {
					validFiles.add(file.getAbsolutePath());
				}
			}
		}

		return validFiles;
	}

	private boolean isValidFile(final String extensionsRegex, final String file) {
		final String extension = getExtension(file);

		final Pattern pattern = Pattern.compile(extensionsRegex,
				Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(extension);
		return matcher.find();
	}

	private String getExtension(final String fileName) {
		final String extension;

		final int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i);
		} else {
			extension = "";
		}

		return extension;
	}
}
