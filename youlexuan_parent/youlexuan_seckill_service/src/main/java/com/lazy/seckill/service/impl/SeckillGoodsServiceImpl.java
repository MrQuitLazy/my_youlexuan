package com.lazy.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.mapper.TbSeckillGoodsMapper;
import com.lazy.pojo.TbSeckillGoods;
import com.lazy.pojo.TbSeckillGoodsExample;
import com.lazy.pojo.TbSeckillGoodsExample.Criteria;
import com.lazy.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_goods服务实现层
 * @author lazy
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private RedisTemplate redisTemplate;


	@Override
	public List<TbSeckillGoods> findList() {
		//将商品列表装入缓存
		System.out.println("从缓存中读取秒杀商品>>>>>");
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		if(seckillGoodsList == null || seckillGoodsList.size() == 0 ){
			System.out.println("缓存中没有秒杀商品>>>>>");
			System.out.println("从数据库读取秒杀商品>>>>>");
			TbSeckillGoodsExample example=new TbSeckillGoodsExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");//审核通过
			criteria.andStockCountGreaterThan(0);//剩余库存大于0
			criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
			criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
			seckillGoodsList = seckillGoodsMapper.selectByExample(example);
			//将商品列表装入缓存
			System.out.println("将秒杀商品加入缓存>>>>>");
			for(TbSeckillGoods seckillGoods:seckillGoodsList){
				redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			}
		}
		return seckillGoodsList;
	}


/*******************************这是分割线，以上功能都是用到的**********************************/
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods != null){			
						if(seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0){
				criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
			}			if(seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0){
				criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
			}			if(seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0){
				criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
			}			if(seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0){
				criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
			}			if(seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0){
				criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
			}
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
