package com.lazy.pay.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lazy.entity.Result;
import com.lazy.order.service.OrderService;
import com.lazy.pay.service.AliPayService;
import com.lazy.pojo.TbPayLog;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付控制层
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;
    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return aliPayService.createNative(payLog.getOutTradeNo(), String.format("%.2f", payLog.getTotalFee()));
        }else{
            return new HashMap();
        }
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            // 调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (map == null) {// 出错
                result = new Result(false, "查询支付状态异常");
                break;
            }
            // 如果成功
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                result = new Result(true, "支付成功");

                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));

                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束");
                break;
            }
            try {
                Thread.sleep(1000);// 间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 如果变量超过设定值退出循环，超时为3分钟
            System.out.println("第"+  x++ +"次查询");
            if (x >= 20) {
                result = new Result(false, "二维码超时");
                break;
            }
        }

        return result;
    }




}
