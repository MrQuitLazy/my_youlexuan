package com.lazy.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lazy.cart.service.CartService;
import com.lazy.group.Cart;
import com.lazy.mapper.TbGoodsMapper;
import com.lazy.mapper.TbItemMapper;
import com.lazy.mapper.TbSellerMapper;
import com.lazy.pojo.TbItem;
import com.lazy.pojo.TbOrderItem;
import com.lazy.pojo.TbSeller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品SKU ID查询SKU商品信息
        TbItem add_item= itemMapper.selectByPrimaryKey(itemId);

        //2.获取商家ID（页面根据id进行商家分组展示）
        TbSeller seller = sellerMapper.selectByPrimaryKey(add_item.getSellerId());
        
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, add_item.getSellerId());

        //4.如果购物车列表中不存在该商家的购物车
        if(cart == null){
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(add_item.getSellerId());
            cart.setSellerName(add_item.getSeller());
            TbOrderItem orderItem = createOrderItem(add_item, num);
            List<TbOrderItem> list = new ArrayList<>();
            list.add(orderItem);
            cart.setOrderItemList(list);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }
        //5.如果购物车列表中存在该商家的购物车
        else {
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            //5.1. 如果没有，新增购物车明细
            if(orderItem == null){
                orderItem = createOrderItem(add_item,num);
                cart.getOrderItemList().add(orderItem);
            }
            //5.2. 如果有，在原购物车明细上添加数量，更改金额
            else {
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()* add_item.getPrice().longValue()));
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                // 如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue() == itemId){
                return  orderItem;
            }
        }
        return null;

    }

    private TbOrderItem createOrderItem(TbItem add_item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(add_item.getGoodsId());
        orderItem.setItemId(add_item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(add_item.getImage());
        orderItem.setPrice(add_item.getPrice());
        orderItem.setSellerId(add_item.getSellerId());
        orderItem.setTitle(add_item.getTitle());
        System.out.println("咋回事啊");
        System.out.println(add_item.getPrice());
        System.out.println(add_item.getPrice().doubleValue());
        orderItem.setTotalFee(new BigDecimal(num.longValue() * add_item.getPrice().doubleValue()));
        return orderItem;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据：" + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据：" + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

}
