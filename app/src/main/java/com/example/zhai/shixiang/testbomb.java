package com.example.zhai.shixiang;

/**
 * Created by Yingtong Dou on 2016/6/8.
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class testbomb extends BmobObject {

    private String playerName;
    private Integer score;
    private Boolean isPay;
    private BmobFile pic;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getIsPay() {
        return isPay;
    }

    public void setIsPay(Boolean isPay) {
        this. isPay = isPay;
    }

    public BmobFile getPic() {
        return pic;
    }

    public void setPic(BmobFile pic) {
        this.pic = pic;
    }
}
