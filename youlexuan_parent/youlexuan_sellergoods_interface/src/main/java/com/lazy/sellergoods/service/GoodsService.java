package com.lazy.sellergoods.service;

import com.lazy.entity.PageResult;
import com.lazy.group.Goods;
import com.lazy.pojo.TbGoods;
import com.lazy.pojo.TbItem;

import java.util.List;

/**
 * goods服务层接口
 * @author lazy
 *
 */
public interface GoodsService {
	/**
	 * 根据商品ID和状态查询Item表信息
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status );

	/**
	 * 增加，传递组合参数
	 */
	public void add(Goods goods);

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
//
//	/**
//	 * 返回分页列表
//	 * @return
//	 */
//	public PageResult findPage(int pageNum, int pageSize);
//



	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


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
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/**
	 * 批量修改状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long []ids, String status);


}
