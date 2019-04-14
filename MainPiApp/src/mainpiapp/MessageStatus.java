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
public class MessageStatus {

    private int type;
    private String payload;
    
    MessageStatus(int type, String payload)
    {
        this.type = type;
        this.payload = payload;
    }
    
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
