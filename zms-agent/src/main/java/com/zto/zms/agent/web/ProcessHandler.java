/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.agent.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zto.zms.agent.core.manager.UnixProcessManager;
import com.zto.zms.common.ZmsException;
import com.zto.zms.utils.Assert;
import com.zto.zms.agent.web.dto.ProcessRequestDTO;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.common.Result;
import com.zto.zms.agent.core.manager.ProcessManager;
import com.zto.zms.utils.HMacVerifyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;
import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;

/**
 * <p>Class: ProcessController</p>
 * <p>Description: 进程处理</p>
 *
 * @author lidawei
 * @date 2020/2/24
 **/
public class ProcessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private ProcessManager processManager = new UnixProcessManager();
    private String token = System.getProperty(ZMS_TOKEN);
    private Logger logger = LoggerFactory.getLogger(ProcessHandler.class);

    /**
     * 启动服务进程
     *
     * @param processRequestDto
     * @return
     * @throws Exception
     */
    public Result<Boolean> start(ProcessRequestDTO processRequestDto, String sign) throws Exception {
        Assert.that(HMacVerifyUtil.verifySignature(sign, processRequestDto, token), "验证签名不通过");
        boolean success = processManager.start(processRequestDto);
        if (success) {
            return Result.success(null);
        }
        return Result.error("401", "启动失败");
    }

    /**
     * 停止服务进程
     *
     * @param processRequestDto
     * @return
     * @throws Exception
     */
    public Result<Boolean> stop(ProcessRequestDTO processRequestDto, String sign) throws Exception {
        Assert.that(HMacVerifyUtil.verifySignature(sign, processRequestDto, token), "验证签名不通过");
        boolean success = processManager.stop(processRequestDto.getProgramName());
        if (success) {
            return Result.success(null);
        }
        return Result.error("401", "停止服务进程失败");
    }

    public Result<String> readProcessStdoutLog(String programName, int offset, int length) throws Exception {
        return Result.success(processManager.readProcessStdoutLog(programName, offset, length));
    }

    public Result<String> readProcessStderrLog(String programName, int offset, int length) throws Exception {
        return Result.success(processManager.readProcessStderrLog(programName, offset, length));
    }

    public Result<ProcessStdLog> tailProcessStdoutLog(String programName, int offset, int length) throws Exception {
        return Result.success(processManager.tailProcessStdoutLog(programName, offset, length));
    }

    public Result<ProcessStdLog> tailProcessStderrLog(String programName, int offset, int length) throws Exception {
        return Result.success(processManager.tailProcessStderrLog(programName, offset, length));
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //100 Continue
        if (is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }
        // 获取请求的uri
        String uri = req.uri();
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri);
        Result res = null;
        try {
            if (queryDecoder.path().equals("/api/agent/process/start") ||
                    queryDecoder.path().equals("/api/agent/process/stop")) {
                String sign = queryDecoder.parameters().get("sign").get(0);
                ByteBuf jsonBuf = req.content();
                String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
                ProcessRequestDTO dto = JSONObject.parseObject(jsonStr, ProcessRequestDTO.class);
                if (uri.startsWith("/api/agent/process/start")) {
                    res = this.start(dto, sign);
                } else if (uri.startsWith("/api/agent/process/stop")) {
                    res = this.stop(dto, sign);
                }
            } else if (queryDecoder.path().equals("/api/agent/process/readProcessStdoutLog")
                    || queryDecoder.path().equals("/api/agent/process/readProcessStderrLog")
                    || queryDecoder.path().equals("/api/agent/process/tailProcessStdoutLog")
                    || queryDecoder.path().equals("/api/agent/process/tailProcessStderrLog")) {
                Map<String, List<String>> parameters = queryDecoder.parameters();
                String programName = parameters.get("programName").get(0);
                int offset = Integer.valueOf(parameters.get("offset").get(0));
                int length = Integer.valueOf(parameters.get("length").get(0));
                if (uri.startsWith("/api/agent/process/readProcessStdoutLog")) {
                    res = this.readProcessStdoutLog(programName, offset, length);
                } else if (uri.startsWith("/api/agent/process/readProcessStderrLog")) {
                    res = this.readProcessStderrLog(programName, offset, length);
                } else if (uri.startsWith("/api/agent/process/tailProcessStdoutLog")) {
                    res = this.tailProcessStdoutLog(programName, offset, length);
                } else if (uri.startsWith("/api/agent/process/tailProcessStderrLog")) {
                    res = this.tailProcessStderrLog(programName, offset, length);
                }
            }
            if (res == null) {
                res = Result.error("404", uri + "不存在");
            }
        } catch (ZmsException e) {
            logger.error(e.getMessage(), e);
            res = Result.error(String.valueOf(e.getCode()), e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            res = Result.error("401", e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res = Result.error("500", "系统异常:"+e.getMessage());
        }
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(JSON.toJSONString(res), CharsetUtil.UTF_8));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}


