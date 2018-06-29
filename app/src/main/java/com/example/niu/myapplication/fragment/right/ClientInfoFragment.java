package com.example.niu.myapplication.fragment.right;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.niu.myapplication.App;
import com.example.niu.myapplication.BaseFragment;
import com.example.niu.myapplication.R;
import com.example.niu.myapplication.adapter.CouponsRecyclerAdapter;
import com.example.niu.myapplication.bean.CouponsEntry;
import com.example.niu.myapplication.bean.goodsBean;
import com.example.niu.myapplication.utils.Hint;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by wanglei on 18-5-22.
 */

public class ClientInfoFragment extends BaseFragment {
    @BindView(R.id.tv_client_name)
    TextView tvClientName;
    @BindView(R.id.lv_youhuiquan)
    LinearLayout lvYouhuiquan;
    @BindView(R.id.lv_vip)
    LinearLayout lvVip;
    @BindView(R.id.tv_client_mobile)
    TextView tvClientMobile;
    @BindView(R.id.bt_client_change)
    TextView btClientChange;
    @BindView(R.id.tv_client_consume_numbe)
    TextView tvClientConsumeNumbe;
    @BindView(R.id.tv_client_last_consume_time)
    TextView tvClientLastConsumeTime;
    @BindView(R.id.ll_vip_card)
    RelativeLayout llVipCard;
    @BindView(R.id.ll_coupons)
    LinearLayout llCoupons;
    Unbinder unbinder;
    @BindView(R.id.tv_vip_style)
    TextView tvVipStyle;
    @BindView(R.id.tv_validity_period)
    TextView tvValidityPeriod;
    @BindView(R.id.tv_discount)
    TextView tvDiscount;
    @BindView(R.id.tv_vip_name)
    TextView tvVipName;
    Unbinder unbinder1;

    boolean isSelectVipCard;
    @BindView(R.id.rv_coupons)
    RecyclerView rvCoupons;
    Unbinder unbinder2;
    CouponsRecyclerAdapter couponsRecyclerAdapter;
    onClientChangeListener onClientChangeListener;
    private ArrayList<CouponsEntry> CouponsEntryArrayList;
    @Override
    protected void initData() {

    }

    @Override
    protected void initView(View rootView) {
        CouponsEntryArrayList = new ArrayList<>();
        EventBus.getDefault().register(this);
        couponsRecyclerAdapter = new CouponsRecyclerAdapter(activity,CouponsEntryArrayList);
        rvCoupons.setLayoutManager(new GridLayoutManager(activity,3));
        rvCoupons.setAdapter(couponsRecyclerAdapter);
    }

    @Override
    protected int getLayout() {

        return R.layout.client_info;
    }


    @OnClick({R.id.bt_client_change, R.id.tv_client_consume_numbe, R.id.tv_client_last_consume_time, R.id.ll_vip_card, R.id.ll_coupons})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_client_change:
                if (onClientChangeListener!=null){
                   onClientChangeListener.onClientChange();
                }
                break;
            case R.id.tv_client_consume_numbe:
                break;
            case R.id.tv_client_last_consume_time:
                break;
            case R.id.ll_vip_card:
                if (!isSelectVipCard) {
                    llVipCard.setBackgroundResource(R.drawable.vip_select_bg);
                    MessageEvent messageEvent = new MessageEvent("vip");
                    messageEvent.setYouhuiquan("1");
                    Hint.Short(getActivity(),tvDiscount.getText().toString());
                    messageEvent.setUservip(tvDiscount.getText().toString());
                    EventBus.getDefault().postSticky(messageEvent);
                } else {
                    llVipCard.setBackgroundResource(R.drawable.vip_bg);
                    MessageEvent messageEvent = new MessageEvent("vip");
                    messageEvent.setYouhuiquan("0");
                    Hint.Short(getActivity(),tvDiscount.getText().toString());
                    messageEvent.setUservip(tvDiscount.getText().toString());
                    EventBus.getDefault().postSticky(messageEvent);

                }
                isSelectVipCard = !isSelectVipCard;
                break;
            case R.id.ll_coupons:
                break;
        }
    }

    public void setOnClientChangeListener(onClientChangeListener onClientChangeListener){


        this.onClientChangeListener = onClientChangeListener;
    }



    public interface onClientChangeListener{

        public void onClientChange();
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MessageEvent messageEvent) {
//        Log.d(TAG, "onEvent: Left_crashier_fragment");
        if (messageEvent.getType().equals("client_info")) {
            try {
                String clientinfo= messageEvent.getClientinfo();
                JSONObject client_info = new JSONObject(clientinfo);
                JSONObject base_info  =   client_info.getJSONObject("base_info");
                JSONObject purchase  =   client_info.getJSONObject("purchase");
                JSONArray couponsArr  =   client_info.getJSONArray("coupons");
                App.store.put("userid",base_info.getString("id")).commit();

                JSONObject card = null;
                try {
                 card  =   client_info.getJSONObject("card");
            }catch (JSONException e){
                
            }
                tvClientName.setText(base_info.getString("nickname"));
                tvClientMobile.setText(base_info.getString("mobile"));
                tvClientConsumeNumbe.setText(purchase.getString("consumption"));
                tvClientLastConsumeTime.setText(purchase.getString("last_time"));
                if (couponsArr.length()>0) {
                    lvYouhuiquan.setVisibility(View.VISIBLE);
                    for (int i = 0; i < couponsArr.length(); i++) {
                        JSONObject coupons = couponsArr.getJSONObject(i);
                        JSONObject json = coupons.getJSONObject("coupon");
                        CouponsEntryArrayList.add(new CouponsEntry(json.getString("id"), json.getString("store_id"), json.getString("title"), json.getString("amount"), json.getString("coupon_type"), json.getString("min_amount_use"), json.getString("expire_date_text")));
                    }
                    couponsRecyclerAdapter.updateData(CouponsEntryArrayList);
                }else {
                    lvYouhuiquan.setVisibility(View.GONE);
                }
//                tvVipStyle.setText(card.getString("title"));//卡信息
//                tvValidityPeriod.setText(card.getString("card_type"));//有效
//                tvDiscount.setText(card.getString("discount"));//几折
//                tvVipName.setText(card.getString("last_time"));//版本

                if (card!=null&&card.length()>0) {
                    lvVip.setVisibility(View.VISIBLE);
                    tvVipStyle.setText(card.getString("title"));
                    tvDiscount.setText(card.getString("discount"));
                    tvVipName.setText(card.getString("card_type"));
                }else {
                    lvVip.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

}
