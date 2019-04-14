/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import static mainpiapp.FXMLDocumentController.MESSAGE_HEADER_LENGTH;
import static mainpiapp.FXMLDocumentController.MAX_PAYLOAD_SIZE;
import mainpiapp.MessageHeader;

/**
 *
 * @author mathieu
 */
public class ClientIO {
    
    private String name;
    private Socket clientSocket;
    
    private InputStream inputBuffer;
    private OutputStream outputBuffer;
    
    // private attribute pre-allocated
    
    
    // headerToBytes pre-allocates attributes
    private ByteBuffer H2B_Bytes;
    private byte[] H2B_returnBytes;
    
    // BytesToHeader pre-allocatedAttributes
    private MessageHeader B2H_returnHeader;
    
    // readStatus pre-allocated attributes
    private MessageStatus status;
    private byte[] headerByte;
    private byte[] payloadByte;
    
    ClientIO(String pName, Socket pSocket) throws Exception
    {
        this.clientSocket = pSocket;
        this.name = pName;
        this.inputBuffer = this.clientSocket.getInputStream();
        this.outputBuffer = this.clientSocket.getOutputStream();
        
        // readStatus pre-allocated attributes
        this.status = new MessageStatus(0, "");
        this.headerByte = new byte[MESSAGE_HEADER_LENGTH];
        this.payloadByte = new byte[MAX_PAYLOAD_SIZE];
        
        // BytesToHeader pre-allocatedAttributes
        this.B2H_returnHeader = new MessageHeader("RPI", 0,0,0);
        
        // headerToBytes pre-allocates attributes
        this.H2B_Bytes = ByteBuffer.allocate(MESSAGE_HEADER_LENGTH);
        this.H2B_Bytes.order(ByteOrder.LITTLE_ENDIAN);
        this.H2B_returnBytes = new byte[MESSAGE_HEADER_LENGTH];
        
        
        
    }
    
    public int read(byte[] buffer, int offset, int nbBytes) throws Exception
    {
        if(!isConnected())
        {
            throw new IOException("Socket is not connected");
        }
        
        return inputBuffer.read(buffer, offset, nbBytes);
    }
    
    public void write(byte[] buffer) throws Exception
    {
        if(!isConnected())
        {
            throw new IOException("Socket is not conencted");
        }
        
        outputBuffer.write(buffer);
    }
    
    public void close() throws Exception
    {
        if(clientSocket != null)
        {
            clientSocket.close();
        }
        
        if(inputBuffer != null)
        {
            inputBuffer.close();
        }
        
        if(outputBuffer != null)
        {
            outputBuffer.close();
        }  
    }
    
