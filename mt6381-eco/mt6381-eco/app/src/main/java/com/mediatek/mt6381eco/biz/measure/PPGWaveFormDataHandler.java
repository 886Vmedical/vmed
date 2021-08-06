package com.mediatek.mt6381eco.biz.measure;

import com.mediatek.mt6381eco.biz.measure.view.DatatypeConverter;
import com.mediatek.mt6381eco.biz.measure.view.PPGFilterService;
import java.util.List;

public class PPGWaveFormDataHandler extends WaveFormDataHandler {
  float baseline;
  float delta;
  float[] buf_ppg_disp = new float[256];
  int idx_ppg_disp = 0;

  private final PPGFilterService ppgFilterService = new PPGFilterService();

  protected PPGWaveFormDataHandler(List<Float> data) {
    super(data);
  }

  @Override float toMv(int value) {
    return DatatypeConverter.ppg1ConvertToMv(value);
  }

  @Override float filter(float value) {
    float filtereddmv = ppgFilterService.filter(value);
    float drawMv;
    float tmp;
    float threshold, threshold2;
    float max;
    float min;

    // min/max over window size (2sec)
    min = buf_ppg_disp[0];
    max = buf_ppg_disp[0];
    for(int i=1; i < 256; i++) {
        if(buf_ppg_disp[i] > max) {
            max = buf_ppg_disp[i];
        }
        if(buf_ppg_disp[i] < min) {
            min = buf_ppg_disp[i];
        }
    }
    delta = max - min;
    baseline = (max + min) / 2;

    // data buffer
    buf_ppg_disp[idx_ppg_disp] = filtereddmv;
    idx_ppg_disp++;
    if(idx_ppg_disp >= 256) {
        idx_ppg_disp = 0;
    }

    // adjust signal scale based on max/min value
    if(delta < 1){
        delta = 1;
    }
    tmp = (filtereddmv - baseline) / delta;

    // display boundary is around -0.9 ~ 0.9
    threshold = 0.75f;
    threshold2 = 0.6f;
    if(tmp > threshold2) {
        tmp = threshold2 + (tmp - threshold2) / 2;
    } else if(tmp < -threshold2) {
        tmp = -threshold2 + (tmp + threshold2) / 2;
    }
      /*if(tmp > threshold) {
          tmp = threshold + (tmp - threshold) / 2;
      } else if(tmp > threshold2) {
          tmp = threshold2 + (tmp - threshold2) / 1.5f;
      } else if(tmp < -threshold) {
          tmp = -threshold + (tmp + threshold) / 2;
      } else if(tmp < -threshold2) {
          tmp = -threshold2 + (tmp + threshold2) / 1.5f;
      }*/
      drawMv = tmp;

    threshold = 0.9f;
    if(drawMv > threshold) {
        drawMv = threshold;
    } else if(drawMv < -threshold) {
        drawMv = -threshold;
    }

    return drawMv * -1;
  }
}
