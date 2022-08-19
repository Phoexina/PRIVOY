package ch.ethz.infk.pps.zeph.client.transformation;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import ch.ethz.infk.pps.zeph.client.data.CiphertextData;
import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

//ToDo:监测到不活跃直接关闭可能存在问题
public class CiphertextTransformationFacade {
    public SocketChannel socketChannel;
    public static String HOST="8.130.10.212";
    public static int TCP_PORT=9009;
    private static Long number=0L;
    public static boolean OPEN=true;

    private CiphertextTransformationFacade(){

    }
    public volatile static CiphertextTransformationFacade ciphertextTransformationFacade = null;
    public static CiphertextTransformationFacade getInstance(){
        if (ciphertextTransformationFacade==null){
            synchronized (CiphertextTransformationFacade.class) {
                if (ciphertextTransformationFacade==null){
                    ciphertextTransformationFacade=new CiphertextTransformationFacade();
                }
            }
        }
        return ciphertextTransformationFacade;
    }


    public void startNetty() throws InterruptedException {
        Log.i("HttpNettyUtil.startNetty","netty start");
        OPEN=true;
        if(start()) {
            String hello = MyIdentity.myIdentity.getId() + " netty start";
            ByteBuf bb = Unpooled.wrappedBuffer((hello.getBytes(CharsetUtil.UTF_8)));
            socketChannel.writeAndFlush(bb);
        }

    }

    public void sendCiphertext(CiphertextData data){
        try {
            ObjectMapper mapper = new ObjectMapper();
            ByteBuf bb =Unpooled.wrappedBuffer(mapper.writeValueAsBytes(data));
            socketChannel.writeAndFlush(bb);
            number++;
            Log.i("Ciphertext"+number,data.toJSON());
        } catch (JsonProcessingException e) {
            Log.e("CiphertextTransformationFacade.sendCiphertext","ObjectMapper to json failed");
            e.printStackTrace();
        }
    }

    public void send2server(String msg){
        ByteBuf bb = Unpooled.wrappedBuffer((msg.getBytes(CharsetUtil.UTF_8)));
        socketChannel.writeAndFlush(bb);
    }

    private Boolean start() throws InterruptedException {
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
        bootstrap.remoteAddress( HOST,TCP_PORT);
        bootstrap.handler(new MyChannelInitializer());
        try {
            ChannelFuture future =bootstrap.connect(new InetSocketAddress( HOST,TCP_PORT)).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel)future.channel();
                System.out.println("连接服务器 成功---------");
                return true;
            }else{
                System.out.println("连接服务器 失败---------");
                startNetty();
                return false;
            }
        } catch (Exception e) {
            System.out.println("无法连接----------------5秒后重试");
            //这里最好暂停一下。不然会基本属于毫秒时间内执行很多次。
            //造成重连失败
            TimeUnit.SECONDS.sleep(2);
            startNetty();
            return false;
        }
    }

    class MyChannelInitializer extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new IdleStateHandler(20, 10, 0, TimeUnit.SECONDS));
            pipeline.addLast(new NettyClientHandler());

        }
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Log.e("NettyClientHandler.channelInactive","Inactive");
            ctx.channel().close();
            if(OPEN) startNetty();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                //该事件需要配合 io.netty.handler.timeout.IdleStateHandler使用
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                if (idleStateEvent.state() == IdleState.READER_IDLE) {
                    //超过指定时间没有读事件,关闭连接
                    Log.e("NettyClientHandler.channelInactive","close "+ctx.channel().remoteAddress());
                    ctx.channel().close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        }
    }


}
