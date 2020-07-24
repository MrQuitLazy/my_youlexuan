package com.lazy.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.mapper.TbSpecificationOptionMapper;
import com.lazy.mapper.TbTypeTemplateMapper;
import com.lazy.pojo.TbSpecificationOption;
import com.lazy.pojo.TbSpecificationOptionExample;
import com.lazy.pojo.TbTypeTemplate;
import com.lazy.pojo.TbTypeTemplateExample;
import com.lazy.pojo.TbTypeTemplateExample.Criteria;
import com.lazy.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * type_template服务实现层
 * @author lazy
 *
 */
@Service(timeout = 3000)
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}

	/**
	 * 分页 条件查询
	 * @param typeTemplate	查询条件
	 * @param pageNum 		当前页码
	 * @param pageSize		每页记录数
	 * @return
	 */
	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		//加入页码信息
		PageHelper.startPage(pageNum, pageSize);
		//创建条件查询对象
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		//穿件条件存储对象
		Criteria criteria = example.createCriteria();
		//添加条件到条件对象
		if(typeTemplate.getName() != null && typeTemplate.getName().length()>0){
			criteria.andNameLike("%"+typeTemplate.getName()+"%");
		}
		//根据条件查询对象查询
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		//存储所有的模板对赢得品牌和规格到redis
		saveBrandAndSpecToRedis();
		//获取查询数据，存储到页面结果对象，并返回
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 存储所有的模板对赢得品牌和规格到redis
	 */
	private void saveBrandAndSpecToRedis() {
		//获取模板数据，查询所有
		List<TbTypeTemplate> typeTemplateList = findAll();
		//遍历查询到的所有数据
		for (TbTypeTemplate tbTypeTemplate : typeTemplateList) {
			//品牌列表操作
			//将返回的品牌列表的json数据转换为对象
			List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
			//存储品牌列表到redis
			redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(),brandList);

			//规格列表操作
			//根据模板id获取相对应的规格的选项
			List<Map> specList = findSpecList(tbTypeTemplate.getId());
			//存储规格列表到redis
			redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specList);
		}
	}

	@Override
	public List<Map> selectOptionList() {
		return typeTemplateMapper.selectOptionList();
	}

	/**
	 * 查找规格选项
	 * @param id 模板id
	 * @return
	 */
	@Override
	public List<Map> findSpecList(Long id) {
		//根据主键查找模板
		TbTypeTemplate typeTemplate =typeTemplateMapper.selectByPrimaryKey(id);
		//把模板的规格对赢得json数据转换为对象集合
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		//遍历规格集合
		for (Map map:list) {
			//查询规格选项列表
			//创建规格选项条件查询对象
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			//创建条件对象
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			//往条件对象添加条件
			criteria.andSpecIdEqualTo( new Long( (Integer)map.get("id") ) );
			//根据条件查询对象进行查询，返回结果集合
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
			//将结果集he存放在map中
			map.put("options", options);
		}
		return list;
	}

}
