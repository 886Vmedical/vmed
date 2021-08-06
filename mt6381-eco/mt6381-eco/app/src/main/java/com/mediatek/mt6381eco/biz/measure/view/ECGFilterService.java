package com.mediatek.mt6381eco.biz.measure.view;

public class ECGFilterService implements FilterService {
  int ecg_skip_cnt = 0;
  int ecg_skip_init = 64; //64/512 sec
  int i4EcgLpfOrder = 32;
  float[] f4EcgLpfCoeff = {
      0.004823f, -0.007923f, 0.000878f, 0.013837f, -0.019279f, 0.001859f, 0.026757f, -0.034345f,
      0.002673f, 0.044334f, -0.055630f, 0.003130f, 0.080969f, -0.114953f, 0.003291f, 0.550358f,
      0.550358f, 0.003291f, -0.114953f, 0.080969f, 0.003130f, -0.055630f, 0.044334f, 0.002673f,
      -0.034345f, 0.026757f, 0.001859f, -0.019279f, 0.013837f, 0.000878f, -0.007923f, 0.004823f
  };

  private final boolean initialized = false;
  private int i4EcgBufIndex = 0; // should be global variable
  private final float[] f4EcgBuf = new float[32]; // size = i4EcgLpfOrder

  private final float[] bw_ecg_delay_buf = new float[256];
  private int bw_ecg_cnt2;

  public ECGFilterService() {
    for (int i = 0; i < i4EcgLpfOrder; i++) {
      f4EcgBuf[i] = 0;
    }

    for (int i = 0; i < 256; i++) {
      bw_ecg_delay_buf[i] = 0;
    }
    bw_ecg_cnt2 = 0;
  }

  @Override public float filter(float data) {
    int fs = 128;
    float baseline;
    int half_second = fs / 4;
    float sum;

    if (bw_ecg_cnt2 == half_second) {
      bw_ecg_cnt2 = 0;
    }
    bw_ecg_delay_buf[bw_ecg_cnt2] = data;
    bw_ecg_cnt2++;

    if (ecg_skip_cnt < fs/2) {
      ecg_skip_cnt++;
      return 0;
    } else {
        sum = 0;
      for (int i = 0; i < half_second; i++) {
          sum += bw_ecg_delay_buf[i];
      }
      baseline = sum / half_second;

      f4EcgBuf[i4EcgBufIndex % i4EcgLpfOrder] = (data - baseline);
      float varLPF = 0;
      for (int idx = 0; idx < i4EcgLpfOrder; idx++) {
        varLPF += f4EcgLpfCoeff[idx] * f4EcgBuf[(i4EcgBufIndex + idx) % i4EcgLpfOrder];
      }
      i4EcgBufIndex = (i4EcgBufIndex + 1) % i4EcgLpfOrder;

      return varLPF;
    }
  }
}
