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

package com.zto.zms.agent;

import com.zto.zms.agent.web.ProcessHandler;
import com.zto.zms.agent.web.task.ServiceMonitorTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <p> Description: </p>
 * @author lidawei
 */
public class AgentServer extends ChannelInitializer<SocketChannel> {
	private Logger logger = LoggerFactory.getLogger(AgentServer.class);
	private int port;
	public AgentServer(int port){
		this.port = port;
	}
	public void start() throws Exception{
		ServerBootstrap bootstrap = new ServerBootstrap();
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup work = new NioEventLoopGroup();
		bootstrap.group(boss,work)
				.handler(new LoggingHandler(LogLevel.DEBUG))
				.channel(NioServerSocketChannel.class)
				.childHandler(this);

		ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).sync();
		logger.info("agent server start up on port : " + port);
		f.channel().closeFuture().sync();

	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast(new HttpServerCodec());// http 编解码
		pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024)); // http 消息聚合器                                                                     512*1024为接收的最大contentlength
		pipeline.addLast(new ProcessHandler());// 请求处理器

	}
	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(System.getProperty("server.port","18080"));
		ServiceMonitorTask task = new ServiceMonitorTask();
		task.start();
		AgentServer server = new AgentServer(port);
		server.start();
	}
}

