package com.lazy.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.lazy.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 30000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void importList(List<TbItem> list) {

        for (TbItem item : list) {
            // 将spec字段中的json字符串转换为map
            Map<String, Object> specMap = JSON.parseObject(item.getSpec());

            Map map = new HashMap();

            for (String key : specMap.keySet()) {
                // 处理中文规格名称
                map.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }
            item.setSpecMap(map); // 给带动态域注解的字段赋值
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
         * 根据关键字搜索列表
         * @param searchMap
         * @return
         */
    @Override
    public Map<String, Object> search(Map searchMap) {

        String s = (String) searchMap.get("keywords");
        String news = s.replaceAll(" ","");
        searchMap.put("keywords",news);
        //创建查询结果存储对象
        Map<String, Object> map = new HashMap<>();

        //1.关键字查询 + 高亮显示 + 分类、品牌、规格、价格筛选
        hightSearch(searchMap, map);

        //2.根据关键字查询列表分类
        categoryListSearch(searchMap,map);
        //3.查询品牌和规格
        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");

        //如果有分类名称
        if(!"".equals(categoryName)){

            brandAndSpecListSearch(categoryName,map);
        }else{
            List<String> categoryList = (List<String>) map.get("categoryList");
            if(categoryList.size()>0){
                //获取第一个分类对应的品牌和规格
                brandAndSpecListSearch(categoryList.get(0),map);
            }
        }
        return map;
    }

    /**
     * 分类查询列表
     * @param searchMap
     * @param map
     */
    private void categoryListSearch(Map searchMap, Map<String, Object> map) {
        List<String> list = new ArrayList();
        //创建查询对象
        Query  query = new SimpleQuery();
        //创建并初始化查询条件对象，按关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //查询对象添加条件对象
        query.addCriteria(criteria);

        //创建并初始化分组对象，按照分组进行查询
        GroupOptions options = new GroupOptions().addGroupByField("item_category");
        //查询对象添加分组对象
        query.setGroupOptions(options);
        //常见分组页对象，得到分组页，使用solr模板查询
        GroupPage<TbItem> pa = solrTemplate.queryForGroupPage(query, TbItem.class);
        //分组页对象根据分组字段得到分组结果，赋值给分组结果集合
        GroupResult<TbItem> result = pa.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = result.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            //遍历分组入口集合，得到值
            list.add(entry.getGroupValue());
        }
        map.put("categoryList",list);
    }

    /**
     * 高亮+条件 查询
     * @param searchMap
     * @param map
     */
    private void hightSearch(Map searchMap, Map<String, Object> map) {
        HighlightQuery query = new SimpleHighlightQuery();//创建高亮查询对象
        HighlightOptions options=new HighlightOptions().addField("item_title");//设置高亮的域
        options.setSimplePrefix("<span style='color:red'>");//高亮前缀
        options.setSimplePostfix("</span>");//高亮后缀
        query.setHighlightOptions(options);//将高亮选项加入到查询对象中

        //1. 关键字查询
        //is：基于分词后的结果 和 传入的参数匹配
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);


        //2按照分类筛选
        if(!"".equals(searchMap.get("category"))){
            //过滤查询条件对象
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);

        }
        //3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //4过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //5价格查询
        if (!"".equals(searchMap.get("price"))) {
            //区价格范围并分割字符串
            String[] pricesArr = searchMap.get("price").toString().split("-");
            String minPrice = pricesArr[0];//最低价

            Criteria criteria1 = new Criteria("item_price").greaterThanEqual(minPrice);
            FilterQuery filterQuery1 = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery1);

            if(!"*".equals(pricesArr[1])){
                String maxPrice = pricesArr[1];//最高价
                Criteria criteria2 = new Criteria("item_price").lessThanEqual(maxPrice);
                FilterQuery filterQuery2 = new SimpleFilterQuery(criteria2);
                query.addFilterQuery(filterQuery2);
            }
        }
        //6分页
        Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码;
        if(pageNo == null){
            pageNo = 1;
        }
        Integer pageSize= (Integer) searchMap.get("pageSize");//提取页面容量;
        if(pageSize == null){
            pageSize = 20;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //7.排序
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");
        if (sortField != null && !"".equals(sortField)){
            Sort s = new Sort("desc".equals(sort)?Sort.Direction.DESC:Sort.Direction.ASC,sortField);
            query.addSort(s);
        }
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {
            TbItem item = h.getEntity();
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮结果
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
    }



    /**
     * 查询品牌和规格的列表
     * @param category
     * @param map
     */
    private void brandAndSpecListSearch(String category,Map<String,Object> map){
        //获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(typeId != null){
            //根据模板查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);

            // 根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);

        }

    }
}
