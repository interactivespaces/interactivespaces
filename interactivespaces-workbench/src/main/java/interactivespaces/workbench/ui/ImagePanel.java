/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.workbench.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * A panel with an image in it.
 *
 * @author Keith M. Hughes
 */
public class ImagePanel extends JPanel {
	private BufferedImage img;

	public ImagePanel() {
	}

	public ImagePanel(String imagePath) {
		setImage(loadImage(imagePath));
	}

	public ImagePanel(BufferedImage image) {
		setImage(image);
	}

	public void paintComponent(Graphics g) {
		if (img != null)
			g.drawImage(img, 0, 0, null);
	}
	
	public void setImage(BufferedImage img) {
	    this.img = img;
	    Dimension size;
	    if (img != null)
	    	size = new Dimension(img.getWidth(null), img.getHeight(null));
	    else
	    	size = new Dimension(0,0);
	    
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);
	    setLayout(null);
	    
	    repaint();
	}
	
	/**
	 * Load images to go in the box.
	 */
	public static BufferedImage loadImage(String imagePath) {
		InputStream imageStream = Thread.currentThread()
    		.getContextClassLoader()
    		.getResourceAsStream(imagePath);
		try {
			return ImageIO.read(imageStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		} finally {
			try {
				if (imageStream != null)
					imageStream.close();
			} catch (IOException e) {
				// Don't care.
			}
		}
	}
}
