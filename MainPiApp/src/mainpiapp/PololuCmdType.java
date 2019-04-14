package mainpiapp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mathieu
 */
public enum PololuCmdType {
    POLOLU_CMD_POSITION             (0),
    POLOLU_CMD_SPEED                (1),
    POLOLU_CMD_ACCELERATION         (2)
    ;
    
    private final int type;
    
    PololuCmdType(int pType)
    {
        this.type = pType;
    }
    
    public int Value()
    {
        return this.type;
    }
    
    
}
