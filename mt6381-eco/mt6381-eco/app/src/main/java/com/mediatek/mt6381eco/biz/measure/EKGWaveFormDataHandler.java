package com.mediatek.mt6381eco.biz.measure;

import com.mediatek.mt6381eco.biz.measure.view.DatatypeConverter;
import com.mediatek.mt6381eco.biz.measure.view.ECGFilterService;
import java.util.List;

public class EKGWaveFormDataHandler extends WaveFormDataHandler {
  private final ECGFilterService ecgFilterService = new ECGFilterService();

  public EKGWaveFormDataHandler(List<Float> data) {
    super(data);
  }

  @Override float toMv(int value) {
    return DatatypeConverter.ecgConvertToMv(value);
  }

  @Override float filter(float value) {
      float filter_mv = ecgFilterService.filter(value);
      float boudary_h = 0.9f;
      float boudary_l = -1.8f;

      if(filter_mv > boudary_h) {
          filter_mv = boudary_h;
      } else if( filter_mv < boudary_l) {
          filter_mv = boudary_l;
      }
      return filter_mv;
  }
}
