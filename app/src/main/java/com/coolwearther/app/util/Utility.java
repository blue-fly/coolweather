package com.coolwearther.app.util;

import android.text.TextUtils;

import com.coolwearther.app.db.CoolWeatherDB;
import com.coolwearther.app.model.City;
import com.coolwearther.app.model.County;
import com.coolwearther.app.model.Province;

/**
 * Created by dellpc on 2015-08-30.
 */
public class Utility {

    /*
    解析和处理服务器返回的省级数据
     */
    public static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces=response.split(",");
            if(allProvinces!=null&&allProvinces.length>0){
                for(String str:allProvinces){
                    //代号|城市
                    Province province=new Province();
                    String[] array=str.split("\\|");
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }

                return true;
            }

        }
        return false;
    }


    /*
    解析和处理服务器返回的市级信息
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int
            provinceId){
        if(!TextUtils.isEmpty(response)){

            String[] allCities=response.split(",");
            if(allCities!=null&&allCities.length>0){

                for(String str:allCities){
                    City city=new City();
                    String[] array=str.split("\\|");
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);

                    //将解析出来的数据存储到city表
                    coolWeatherDB.saveCity(city);
                }

                return true;
            }
        }

        return false;

    }

    /*
    解析和处理服务器返回的县级信息
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int
            cityId){

        if(!TextUtils.isEmpty(response)){

            String[] allCounties=response.split(",");
            if(allCounties!=null&&allCounties.length>0){
                for(String str:allCounties){
                    County county=new County();
                    String[] array=str.split("\\|");

                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);

                    //将解析出来的数据存储到County表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }

        return false;
    }
}
