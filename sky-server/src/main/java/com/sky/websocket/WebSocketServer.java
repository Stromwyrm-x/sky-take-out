package com.sky.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer
{
    private static Map<String, Session> sessionMap=new HashMap<>();

    @OnOpen
    public void open(Session session, @PathParam(value = "sid")String sid)
    {
        log.info("建立连接，{}",sid);
        sessionMap.put(sid,session);
    }

    @OnMessage
    public void message(@PathParam(value = "sid")String sid,String message)
    {
        log.info("收到了消息，{}",message);
    }


    @OnClose
    public void close(@PathParam(value = "sid")String sid)
    {
        log.info("关闭连接，{}",sid);
        sessionMap.remove(sid);
    }

    public void sendToAll(String message) throws IOException
    {
        Collection<Session> values = sessionMap.values();
        if (!CollectionUtils.isEmpty(values))
        {
            for (Session value : values)
            {
                value.getBasicRemote().sendText(message);
            }
        }
    }

}
