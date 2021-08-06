package com.mediatek.blenativewrapper.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class IteratorUtils {

  public  static<T> Iterator<T> just(T item){
    ArrayList<T> list = new ArrayList<>();
    list.add(item);
    return list.iterator();
  }

}
