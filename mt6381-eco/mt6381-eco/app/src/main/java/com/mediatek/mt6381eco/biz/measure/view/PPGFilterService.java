package com.mediatek.mt6381eco.biz.measure.view;

public class PPGFilterService implements FilterService {
  private static final int I4PPGLPFORDER = 20; // Max 20
  private static final float[] P4PPGLPFCOEFF = {
      0.0030f, 0.0100f, 0.0210f, 0.0330f, 0.0470f, 0.0610f, 0.0730f, 0.0840f, 0.0910f, 0.0950f,
      0.0950f, 0.0910f, 0.0840f, 0.0730f, 0.0610f, 0.0470f, 0.0330f, 0.0210f, 0.0100f, 0.0030f
  };
  int ppg_skip_cnt = 0;
  private final float[] f4PpgBuf = new float[20];
  private boolean initialized = false;
  private int i4PpgBufIndex = 0;

  private final float[] bw_ppg_delay_buf = new float[256];
  private int bw_ppg_cnt;
  private float ppg_pre_value = 0;
  private int ppg_disp_state = 0;
  private float ppg_pre_diff = 0;
  private float ppg_keep_value = 0;
  private float ppg_keep_offset = 0;

  public PPGFilterService() {
    for (int i = 0; i < I4PPGLPFORDER; i++) {
      f4PpgBuf[i] = 0f;
    }

    for (int i = 0; i < 256; i++) {
        bw_ppg_delay_buf[i] = 0;
    }
      bw_ppg_cnt = 0;
  }

  @Override public float filter(float data) {
      int fs = 128;
      float baseline;
      int window = fs / 2;
      float sum;
      float diff;

      diff = data - ppg_pre_value;
      switch(ppg_disp_state){
          case 1:
              if( (diff > 0 && ppg_pre_diff <= 0) || (diff < 0 && ppg_pre_diff >= 0) ) {
                  ppg_disp_state = 0;
                  ppg_keep_offset = data - ppg_keep_value;
              }
              break;
          default:
              ppg_keep_value = ppg_pre_value - ppg_keep_offset;
              if((diff > 30 || diff < -30) && initialized) {
                  ppg_disp_state = 1;
              }
      }
      ppg_pre_diff = diff;
      ppg_pre_value = data;

      if(ppg_disp_state==1) {
          data = ppg_keep_value;
      } else {
          data -= ppg_keep_offset;
      }

      if (bw_ppg_cnt == window) {
          bw_ppg_cnt = 0;
      }
      bw_ppg_delay_buf[bw_ppg_cnt] = data;
      bw_ppg_cnt++;

      if (ppg_skip_cnt < fs/2) {
        ppg_skip_cnt++;
        return 0;
      } else {
        sum = 0;
        for (int i = 0; i < window; i++) {
          sum += bw_ppg_delay_buf[i];
        }
        baseline = sum / window;

        if (!initialized) {
          initialized = true;
        }
        f4PpgBuf[i4PpgBufIndex % I4PPGLPFORDER] = (data - baseline);
        float varLPF = 0;
        for (int idx = 0; idx < I4PPGLPFORDER; idx++) {
          varLPF += P4PPGLPFCOEFF[idx] * f4PpgBuf[(i4PpgBufIndex + idx) % I4PPGLPFORDER];
        }
        i4PpgBufIndex = (i4PpgBufIndex + 1) % I4PPGLPFORDER;
        return varLPF;
      }
  }
}
