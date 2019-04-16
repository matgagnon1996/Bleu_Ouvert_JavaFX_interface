/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectatorpiapp;

/**
 *
 * @author mathieu
 */
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;


public class ImageContainer {
	private ObjectProperty<Image> image = new SimpleObjectProperty<Image>();

	public Image getImage() {
		return image.get();
	}

	public void setImage(Image image) {
		this.image.set(image);
	}
	
	public void addListener(ChangeListener<Image> listener){
		image.addListener(listener);
	}
}
