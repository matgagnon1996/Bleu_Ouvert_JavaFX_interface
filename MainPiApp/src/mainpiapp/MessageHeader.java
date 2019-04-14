/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

/**
 *
 * @author mathieu
 */
class MessageHeader {

    private String Signature;
    private int PayloadType;
    private int PayloadSize;
    private int PayloadSource;
    
    MessageHeader(String pSignature, int pPayloadType, int pPayloadSize, int pPayloadSource)
    {
        this.Signature = pSignature;
        this.PayloadType = pPayloadType;
        this.PayloadSize = pPayloadSize;
        this.PayloadSource = pPayloadSource;
    }
    
    public String getSignature()
    {
        return this.Signature;
    }
    
    public int getPayloadType()
    {
        return this.PayloadType;
    }
    
    public int getPayloadSize()
    {
        return this.PayloadSize;
    }
            
    public int getPayloadSource()
    {
        return this.PayloadSource;
    }
    
    public void setSignature(String Signature) {
        this.Signature = Signature;
    }

    public void setPayloadType(int PayloadType) {
        this.PayloadType = PayloadType;
    }

    public void setPayloadSize(int PayloadSize) {
        this.PayloadSize = PayloadSize;
    }

    public void setPayloadSource(int PayloadSource) {
        this.PayloadSource = PayloadSource;
    }
        
    
    
    
}
