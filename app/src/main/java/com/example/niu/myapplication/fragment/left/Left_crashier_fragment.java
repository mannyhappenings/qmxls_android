package com.example.niu.myapplication.fragment.left;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.niu.myapplication.App;
import com.example.niu.myapplication.BaseFragment;
import com.example.niu.myapplication.R;
import com.example.niu.myapplication.activity.MainActivity;
import com.example.niu.myapplication.adapter.GoodsSelectAdapter;
import com.example.niu.myapplication.bean.goodsBean;
import com.example.niu.myapplication.bean.orderIteminfo;
import com.example.niu.myapplication.bean.ordersBean;
import com.example.niu.myapplication.fragment.right.MessageEvent;
import com.example.niu.myapplication.fragment.right.RightFragmentType;
import com.example.niu.myapplication.utils.APPUtil;
import com.example.niu.myapplication.utils.AidlUtil;
import com.example.niu.myapplication.utils.AssetsUtil;
import com.example.niu.myapplication.utils.Data;
import com.example.niu.myapplication.utils.DataModel;
import com.example.niu.myapplication.utils.DecimalFormatUtils;
import com.example.niu.myapplication.utils.ESCUtil;
import com.example.niu.myapplication.utils.Hint;
import com.example.niu.myapplication.utils.RandomUntil;
import com.example.niu.myapplication.utils.SharePreferenceUtil;
import com.example.niu.myapplication.utils.ToastUtils;
import com.example.niu.myapplication.utils.UPacketFactory;
import com.example.niu.myapplication.utils.Xutils;
import com.nostra13.universalimageloader.utils.L;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import sunmi.ds.DSKernel;
import sunmi.ds.SF;
import sunmi.ds.callback.ICheckFileCallback;
import sunmi.ds.callback.IConnectionCallback;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.callback.ISendCallback;
import sunmi.ds.callback.ISendFilesCallback;
import sunmi.ds.callback.QueryCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;
import sunmi.ds.data.DataPacket;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by NIU on 2018/5/18.
 */

public class Left_crashier_fragment extends BaseFragment implements GoodsSelectAdapter.onItemClick {

    @BindView(R.id.rv_select_goods)
    RecyclerView rvSelectedGoods;
    String isPay="0";
    View alphaView;
    ZLoadingDialog dialog = new ZLoadingDialog(getActivity());

    private DSKernel mDSKernel = null;






    ArrayList<goodsBean> selectedGoodsArrayList;

    GoodsSelectAdapter goodsSelectAdapter;

    LinearLayoutManager linearLayoutManager;

    //存储当前商品Id,与数量
    Map<String, Integer> goodsSelectMap;

    //商品id 对应选择的商品
    Map<String, goodsBean> selectedGoodsMap;

