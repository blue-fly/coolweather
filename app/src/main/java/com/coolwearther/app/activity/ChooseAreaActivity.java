package com.coolwearther.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolwearther.app.R;
import com.coolwearther.app.db.CoolWeatherDB;
import com.coolwearther.app.model.City;
import com.coolwearther.app.model.County;
import com.coolwearther.app.model.Province;
import com.coolwearther.app.util.HttpCallbackListener;
import com.coolwearther.app.util.HttpUtil;
import com.coolwearther.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dellpc on 2015-08-30.
 */
public class ChooseAreaActivity extends Activity{


    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;


    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;

    //选中的省份
    private  Province selectedProvince;

    //选中的城市
    private City selectedCity;


    //当前选中的级别
    private int currentLevel;


    private ProgressDialog progressDialog;
    private ListView listview;
    private TextView textView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<String>();
    private CoolWeatherDB coolWeatherDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listview= (ListView) findViewById(R.id.list_item);
        textView= (TextView) findViewById(R.id.title_text);

        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listview.setAdapter(adapter);

        coolWeatherDB=CoolWeatherDB.getInstance(this);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    //加载城市
                    queryCities();

                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    //加载县
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String countyCode=countyList.get(position).getCountyCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //加载省级数据
        queryProvinces();

    }

    /*
    查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        provinceList=coolWeatherDB.loadProvinces();
        //从数据库中查
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }

            //刷新列表
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            textView.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else{
            //联网查
            queryFromServer(null,"province");
        }

    }

    /*
    查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }

            //刷新
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            textView.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /*
    查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        //市内所有的县
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }

            //刷新
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }

    }

    /*
    根据传入的代号和类型从服务器上查询省市县数据
     */
    private void queryFromServer(final String code,final String type){
        //网址
        String address;
        //如果不为空就是省级以下的
        if(!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            //就是省级的
            address="http://www.weather.com.cn/data/list3/city.xml";
        }

        //进度条
        //Log.d("ChooseAreaActivity","这里是进度条");
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if("city".equals(type)){
                    result=Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince
                            .getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity
                            .getId());
                }

                if(result){
                    //通过runOnUiThread方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });

                }

            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }


    /*
    显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /*
    关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /*
    捕获Back按键，根据当前的级别来判断，此时应该返回市列表省列表还是直接退出
     */
    @Override
    public void onBackPressed() {
        if(currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}
