package com.zpain.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangjun
 * @date 2021/10/19  13:12
 */
@RestController
@RefreshScope
public class GateWayController {
    @Value("${spring.cloud.nacos.config.server-addr}")
    private String value;

    @GetMapping("/getValue")
    public String getValue(){
        return value;
    }
}
