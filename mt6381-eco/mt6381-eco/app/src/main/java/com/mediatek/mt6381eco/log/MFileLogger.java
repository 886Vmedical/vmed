package com.mediatek.mt6381eco.log;

import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MFileLogger {
  private final static long WRITE_TIMEOUT = 30L;
  private static File mDefaultFolder;
  private final File mFolder;
  private final String mFileNamePattern;
  private BufferedWriter mBufferedWriter = null;

  private long mLastWriteTimestamp = 0L;
  private File currentFile;

  public MFileLogger() {
    this(mDefaultFolder);
  }

  public MFileLogger(File folder) {
    this(folder, "yyyy_MM_dd_HH_mm_ss.SSS.'log'");
  }

  public MFileLogger(String mFileNamePattern) {
    this(mDefaultFolder, mFileNamePattern);
  }

  public MFileLogger(File folder, String fileNamePattern) {
    mFolder = folder;
    mFileNamePattern = fileNamePattern;
  }

  public static void setDefaultFolder(File folder) {
    mDefaultFolder = folder;
  }

  public void close() {
    if (mBufferedWriter != null) {
      try {
        mBufferedWriter.flush();
        mBufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      mBufferedWriter = null;
    }
  }

  public File getCurrentFile() {
    return currentFile;
  }

  public void reset() {
    close();
    currentFile = new File(mFolder, generateFileName());
    if (mFolder.exists() || mFolder.mkdirs()) {
      try {
        mBufferedWriter = new BufferedWriter(new FileWriter(currentFile, true));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      mBufferedWriter = null;
    }
  }

  public void write(String message, Object... args) {
    if (args != null && args.length > 0) {
      message = String.format(message, args);
    }

    try {
      if (mBufferedWriter != null) {
        mBufferedWriter.write(message);
        flushIfNeed();
      }
    } catch (IOException e) {
      Log.e(getClass().getSimpleName(), e.getMessage(), e);
    }
  }

  public void write(int c) {
    if (mBufferedWriter != null) {
      try {
        mBufferedWriter.write(c);
        flushIfNeed();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void write(char[] chars, int start, int len) {
    if (mBufferedWriter != null) {
      try {
        mBufferedWriter.write(chars, start, len);
        flushIfNeed();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void newLine() {
    if (mBufferedWriter != null) {
      try {
        mBufferedWriter.newLine();
        flushIfNeed();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void flushIfNeed() throws IOException {
    if (System.currentTimeMillis() - mLastWriteTimestamp > WRITE_TIMEOUT) {
      mBufferedWriter.flush();
      mLastWriteTimestamp = System.currentTimeMillis();
    }
  }

  public String generateFileName() {
    return new SimpleDateFormat(mFileNamePattern, Locale.getDefault()).format(
        System.currentTimeMillis());
  }
}
