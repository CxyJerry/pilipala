package com.jerry.pilipala.domain.message.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.apistd.uni.UniResponse;
import com.apistd.uni.sms.UniMessage;
import com.apistd.uni.sms.UniSMS;
import com.jerry.pilipala.domain.message.service.SmsService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    @Value("${spring.mail.username}")
    private String sender;
    private final SmsConfig smsConfig;
    private final JavaMailSenderImpl javaMailSender;

    public SmsServiceImpl(SmsConfig smsConfig,
                          JavaMailSenderImpl javaMailSender) {
        this.smsConfig = smsConfig;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendCode(String tel, String code, Integer expireMinutes) {
        HashMap<String, String> templateData = new HashMap<>();
        templateData.put("code", code);
        templateData.put("ttl", String.valueOf(expireMinutes));

        UniMessage message = UniSMS.buildMessage()
                .setTo(tel)
                .setSignature(smsConfig.getSignature())
                .setTemplateId("pub_verif_login_ttl")
                .setTemplateData(templateData);
        try {
            UniResponse res = message.send();
            log.info("result: {}", res);
        } catch (Exception e) {
            log.error("send sms error: ", e);
            throw BusinessException.businessError("短信验证码发送失败");
        }


    }

    @Override
    public void sendEmailCode(String email, String code, Integer expireMinutes) {
        TemplateEngine engine = TemplateUtil.createEngine(
                new TemplateConfig("templates",
                        TemplateConfig.ResourceMode.CLASSPATH));
        Template template = engine.getTemplate("login-email.html");
        Dict params = Dict.create().set("receiver", email)
                .set("code", code)
                .set("expire", expireMinutes);
        String emailContent = template.render(params);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Pilipala-登录验证码邮件");
            helper.setText(emailContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("邮件验证码发送失败", e);
            throw BusinessException.businessError("邮件验证码发送失败");
        }

    }
}
