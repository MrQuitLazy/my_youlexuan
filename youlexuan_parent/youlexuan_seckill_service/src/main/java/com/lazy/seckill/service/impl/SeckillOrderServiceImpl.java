package com.lazy.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.mapper.TbSeckillGoodsMapper;
import com.lazy.mapper.TbSeckillOrderMapper;
import com.lazy.pojo.TbSeckillGoods;
import com.lazy.pojo.TbSeckillOrder;
import com.lazy.pojo.TbSeckillOrderExample;
import com.lazy.pojo.TbSeckillOrderExample.Criteria;
import com.lazy.seckill.service.SeckillOrderService;
import com.lazy.util.IdWorker;
import com.lazy.util.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_order服务实现层
 *
 * @author lazy
 */
@Service(timeout = 3000)
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisLock redisLock;

    /**
     * 从Redis中删除Order，用于恢复库存
     * @param userId	用户Id
     * @param orderId	订单Id
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        // 根据用户Id 查询秒杀订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

        //如果秒杀订单 不为空 或者 秒杀订单的Id的值和要删除订单Id相同
        if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
            // 删除缓存中的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);

            //恢复库存
            // 1.从缓存中提取秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            if (seckillGoods != null) {
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                //如果秒杀商品
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);// 存入缓存
            }
        }
    }
    /**
     * saveOrderFromRedisToDb 从Redis中读取支付成功的订单并保存到数据库
     * @param userId    用户Id
     * @param orderId   订单Id
     * @param transactionId
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        System.out.println("seckill>>>>saveOrderFromRedisToDb:" + userId);
        // 根据用户ID查询日志
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        // 如果与传递过来的订单号不符
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("支付订单与抢购订单不符");
        }
        seckillOrder.setTransactionId(transactionId);// 交易流水号
        seckillOrder.setPayTime(new Date());// 支付时间
        seckillOrder.setStatus("1");// 状态

        seckillOrderMapper.insert(seckillOrder);// 保存到数据库
        redisTemplate.boundHashOps("seckillOrder").delete(userId);// 从redis中清除
    }

    @Override
    public TbSeckillOrder  searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void submitOrder(Long seckillId, String userId) {
        String lockKey = "createSecKillOrder";
        // 过期时间：1秒
        long ex = 1000;
        String lockVal = String.valueOf(System.currentTimeMillis() + ex);
        boolean lock = redisLock.lock(lockKey, lockVal);
        System.out.println(userId+">>>"+lock);

        if (lock) {
            System.out.print("用户：" + userId + "   抢>>>>>" );
            //从缓存中查询秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
            if (seckillGoods == null) {
                throw new RuntimeException("商品不存在");
            }
            if (seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("商品已抢完");
            }
            System.out.print("还有货");
            //扣减（redis）库存
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);//放回缓存
            if (seckillGoods.getStockCount() == 0) {//如果已经被秒光
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库
                redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
                System.out.print(">>>最后一件被抢了");
            }
            //保存（redis）订单
            long orderId = idWorker.nextId();
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(orderId);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            seckillOrder.setUserId(userId);//设置用户ID
            seckillOrder.setStatus("0");//状态
            redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
            System.out.println(">>>redis保存订单:" + userId);
            // 释放锁
            redisLock.unlock(lockKey, lockVal);
        }
    }

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 分页
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 分页+查询
     */
    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}