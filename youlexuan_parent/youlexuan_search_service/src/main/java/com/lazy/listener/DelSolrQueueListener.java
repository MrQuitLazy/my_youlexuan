package com.lazy.listener;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.List;

@Component
public class DelSolrQueueListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息。。。");
        try{
            ObjectMessage obj = (ObjectMessage) message;
            Long[] ids = (Long[]) obj.getObject();
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
