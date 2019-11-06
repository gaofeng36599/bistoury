/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package qunar.tc.bistoury.proxy.communicate.agent.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qunar.tc.bistoury.common.JacksonSerializer;
import qunar.tc.bistoury.common.TypeResponse;
import qunar.tc.bistoury.proxy.communicate.Session;
import qunar.tc.bistoury.proxy.communicate.SessionManager;
import qunar.tc.bistoury.proxy.service.profiler.ProfilerStateManager;
import qunar.tc.bistoury.remoting.protocol.Datagram;
import qunar.tc.bistoury.remoting.protocol.ResponseCode;

import java.util.Map;
import java.util.Set;

import static qunar.tc.bistoury.remoting.protocol.ResponseCode.RESP_TYPE_CONTENT;

/**
 * @author zhenyu.nie created on 2019 2019/5/14 18:12
 */
@Service
public class AgentResponseProcessor implements AgentMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AgentResponseProcessor.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ProfilerStateManager profilerStateManager;

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(
                RESP_TYPE_CONTENT.getCode(),
                ResponseCode.RESP_TYPE_EXCEPTION.getCode(),
                ResponseCode.RESP_TYPE_SINGLE_END.getCode()
        );
    }

    @Override
    public void process(ChannelHandlerContext ctx, Datagram message) {
        String id = message.getHeader().getId();
        Session session = sessionManager.getSession(id);
        if (session != null) {
            session.writeToUi(message);
        } else {
            if (!canIgnore(message)) {
                logger.warn("id [{}] can not get session, write response fail, {}", id, ctx.channel());
            }
        }
    }

    private boolean canIgnore(Datagram datagram) {
        String profilerId = datagram.getHeader().getId();
        boolean isProfilerResponse = profilerStateManager.isProfilerRequest(profilerId);
        if (isProfilerResponse && datagram.getHeader().getCode() == RESP_TYPE_CONTENT.getCode()) {

            ByteBuf body = datagram.getBody();
            byte[] data = new byte[body.readableBytes()];
            body.readBytes(data);
            TypeResponse<Map<String, Object>> profilerInfoType = JacksonSerializer.deSerialize(data, new TypeReference<TypeResponse<Map<String, Object>>>() {
            });
            profilerStateManager.dealProfiler(profilerId, profilerInfoType);
        }
        return isProfilerResponse;
    }
}
