package com.lazy.listener;

import com.lazy.page.servcie.ItemPageService;
import com.lazy.page.service.impl.ItemPageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class DelHtmlTopicListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage obj = (ObjectMessage) message;
            Long[] ids = (Long[]) obj.getObject();
                itemPageService.deleteItemHtml(ids);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
