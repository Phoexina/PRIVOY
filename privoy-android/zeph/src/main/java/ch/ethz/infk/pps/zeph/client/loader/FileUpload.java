package ch.ethz.infk.pps.zeph.client.loader;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformationFacade;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class FileUpload {
    private FileUploadFile fileUploadFile;
    public static String HOST="59.110.115.66";
    public static int TCP_PORT=8080;

    public FileUpload(File file) throws Exception {
        fileUploadFile = new FileUploadFile();
        fileUploadFile.setFile(file);
        fileUploadFile.setFile_md5(MyIdentity.getInstance().getId()+"");
        fileUploadFile.setStarPos(0);
        connect();
    }

    public void connect() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY,true);
        bootstrap.handler(new MyChannelInitializer());
        try {
            ChannelFuture future = bootstrap.connect(HOST,TCP_PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new ObjectEncoder());
            pipeline.addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
            pipeline.addLast(new FileUploadClientHandler());

        }
    }


    class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
        private int byteRead;
        private volatile int start = 0;
        private volatile int lastLength = 0;
        public RandomAccessFile randomAccessFile;
        //private FileUploadFile fileUploadFile;

        public FileUploadClientHandler() {
            //fileUploadFile=file;
            if (fileUploadFile.getFile().exists()) {
                if (!fileUploadFile.getFile().isFile()) {
                    System.out.println("Not a file :" + fileUploadFile.getFile());
                    return;
                }
            }
        }

        public void channelActive(ChannelHandlerContext ctx) {
            try {
                randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                randomAccessFile.seek(fileUploadFile.getStarPos());
                lastLength = (int) randomAccessFile.length() / 10;
                byte[] bytes = new byte[lastLength];
                if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                    fileUploadFile.setEndPos(byteRead);
                    fileUploadFile.setBytes(bytes);
                    ctx.writeAndFlush(fileUploadFile);
                } else {
                    System.out.println("文件已经读完");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Integer) {
                start = (Integer) msg;
                if (start != -1) {
                    randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                    randomAccessFile.seek(start);
                    int a = (int) (randomAccessFile.length() - start);
                    int b = (int) (randomAccessFile.length() / 10);
                    if (a < b) {
                        lastLength = a;
                    }
                    Log.i("FileUploadClientHandler","read: " + lastLength+"/" + randomAccessFile.length());
                    byte[] bytes = new byte[lastLength];
                    //System.out.println("-----------------------------" + bytes.length);
                    if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
                        //System.out.println("byte 长度：" + bytes.length);
                        fileUploadFile.setEndPos(byteRead);
                        fileUploadFile.setBytes(bytes);
                        try {
                            ctx.writeAndFlush(fileUploadFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        fileUploadFile.setEndPos(byteRead);
                        fileUploadFile.setBytes(bytes);
                        ctx.writeAndFlush(fileUploadFile);
                        randomAccessFile.close();
                        ctx.close();
                        Log.i("FileUploadClientHandler","finished " + byteRead);
                    }
                }
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
