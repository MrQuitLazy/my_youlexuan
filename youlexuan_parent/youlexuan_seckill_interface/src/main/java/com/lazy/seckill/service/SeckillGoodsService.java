package com.lazy.seckill.service;

import com.lazy.entity.PageResult;
import com.lazy.pojo.TbSeckillGoods;

import java.util.List;

/**
 * seckill_goods服务层接口
 * @author lazy
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回当前正在参与秒杀的商品
	 * @return
	 */
	public List<TbSeckillGoods> findList();

/*******************************这是分割线，以上功能都是用到的**********************************/
	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckillGoods);


	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckillGoods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize);
	
}
