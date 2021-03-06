package com.lazy.order.service.impl;

import com.alibaba.druid.filter.AutoLoad;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.group.Cart;
import com.lazy.mapper.TbOrderItemMapper;
import com.lazy.mapper.TbOrderMapper;
import com.lazy.mapper.TbPayLogMapper;
import com.lazy.order.service.OrderService;
import com.lazy.pojo.TbOrder;
import com.lazy.pojo.TbOrderExample;
import com.lazy.pojo.TbOrderExample.Criteria;
import com.lazy.pojo.TbOrderItem;
import com.lazy.pojo.TbPayLog;
import com.lazy.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * order服务实现层
 * @author lazy
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		List<String> orderIdList=new ArrayList();//订单ID列表
		double total_money=0;//总金额 （元）


		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();
			System.out.println("商家sellerId：" + cart.getSellerId());
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);
			tbOrder.setUserId(order.getUserId());
			tbOrder.setCreateTime(new Date());
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setStatus("1");
			tbOrder.setUpdateTime(new Date());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(tbOrder.getReceiver());
			tbOrder.setSellerId(cart.getSellerId());
			double money = 0;
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				System.out.println("产品id：" + orderItem.getItemId());
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				money += orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
			orderIdList.add(orderId+"");//添加到订单列表
			total_money+=money;//累加到总金额
		}
		if("1".equals(order.getPaymentType())){//如果是支付宝支付
			TbPayLog payLog=new TbPayLog();
			String outTradeNo=  idWorker.nextId()+"";//支付订单号
			payLog.setOutTradeNo(outTradeNo);//支付订单号
			payLog.setCreateTime(new Date());//创建时间
			//订单号列表，逗号分隔
			String ids=orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			payLog.setOrderList(ids);//订单号列表，逗号分隔
			payLog.setPayType("1");//支付类型
			payLog.setTotalFee(new BigDecimal(total_money));// 总金额
			payLog.setTradeState("0");//支付状态
			payLog.setUserId(order.getUserId());//用户ID
			payLogMapper.insert(payLog);//插入到支付日志表
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
		}

		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//已支付
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		String orderList = payLog.getOrderList();//获取订单号列表
		String[] orderIds = orderList.split(",");//获取订单号数组

		for(String orderId:orderIds){
			TbOrder order = orderMapper.selectByPrimaryKey( Long.parseLong(orderId) );
			if(order!=null){
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//3.清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}


	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order != null){			
						if(order.getPaymentType() != null && order.getPaymentType().length() > 0){
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}			if(order.getPostFee() != null && order.getPostFee().length() > 0){
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}			if(order.getStatus() != null && order.getStatus().length() > 0){
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}			if(order.getShippingName() != null && order.getShippingName().length() > 0){
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}			if(order.getShippingCode() != null && order.getShippingCode().length() > 0){
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}			if(order.getUserId() != null && order.getUserId().length() > 0){
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}			if(order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0){
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}			if(order.getBuyerNick() != null && order.getBuyerNick().length() > 0){
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}			if(order.getBuyerRate() != null && order.getBuyerRate().length() > 0){
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}			if(order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0){
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}			if(order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0){
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}			if(order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0){
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}			if(order.getReceiver() != null && order.getReceiver().length() > 0){
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}			if(order.getInvoiceType() != null && order.getInvoiceType().length() > 0){
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}			if(order.getSourceType() != null && order.getSourceType().length() > 0){
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}			if(order.getSellerId() != null && order.getSellerId().length() > 0){
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
