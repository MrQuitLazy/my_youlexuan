package com.lazy.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lazy.entity.Result;
import com.lazy.pay.service.AliPayService;
import com.lazy.pojo.TbSeckillOrder;
import com.lazy.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {

        // 获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        //
        int x = 0;
        while (true) {
            // 调用查询接口，查询支付状态
            Map<String, String> map = aliPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "查询支付状态异常");
                break;
            }
            // 如果成功
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                result = new Result(true, "支付成功");
                // 保存秒杀结果到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
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
                Thread.sleep(3000);// 间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 如果变量超过设定值退出循环，超时为3分钟
            x++;
            if (x >= 60) {
                result = new Result(false, "超过时间未支付，订单取消");
                // 1.调用支付宝的关闭订单接口
                Map<String, String> payresult = aliPayService.closePay(out_trade_no);
                if ("10000".equals(payresult.get("code"))) {
                    // 2.调用删除
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                    System.out.println("删除缓存秒杀商品，还原库存>>>>>");
                }
                break;
            }
        }
        return result;
    }

    /**
     * 生成支付宝的二维码链接
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        // 获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        // 到redis查询秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        // 判断秒杀订单存在
        System.out.println(seckillOrder.getId());
        System.out.println(seckillOrder.getMoney());
        if (seckillOrder != null) {
            //String.format(格式,BigDecimal类型)
            //%.2f ---> %占位符 .2f 保留小数点后两位
            return aliPayService.createNative(seckillOrder.getId() + "", String.format("%.2f", seckillOrder.getMoney()));
        } else {
            return new HashMap();
        }
    }
}
