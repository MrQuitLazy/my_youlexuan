package com.lazy.content.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.mapper.TbContentMapper;
import com.lazy.pojo.TbContent;
import com.lazy.pojo.TbContentExample;
import com.lazy.pojo.TbContentExample.Criteria;
import com.lazy.content.service.ContentService;

import com.lazy.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * content服务实现层
 * @author lazy
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		System.out.println("123");
		List<TbContent> contentList= (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		if(contentList==null){
			System.out.println("从数据库读取数据放入缓存");
			//根据广告分类ID查询广告列表
			TbContentExample contentExample=new TbContentExample();
			Criteria criteria = contentExample.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");//开启状态
			contentExample.setOrderByClause("sort_order");//排序
			contentList = contentMapper.selectByExample(contentExample);//获取广告列表
			redisTemplate.boundHashOps("content").put(categoryId, contentList);//存入缓存
		}else{
			System.out.println("从缓存读取数据");
		}
		return  contentList;
	}



	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

//	/**
//	 * 分页
//	 */
//	@Override
//	public PageResult findPage(int pageNum, int pageSize) {
//		PageHelper.startPage(pageNum, pageSize);
//		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
//		return new PageResult(page.getTotal(), page.getResult());
//	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		System.out.println("修改开始");
		//查询修改前的分类Id
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		redisTemplate.boundHashOps("content").delete(categoryId);
		System.out.println("全部删除");

		contentMapper.updateByPrimaryKey(content);
		//如果分类ID发生了修改,清除修改后的分类ID的缓存
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			System.out.println("全部删除");
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类ID
			redisTemplate.boundHashOps("content").delete(categoryId);

			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content != null){			
						if(content.getTitle() != null && content.getTitle().length() > 0){
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}			if(content.getUrl() != null && content.getUrl().length() > 0){
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}			if(content.getPic() != null && content.getPic().length() > 0){
				criteria.andPicLike("%" + content.getPic() + "%");
			}			if(content.getStatus() != null && content.getStatus().length() > 0){
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
