/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectatorpiapp;

import java.net.URL;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSink;

/**
 *
 * @author mathieu
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Label text;
    
    @FXML
    private ImageView cam1_view, cam2_view;
    
    private AppSink cam1, cam2;
    private Pipeline pipe_cam1, pipe_cam2;
    Bus bus_cam1, bus_cam2;
    private StringBuilder caps_cam1, caps_cam2;
    private ImageContainer img_cam1, img_cam2;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      
        
        initPipelineCam1();
        initPipelineCam2();
        initThreadPhrase();
    }    
    
    
    private void initPipelineCam1()
    {
        
        // set image view property
        cam1_view.setPreserveRatio(true);
        
        // create gstreamer pipeline
        pipe_cam1 = (Pipeline) Gst.parseLaunch("tcpclientsrc host=math-pi-1.local port=10003 ! gdpdepay ! rtph264depay ! avdec_h264 ! videoconvert ! appsink name=cam1");
       
        // link info to display
        bus_cam1 = pipe_cam1.getBus();
        bus_cam1.connect((Bus.EOS) gstObject -> System.out.println("EOS "+gstObject));
        bus_cam1.connect((Bus.ERROR) (gstObject, i, s) -> System.out.println("ERROR "+i+" "+s+" "+gstObject));
        bus_cam1.connect((Bus.WARNING) (gstObject, i, s) -> System.out.println("WARN "+i+" "+s+" "+gstObject));
        bus_cam1.connect((Bus.EOS) obj -> Gst.quit() );
        
        // get appsink
        cam1 = (AppSink) pipe_cam1.getElementByName("cam1");
        cam1.set("emit-signals", true);
        
        // link pipeline
        pipe_cam1.link(cam1);
        
        // connect appListener
        AppSinkListener GSTListener = new AppSinkListener();
        cam1.connect(GSTListener);    
        
        // create caps
        caps_cam1 = new StringBuilder("video/x-raw, ");
        // JNA creates ByteBuffer using native byte order, set masks according to that.
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            caps_cam1.append("format=BGRx");
        } else {
            caps_cam1.append("format=xRGB");
        }
        cam1.setCaps(new Caps(caps_cam1.toString()));
        cam1.set("max-buffers", 5000);
        cam1.set("drop", true);
       
        // get image container
        img_cam1 = GSTListener.getImageContainer();
        
        // add listener
         img_cam1.addListener(new ChangeListener<Image>() {

			@Override
			public void changed(ObservableValue<? extends Image> observable, Image oldValue, final
					Image newValue) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						cam1_view.setImage(newValue);
					}
				});
				
			}
        	
            });
        
        // start pipeline
        pipe_cam1.play();
        
    }
    
    private void initPipelineCam2()
    {
        // set image view property
        cam2_view.setPreserveRatio(true);
        
        // create gstreamer pipeline
        pipe_cam2 = (Pipeline) Gst.parseLaunch("tcpclientsrc host=math-pi-2.local port=10004 ! gdpdepay ! rtph264depay ! avdec_h264 ! videoconvert ! appsink name=cam2");
       
        // link info to display
        bus_cam2 = pipe_cam2.getBus();
        bus_cam2.connect((Bus.EOS) gstObject -> System.out.println("EOS "+gstObject));
        bus_cam2.connect((Bus.ERROR) (gstObject, i, s) -> System.out.println("ERROR "+i+" "+s+" "+gstObject));
        bus_cam2.connect((Bus.WARNING) (gstObject, i, s) -> System.out.println("WARN "+i+" "+s+" "+gstObject));
        bus_cam2.connect((Bus.EOS) obj -> Gst.quit() );
        
        // get appsink
        cam2 = (AppSink) pipe_cam2.getElementByName("cam2");
        cam2.set("emit-signals", true);
        
        // link pipeline
        pipe_cam2.link(cam2);
        
        // connect appListener
        AppSinkListener GSTListener = new AppSinkListener();
        cam2.connect(GSTListener);    
        
        // create caps
        caps_cam2 = new StringBuilder("video/x-raw, ");
        // JNA creates ByteBuffer using native byte order, set masks according to that.
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            caps_cam2.append("format=BGRx");
        } else {
            caps_cam2.append("format=xRGB");
        }
        cam2.setCaps(new Caps(caps_cam2.toString()));
        cam2.set("max-buffers", 5000);
        cam2.set("drop", true);
       
        // get image container
        img_cam2 = GSTListener.getImageContainer();
        
        // add listener
         img_cam2.addListener(new ChangeListener<Image>() {

			@Override
			public void changed(ObservableValue<? extends Image> observable, Image oldValue, final
					Image newValue) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						cam2_view.setImage(newValue);
					}
				});
				
			}
        	
            });
        
        // start pipeline
        pipe_cam2.play();
    }
    
    private void initThreadPhrase()
    {
        // launch thread to 
        Thread statusThread = new Thread(){
                public void run()
                {
                    while(true)
                    {
                        try
                        {
                            
                            // TODO à compléter avec des vraies phrases
                           Platform.runLater(new Runnable() {
					@Override
					public void run() {
                                            text.setText("aldjoehsf;oihme2p9qyw9p8yfd2e lwhrpnif2yzdep9 ;wrhpnfd12d3y98 elaurgfpb2u31ye⁹ 3ifue9pe2h392fheo");
                                        }
                           });
                            Thread.sleep(15000);
                            
                            
                            Platform.runLater(new Runnable() {
					@Override
					public void run() {
                                            text.setText("Allo je suis beau et bleu ouvert");
                                        }
                            });
                            Thread.sleep(15000);
                            
                            Platform.runLater(new Runnable() {
					@Override
					public void run() {
                                            text.setText("La terre est en train de mourir à cause du manque de recyclage");
                                        }
                            });
                            
                            Thread.sleep(15000);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            statusThread.setDaemon(true);
            statusThread.start();
    }
    
    
}
