/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import mainpiapp.Server;
import mainpiapp.ClientIO;
import java.net.Socket;
import java.nio.ByteOrder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import mainpiapp.MessageHeader;
import mainpiapp.MessageType;

import java.nio.ByteOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.PadLinkException;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.freedesktop.gstreamer.lowlevel.MainLoop;

/**
 *
 * @author mathieu
 */
public class FXMLDocumentController implements Initializable {
    
    public static final int MESSAGE_HEADER_LENGTH = 16; // bytes
    public static final int MAX_PAYLOAD_SIZE = 128; // bytes
    public static final int COMMAND_PORT = 10001;
    public static final int STATUS_PORT = 10002;
    public static final int CAM1_PORT = 10003;
    public static final int CAM2_PORT = 10004;
    
    public static final double MAX_PET_1_TEMP = 265.0;
    public static final double MIN_PET_1_TEMP = 255.0;
    public static final double DEFAULT_PET_1_TEMP = 260.0;
    
    public static final double MAX_HDPE_2_TEMP = 85.0;
    public static final double MIN_HDPE_2_TEMP = 140.0;
    public static final double DEFAULT_HDPE_2_TEMP = 135.0;
    
    public static final double MAX_PP_5_TEMP = 145.0;
    public static final double MIN_PP_5_TEMP = 175.0;
    public static final double DEFAULT_PP_5_TEMP = 165.0;
    
    public static final double MAX_WEIGHT = 2500;
    
    public static final int MAX_QR_CODE_HEIGHT = 135;
    public static final int MAX_QR_CODE_WIDTH = 135;
    
    public static final int MAX_NUMBER_OF_STICKER = 100;
    
    private Server serverCommand;
    private Server serverStatus;
    private HashMap<String, ClientIO> CP_clientList;
    private HashMap<String, ClientIO> SP_clientList;
    
    
    private static ObservableDataSts StatusStruct;
    
    private static boolean threadRunning = true;
    
    private Media media;
    
    private QRCodeGenerator qrGenerator;
   
    private static byte[] soundBytes = null;

    @FXML
    private Button start_button, print_button, stop_button, move_servo_button, heat_button;
    
    @FXML
    private ToggleButton toggle_button_mode;
    
    @FXML
    private Label current_temp, command_temp, current_weight, command_weight, sts_fsm_label;
    
    @FXML
    private TextField temp_input, weight_input, lot_number_input, nb_sticker_input;
    
    @FXML
    private Circle  sts_cam1, sts_cam2, sts_control, sts_printer, sts_bourrage;
    
    
    @FXML
    private ComboBox channel_choice, plastic_type;
    
    @FXML
    private DatePicker date_input;
    
    @FXML
    private ImageView cam1_view, cam2_view;
    
    @FXML
    private Slider slider_servo_pos;
    
