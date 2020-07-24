package com.lazy.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.lazy.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtil smsUtil;
    @JmsListener(destination = "sms_queue")
    public void sendSms(Map<String, String> map){
        try {
            SendSmsResponse response = smsUtil.sendSms(map);
            System.out.println("Code=" + response.getCode());
            System.out.println("Message=" + response.getMessage());
            System.out.println("Message=" + response.getBizId());
            System.out.println("Message=" + response.getRequestId());

        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
