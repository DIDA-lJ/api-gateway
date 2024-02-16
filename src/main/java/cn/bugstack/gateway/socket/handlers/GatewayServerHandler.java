package cn.bugstack.gateway.socket.handlers;

import cn.bugstack.gateway.bind.IGenericReference;
import cn.bugstack.gateway.session.GatewaySession;
import cn.bugstack.gateway.session.defaults.DefaultGatewaySessionFactory;
import cn.bugstack.gateway.socket.BaseHandler;
import cn.bugstack.gateway.socket.agreement.RequestParser;
import cn.bugstack.gateway.socket.agreement.ResponseParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author linqi
 * @version 1.0.0
 * @description 网络协议处理器
 */
public class GatewayServerHandler extends BaseHandler<FullHttpRequest> {

    private final Logger logger = LoggerFactory.getLogger(GatewayServerHandler.class);

    private final DefaultGatewaySessionFactory gatewaySessionFactory;

    public GatewayServerHandler(DefaultGatewaySessionFactory gatewaySessionFactory) {
        this.gatewaySessionFactory = gatewaySessionFactory;
    }

    @Override
    protected void session(ChannelHandlerContext ctx, final Channel channel, FullHttpRequest request) {
        logger.info("网关接收请求 uri：{} method：{}", request.uri(), request.method());
        // 1. 解析请求参数
        RequestParser requestParser = new RequestParser(request);
        String uri = requestParser.getUri();
        if (null == uri) return;
        Map<String, Object> args = requestParser.parse();

        // 2. 调用会话服务
        GatewaySession gatewaySession = gatewaySessionFactory.openSession(uri);
        IGenericReference reference = gatewaySession.getMapper();
        Object result = reference.$invoke(args);

        // 3. 封装返回结果
        DefaultFullHttpResponse response = new ResponseParser().parse(result);
        channel.writeAndFlush(response);
    }

}

