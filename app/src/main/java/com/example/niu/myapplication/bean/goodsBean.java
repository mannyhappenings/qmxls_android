package com.example.niu.myapplication.bean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.DataSupport;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by wanglei on 18-5-19.
 */

public class goodsBean extends DataSupport implements Serializable{
/*

    String goodsimg, String goodsName,
    double price, int number, String goodsId , String unit,String storeid,String storemobile ,String local_image, String product_no) {
*/

    private int id;
    private int goodsPic;
    @SerializedName("name")
    private String goodsName;
    @SerializedName("sell_price")
    private double price;
    private String goodsId;
    private String unit;
    @SerializedName("sort")
    private int number;
    private String changeType;
    private String goodsimg;
    private String storeid;
    private String storemobile;
    private String local_image;
    private String product_no;


      public String getProduct_no() {
        return product_no;
    }

    public void setProduct_no(String product_no) {
        this.product_no = product_no;
    }

    public String getStoreid() {
        return storeid;
    }

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public String getStoremobile() {
        return storemobile;
    }

    public void setStoremobile(String storemobile) {
        this.storemobile = storemobile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public goodsBean(String goodsimg, String goodsName,
                     double price, int number, String goodsId , String unit,String storeid,String storemobile ,String local_image, String product_no) {
        this.goodsName = goodsName;
        this.goodsimg = goodsimg;
        this.price = price;
        this.goodsId = goodsId;
        this.unit = unit;
        this.number = number;
        this.storeid = storeid;
        this.storemobile = storemobile;
        this.local_image = local_image;
        this.product_no = product_no;
    }

    public String getGoodsimg() {
        return goodsimg;
    }

    public void setGoodsimg(String goodsimg) {
        this.goodsimg = goodsimg;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "goodsBean{" +
                "goodsPic=" + goodsPic +
                ", goodsName='" + goodsName + '\'' +
                ", price='" + price + '\'' +
                ", goodsimg='" + goodsimg + '\'' +
                ", goodsId='" + goodsId + '\'' +
                ", number=" + number +
                '}';
    }

    public int getGoodsPic() {
        return goodsPic;
    }

    public goodsBean(String goodsimg, String goodsName, double price, int number,String goodsId ) {
        this.goodsimg = goodsimg;
        this.goodsName = goodsName;
        this.price = price;
        this.goodsId = goodsId;
        this.number = number;
    }

    public goodsBean(String goodsimg, String goodsName, double price) {
        this.goodsimg = goodsimg;
        this.goodsName = goodsName;
        this.price = price;
    }

    public goodsBean() {
    }

    public goodsBean(String goodsimg, String goodsName, double price, int number) {
        this.goodsimg = goodsimg;
        this.goodsName = goodsName;
        this.price = price;
        this.number = number;
    }

    public void setGoodsPic(int goodsPic) {
        this.goodsPic = goodsPic;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }



    public String getLocal_image() {
        return local_image;
    }

    public void setLocal_image(String local_image) {
        this.local_image = local_image;
    }
}