    public void flush() throws Exception
    {
        if(!isConnected())
        {
            throw new IOException("Socket is not conencted");
        }
        
        outputBuffer.flush();
        int numberOfByteToSkip = inputBuffer.available();
        inputBuffer.skip(numberOfByteToSkip);
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public Socket getSocket()
    {
        return this.clientSocket;
    }
    
    public boolean isConnected()
    {
        if(clientSocket == null)
        {
            return false;
        }else
        {
            return clientSocket.isConnected();
        }
    }
    
    public MessageHeader byteToHeader(byte[] byteArray)
    {
        // get new header infos
        String sig = new String(Arrays.copyOfRange(byteArray, 0, 4));
        
        ByteBuffer buf = ByteBuffer.wrap(Arrays.copyOfRange(byteArray, 4, 8));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        int PayloadType = buf.getInt(0);
        
        buf = ByteBuffer.wrap(Arrays.copyOfRange(byteArray, 8, 12));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        int PayloadSize = buf.getInt(0);
        
        buf = ByteBuffer.wrap(Arrays.copyOfRange(byteArray, 12, 16));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        int PayloadSource = buf.getInt(0);
        
        // set info to private object
        B2H_returnHeader.setPayloadSize(PayloadSize);
        B2H_returnHeader.setSignature(sig);
        B2H_returnHeader.setPayloadSource(PayloadSource);
        B2H_returnHeader.setPayloadType(PayloadType);
        
        sig = null;
        buf = null;
        
        return B2H_returnHeader;
    }
    
    public byte[] headerToByte(MessageHeader mesg)
    {
        // clear array of bytes
        H2B_Bytes.clear();
      
        // insert bytes
        H2B_Bytes.put(mesg.getSignature().getBytes());
        H2B_Bytes.putInt(4, mesg.getPayloadType());
        H2B_Bytes.putInt(8, mesg.getPayloadSize());
        H2B_Bytes.putInt(12, mesg.getPayloadSource());
        
        // get array of bytes
        H2B_Bytes.rewind();
        H2B_Bytes.get(H2B_returnBytes);
        
        return H2B_returnBytes;
    }
    
    public void sendServoCommand(MessageHeader header, int channel, int commandType, int value) throws Exception
    {
        // send header to client
        byte[] headerToSend = headerToByte(header);
 
        write(headerToSend);

        // construct data to send
        byte[]payload =  new byte[header.getPayloadSize()];
        ByteBuffer bytes = ByteBuffer.allocate(header.getPayloadSize());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        bytes.putInt(channel);
        bytes.putInt(commandType);
        bytes.putInt(value);
        
        bytes.rewind();
        bytes.get(payload);
        
        // send payload
        write(payload);
        
        payload = null;
        
    } 
    
    public void sendHeatCommand(MessageHeader header, String temperature) throws Exception
    {
        // send header to client
        byte[] headerToSend = headerToByte(header);
        write(headerToSend);
        
        // construct data to send
        byte[]payload =  new byte[header.getPayloadSize()];
        ByteBuffer bytes = ByteBuffer.allocate(header.getPayloadSize());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        bytes.put(temperature.getBytes());
        bytes.rewind();
        bytes.get(payload);
        
        // send payload
        write(payload);
        
        payload = null;
    }
    
    public void sendPrintCommand(MessageHeader header, int numberOfPrint, byte[] bitmap) throws Exception
    {
        // send header to client
        byte[] headerToSend = headerToByte(header);
        write(headerToSend);
        
        // construct data to send
        byte[]payload =  new byte[header.getPayloadSize()];
        ByteBuffer bytes = ByteBuffer.allocate(header.getPayloadSize());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        bytes.putInt(numberOfPrint);
        for(int i = 0; i < bitmap.length; i++)
        {
            bytes.put(i+4, bitmap[i]);
        }
        
        System.out.println("Allo");
        bytes.rewind();
        bytes.get(payload);
        
        // send payload
        write(payload);
        
        payload = null;
    }
    
    public MessageStatus readStatus() throws Exception
    {   
        // read header
        
        int nbBytesRead = 0;
        nbBytesRead += read(this.headerByte,0,MESSAGE_HEADER_LENGTH);
        while(nbBytesRead != MESSAGE_HEADER_LENGTH)
        {
            nbBytesRead += read(this.headerByte, nbBytesRead,MESSAGE_HEADER_LENGTH - nbBytesRead);
        }
        
        if(nbBytesRead != MESSAGE_HEADER_LENGTH)
        {
            throw new IOException("Header length incorrect : "+ nbBytesRead);
        }
        
        // convert header bytes to object
        MessageHeader header = byteToHeader(this.headerByte);
       
        // read payload
        Arrays.fill(this.payloadByte, (byte)0);
        nbBytesRead = 0;
        nbBytesRead += read(this.payloadByte, 0, header.getPayloadSize());
        while(nbBytesRead != header.getPayloadSize())
        {
            nbBytesRead += read(this.payloadByte, nbBytesRead, header.getPayloadSize() - nbBytesRead);
        }
        
        if(nbBytesRead != header.getPayloadSize())
        {
            throw new IOException("Payload size incorrect : "+ nbBytesRead + " "+ header.getPayloadSize() + " : "+ new String(this.payloadByte));
        }
        
        // add return values
        String payloadStr = (new String(this.payloadByte)).substring(0, nbBytesRead);
        this.status.setPayload(payloadStr);
        this.status.setType(header.getPayloadType());
        
        // free memory
        payloadStr = null;
        
        return this.status;   
    }
}