    //挂单id对应所选商品
    Map<Integer,  Map<String, goodsBean>>pendOrderMap;
    //对应挂单次数
    private int pendOrderId;
    @BindView(R.id.iv_menu)
    ImageView ivMenu;
    @BindView(R.id.ll_top)
    RelativeLayout llTop;
    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.tv_total_discount)
    TextView tvTotalDiscount;
    @BindView(R.id.tv_all_cancel)
    TextView tvAllCancel;
    @BindView(R.id.tv_all_collection)
    TextView tvAllCollection;
    @BindView(R.id.ll_bottom_menu)
    LinearLayout llBottomMenu;
    @BindView(R.id.ll_empty_goods)
    LinearLayout llEmptyGoods;
    @BindView(R.id.rl_youhuiquan)
    RelativeLayout rlYouhuiquan;
    @BindView(R.id.youhuimoney)
    TextView youhuimoney;
    private onPendingOrderSucess onPendingOrderSucess;

    @BindView(R.id.ll_goods_items)
    RelativeLayout llGoodsItems;
    double totalMoney = 0.00;
    double youhuiMoney = 0.00;
    goodsBean goodsBean;
    private final String imgKey = "MAINSCREENIMG";
    //轮播图文件在本地缓存key
    private final String imgsKey = "MAINSCREENIMGS";
    /**
     * 轮播图集合
     * slide collection
     */
    List<String> imgs = new ArrayList<>();
    private MyHandler myHandler;

    private IConnectionCallback mIConnectionCallback = new IConnectionCallback() {
        @Override
        public void onDisConnect() {
            Message message = new Message();
            message.what = 1;
            message.obj = "与远程服务连接中断";
            myHandler.sendMessage(message);
        }

        @Override
        public void onConnected(ConnState state) {
            Message message = new Message();
            message.what = 1;
            switch (state) {
                case AIDL_CONN:
                    message.obj = "与远程服务绑定成功";
                    break;
                case VICE_SERVICE_CONN:
                    message.obj = "与副屏服务通讯正常";
                    break;
                case VICE_APP_CONN:
                    message.obj = "与副屏app通讯正常";
                    break;
                default:
                    break;
            }
            myHandler.sendMessage(message);
        }
    };

    private static class MyHandler extends Handler {
        private WeakReference<Activity> mActivity;

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() != null && !mActivity.get().isFinishing()) {
                switch (msg.what) {
                    case 1://消息提示用途
                        Toast.makeText(mActivity.get(), msg.obj + "", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private IReceiveCallback mIReceiveCallback = new IReceiveCallback() {
        @Override
        public void onReceiveData(DSData data) {

        }

        @Override
        public void onReceiveFile(DSFile file) {

        }

        @Override
        public void onReceiveFiles(DSFiles files) {

        }

        @Override
        public void onReceiveCMD(DSData cmd) {

        }
    };
    private IReceiveCallback mIReceiveCallback2 = new IReceiveCallback() {
        @Override
        public void onReceiveData(DSData data) {

        }

        @Override
        public void onReceiveFile(DSFile file) {

        }

        @Override
        public void onReceiveFiles(DSFiles files) {

        }

        @Override
        public void onReceiveCMD(DSData cmd) {

        }
    };

    @Override
    protected void initData() {
        myHandler = new MyHandler(getActivity());
        imgs.add(Environment.getExternalStorageDirectory().getPath() + "/f_shoukuan.png");
        initSdk();
//        mDSKernel = DSKernel.newInstance();
//        DSKernel mDSKernel =DSKernel.newInstance();
//        mDSKernel.init(context, mConnCallback);    //绑定服务的回调
//        mDSKernel.addReceiveCallback(mReceiveCallback);  //双屏通信接收数据回调
//        mDSKernel = DSKernel.newInstance();
//        mDSKernel.init(getActivity(), mIConnectionCallback);
//        mDSKernel.addReceiveCallback(mIReceiveCallback);
//        mDSKernel.addReceiveCallback(mIReceiveCallback2);
//        mDSKernel.removeReceiveCallback(mIReceiveCallback);
//        mDSKernel.removeReceiveCallback(mIReceiveCallback2);
        pendOrderMap = new HashMap<>();
        selectedGoodsMap = new LinkedHashMap<>();
        selectedGoodsArrayList = new ArrayList<>();
        goodsSelectMap = new HashMap<>();
        goodsSelectAdapter = new GoodsSelectAdapter(activity, selectedGoodsArrayList);
        goodsSelectAdapter.setOnItemClick(this);
        linearLayoutManager = new LinearLayoutManager(activity);
        rvSelectedGoods.setLayoutManager(linearLayoutManager);
        rvSelectedGoods.addItemDecoration(new DividerItemDecoration(activity,DividerItemDecoration.VERTICAL));
        rvSelectedGoods.setAdapter(goodsSelectAdapter);

    }


    @Override
    protected void initView(View rootView) {

        EventBus.getDefault().register(this);
        alphaView = LayoutInflater.from(activity)
                .inflate(R.layout.alpha_background_view, null);
    }

    @Override
    protected int getLayout() {
        return R.layout.cashier_left_fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.tv_all_collection, R.id.iv_menu, R.id.rl_youhuiquan})
    public void onViewClicked1(View view) {

        switch (view.getId()) {
            case R.id.tv_all_collection:


                //收款时 整单取消和收款不能点击
                setCancelAndClooectionEnable(false);
                ((MainActivity) getActivity()).showPayFragment();
                MessageEvent messageEvent = new MessageEvent("pay");
                messageEvent.setTotalMoney(totalMoney);
                //必须发两个，一个黏性，一个非黏性
                EventBus.getDefault().postSticky(messageEvent);
                EventBus.getDefault().post(messageEvent);

                App.store.put("goods",selectedGoodsMap.toString());
                App.store.commit();
                L.d("BBBBBB",selectedGoodsMap.toString());


//                String totalMoney = tvTotalMoney.getText().toString();

               /* if (totalMoney.length()!=0){

                    double money = Double.parseDouble(totalMoney);
                    messageEvent.setTotalMoney(money);
                    //必须发两个，一个黏性，一个非黏性
                    EventBus.getDefault().postSticky(messageEvent);
                    EventBus.getDefault().post(messageEvent);
                    Log.d(TAG, "onViewClicked1: send finish");
                }*/


                break;


            case R.id.rl_youhuiquan:
                rlYouhuiquan.setVisibility(View.GONE);
                tvTotalDiscount.setText("0.00");
                break;
            case R.id.iv_menu:



                ((MainActivity) activity).openSlideMenu();

                /*View view1 = LayoutInflater.from(activity)
                        .inflate(R.layout.slide_pop_view,null);
                //弹出一个popWindow
                PopupWindow popupWindow = new PopupWindow(view1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

                //设置popwindow type ji
                popupWindow.setWindowLayoutType(WindowManager.LayoutParams
                .TYPE_TOAST);

                view1.findViewById(R.id.item2)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, SettingActivity
                                .class);
                                activity.startActivity(intent);
                            }
                        });
                popupWindow.setAnimationStyle(R.style.AnimationLeftFade);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        removeAlphaView();
                    }
                });

                setWindowBackGround();
                popupWindow.showAtLocation(ivMenu, Gravity.NO_GRAVITY,0,0);

*/

               /* SlideMenuPopupWindow.show(activity,ivMenu,R.layout.slide_pop_view
                );

                setWindowBackGround();*/

                /*SlideMenuPopupWindow.setOnItem1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(new Intent(activity, MainActivity.class));
                         SlideMenuPopupWindow.disimiss();
                    }
                });

                SlideMenuPopupWindow.setOnItem2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(new Intent(activity, SettingActivity.class));
                        //SlideMenuPopupWindow.disimiss();

                    }
                });
                SlideMenuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        removeAlphaView();
                    }
                });
*/

                break;
        }


    }



    public void sendOrder(Map<String, goodsBean> selectedGoodsMap,String payment_num) {
        try {
            JSONObject object = new JSONObject(selectedGoodsMap.toString());

          String  a=  object.getString("total_amount");
          L.d("NNN",a);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = App.API_URL + "reta/order/receive";
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("order_no",  RandomUntil.getNumLargeLetter(18));
        stringMap.put("user_id", "0");
        stringMap.put("total_amount", tvTotalMoney.getText().toString());
        stringMap.put("amount", tvTotalMoney.getText().toString());
        stringMap.put("user_remarks", "");
        stringMap.put("seller_remarks", "");
        stringMap.put("trade_no", RandomUntil.getNumLargeLetter(18) );
        String payment_id="0";
        if (payment_num.substring(0,2).contains("10")||payment_num.substring(0,2).contains("11")||payment_num.substring(0,2).contains("12")||payment_num.substring(0,2).contains("13")||payment_num.substring(0,2).contains("14")||payment_num.substring(0,2).contains("15")){
            payment_id="010";
        }else {
            payment_id="020";
        }
//        stringMap.put("pay_type", payment_id);
//        stringMap.put("payment_id", payment_id);
        stringMap.put("minus_amount", "");
        stringMap.put("card_minus", "");
        stringMap.put("coupon_minus", "");
        stringMap.put("card_id", "");
        stringMap.put("coupon_id", "");
        stringMap.put("items", goodsSelectMap.toString());

//
//        Xutils.getInstance().post(url, stringMap, new Xutils.XCallBack() {
//            @Override
//            public void onResponse(String str) {
//
//                try {
//                    JSONObject mjsonObjects = new JSONObject(str);
//                    String result = mjsonObjects.getString("status");
//                    String message = mjsonObjects.getString("message");
//                    if (result.equals("true")) {
//                        JSONObject mjsonObject = mjsonObjects.getJSONObject("data");
////                        String username = mjsonObject.getString("username");
////
//                    } else {
//                        Hint.Short(getActivity(), message);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            protected Object clone() throws CloneNotSupportedException {
//                return super.clone();
//            }
//
//        });
    }
    public void sendPay(String auth_code) {
        final String order =RandomUntil.getNumLargeLetter(18);
        String url = App.API_URL + "reta/cashier/do-pay";
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("order_no", order);
        stringMap.put("amount", "0.01");
//        stringMap.put("amount", tvTotalMoney.getText().toString());
        String payment_id="0";
        if (auth_code.substring(0,2).contains("10")||auth_code.substring(0,2).contains("11")||auth_code.substring(0,2).contains("12")||auth_code.substring(0,2).contains("13")||auth_code.substring(0,2).contains("14")||auth_code.substring(0,2).contains("15")){
            payment_id="010";
        }else if (auth_code.substring(0,2).contains("28")){
            payment_id="020";
        }
        stringMap.put("pay_type", payment_id);
        stringMap.put("auth_code",auth_code);
        /**
         * *****************************本地数据库存储******************************
         */
        ordersBean mOrderbean = new ordersBean();
        mOrderbean.setUserid(App.store.getString("userid"));
        mOrderbean.setTotal_amount(totalMoney+"");
//                mOrderbean.setAmount();
        mOrderbean.setUser_remarks("");
        mOrderbean.setSeller_remarks("");
        mOrderbean.setTrade_no(order);
        mOrderbean.setPayment_id(payment_id);
        mOrderbean.setMinus_amount(youhuimoney.getText().toString());//优惠总金额
        mOrderbean.setCard_minus("");//会员卡优惠
        mOrderbean.setCoupon_minus("");//优惠券优惠
        mOrderbean.setCard_id("");
        mOrderbean.setCoupon_id("");

    List<orderIteminfo> orderIteminfoList = new ArrayList<>();

        for(Map.Entry<String, goodsBean> entry : selectedGoodsMap.entrySet()) {
            String namse= entry.getValue().getGoodsName();
            orderIteminfo items = new orderIteminfo();
            items.setData_id(entry.getValue().getGoodsId());//商品id
            items.setEntity_id(entry.getValue().getGoodsId());//实体id
            items.setProduct_no(entry.getValue().getProduct_no());//条形码
            items.setName(entry.getValue().getGoodsName());//
            items.setImage(entry.getValue().getGoodsimg());//
            items.setPrice(entry.getValue().getPrice()+"");//价格
            items.setQty(entry.getValue().getNumber()+"");//数量
            items.setSpec(entry.getValue().getUnit());//规格
            items.setMinus("");//优惠金额
            items.setMessages(""); //描述
            orderIteminfoList.add(items);//
        }
        /*******************************************************************************/
        Xutils.getInstance().post(url, stringMap, new Xutils.XCallBack() {
            @Override
            public void onResponse(String str) {
                try {
                    dialog.dismiss();
                    JSONObject mjsonObjects = new JSONObject(str);
                    String result = mjsonObjects.getString("status");
                    String message = mjsonObjects.getString("message");
                    if (result.equals("true")) {
                    String store= App.store.getString("storeinfo");
                    JSONObject storeinfo=new JSONObject(store);
                        AidlUtil.getInstance().printText(
                                storeinfo.getString("name"), 2, 50, true,
                                false, ESCUtil.alignCenter());
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        AidlUtil.getInstance().printText("销售订单:"+order , 2, 35, false,
                                false, ESCUtil.alignLeft());
                        AidlUtil.getInstance().printText("交易时间:"+format.format(date) , 1, 35, false,
                                false, ESCUtil.alignLeft());
                        AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                                null);
                        AidlUtil.getInstance().printLine(1);
                            for(Map.Entry<String, goodsBean> entry : selectedGoodsMap.entrySet()) {
                                String namse= entry.getValue().getGoodsName();
                                System.out.println(entry.getKey() + ": "  + entry.getValue()+namse);
                                AidlUtil.getInstance().printText(
                                        entry.getValue().getGoodsName() + "    x" + entry.getValue().getNumber(), 0, 35, false,
                                        false, ESCUtil.alignLeft());
                                AidlUtil.getInstance().printText("    ¥" + entry.getValue().getPrice(), 2, 35, false,
                                        false, null);
                                  }
                            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                        null);
                            AidlUtil.getInstance().printLine(1);
                        AidlUtil.getInstance().printText("合   计："+selectedGoodsMap.size(), 2, 35,
                                false, false, ESCUtil.alignLeft());
                        AidlUtil.getInstance().printText("订单金额："+tvTotalMoney.getText().toString(), 2, 35,
                                false, false, ESCUtil.alignLeft());
                        AidlUtil.getInstance().printText("实付金额："+tvTotalMoney.getText().toString(), 2, 35,
                                false, false, ESCUtil.alignLeft());

                        AidlUtil.getInstance().printText("优惠券  ：0.00" , 2, 35,
                                false, false, ESCUtil.alignLeft());

                        AidlUtil.getInstance().printText("支付方式：扫码支付" , 2, 35,
                                false, false, ESCUtil.alignLeft());

                        AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 1, 25, false, false,
                                null);
                        AidlUtil.getInstance().printLine(1);
                        AidlUtil.getInstance().printText("  谢谢您的惠顾，欢迎下次光临！"  , 2, 35, false,
                                false, null);

                        AidlUtil.getInstance().print3Line();

                        AidlUtil.getInstance().cutPrint();

                        MessageEvent  messageEvent = new MessageEvent("allDelete");
                        EventBus.getDefault().post(messageEvent);
                        ((MainActivity)activity).showRightCrashierLayout();
                    } else {
                        Hint.Short(getActivity(), message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ------------>" + (mDSKernel == null));
        if (mDSKernel != null) {
            mDSKernel.checkConnection();
        } else {
            initSdk();
        }
    }

    private void initSdk() {
        mDSKernel = DSKernel.newInstance();
        mDSKernel.init(getActivity(), mIConnectionCallback);
        mDSKernel.addReceiveCallback(mIReceiveCallback);
        mDSKernel.addReceiveCallback(mIReceiveCallback2);
        mDSKernel.removeReceiveCallback(mIReceiveCallback);
        mDSKernel.removeReceiveCallback(mIReceiveCallback2);
//        sendPicture();

        long imgTaskId = (long) SharePreferenceUtil.getParam(getActivity(), imgKey, 0L);
        checkImgFileExist(imgTaskId);
    }

    public void XianjinPay(String shifu,String zhaolin ) {
        final String order =RandomUntil.getNumLargeLetter(18);

        try {
//
            String store= App.store.getString("storeinfo");
            JSONObject storeinfo=new JSONObject(store);

            AidlUtil.getInstance().printText(
                    storeinfo.getString("name"), 2, 50, true,
                    false, ESCUtil.alignCenter());
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            AidlUtil.getInstance().printText("销售订单:"+order , 2, 35, false,
                    false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("交易时间:"+format.format(date) , 1, 35, false,
                    false, ESCUtil.alignLeft());

            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);
            for(Map.Entry<String, goodsBean> entry : selectedGoodsMap.entrySet()) {
                String namse= entry.getValue().getGoodsName();
                System.out.println(entry.getKey() + ": "  + entry.getValue()+namse);
                AidlUtil.getInstance().printText(
                        entry.getValue().getGoodsName() + "    x" + entry.getValue().getNumber(), 0, 35, false,
                        false, ESCUtil.alignLeft());
                AidlUtil.getInstance().printText("    ￥" + entry.getValue().getPrice(), 2, 35, false,
                        false, null);
            }
            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);

            AidlUtil.getInstance().printText("合   计："+selectedGoodsMap.size(), 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("订单金额："+tvTotalMoney.getText().toString(), 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("优惠金额  ：0.00" , 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("现金收款："+shifu+".00", 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("应付金额："+tvTotalMoney.getText().toString(), 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("找    零："+zhaolin , 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("支付方式：现金收款" , 2, 35,
                    false, false, ESCUtil.alignLeft());
//                    AidlUtil.getInstance().printText("支付方式：标记收款-"+payType , 2, 35,
//                            false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 1, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);
//            AidlUtil.getInstance().printText(" 本店地址"  , 2, 35, false,
//                    false, null);

            AidlUtil.getInstance().printText("  谢谢您的惠顾，欢迎下次光临！"  , 2, 35, false,
                    false, null);

            AidlUtil.getInstance().print3Line();

            AidlUtil.getInstance().cutPrint();

            MessageEvent  messageEvent = new MessageEvent("allDelete");
            EventBus.getDefault().post(messageEvent);
            ((MainActivity)activity).showRightCrashierLayout();
//                    } else {
//                        Hint.Short(getActivity(), message);
//                    }

        } catch (JSONException e) {
            e.printStackTrace();
        }

//            }
//
//            @Override
//            protected Object clone() throws CloneNotSupportedException {
//                return super.clone();
//            }
//
//        });
    }

    public void biaojiPay(String payType) {
        final String order =RandomUntil.getNumLargeLetter(18);
        try {
           String store= App.store.getString("storeinfo");
            JSONObject storeinfo=new JSONObject(store);

            AidlUtil.getInstance().printText(
                    storeinfo.getString("name"), 2, 50, true,
                    false, ESCUtil.alignCenter());
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            AidlUtil.getInstance().printText("销售订单:"+order , 2, 35, false,
                    false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("交易时间:"+format.format(date) , 1, 35, false,
                    false, ESCUtil.alignLeft());

            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);
            for(Map.Entry<String, goodsBean> entry : selectedGoodsMap.entrySet()) {
                String namse= entry.getValue().getGoodsName();
                System.out.println(entry.getKey() + ": "  + entry.getValue()+namse);
                AidlUtil.getInstance().printText(
                        entry.getValue().getGoodsName() + "    x" + entry.getValue().getNumber(), 0, 35, false,
                        false, ESCUtil.alignLeft());
                AidlUtil.getInstance().printText("    ￥" + entry.getValue().getPrice(), 2, 35, false,
                        false, null);
            }
            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 2, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);

            AidlUtil.getInstance().printText("合   计："+selectedGoodsMap.size(), 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("订单金额："+tvTotalMoney.getText().toString(), 2, 35,
                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("优惠金额  ：0.00" , 2, 35,
                    false, false, ESCUtil.alignLeft());
//            AidlUtil.getInstance().printText("现金收款："+shifu+".00", 2, 35,
//                    false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("应付金额："+tvTotalMoney.getText().toString(), 2, 35,
                    false, false, ESCUtil.alignLeft());
//            AidlUtil.getInstance().printText("找    零："+zhaolin , 2, 35,
//                    false, false, ESCUtil.alignLeft());

                    AidlUtil.getInstance().printText("支付方式：标记收款-"+payType , 2, 35,
                            false, false, ESCUtil.alignLeft());
            AidlUtil.getInstance().printText("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _", 1, 25, false, false,
                    null);
            AidlUtil.getInstance().printLine(1);
//            AidlUtil.getInstance().printText(" 本店地址"  , 2, 35, false,
//                    false, null);

            AidlUtil.getInstance().printText("  谢谢您的惠顾，欢迎下次光临！"  , 2, 35, false,
                    false, null);

            AidlUtil.getInstance().print3Line();

            AidlUtil.getInstance().cutPrint();

            MessageEvent  messageEvent = new MessageEvent("allDelete");
            EventBus.getDefault().post(messageEvent);
            ((MainActivity)activity).showRightCrashierLayout();
//                    } else {
//                        Hint.Short(getActivity(), message);
//                    }

        } catch (JSONException e) {
            e.printStackTrace();
        }

//            }
//
//            @Override
//            protected Object clone() throws CloneNotSupportedException {
//                return super.clone();
//            }
//
//        });
    }


    private void removeAlphaView() {

        ((ViewGroup) activity.getWindow().getDecorView().
                findViewById(android.R.id.content)).removeView(alphaView);
    }

    private void setWindowBackGround() {

        ViewGroup viewGroup = activity.getWindow().getDecorView().
                findViewById(android.R.id.content);


        viewGroup.addView(alphaView);

    }

    public void updateSelectedGoods(goodsBean goodsBean) {


        Log.d("wanglei", "updateSelectedGoods: goodsbean= "+goodsBean.toString());


        //   if (selectedGoodsMap.get(goodsBean.getGoodsId())!=null) {

        selectedGoodsMap.put(goodsBean.getGoodsId(), goodsBean);

        Log.d(TAG, "updateSelectedGoods: selectedGoodsMap= "+selectedGoodsMap.size());
        //   }

           /* for (int i = 0; i < selectedGoodsArrayList.size(); i++) {



                if (selectedGoodsArrayList.get(i).getGoodsId().equals(goodsBean.getGoodsId())){

                       selectedGoodsArrayList.set(i,goodsBean);
                }else {
                    selectedGoodsArrayList.add(goodsBean);
                }
            }

            if (selectedGoodsArrayList.size()==0){
                selectedGoodsArrayList.add(goodsBean);
            }*/

        selectedGoodsArrayList.clear();

        if (!selectedGoodsMap.isEmpty()) {
            for (Map.Entry<String, goodsBean> map :
                    selectedGoodsMap.entrySet()) {


                selectedGoodsArrayList.add(map.getValue());

            }

        }


        Log.d(TAG, "updateSelectedGoods: size= "+selectedGoodsArrayList.size()

        +selectedGoodsArrayList.get(0).toString());

//     if (selectedGoodsMap.size()==0){
         long imgsMenuTaskId = (long) SharePreferenceUtil.getParam(getActivity(), imgsKey, 0L);
         checkImgsMenuExists(imgsMenuTaskId);
//     }else {
//         showImgsMenu(0l);
//     }

        goodsSelectAdapter.updateData(selectedGoodsArrayList);

        for (goodsBean good:selectedGoodsArrayList) {


            goodsSelectMap.put(good.getGoodsId(),good.getNumber());


        }


    }

    @Override
    public void onItemClick(View view, int position) {

        if (((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.CHANGE_GOODS_COUNT)||
                ((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.UNSELECTGOODS)||
                ((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.PAY)
                ){

            /*Toast.makeText(activity, "请先完成当前操作", Toast.LENGTH_SHORT)
                    .show();*/

            ToastUtils.showShortToast("请先完成当前操作");
            return;
        }

        ((MainActivity) activity).showChangeGoodsFragment();

        MessageEvent messageEvent = new MessageEvent("changeCount");
        messageEvent.setGoodsBean(selectedGoodsArrayList.get(position));
        EventBus.getDefault().postSticky(messageEvent);

    }


    /***
     * @Function 整单取消
     */
    public void allDelete(){

        selectedGoodsMap.clear();
        goodsSelectMap.clear();

        selectedGoodsArrayList.clear();

        Log.d(TAG, "allDelete: selectedGoodsArrayList= "+selectedGoodsArrayList.size());
        goodsSelectAdapter.updateData(selectedGoodsArrayList);
        updateTotalMoney();
    }
    @Override
    public void onDelete(View view, int position) {

        if (((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.CHANGE_GOODS_COUNT)
                ||((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.UNSELECTGOODS)||
                ((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.PAY)
                ) {
           /* Toast.makeText(activity, "请先完成当前操作", Toast.LENGTH_SHORT)
                    .show();*/

            ToastUtils.showShortToast("请先完成当前操作");
            return;
        }
        selectedGoodsMap.remove(selectedGoodsArrayList.get(position).getGoodsId());

        Log.d(TAG, "onDelete: goodsId= " + selectedGoodsMap.get(selectedGoodsArrayList.get(position).getGoodsId())

                + " id= " + selectedGoodsArrayList.get(position).getGoodsId());

        if (!goodsSelectMap.isEmpty() && goodsSelectMap.get(selectedGoodsArrayList.get(position).getGoodsId()) != null) {

            goodsSelectMap.remove(selectedGoodsArrayList.get(position).getGoodsId());
        }
        selectedGoodsArrayList.remove(position);

        goodsSelectAdapter.notifyItemRemoved(position);

        goodsSelectAdapter.updateData(selectedGoodsArrayList);
        updateTotalMoney();

    }

    private void checkImgsMenuExists(final long taskId) {
        sendImgsMenu();
        if (taskId < 0) {
            sendImgsMenu();
            return;
        }

        checkFileExist(taskId, new ICheckFileCallback() {
            @Override
            public void onCheckFail() {
                Log.d(TAG, "onCheckFail: ------->file is not exist");
                sendImgsMenu();
            }

            @Override
            public void onResult(boolean exist) {
                if (exist) {
                    Log.d(TAG, "onResult: ---------->file is exist");
//                    dismissDialog();
                    showImgsMenu(taskId);
                } else {
                    Log.d(TAG, "onResult: --------->file is not exists");
                    sendImgsMenu();
                }
            }
        });
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventsticky(MessageEvent messageEvent) {
        Log.d(TAG, "onEvent: pay= " + messageEvent.getAuthCode() + " type= " +
                messageEvent.getType());
        if (messageEvent.getType().equals("ThridisPay")) {
            Log.d(TAG, "onEvent: ThridFragmentPay= " + messageEvent.getIsPay());
            isPay= messageEvent.getIsPay();

        }   if (messageEvent.getType().equals("ThridFragmentPay")) {
//            Log.d(TAG, "onEvent: ThridFragmentPay= " );
            //if (isPay.equals("1"))

                if (((MainActivity)getActivity()).isRightFragmentShow(RightFragmentType.PAY)){
                String payment_num= messageEvent.getAuthCode();
//                Toast.makeText(getActivity(), "支付码： "+ messageEvent.getAuthCode(), Toast.LENGTH_SHORT).show();
                if (APPUtil.isThirdPay(payment_num)) {
                    sendPay(messageEvent.getAuthCode());
                    dialog.setLoadingBuilder(Z_TYPE.STAR_LOADING)//设置类型
                            .setLoadingColor(Color.BLACK)//颜色
                            .setHintText("支付中，请稍后...")
                            .show();
                }else {
                    Toast.makeText(getActivity(), "请正确扫码支付！ ", Toast.LENGTH_SHORT).show();

                }

            }else {

                String payment_num= messageEvent.getAuthCode();
                ToastUtils.showShortToast(payment_num);

               // queryFromSQL(payment_num);

                List<goodsBean> goodsBeanList = DataSupport.where("product_no = ?"
                ,payment_num).find(com.example.niu.myapplication.bean.goodsBean.class);

                if (goodsBeanList.size()>0){
                    /*ToastUtils.showShortToast(""
                            +(((MainActivity)getActivity()).
                            isRightFragmentShow(RightFragmentType.PAY)));*/

                    //ToastUtils.showShortToast("you");
                    MessageEvent message = new MessageEvent("update");
                    message.setGoodsBean(goodsBeanList.get(0));




                    goodsBean = goodsBeanList.get(0);
                    if (goodsBean != null) {

                        if (goodsSelectMap.get(goodsBean.getGoodsId()) == null) {

                            goodsSelectMap.put(goodsBean.getGoodsId(), 1);

                        } else {

                            int number = goodsSelectMap.get(goodsBean.getGoodsId());
                            Log.d(TAG, "onEvent: number= " + number + " id= " +
                                    goodsBean.getGoodsId()

                                    + " type= " + goodsBean.getChangeType());
                            if (goodsBean.getChangeType() == null) {
                                Log.d(TAG, "onEvent: number++");
                                number++;
                            } else if (goodsBean.getChangeType().equals("countChange")) {
                                number = goodsBean.getNumber();
                                goodsBean.setChangeType("");
                            }else{
                                number++;


                            }
                            goodsSelectMap.put(goodsBean.getGoodsId(), number);
                        }

                        goodsBean.setNumber(goodsSelectMap.get(goodsBean.getGoodsId()));
                    }

                    //去更新集合
                    updateSelectedGoods(goodsBean);

                    //去更新应收金额有两个地方 商品选择数量变化时与删除商品时

                    updateTotalMoney();




                    //EventBus.getDefault().post(messageEvent);
                }
               else {

                    Toast.makeText(getActivity(), "未搜索到该商品信息！ ", Toast.LENGTH_SHORT).show();
                }
            }

        }
        //现金支付
        if (messageEvent.getType().equals("XianjingPay")) {
            Log.d(TAG, "onEvent: XianjingPay= " + messageEvent.getAuthCode());
            XianjinPay( messageEvent.getShifu_pay(), messageEvent.getZhaoling_pay());
        }
        //标记支付
        if (messageEvent.getType().equals("BiaojiPay")) {
//            Log.d(TAG, "onEvent: BiaojiPay= " + messageEvent.getAuthCode());
            biaojiPay(messageEvent.getIsPay());
        }
        //优惠券相关操作
        if (messageEvent.getType().equals("coupons")) {
            if ( messageEvent.getYouhuiquan().equals("1")){

                try {
                    if (messageEvent.getYouhuitype().equals("1")) {

                        Double cny = Double.parseDouble(tvTotalDiscount.getText().toString());//6.2041    这个是转为double类型
                        DecimalFormat df = new DecimalFormat("0.00");
                        String CNY = df.format(cny); //6.20   这个是字符串，但已经是我要的两位小数了
                        Log.i(TAG, CNY);
                        Double cny2 = Double.parseDouble(CNY); //6.20

                        String min_amount = df.format(Double.parseDouble(messageEvent.getMin_amount_use())); //6.20   这个是字符串，但已经是我要的两位小数了
//                    Log.i(TAG, CNY);
                        Double min_ = Double.parseDouble(min_amount); //6.20
                        if (min_<=cny2){
                            Hint.Short(getActivity(),"该优惠券不满足使用条件");
                            return;
                        }
                        String CNY3 = df.format(Double.parseDouble(messageEvent.getCheap_money())); //6.20   这个是字符串，但已经是我要的两位小数了
//                    Log.i(TAG, CNY);
                        Double cny3 = Double.parseDouble(CNY3); //6.20
                        Double TotalDiscount = cny3+cny2;
                        rlYouhuiquan.setVisibility(View.VISIBLE);
                        tvTotalDiscount.setText("" +TotalDiscount );
                        youhuimoney.setText("优惠" + messageEvent.getCheap_money() + "元");
                    }else if (messageEvent.getYouhuitype().equals("2"))  {
                        Double cny = Double.parseDouble(tvTotalDiscount.getText().toString());//6.2041    这个是转为double类型
                        DecimalFormat df = new DecimalFormat("0.00");
                        String CNY = df.format(cny); //6.20   这个是字符串，但已经是我要的两位小数了
                        Log.i(TAG, CNY);
                        Double cny2 = Double.parseDouble(CNY);

                        Double cny4 = Double.parseDouble(tvTotalMoney.getText().toString().substring(1,tvTotalMoney.getText().toString().length()));//6.2041    这个是转为double类型
                        DecimalFormat df4 = new DecimalFormat("0.00");
                        String CNY4 = df4.format(cny4); //6.20   这个是字符串，但已经是我要的两位小数了
                        Log.i(TAG, CNY4);
                        Double TotalMoney = Double.parseDouble(CNY4);
                       Double bbb= TotalMoney -cny2;
                        //tvTotalDiscount.setText("");

                        //6.20
                        String CNY3 = df.format(Double.parseDouble(messageEvent.getCheap_money())); //6.20   这个是字符串，但已经是我要的两位小数了
//                    Log.i(TAG, CNY);
                        Double cny3 = Double.parseDouble(CNY3); //6.20
                        Double TotalDiscount = (1-cny3*0.1)*bbb;
                        youhuimoney.setText("折扣" + messageEvent.getCheap_money() + "折");
                        tvTotalDiscount.setText("" +TotalDiscount);
                    }
                }catch (Exception e){

                }

            }else {
                rlYouhuiquan.setVisibility(View.GONE);
//            tvTotalDiscount.setText("0.00");
                DecimalFormat df = new DecimalFormat("0.00");
                String CNY3 = df.format(Double.parseDouble(tvTotalDiscount.getText().toString())); //6.20   这个是字符串，但已经是我要的两位小数了
//                    Log.i(TAG, CNY);
                Double cny3 = Double.parseDouble(CNY3); //6.20

                String CNY = df.format(Double.parseDouble(messageEvent.getCheap_money())); //6.20   这个是字符串，但已经是我要的两位小数了
//                    Log.i(TAG, CNY);
                Double cny = Double.parseDouble(CNY); //6.20
                Double abb = cny -cny3;
            tvTotalDiscount.setText(abb+"");
        }

        }      if (messageEvent.getType().equals("vip")) {
            if ( messageEvent.getYouhuiquan().equals("1")){
//                rlYouhuiquan.setVisibility(View.VISIBLE);
//                youhuimoney.setText("优惠"+messageEvent.getCheap_money()+"元");
                try {
//                    int a = Integer.parseInt(str);

                   Double cny=  (1-Double.parseDouble(messageEvent.getUservip())*0.1)*Double.parseDouble(tvTotalMoney.getText().toString().substring(1,tvTotalMoney.getText().toString().length()));
//                    Double cny = Double.parseDouble();//6.2041    这个是转为double类型
                    DecimalFormat df = new DecimalFormat("0.00");
                    String CNY = df.format(cny); //6.20   这个是字符串，但已经是我要的两位小数了
                    Log.i(TAG, CNY);
                    Double cny2 = Double.parseDouble(CNY); //6.20
                    tvTotalDiscount.setText(""+cny2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            }else {
//                rlYouhuiquan.setVisibility(View.GONE);
                tvTotalDiscount.setText("0.00");
                try {
//                    int a = Integer.parseInt(str);

                    Double cny=  (Double.parseDouble(tvTotalDiscount.getText().toString()));
//                    Double cny = Double.parseDouble();//6.2041    这个是转为double类型
                    DecimalFormat df = new DecimalFormat("0.00");
                    String CNY = df.format(cny); //6.20   这个是字符串，但已经是我要的两位小数了
                    Log.i(TAG, CNY);
                    Double cny2 = Double.parseDouble(CNY); //6.20
                    tvTotalDiscount.setText(""+cny2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent messageEvent) {
        Log.d(TAG, "onEvent: Left_crashier_fragment");
        if (messageEvent.getType().equals("update")) {

            //判断当前商品选择数量

            goodsBean = messageEvent.getGoodsBean();
            if (goodsBean != null) {

                if (goodsSelectMap.get(goodsBean.getGoodsId()) == null) {

                    goodsSelectMap.put(goodsBean.getGoodsId(), 1);

                } else {

                    int number = goodsSelectMap.get(goodsBean.getGoodsId());
                    Log.d(TAG, "onEvent: number= " + number + " id= " +
                            goodsBean.getGoodsId()

                            + " type= " + goodsBean.getChangeType());
                    if (goodsBean.getChangeType() == null) {
                        Log.d(TAG, "onEvent: number++");
                        number++;
                    } else if (goodsBean.getChangeType().equals("countChange")) {
                        number = goodsBean.getNumber();
                        goodsBean.setChangeType("");
                    }else{
                        number++;


                    }
                    goodsSelectMap.put(goodsBean.getGoodsId(), number);
                }

                goodsBean.setNumber(goodsSelectMap.get(goodsBean.getGoodsId()));
            }

            //去更新集合
            updateSelectedGoods(goodsBean);

            //去更新应收金额有两个地方 商品选择数量变化时与删除商品时

            updateTotalMoney();


        }else if (messageEvent.getType().equals("allDelete")){


            allDelete();

        }else if (messageEvent.getType().equals("cancelAllDelete")){

            Log.d(TAG, "onEvent: cancelCollection 111");
            setCancelAndClooectionEnable(true);

        }else if (messageEvent.getType().equals("cancelCollection")){
            Log.d(TAG, "onEvent: cancelCollection 2222");
            //这里是当支付界面三个页面任一个页面点击取消订单
                 setCancelAndClooectionEnable(true);

            MessageEvent messageEvent2 = new MessageEvent("ThridisPay");
            messageEvent2.setIsPay("0");
            EventBus.getDefault().post(messageEvent2);

        }else if(messageEvent.getType().equals("pendingOrder")){


            //目前只支持挂一单
            if (pendOrderId>=1){

                return;
            }

            if (!selectedGoodsMap.isEmpty()){
                //因为map不是值传递,所以要进行深拷贝
                Map<String, goodsBean> selectedGoodsMap2 = new HashMap<>();

                goodsBean goodsBean ;
                for (Map.Entry<String, goodsBean>map:
                     selectedGoodsMap.entrySet()) {
                    goodsBean = map.getValue();

                    selectedGoodsMap2.put(map.getKey(),map.getValue());
                }

                //selectedGoodsMap2.putAll(selectedGoodsMap);
                Log.d(TAG, "onEvent: putall");

                for (Map.Entry<String,goodsBean>item:
                     selectedGoodsMap.entrySet()) {

                    selectedGoodsMap2.put(item.getKey(),
                            (com.example.niu.myapplication.bean.goodsBean) deepClone(item.getValue()));
                }


                pendOrderMap.put(pendOrderId, selectedGoodsMap2);


                /*for (Map.Entry<Integer, Map<String, goodsBean>> map:
                     pendOrderMap.entrySet()) {
                    int key = map.getKey();
                    HashMap<String,goodsBean> map1 = (HashMap<String, goodsBean>) pendOrderMap.get(key);


                    for (Map.Entry goods:
                         map1.entrySet()) {

                        Log.d(TAG, "onEvent: key= "+goods.getKey());
                        Log.d(TAG, "onEvent: map= "+goods.getValue());

                    }
                    
                    
                    
                }*/
                allDelete();


                messageEvent = new MessageEvent("pendingOrderSucess");

                messageEvent.setTypeInt(pendOrderId);
                EventBus.getDefault().post(messageEvent);

               // pendOrderId = 0;
                pendOrderId++;
            }



        }else if (messageEvent.getType().equals("takeOrder")){


           // ToastUtils.showShortToast("take order");
            showPendingOrder(messageEvent.getTypeInt());

        }

    }

    private void showPendingOrder(int typeInt) {


        //ToastUtils.showShortToast("take order typeInt= "+typeInt);
        Log.d(TAG, "showPendingOrder: pendOrder size= "+pendOrderMap.isEmpty()

        +" typeId= "+typeInt);
        Map<String, goodsBean> orderMap = pendOrderMap.get(typeInt);


        //updateSelectedGoods();




        //ToastUtils.showShortToast("take order typeInt= isEmpty= "+orderMap.isEmpty());

        if (!orderMap.isEmpty()) {
            allDelete();
            for (Map.Entry<String, goodsBean> goodsBean1:
                 orderMap.entrySet()) {
                Log.d(TAG, "showPendingOrder: "+(goodsBean1.getValue()==goodsBean));
               /* selectedGoodsMap.clear();
                goodsSelectMap.clear();

                selectedGoodsArrayList.clear();

                goodsSelectAdapter.updateData(selectedGoodsArrayList);
                updateTotalMoney();*/

                updateSelectedGoods(goodsBean1.getValue());

                //去更新应收金额有两个地方 商品选择数量变化时与删除商品时

                updateTotalMoney();

                //取单结束在把他设为0
                pendOrderId = 0;
            }

        }
    }


    /***
     *
     * @Function 更新左侧应收金额
     */
    public void updateTotalMoney() {

        if (selectedGoodsArrayList!=null&&selectedGoodsArrayList.size()!=0){
            totalMoney = 0.00;
            Log.d(TAG, "updateTotalMoney: ");
           setCancelAndClooectionEnable(true);
            for (goodsBean goods:
                 selectedGoodsArrayList) {
                totalMoney+=goods.getPrice()*goods.getNumber();
                Log.d(TAG, "updateTotalMoney: price= "+goods.getPrice()+" number= "+
                goods.getNumber()+" totalMoney= "+totalMoney);
                tvTotalMoney.setText(DecimalFormatUtils.doubleToMoney(totalMoney));
            }
            //totalMoney = 0.00;

        }else{

            totalMoney = 0.00;

            setCancelAndClooectionEnable(false);

            tvTotalMoney.setText("¥ "+totalMoney);


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void sendImgsMenu() {
        Hint.Short(getActivity(),"sendImgsMenu");
        mDSKernel.sendFiles(DSKernel.getDSDPackageName(), "", imgs, new ISendFilesCallback() {
            @Override
            public void onAllSendSuccess(long fileId) {
//                dismissDialog();
                Hint.Short(getActivity(),fileId+"");
                showImgsMenu(fileId);
                SharePreferenceUtil.setParam(getActivity(), imgsKey, fileId);
            }

            @Override
            public void onSendSuccess(String path, long taskId) {
            }

            @Override
            public void onSendFaile(int errorId, String errorInfo) {
//                showToast("发送轮播图失败---->" + errorInfo);
//                dismissDialog();
            }

            @Override
            public void onSendFileFaile(String path, int errorId, String errorInfo) {
//                dismissDialog();
//                showToast("发送轮播图失败---->" + errorInfo);
            }

            @Override
            public void onSendProcess(String path, long totle, long sended) {
            }
        });
    }

    private void showImgsMenu(long taskId) {
        final JSONObject data = new JSONObject();
        try {

            data.put("title", "企小店欢迎您");
            JSONObject head = new JSONObject();
            head.put("param1", "商品名");
            head.put("param2", "单价");
            head.put("param3", "数量");
            head.put("param4", "金额");
            data.put("head", head);
            data.put("alternateTime", 1000);
            JSONArray list = new JSONArray();

            for (int i = 0; i < selectedGoodsArrayList.size(); i++) {
                JSONObject listItem = new JSONObject();
                listItem.put("param1", selectedGoodsArrayList.get(i).getGoodsName());
                listItem.put("param2", "¥ "+selectedGoodsArrayList.get(i).getPrice());
                listItem.put("param3", "x "+selectedGoodsArrayList.get(i).getNumber());
                listItem.put("param4","¥ "+selectedGoodsArrayList.get(i).getPrice()*selectedGoodsArrayList.get(i).getNumber() );
                list.put(listItem);
            }
            data.put("list", list);
            JSONArray KVPList = new JSONArray();
            JSONObject KVPListOne = new JSONObject();
            KVPListOne.put("name", "应收金额 ");
            KVPListOne.put("value", "132.00\n");
            JSONObject KVPListTwo = new JSONObject();
            KVPListTwo.put("name", "优惠 ");
            KVPListTwo.put("value", "12.00");
            JSONObject KVPListThree = new JSONObject();
            KVPListThree.put("name", "会员折扣 ");
            KVPListThree.put("value", "10");
            JSONObject KVPListFour = new JSONObject();
            KVPListFour.put("name", "应收金额 ");
            KVPListFour.put("value", "120.00");
            KVPList.put(0, KVPListOne);
            KVPList.put(1, KVPListTwo);
            KVPList.put(2, KVPListThree);
            KVPList.put(3, KVPListFour);
            data.put("KVPList", KVPList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json1 = UPacketFactory.createJson(DataModel.SHOW_IMGS_LIST, data.toString());
        mDSKernel.sendCMD(DSKernel.getDSDPackageName(), json1, taskId, null);
    }
    @OnClick({R.id.ll_top, R.id.tv_total_money, R.id.tv_total_discount, R.id.tv_all_cancel, R.id.ll_bottom_menu, R.id.ll_empty_goods, R.id.rv_select_goods, R.id.ll_goods_items})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.ll_top:
                break;
            case R.id.tv_total_money:
                break;
            case R.id.tv_total_discount:
                break;
            case R.id.tv_all_cancel:

                //整单取消时候不能点击

                setCancelAndClooectionEnable(false);
                ((MainActivity)activity).showUnselectGoodsLayout();


                break;

            case R.id.ll_bottom_menu:
                break;
            case R.id.ll_empty_goods:
                break;
            case R.id.rv_select_goods:
                break;
            case R.id.ll_goods_items:
                break;
        }
    }

    private void setCancelAndClooectionEnable(boolean enable) {

        tvAllCancel.setEnabled(enable);
        tvAllCollection.setEnabled(enable);

        MessageEvent messageEvent = new MessageEvent("ThridisPay");
        messageEvent.setIsPay("1");
        EventBus.getDefault().post(messageEvent);

    }




    public void setOnPendingOrderSucess(onPendingOrderSucess onPendingOrderSucess){

        this.onPendingOrderSucess = onPendingOrderSucess;

    }



    public interface onPendingOrderSucess{


        public void onPendingOrderSucess(int position);


    }


    public static Object deepClone(Object obj) {
        try {// 将对象写到流里
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);// 从流里读出来
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (oi.readObject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查单张图图缓存问价是否存在
     *
     * @param taskID
     */
    private void checkImgFileExist(final long taskID) {

        sendPicture();
        if (taskID < 0) {
            sendPicture();
            return;
        }
        checkFileExist(taskID, new ICheckFileCallback() {
            @Override
            public void onCheckFail() {
                //检查缓存文件失败
                Log.d(TAG, "onCheckFail: ------------>file not exist");
                sendPicture();
            }

            @Override
            public void onResult(boolean exist) {
                if (exist) {
                    //缓存文件存在
                    Log.d(TAG, "onResult: --------->file is exist");
//                    dismissDialog();
                    showPicture(taskID);
                } else {
                    //缓存文件不存在
                    Log.d(TAG, "onResult: --------->file is not exist");
                    sendPicture();
                }
            }
        });
    }
    private void sendPicture() {

//        Hint.Short(getActivity(),"--");
        Log.d(TAG, "sendPicture: --------->1111111111111111111");
        mDSKernel.sendFile(DSKernel.getDSDPackageName(), Environment.getExternalStorageDirectory().getPath() + "/f_index.jpg", new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {
//                dismissDialog();
                SharePreferenceUtil.setParam(getActivity(), imgKey, taskId);
//                Log.d(TAG, "sendPicture: --------->222222222222222222");
                showPicture(taskId);
            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                Log.d("TAG", "onSendFail: -------------------->" + errorId + "  " + errorInfo);
                //                showToast("发送单张图片失败---->" + errorInfo);
//                dismissDialog();

            }

            @Override
            public void onSendProcess(long totle, long sended) {
                Log.d(TAG, "sendPicture: --------->"+totle+"  "+sended);
            }
        });
    }

    /**
     * 展示单张图片
     *
     * @param taskId
     */
    private void showPicture(long taskId) {
        //显示图片
        try {
            JSONObject json = new JSONObject();
            json.put("dataModel", "SHOW_IMG_WELCOME");
            json.put("data", "default");
            mDSKernel.sendCMD(DSKernel.getDSDPackageName(), json.toString(), taskId, new ISendCallback() {
                @Override
                public void onSendSuccess(long taskId) {
                    Log.d(TAG, "sendPicture: --------->33333333333333");
                }

                @Override
                public void onSendFail(int errorId, String errorInfo) {

                }

                @Override
                public void onSendProcess(long totle, long sended) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void checkFileExist(long fileId, final ICheckFileCallback mICheckFileCallback) {
        DataPacket packet = new DataPacket.Builder(DSData.DataType.CHECK_FILE).data("def").
                recPackName(DSKernel.getDSDPackageName()).addCallback(new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {

            }
            @Override
            public void onSendFail(int errorId, String errorInfo) {
                if (mICheckFileCallback != null) {
                    mICheckFileCallback.onCheckFail();
                }
            }
            @Override
            public void onSendProcess(long totle, long sended) {

            }
        }).isReport(true).build();
        packet.getData().fileId = fileId;
        mDSKernel.sendQuery(packet, new QueryCallback() {
            @Override
            public void onReceiveData(DSData data) {
                boolean exist = TextUtils.equals("true", data.data);
                if (mICheckFileCallback != null) {
                    mICheckFileCallback.onResult(exist);
                }
            }
        });
    }


}