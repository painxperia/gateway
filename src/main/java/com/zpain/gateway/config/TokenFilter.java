package com.zpain.gateway.config;

import com.alibaba.fastjson.JSON;
import com.zpain.gateway.domain.TokenInfo;
import com.zpain.gateway.service.RedisManager;
import com.zpain.gateway.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author zhangjun
 * @date 2021/10/26  9:51
 */
@Component
@Slf4j
public class TokenFilter implements GlobalFilter {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisManager redisManager;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        String hostAddress = request.getRemoteAddress().getAddress().getHostAddress();
        if (path.matches("/user/login")||path.matches("/user/register")){
            return chain.filter(exchange);
        }
        String token = request.getHeaders().getFirst(tokenHeader);
        //token不存在 无权限
        if (StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String authToken = token.substring(tokenHead.length());
        String username = jwtTokenUtil.getUsernameFromToken(authToken);
        //username 解析不存在
        if (StringUtils.isEmpty(username)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String key = String.format("%s-token", username);
        String tokenInfo = redisManager.getCache(key);
        //token不存在redis当中 用户未登录 权限不存在
        if (StringUtils.isBlank(tokenInfo)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        TokenInfo tokenModel = JSON.parseObject(tokenInfo, TokenInfo.class);
        return chain.filter(exchange);
    }
}
