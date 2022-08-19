package ch.ethz.infk.pps.zeph.client.loader;

import ch.ethz.infk.pps.zeph.client.util.LoaderUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.RandomAccessFile;

public class FileUploadServer {
    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
                    ch.pipeline().addLast(new FileUploadServerHandler());
                }
            });
            System.out.println("file server: open");
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
            System.out.println("file server: ?");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("read: end");
        }
    }

    /*
    public static void main(String[] args) {
        LoaderUtil.init("password");
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            new FileUploadServer().bind(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
        private int byteRead;
        private volatile int start = 0;
        private String file_dir = "data/toloader";

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FileUploadFile) {
                FileUploadFile ef = (FileUploadFile) msg;
                byte[] bytes = ef.getBytes();
                byteRead = ef.getEndPos();
                String md5 = ef.getFile_md5();//文件名
                String path = file_dir + File.separator + md5;
                File file = new File(path);
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(start);
                randomAccessFile.write(bytes);
                start = start + byteRead;
                System.out.println("read: "+path+" >"+byteRead+"/"+start);
                if (byteRead > 0) {
                    ctx.writeAndFlush(start);
                } else {
                    FileLoader.getInstance().loadIdentity(file);
                    randomAccessFile.close();
                    ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}