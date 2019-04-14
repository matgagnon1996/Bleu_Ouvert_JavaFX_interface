/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;

import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Gst;

import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSink;


/**
 *
 * @author mathieu
 */
public class FFmpegFXImageDecoder {
    
    private OpenCVFrameGrabber frameGrabber;
    private Frame frame;
    private BufferedImage image;
    private Java2DFrameConverter frameConverter;
    private ImageView view;
    private VideoCapture video;
    private Mat imageMat;
    
    
    FFmpegFXImageDecoder(int port, ImageView view) throws Exception
    {
       
       // TODO : à voir pour gstreamer en JAVA 
       AppSink videosink = new AppSink("GstVideoComponent");
        System.err.println("Allo");
        this.frameGrabber.start();
        // TODOD
        this.frameConverter = new Java2DFrameConverter();
        this.view = view;
    }
    
    public void FFmpegFXImageDecoderStart() throws Exception
    {
        while (!Thread.interrupted()) {
            //frame = this.video.grab();
            if (frame != null) {
                
                //image = frameConverter.convert(imageMat);
                if (image != null) {
                    Platform.runLater(() ->
                        view.setImage(SwingFXUtils.toFXImage(image, null)));
                }
            }
        }
    }
    
    
}