    // gstreamer private attribute
    private AppSink videosink;
    private Pipeline pipe;
    //private Bin bin;
    Bus bus;
    private StringBuilder caps;
            
          
    @FXML
    public void toggleModeButton(MouseEvent event)
    {
        // selected == extrudeur mode
        // not selected == crusher mode
        if(toggle_button_mode.isSelected())
        {
            toggle_button_mode.setText("Extrudeur");
            toggle_button_mode.setStyle("-fx-background-color: #22c3f3; -fx-text-fill: white; -fx-font-weight: bold;");
        }else
        {
            toggle_button_mode.setText("Déchiqueteur");
            toggle_button_mode.setStyle("-fx-background-color: #90c84e; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }
    
    
    @FXML
    public void sendEventHandler(MouseEvent event)
    {
        ClientIO clientToSend = CP_clientList.get("RPI-Control");
        
        String messageStr = "Allo j'envoie un message";
        byte[] array;
        if(clientToSend.isConnected())
        {
            try
            {
                MessageHeader mesgHdr = new MessageHeader("RPI", MessageType.ACK_MSG.Value(), messageStr.length(), 3);
                array = clientToSend.headerToByte(mesgHdr);
                
               
                clientToSend.write(array);
                clientToSend.write(messageStr.getBytes());
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            
        }
        
    }
    

    
     @FXML
    public void sendPrintCommandEventHandler(MouseEvent event)
    {
        // get client to send
        ClientIO clientToSend = CP_clientList.get("RPI-Printer");
        
        try
        {
            // get date
            LocalDate localDate = date_input.getValue();
            if(localDate == null)
            {
                localDate = LocalDate.now();
            }
            String date = localDate.toString();
            
            // get platsic type
            Object plastic_t = plastic_type.getSelectionModel().getSelectedItem();
            if(plastic_t == null)
            {
                throw new NumberFormatException();
            }
            String plastic = plastic_t.toString();
            
            // get lot number
            String lotNumber = lot_number_input.getText();
            if(lotNumber.isEmpty())
            {
                throw new NumberFormatException();
            }
            
            // get number of sticker
            String numberSticker = nb_sticker_input.getText();
            if(numberSticker.isEmpty())
            {
                throw new NumberFormatException();
            }
            
            int numberOfSticker = Integer.parseInt(numberSticker);
            if(numberOfSticker > MAX_NUMBER_OF_STICKER)
            {
                // too much sticker to print
                throw new NumberFormatException();
            }
            
            System.out.println(date);
            System.out.println(plastic);
            System.out.println(lotNumber);
            System.out.println(numberOfSticker);

            // generate QR code (PNG format)
            String qrString = "bleu_ouvert \n" + "Date : " + date + "\n" + "Type de plastique : " + plastic + "\n" + "Numéro de lot : "+ lotNumber + "\n";
            QRCodeGenerator.generateQRCodeImage(qrString, MAX_QR_CODE_WIDTH, MAX_QR_CODE_HEIGHT, "./QR_GENERATED.png");
            
            byte[] byteToSend = QRCodeGenerator.getQRCodeImage(qrString, MAX_QR_CODE_WIDTH, MAX_QR_CODE_HEIGHT);
            
            System.out.println(byteToSend.length);
         
            // send command
            if(clientToSend.isConnected())
            {
                clientToSend.sendPrintCommand(new MessageHeader("RPI",MessageType.PRINT_CMD.Value(), byteToSend.length + 4, 3),
                                                numberOfSticker, byteToSend);
            }else
            {
                throw new Exception();
            }

        }catch(NullPointerException e)
        {
            // client has been remove frome the list
            errorMessage("Le client " + "RPI-Printer" +" n'est plus connecté");
        
        }catch(IOException e)
        {
            // client is unconnected
            errorMessage("Le client " + "RPI-Printer" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }catch(NumberFormatException e)
        {
            errorMessage("Les informations d'impression sont invalides");
        }catch(Exception e)
        {
             // client is unconnected
            errorMessage("Le client " + "RPI-Printer" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }
        
    }
     
    @FXML
    public void sendMotorCommandEventHandler(MouseEvent event)
    {
        // get client to send
        ClientIO clientToSend = CP_clientList.get("RPI-Control");
        
        // open a new windows
        int channel = channel_choice.getSelectionModel().getSelectedIndex();
        int value = (int)(slider_servo_pos.getValue());
        
        // send command
        try
        {
            if(clientToSend.isConnected())
            {
                clientToSend.sendServoCommand(new MessageHeader("RPI",MessageType.MANUAL_CMD_SERVO.Value(), 12, 3),channel, 
                                                PololuCmdType.POLOLU_CMD_POSITION.Value(),value);
            }else
            {
                throw new Exception();
            }
        }catch(NullPointerException e)
        {
            // client has been remove frome the list
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
        }catch(Exception e)
        {
            // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
            //
        }
        
    }
    
    @FXML
    public void sendHeatCommandEventHandler(MouseEvent event)
    {
        // get client to send
        ClientIO clientToSend = CP_clientList.get("RPI-Control");
        
        String temperature = "";
        
        // send command
        try
        {
            // get temperature 
            temperature = temp_input.getText();
            
            if(temperature.isEmpty())
            {
                throw new NumberFormatException();
            }
            
            double temp = Double.valueOf(temperature);
            
            if(temp > MAX_PET_1_TEMP || temp < MAX_HDPE_2_TEMP)
            {
                throw new NumberFormatException();
            }
            
            if(clientToSend.isConnected())
            {
                // send to client
                clientToSend.sendHeatCommand(new MessageHeader("RPI", MessageType.HEATER_MANUAL_CMD.Value(), temperature.length(), 3), temperature);
                
                // update GUI
                StatusStruct.setCommandTemperature(Double.valueOf(temperature));
            }else
            {
                throw new IOException();
            }
        }catch(NullPointerException e)
        {
            // client has been remove frome the list
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
        }catch(IOException e)
        {
            // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }catch(NumberFormatException e)
        {
            errorMessage("La température demandé est invalide");
        }catch(Exception e)
        {
             // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }
        
    }
    
    @FXML
    public void sendStartCommandEventHandler(MouseEvent event)
    {
        // get client to send
        ClientIO clientToSend = CP_clientList.get("RPI-Control");
        
        String temperature = "";
        String weight = "";
        
        // send command
        try
        {
            if(toggle_button_mode.isSelected())
            {
                // extruder mode //
                
                // verify that a job is not already started
                if(!(StatusStruct.getFSMStatus().equals("En attente")))
                {
                    throw new InterruptedException();
                }
                
                // get platsic type
                Object plastic_t = plastic_type.getSelectionModel().getSelectedItem();
                if(plastic_t == null)
                {
                    throw new NumberFormatException();
                }
                
                
                // get temperature 
                temperature = temp_input.getText();
                
                if(temperature.isEmpty())
                {
                    // get default value depending on type
                    if(plastic_t.toString().equals("PET(#1)"))
                    {
                        temperature = String.valueOf(DEFAULT_PET_1_TEMP);
                    }else if(plastic_t.toString().equals("HDPE(#2)"))
                    {
                        temperature = String.valueOf(DEFAULT_HDPE_2_TEMP);
                    }else if(plastic_t.toString().equals("PP(#5)"))
                    {
                       temperature = String.valueOf(DEFAULT_PP_5_TEMP);
                    }else
                    {
                        throw new NumberFormatException();
                    }    
                }
                
                // check if temperature is in range
                double temp =  Double.valueOf(temperature);
                if(temp > MAX_PET_1_TEMP || temp < MAX_HDPE_2_TEMP)
                {
                    throw new NumberFormatException();
                }
                
                
                if(clientToSend.isConnected())
                {
                    // send to client
                    clientToSend.sendHeatCommand(new MessageHeader("RPI", MessageType.EXTRUDER_MODE_CMD.Value(), temperature.length(), 3), temperature);

                    // update GUI
                    StatusStruct.setCommandTemperature(Double.valueOf(temperature));
                    StatusStruct.setCommandWeight(0.0);
                }else
                {
                    throw new IOException();
                }
                
            }else
            {
                // crusher mode //
                
                if(!(StatusStruct.getFSMStatus().equals("En attente")))
                {
                    throw new InterruptedException();
                }
                
                // get weight 
                weight = weight_input.getText();
                
                if(weight.isEmpty())
                {
                    throw new NumberFormatException();
                }
                
                double w = Double.valueOf(weight);
                
                if(w > MAX_WEIGHT || w < 0)
                {
                    throw new NumberFormatException();
                }
                
                if(clientToSend.isConnected())
                {
                    clientToSend.sendHeatCommand(new MessageHeader("RPI", MessageType.CRUSHER_MODE_CMD.Value(), weight.length(), 3), weight);
                    
                    // update GUI
                    StatusStruct.setCommandWeight(Double.valueOf(weight));
                }else
                {
                    throw new IOException();
                }
                

                
            }
        }catch(InterruptedException e)
        {
            errorMessage("Une tâche est déjà démarrée, veuillez l'arrêter");
        }catch(NullPointerException e)
        {
            // client has been remove frome the list
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
        }catch(IOException e)
        {
            // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }catch(NumberFormatException e)
        {
            errorMessage("Paramètres invalides");
        }catch(Exception e)
        {
             // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }
        
    }
    
    @FXML
    public void sendStopCommandEventHandler(MouseEvent event)
    {
        // get client to send
        ClientIO clientToSend = CP_clientList.get("RPI-Control");
        
        // send command
        try
        {
            if(clientToSend.isConnected())
            {
                // send to client
                clientToSend.sendHeatCommand(new MessageHeader("RPI", MessageType.STOP_CMD.Value(), "stop".length(), 3), "stop");

                // update GUI
                StatusStruct.setCommandWeight(0.0);
                StatusStruct.setCommandTemperature(0.0);
            }else
            {
                throw new IOException();
            }

                
            
        }catch(NullPointerException e)
        {
            // client has been remove frome the list
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
        }catch(IOException e)
        {
            // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }catch(NumberFormatException e)
        {
            errorMessage("La température demandé est invalide");
        }catch(Exception e)
        {
             // client is unconnected
            errorMessage("Le client " + "RPI-Control" +" n'est plus connecté");
            setUnconnectedStatus(clientToSend.getName());
            CP_clientList.remove(clientToSend.getName());
        }
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       StatusStruct = new ObservableDataSts();
       initServer();
       initUpdateThread();
       initComboBox();
       initQrCode();
       try
       {
          // initCamera();
       }catch(Exception e)
       {
           e.printStackTrace();
       }
       
       // TEST
       // init camera 
       
    }  
    
    public void initQrCode()
    {
        qrGenerator = new QRCodeGenerator();
    }
    
  
    
    public void initServer()
    {
        // init thread waiting for connection
        try
        {
            CP_clientList = new HashMap<String, ClientIO>();
            SP_clientList = new HashMap<String, ClientIO>();
            serverCommand = new Server("math-main-pi.local", COMMAND_PORT);
            serverStatus = new Server("math-main-pi.local", STATUS_PORT);
            
            
            // wait on connection of the 2 pi (start thread) //
            Thread waitingCP_connectionThread = new Thread(){
                public void run()
                {
                    while(true)
                    {
                        try
                        {
                            // wait on connection //
                            ClientIO client = new ClientIO("", serverCommand.waitConnection());
                            
                            // wait on client info //
                            byte[] readBytes = new byte[MESSAGE_HEADER_LENGTH];
                            
                            if(client.isConnected())
                            {
                                client.read(readBytes, 0, MESSAGE_HEADER_LENGTH);
                            }
                            
                            // parse recieved message
                            MessageHeader mesg = client.byteToHeader(readBytes);
                            
                            // read payload of message
                            if(client.isConnected() && (mesg.getPayloadType() == MessageType.ID_MSG.Value()))
                            {
                                client.read(readBytes, 0, mesg.getPayloadSize());
                            }else
                            {
                                System.out.println("Erreur, ce n'est pas un message d'identification");
                            }
                            
                            // add client to list
                            client.setName(new String(Arrays.copyOfRange(readBytes, 0, mesg.getPayloadSize())));
                            CP_clientList.put(client.getName(), client);
                            
                           Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    setConnectedStatus(client.getName());
                                }
                            });
                           
                            
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
             Thread waitingCS_connectionThread = new Thread(){
                public void run()
                {
                    while(true)
                    {
                        try
                        {
                            // wait on connection //
                            ClientIO client = new ClientIO("", serverStatus.waitConnection());
                            
                            // wait on client info //
                            byte[] readBytes = new byte[MESSAGE_HEADER_LENGTH];
                            
                            if(client.isConnected())
                            {
                                client.read(readBytes, 0, MESSAGE_HEADER_LENGTH);
                            }
                            
                            // parse recieved message
                            MessageHeader mesg = client.byteToHeader(readBytes);
                            
                            // read payload of message
                            if(client.isConnected() && (mesg.getPayloadType() == MessageType.ID_MSG.Value()))
                            {
                                client.read(readBytes, 0, mesg.getPayloadSize());
                            }else
                            {
                                System.out.println("Erreur, ce n'est pas un emssage d'identification");
                            }
                            
                            // add client to list
                            client.setName(new String(Arrays.copyOfRange(readBytes, 0, mesg.getPayloadSize())));
                            SP_clientList.put(client.getName(), client);
                            
                           
                           // lauch new thread for waiting on status
                           waitForStatus(client.getName());
                           
                            
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            waitingCP_connectionThread.setDaemon(true);
            waitingCS_connectionThread.setDaemon(true);
            waitingCP_connectionThread.start();
            waitingCS_connectionThread.start();
            
        }catch(Exception e ){
            e.printStackTrace();
        }
        
        
    }
    
    public void initComboBox()
    {
        channel_choice.getItems().removeAll(channel_choice.getItems());
        channel_choice.getItems().addAll("Moteur Convoyeur", "Moteur Extrudeur","Moteur Élément Chauffant", "Moteur Distributeur", "Moteur Déchiqueteur");
        plastic_type.getItems().removeAll(channel_choice.getItems());
        plastic_type.getItems().addAll("PET(#1)", "HDPE(#2)", "PP(#5)");
        plastic_type.valueProperty().addListener(new ChangeListener<String>() {
        @Override public void changed(ObservableValue ov, String t, String t1) {
          switch(t1)
          {
              case "PET(#1)":
                  temp_input.setText(String.valueOf(DEFAULT_PET_1_TEMP));
                  break;
                  
              case "HDPE(#2)":
                  temp_input.setText(String.valueOf(DEFAULT_HDPE_2_TEMP));
                  break;
                  
              case "PP(#5)" : 
                  temp_input.setText(String.valueOf(DEFAULT_PP_5_TEMP));
                  break;
                  
              default :
                  break;
                         
          }
        }    
    });
    }
    
    public void initUpdateThread() 
    {
        Thread updateStatusThread = new Thread(){
                public void run()
                {
                    DecimalFormat df = new DecimalFormat("0.00##");
                    while(true)
                    {
                       if(StatusStruct != null)
                       {
                            Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    // update info on screen
                                    current_temp.setText(df.format(StatusStruct.getActualTemperature()));
                                    command_temp.setText(df.format(StatusStruct.getCommandTemperature()));
                                    current_weight.setText(df.format(StatusStruct.getActualWeight()));
                                    command_weight.setText(df.format(StatusStruct.getCommandWeight()));
                                    sts_fsm_label.setText(StatusStruct.getFSMStatus());
                                    
                                    // set bourrage status
                                    if(StatusStruct.isBlocked())
                                    {
                                        sts_bourrage.setFill(Color.rgb(144, 200, 78));
                                        sts_bourrage.setStroke(Color.rgb(144, 200, 78));
                                    }else
                                    {
                                        sts_bourrage.setFill(Color.rgb(214, 19, 28));
                                        sts_bourrage.setStroke(Color.rgb(214, 19, 28));
                                    }
                                }
                            });
                            
                       }
                       
                       try
                       {
                           Thread.sleep(1000);
                       }catch( Exception e){}
                       
                    }
                }
            };
        
        updateStatusThread.setDaemon(true);
        updateStatusThread.start();
    }
    
    private void setConnectedStatus(String clientName)
    {
        switch(clientName)
        {
            case "RPI-Control" :
                sts_control.setFill(Color.rgb(144, 200, 78));
                sts_control.setStroke(Color.rgb(144, 200, 78));
                break;
                
            case "RPI-Printer":
                sts_printer.setFill(Color.rgb(144, 200, 78));
                sts_printer.setStroke(Color.rgb(144, 200, 78));
                break;
                
            case "RPI-Cam1":
                sts_cam1.setFill(Color.rgb(144, 200, 78));
                sts_cam1.setStroke(Color.rgb(144, 200, 78));
                break;
                
            case "RPI-Cam2":
                sts_cam2.setFill(Color.rgb(144, 200, 78));
                sts_cam2.setStroke(Color.rgb(144, 200, 78));
                break;
                
            default :
                break;       
        }
    }
    
        private void setUnconnectedStatus(String clientName)
    {
        switch(clientName)
        {
            case "RPI-Control" :
                sts_control.setFill(Color.rgb(214, 19, 28));
                sts_control.setStroke(Color.rgb(214, 19, 28));
                break;
                
            case "RPI-Printer":
                sts_printer.setFill(Color.rgb(214, 19, 28));
                sts_printer.setStroke(Color.rgb(214, 19, 28));
                break;
                
            case "RPI-Cam1":
                sts_cam1.setFill(Color.rgb(214, 19, 28));
                sts_cam1.setStroke(Color.rgb(214, 19, 28));
                break;
                
            case "RPI-Cam2":
                sts_cam2.setFill(Color.rgb(214, 19, 28));
                sts_cam2.setStroke(Color.rgb(214, 19, 28));
                break;
                
            default :
                break;       
        }
    }
        
        private void errorMessage(String message)
        {
             JOptionPane.showMessageDialog(null, message, "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        private void waitForStatus(String name)
        {
            Thread waitingForStatusThread = new Thread(){
                public void run()
                {
                    String clientName = name;
                    ClientIO clientToReceived = SP_clientList.get(clientName);
                    
                    while(threadRunning)
                    {
                        // try to read client status
                        try
                        {
                            if(clientToReceived.isConnected())
                            {
                                // blocking on client read
                                MessageStatus stsMesg = clientToReceived.readStatus();
                                
                                // set value according to message received
                                setStatusValue(stsMesg);
                                
                                //Thread.sleep(800);
                            }else
                            {
                                throw new IOException();
                            }
                            
                        }catch(NullPointerException e)
                        {
                            // client has been remove frome the list
                            errorMessage("Le client " + clientName +" n'est plus connecté");
                        }catch(IOException e)
                        {
                            // client is unconnected
                            errorMessage("Le client n'est plus connecté");
                            SP_clientList.remove(clientToReceived.getName());
                            e.printStackTrace();
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            waitingForStatusThread.setDaemon(true);
            waitingForStatusThread.start();
        }
        
        private void setStatusValue(MessageStatus mesg) throws Exception
        {
            int mesgType = mesg.getType();
            String payload = mesg.getPayload();
            
            if(mesgType == MessageType.STS_TEMPERATURE_MSG.Value())
            {
                double tempValue = Double.valueOf(payload);
                StatusStruct.setActualTemperature(tempValue);
                
            }else if(mesgType == MessageType.STS_WEIGHT_MSG.Value())
            {
                double weightValue = Double.valueOf(payload);
                StatusStruct.setActualWeight(weightValue);
            }else if(mesgType == MessageType.STS_CRUSHER_BLOCKED_MSG.Value())
            {
                if(payload.toUpperCase().equals("FALSE"))
                {
                    StatusStruct.setBlocked(false);
                }else
                {
                    StatusStruct.setBlocked(true);
                }
                
            }else if(mesgType == MessageType.STS_FSM_MSG.Value())
            {
                StatusStruct.setFSMStatus(payload);  
            }else
            {
                // do nothing
            }
            
        }
        

}

