package com.lazy.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.mapper.TbGoodsDescMapper;
import com.lazy.pojo.TbGoodsDesc;
import com.lazy.pojo.TbGoodsDescExample;
import com.lazy.pojo.TbGoodsDescExample.Criteria;
import com.lazy.sellergoods.service.GoodsDescService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * goods_desc服务实现层
 * @author lazy
 *
 */
@Service
public class GoodsDescServiceImpl implements GoodsDescService {

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoodsDesc> findAll() {
		return goodsDescMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoodsDesc> page = (Page<TbGoodsDesc>) goodsDescMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbGoodsDesc goodsDesc) {
		goodsDescMapper.insert(goodsDesc);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoodsDesc goodsDesc){
		goodsDescMapper.updateByPrimaryKey(goodsDesc);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoodsDesc findOne(Long goodsId){
		return goodsDescMapper.selectByPrimaryKey(goodsId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] goodsIds) {
		for(Long goodsId:goodsIds){
			goodsDescMapper.deleteByPrimaryKey(goodsId);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbGoodsDesc goodsDesc, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsDescExample example=new TbGoodsDescExample();
		Criteria criteria = example.createCriteria();
		
		if(goodsDesc != null){			
			
		}
		
		Page<TbGoodsDesc> page= (Page<TbGoodsDesc>)goodsDescMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
