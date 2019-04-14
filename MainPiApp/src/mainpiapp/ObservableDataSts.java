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
public class ObservableDataSts {

    private double ActualTemperature;
    private double CommandTemperature;
    private double ActualWeight;
    private double CommandWeight;
    private String FSMStatus;
    private boolean Blocked;
    
    
    public ObservableDataSts()
    {
        this.ActualTemperature = 0.0;
        this.CommandTemperature = 0.0;
        this.ActualWeight = 0.0;
        this.CommandWeight = 0.0;
        this.Blocked  = false;
        this.FSMStatus = "En attente";       
    }
    
    public double getActualTemperature() {
        return ActualTemperature;
    }

    public void setActualTemperature(double ActualTemperature) {
        this.ActualTemperature = ActualTemperature;
    }

    public double getCommandTemperature() {
        return CommandTemperature;
    }

    public void setCommandTemperature(double CommandTemperature) {
        this.CommandTemperature = CommandTemperature;
    }

    public double getActualWeight() {
        return ActualWeight;
    }

    public void setActualWeight(double ActualWeight) {
        this.ActualWeight = ActualWeight;
    }

    public double getCommandWeight() {
        return CommandWeight;
    }

    public void setCommandWeight(double CommandWeight) {
        this.CommandWeight = CommandWeight;
    }

    public String getFSMStatus() {
        return FSMStatus;
    }

    public void setFSMStatus(String FSMStatus) {
        this.FSMStatus = FSMStatus;
    }

    public boolean isBlocked() {
        return Blocked;
    }

    public void setBlocked(boolean Blocked) {
        this.Blocked = Blocked;
    }
    

    
            
    
}
