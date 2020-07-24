package com.lazy.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lazy.cart.service.CartService;
import com.lazy.entity.Result;
import com.lazy.group.Cart;
import com.lazy.pojo.TbOrderItem;
import com.lazy.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户：" + username);
        String cartListString = CookieUtil.getCookieValue(request,"cartList","utf-8");
        if(cartListString==null || "".equals(cartListString)){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
        if(username.equals("anonymousUser")){//如果未登录
            return cartList_cookie;
        }else {//如果已登录
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);//从redis中提取
            if (cartList_cookie.size() > 0) {// 如果本地存在购物车
                // 合并购物车
                for (Cart cart : cartList_cookie) {
                    for (TbOrderItem orderItem : cart.getOrderItemList()) {
                        cartList_redis = cartService.addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
                    }
                }
                // 清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                // 将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }
    }
    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9009");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录用户：" + username);

            List<Cart> cartList = findCartList();// 获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if(username.equals("anonymousUser")) { //如果是未登录，保存到cookie
                // 单位：秒
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");

                System.out.println("向cookie存入购物车");

            }else{//如果是已登录，保存到redis

                cartService.saveCartListToRedis(username, cartList);
                System.out.println("向Redis存入购物车");
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}