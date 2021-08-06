package com.mediatek.mt6381eco.network.model;

import java.util.List;

public class TemperatureRetrieveResponse {

  public MonthlyStatsBean monthlyStats;
  public List<TemperatureResult> data;

  public static class MonthlyStatsBean {
    //public MonthlyStats systolic;
    //public MonthlyStats diastolic;
    //public MonthlyStats spo2;
    //public MonthlyStats heartRate;
    //public MonthlyStats fatigue;
    //public MonthlyStats pressure;
    public MonthlyStats temperature;

    public static class MonthlyStats {
      public float avg;
      public float stdev;

      public float getHighVal() {
        return avg + stdev * 2;
      }

      public float getLowVal() {
        return avg - stdev * 2;
      }
    }
  }
}
