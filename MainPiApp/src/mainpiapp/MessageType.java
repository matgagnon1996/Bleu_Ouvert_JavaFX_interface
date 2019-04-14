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
public enum MessageType {
    ACK_MSG                     (0),
    NACK_MSG                    (1),
    ID_MSG                      (2),
    ERROR_MSG                   (3),
    STS_TEMPERATURE_MSG         (20),
    STS_CRUSHER_BLOCKED_MSG     (21),
    STS_WEIGHT_MSG              (22),
    STS_FSM_MSG                 (23),
    MANUAL_CMD_SERVO            (30),
    CRUSHER_MODE_CMD            (31),
    EXTRUDER_MODE_CMD           (32),
    HEATER_MANUAL_CMD           (33),
    STOP_CMD                    (34),
    PRINT_CMD                   (50),
    ;
    
    private final int type;
    
    MessageType(int pType)
    {
        this.type = pType;
    }
    
    public int Value()
    {
        return this.type;
    }
    
    

}
