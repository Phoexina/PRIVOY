package netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.CiphertextData;
import drivers.CiphertextDriver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class NettyStart {

    private int port;
    public static Long number=0L;

    public NettyStart(int port) throws Exception {
        this.port = port;
        this.launch();
    }

    public void launch(){
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss,work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new MyChannelInitializer());

        bootstrap.option(ChannelOption.SO_BACKLOG, 128);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("服务器已启动，端口号为:"+port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    class MyChannelInitializer extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {

            ChannelPipeline pipeline = ch.pipeline();
            //pipeline.addLast(new HttpServerCodec());// http 编解码
            //pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024)); // http消息聚合器
            pipeline.addLast(new IdleStateHandler(50, 0, 0, TimeUnit.SECONDS));
            pipeline.addLast(new NettyServerHandler());

        }

    }

    class NettyServerHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("我是服务器 关闭到一个不活跃的频道"+ctx.channel().remoteAddress());
            ctx.channel().close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                //该事件需要配合 io.netty.handler.timeout.IdleStateHandler使用
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                System.out.println(idleStateEvent.state()+":"+IdleState.READER_IDLE);
                if (idleStateEvent.state() == IdleState.READER_IDLE) {
                    //超过指定时间没有读事件,关闭连接
                    System.out.println("超过心跳时间,关闭和服务端的连接:"+ctx.channel().remoteAddress());
                    ctx.channel().close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        //这里是从客户端过来的消息
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object baseMsg) {
            number++;
            String msg1=((ByteBuf)baseMsg).toString(CharsetUtil.UTF_8).trim();
            System.out.println(number+"> "+msg1+" "+ctx.channel().remoteAddress());
            if(msg1.charAt(0)!='{'){
                ByteBuf bb = Unpooled.wrappedBuffer("ok".getBytes(CharsetUtil.UTF_8));
                ctx.writeAndFlush(bb);
                return;
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                CiphertextData data=mapper.readValue(msg1, CiphertextData.class);
                CiphertextDriver driver=CiphertextDriver.getCiphertextDriver();
                driver.send(data);
                //System.out.println(data.toJSON());
            } catch (JsonProcessingException e) {
                ByteBuf bb = Unpooled.wrappedBuffer("error".getBytes(CharsetUtil.UTF_8));
                ctx.writeAndFlush(bb);
                throw new RuntimeException(e);
            }
            ByteBuf bb = Unpooled.wrappedBuffer("pong".getBytes(CharsetUtil.UTF_8));
            ctx.writeAndFlush(bb);
            //ReferenceCountUtil.release(baseMsg);
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
            super.exceptionCaught(ctx, cause);
            System.out.println(number+"> error");
        }

    }



}
