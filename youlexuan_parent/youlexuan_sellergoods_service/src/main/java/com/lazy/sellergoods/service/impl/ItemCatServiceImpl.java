package com.lazy.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.mapper.TbItemCatMapper;
import com.lazy.pojo.TbItemCat;
import com.lazy.pojo.TbItemCatExample;
import com.lazy.pojo.TbItemCatExample.Criteria;
import com.lazy.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * item_cat服务实现层
 * @author lazy
 *
 */
@Service(timeout = 6000)
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public String delete(Long[] ids) {
		StringBuilder str = new StringBuilder("");
		for(Long id:ids){
			if(findByParentId(id).size()>0){
				str.append(id+",");
			}else{
				itemCatMapper.deleteByPrimaryKey(id);
			}
		}
		return str.toString();
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat != null){			
			
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据上级ID查询列表
	 */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);

		List<TbItemCat> list = findAll();
		int i = 0;
		for (TbItemCat itemCat : list) {
			redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
		}

		return  itemCatMapper.selectByExample(example);
	}


}
