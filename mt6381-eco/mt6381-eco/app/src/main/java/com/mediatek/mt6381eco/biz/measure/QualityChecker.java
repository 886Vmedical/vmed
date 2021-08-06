package com.mediatek.mt6381eco.biz.measure;

public class QualityChecker {
  private static final int QUAL_PPG1_HIGH = 25;
  //modify by herman -10
  private static final int QUAL_PPG1_MED = 20;
  private static final int QUAL_PPG1_LOW = 15;
  private static final int QUAL_PPG1_BAD = 5;
  private static final int QUAL_PPG2_HIGH = 25 - 5;
  //modify by herman -7
  private static final int QUAL_PPG2_MED = 20 - 5;
  private static final int QUAL_PPG2_LOW = 15 - 5;
  private static final int QUAL_PPG2_BAD = 5 - 5;
  private static final int QUAL_ECG_HIGH = 5;
  //modify by herman -3
  private static final int QUAL_ECG_MED = 4;
  private static final int QUAL_ECG_LOW = 3;
  private static final int QUAL_ECG_BAD = 0;

  private static final int QUAL_PPG1 = QUAL_PPG1_MED;
  private  static final int QUAL_PPG2 = QUAL_PPG2_MED;
  private static final int QUAL_ECG = QUAL_ECG_MED;

  private static final int QUAL_LOW_THRESHOLD = 512 * 2 / 12; // 2s
  private final QualityItemChecker[] checkers = new QualityItemChecker[] {
      new QualityItemChecker(QUAL_PPG1), new QualityItemChecker(QUAL_PPG2),
      new QualityItemChecker(QUAL_ECG)
  };

  public boolean checkEcgQuality(byte ecg_snr) {
    return checkers[2].checkQuality(ecg_snr);
  }

  public boolean checkPpg1Quality(byte ppg1_snr) {
    return checkers[0].checkQuality(ppg1_snr);
  }

  public boolean checkPpg2Quality(byte ppg2_snr) {
    return checkers[1].checkQuality(ppg2_snr);
  }

  public void reset() {
    for (QualityItemChecker item : checkers) {
      item.reset();
    }
  }

  private class QualityItemChecker {

    private static final boolean GOOD = true;
    private static final boolean BAD = false;
    private final int mQual;
    private int mLowCount = 0;
    private boolean mLastQuality = GOOD;

    private QualityItemChecker(int mQual) {
      this.mQual = mQual;
    }

    public boolean checkQuality(byte snr) {
      if (snr >= mQual) {
        mLastQuality = true;
        mLowCount = 0;
        return true;
      }
      //delete by xiaorang
      //if (mLowCount >= QUAL_LOW_THRESHOLD) {
      //  mLastQuality = false;
      //}
      //end
      ++mLowCount;
      return mLastQuality;
    }

    public void reset() {
      mLowCount = 0;
      mLastQuality = GOOD;
    }
  }
}
