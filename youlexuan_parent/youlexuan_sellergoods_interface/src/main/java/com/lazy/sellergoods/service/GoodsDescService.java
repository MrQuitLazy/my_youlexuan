package com.lazy.sellergoods.service;

import com.lazy.entity.PageResult;
import com.lazy.pojo.TbGoodsDesc;

import java.util.List;

/**
 * goods_desc服务层接口
 * @author lazy
 *
 */
public interface GoodsDescService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoodsDesc> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbGoodsDesc goodsDesc);


	/**
	 * 修改
	 */
	public void update(TbGoodsDesc goodsDesc);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbGoodsDesc findOne(Long goodsId);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] goodsIds);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoodsDesc goodsDesc, int pageNum, int pageSize);
	
}
