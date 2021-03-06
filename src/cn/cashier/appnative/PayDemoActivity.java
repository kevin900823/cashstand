package cn.cashier.appnative;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import cn.cashier.PayMethodListItem;
import cn.cashier.R;
import cn.wd.checkout.api.CheckOut;
import cn.wd.checkout.api.WDCallBack;
import cn.wd.checkout.api.WDPay;
import cn.wd.checkout.api.WDPayResult;
import cn.wd.checkout.api.WDReqParams;
import cn.wd.checkout.api.WDResult;

public class PayDemoActivity extends Activity {


	//填写当前app 对应的商户号
	private final String submerno ="wdtstsub00001";
	
    private ListView payMethod;
	private ProgressDialog loadingDialog;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHss", Locale.CHINA);
    SimpleDateFormat simpleDateFormattemp = new SimpleDateFormat("SSS", Locale.CHINA);
/**
 * 
(result)
|--成功  WDPayResult.RESULT_SUCCESS_HANDLER = 1 ; WDPayResult.RESULT_SUCCESS = "SUCCESS"; 
|
|--用户取消 	WDPayResult.RESULT_CANCEL_HANDLER = -1; WDPayResult.RESULT_CANCEL = "CANCEL";
|
|											|-- 调用sdk失败	(errMsg)	|WDPayResult.FAIL_UNKNOWN_WAY = "UNKNOWN_WAY" 未知的支付渠道
|											|							|WDPayResult.FAIL_EXCEPTION = "FAIL_EXCEPTION";  参数初始错误 或 调起微信支付sdk错误
|											|							|WDPayResult.FAIL_INVALID_PARAMS = "FAIL_INVALID_PARAMS" ; 支付参数不合法 、 支付渠道参数不合法 
|--失败 WDPayResult.RESULT_FAIL= "FAIL"---	|							|WDPayResult.FAIL_NETWORK_ISSUE = "FAIL_NETWORK_ISSUE"; 网络问题造成的支付失败
|		WDPayResult.RESULT_FAIL_HANDLER = 0;|
|											|
											|
											|--支付渠道返回失败	(errMsg)|WDPayResult.RESULT_PAYING_UNCONFIRMED = "RESULT_PAYING_UNCONFIRMED"; 订单正在处理中，无法获取成功确认信息
																		|WDPayResult.FAIL_ERR_FROM_CHANNEL = "FAIL_ERR_FROM_CHANNEL";从第三方app支付渠道返回的错误信息（支付渠道返回失败）
									


 */
    //支付结果返回入口
    WDCallBack bcCallback = new WDCallBack() {
        @Override
        public void done(final WDResult bcResult) {
            final WDPayResult bcPayResult = (WDPayResult)bcResult;
            PayDemoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	CloesLoading();
                    String result = bcPayResult.getResult();
                    Log.i("demo", "done   result="+result);
                    if (result.equals(WDPayResult.RESULT_SUCCESS))
                        Toast.makeText(PayDemoActivity.this, "用户支付成功", Toast.LENGTH_LONG).show();
                    else if (result.equals(WDPayResult.RESULT_CANCEL))
                        Toast.makeText(PayDemoActivity.this, "用户取消支付", Toast.LENGTH_LONG).show();
                    else if(result.equals(WDPayResult.RESULT_FAIL)) {
                    	String info = "支付失败, 原因: " + bcPayResult.getErrMsg()
                                + ", " + bcPayResult.getDetailInfo();
                        Toast.makeText(PayDemoActivity.this, info, Toast.LENGTH_LONG).show();
                    } else if(result.equals(WDPayResult.FAIL_UNKNOWN_WAY)){
                    	Toast.makeText(PayDemoActivity.this, "未知支付渠道", Toast.LENGTH_LONG).show();
                    } else if(result.equals(WDPayResult.FAIL_WEIXIN_VERSION_ERROR)){
                    	Toast.makeText(PayDemoActivity.this, "针对微信 支付版本错误（版本不支持）", Toast.LENGTH_LONG).show();
                    } else if(result.equals(WDPayResult.FAIL_EXCEPTION)){
                    	Toast.makeText(PayDemoActivity.this, "支付过程中的Exception", Toast.LENGTH_LONG).show();
                    } else if(result.equals(WDPayResult.FAIL_ERR_FROM_CHANNEL)){
                    	Toast.makeText(PayDemoActivity.this, "从第三方app支付渠道返回的错误信息，原因: " + bcPayResult.getErrMsg(), Toast.LENGTH_LONG).show();
                    } else if(result.equals(WDPayResult.FAIL_INVALID_PARAMS)){
                    	Toast.makeText(PayDemoActivity.this, "参数不合法造成的支付失败", Toast.LENGTH_LONG).show();
                    }else if(result.equals(WDPayResult.RESULT_PAYING_UNCONFIRMED)){
                    	Toast.makeText(PayDemoActivity.this, "表示支付中，未获取确认信息", Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(PayDemoActivity.this, "invalid return", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };
    
    Handler handler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		CloesLoading();
            
            String info="";
    		switch (msg.what) {
			case WDPayResult.RESULT_SUCCESS_HANDLER:
				info= (String) msg.obj;
				break;
			case WDPayResult.RESULT_CANCEL_HANDLER:
				 info = (String) msg.obj;
				break;
			case WDPayResult.RESULT_FAIL_HANDLER:
				 info = (String) msg.obj;
					break;

			default:
				break;
			}
    		
    		Log.i("demo", "msg.what="+msg.what +" info="+info);
    		Toast.makeText(PayDemoActivity.this, info, Toast.LENGTH_LONG).show();
    		
    	};
    };
	private EditText mGoodsMoney;
	private EditText mGoodsTitle;
	private EditText mGoodsTitleDesc;
	private EditText mOrderTitle;
	private EditText mOrderTitleDesc;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_pay);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        
     
        
        
        payMethod = (ListView) this.findViewById(R.id.payMethod);
        Integer[] payIcons = new Integer[]{R.drawable.wechat,R.drawable.wechat, R.drawable.alipay, R.drawable.alipay,R.drawable.unionpay,R.drawable.unionpay,R.drawable.icon_wonderspay,R.drawable.icon_wonderspay};
        final String[] payNames = new String[]{"微信支付","微信支付 UI反馈", "支付宝支付", "支付宝支付 UI反馈", "银联支付", "银联支付 UI反馈","链支付","链支付UI反馈"};
        String[] payDescs = new String[]{"使用微信支付，以人民币CNY计费","使用微信支付，以人民币CNY计费", "使用支付宝支付，以人民币CNY计费", "使用支付宝支付，以人民币CNY计费", "使用银联支付，以人民币CNY计费", "使用银联支付，以人民币CNY计费", "使用链支付，以人民币CNY计费（暂不使用）", "使用链支付，以人民币CNY计费（暂不使用）"};
        PayMethodListItem adapter = new PayMethodListItem(this, payIcons, payNames, payDescs);
        payMethod.setAdapter(adapter);
        
        
        mGoodsMoney = (EditText) findViewById(R.id.edt_main_money);
        mGoodsTitle = (EditText) findViewById(R.id.edt_main_goods_title);
        mGoodsTitleDesc = (EditText) findViewById(R.id.edt_main_goods_title_desc);
        mOrderTitle = (EditText) findViewById(R.id.edt_main_order_title);
        mOrderTitleDesc = (EditText) findViewById(R.id.edt_main_order_title_desc);
        mOrderTitle.setText(getBillNum());
        
        
        // 如果调起支付太慢, 可以在这里开启动画, 以progressdialog为例
        loadingDialog = new ProgressDialog(PayDemoActivity.this);
        loadingDialog.setMessage("启动第三方支付，请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);
        payMethod.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	// 在主activity的onCreate函数中初始化账户中的AppID和AppSecret 
            	// appId     appid 统一收银台签约获取 id唯一
                // appSecret App Secret 统一收银台签约获取 不唯一 每天都会重新生成 故需要每次设置
                CheckOut.setAppIdAndSecret("wd2015tst001", "6XtC7H8NuykaRv423hrf1gGS09FEZQoB");
                CheckOut.setIsPrint(true);
                /**
                 * 设置访问网络环境  CT 为测试环境 不调用此方法为生产环境
                 */
                CheckOut.setNetworkWay("CT");
            	
            	String money = mGoodsMoney.getText().toString().trim();
            	String goodsTitle = mGoodsTitle.getText().toString().trim();
            	String goodsDesc = mGoodsTitleDesc.getText().toString().trim();
            	String orderTitle = mOrderTitle.getText().toString().trim();
            	mOrderTitle.setText(getBillNum());
            	String orderDesc = mOrderTitleDesc.getText().toString().trim();
            	Long i = 0l ;
            	if(isNumeric(money)){
            		i = Long.parseLong(money);
            	}else{
            		Toast.makeText(PayDemoActivity.this, "请输入正确的交易金额（单位：分）", Toast.LENGTH_LONG).show();
            		return;
            	}
                switch (position) {
                    case 0: //微信
                        loadingDialog.show();
                        //对于微信支付, 手机内存太小会有OutOfResourcesException造成的卡顿, 以致无法完成支付
                        //这个是微信自身存在的问题
                        WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.wepay, submerno,
                        		goodsTitle,               //订单标题
                    			goodsDesc,
                                i,                           //订单金额(分)
                                orderTitle,  //订单流水号
                                orderDesc,
                                null,            //扩展参数(可以null)
                                bcCallback);
                        
                        break;
                    case 1: //微信
                    	loadingDialog.show();
                    	//对于微信支付, 手机内存太小会有OutOfResourcesException造成的卡顿, 以致无法完成支付
                    	//这个是微信自身存在的问题
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.wepay, submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			handler);
                    	break;

                    case 2: //支付宝支付
                        loadingDialog.show();
                        
                        WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.alipay, submerno,
                        		goodsTitle,               //订单标题
                    			goodsDesc,
                                i,                           //订单金额(分)
                                orderTitle,  //订单流水号
                                orderDesc,
                                null,            //扩展参数(可以null)
                                bcCallback);
                        
                        
                        break;
                    case 3: //支付宝支付
                    	loadingDialog.show();
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.alipay, submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			handler);
                    	
                    	break;
                    case 4: //银联支付
                    	loadingDialog.show();
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.uppay, 
                    			submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			bcCallback);
                    	
                    	
                    	break;
                    case 5: //银联支付
                    	loadingDialog.show();
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.uppay, 
                    			submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			handler);
                    	
                    	break;
                    case 6: //链支付
                    	loadingDialog.show();
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.wdepay, 
                    			submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			bcCallback);
                    	
                    	break;
                    case 7: //链支付
                    	loadingDialog.show();
                    	
                    	WDPay.getInstance(PayDemoActivity.this).reqPayAsync(WDReqParams.WDChannelTypes.wdepay, 
                    			submerno,
                    			goodsTitle,               //订单标题
                    			goodsDesc,
                    			i,                           //订单金额(分)
                    			orderTitle,  //订单流水号
                    			orderDesc,
                    			null,            //扩展参数(可以null)
                    			handler);
                    	
                    	break;
                    default:
                }
            }
        });
    }
	
	   public void onResume() {
		   super.onResume();
	    	CloesLoading();
	    }

	private void CloesLoading() {
		if(loadingDialog!=null && loadingDialog.isShowing()){
			//此处关闭loading界面
		    loadingDialog.dismiss();
		}
	};
	 String getBillNum() {
	        return "974"+simpleDateFormat.format(new Date())+simpleDateFormattemp.format(new Date())+"and";
	    }
	 
	 public final static boolean isNumeric(String s) {  
	        if (s != null && !"".equals(s.trim()))  
	            return s.matches("^[0-9]+(.[0-9]{1,2})?$");  
	        else  
	            return false;  
	    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        
        return true;
    }
    
}
