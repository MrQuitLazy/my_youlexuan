package com.lazy.listener;

import com.alibaba.fastjson.JSON;
import com.lazy.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;


@Component
public class ImportSolrQueueListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听收到消息");
        try{
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList =  JSON.parseArray(text,TbItem.class);
            itemSearchService.importList(itemList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
