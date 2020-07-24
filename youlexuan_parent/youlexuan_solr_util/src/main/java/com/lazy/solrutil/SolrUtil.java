package com.lazy.solrutil;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.lazy.mapper.TbItemMapper;
import com.lazy.pojo.TbItem;
import com.lazy.pojo.TbItemExample;
import com.lazy.pojo.TbItemExample.Criteria;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring-*.xml")
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入数据
     */
    @Test(timeout = 30000)
    public void importItemData(){
        TbItemExample example = new TbItemExample();
        Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核的数据
        List<TbItem> tbItems = tbItemMapper.selectByExample(example);
        for (TbItem item : tbItems) {
            Map<String, Object> specMap= JSON.parseObject(item.getSpec());//将spec字段中的json字符串转换为map
            Map map = new HashMap();
            for(String key : specMap.keySet()) {
                map.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }
            item.setSpecMap(map);	//给带动态域注解的字段赋值
            System.out.println(item.getTitle());
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
    }

    public void deletdItemData(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
