package com.example.zhai.shixiang;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * Created by Yingtong Dou on 2016/6/8.
 */
public class PicInfo extends BmobObject {
    private BmobDate date;
    private BmobGeoPoint gpsAdd;
    private List<Byte> pic;

    public void setPicTime(Date date){
        this.date = new BmobDate(date);
    }
    public String getPicTime(){
        return this.date.getDate();
    }

    public BmobGeoPoint getGpsAdd() {
        return gpsAdd;
    }
    public void setGpsAdd(BmobGeoPoint gpsAdd) {
        this.gpsAdd = gpsAdd;
    }

    public List<Byte> getPic(){
        return this.pic;
    }
    public void setPic(List<Byte> pic){
        this.pic = pic;
    }
}
