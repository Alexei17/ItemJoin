/*
 * ItemJoin
 * Copyright (C) CraftationGaming <https://www.craftationgaming.com/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.RockinChaos.itemjoin.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ServerHandler;

public class ImageMap extends MapRenderer {
	
    private int id;
    private String image;
	private BufferedImage imgCache;
	private List<BufferedImage> imgCacheList = new ArrayList<BufferedImage>();
	private boolean isGIF = false;
	private List<Integer> Rendered = new ArrayList<Integer> ();
	
	public ImageMap(String image, int imageID) {
		this.id = imageID;
		this.image = image;
		if (image != null && !image.equalsIgnoreCase("default.jpg")) {
			try { 
				if (Utils.containsIgnoreCase(this.image, ".gif")) {
					this.isGIF = true;
				    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
				    ImageInputStream ciis = ImageIO.createImageInputStream(new File(ItemJoin.getInstance().getDataFolder(), String.valueOf(image)));
				    reader.setInput(ciis, false);
				    for (int i = 0; i < reader.getNumImages(true); i++) { try { this.imgCacheList.add(reader.read(i)); } catch (Exception e) {} }
				    ciis.close();
				} else { this.imgCache = ImageIO.read(new File(ItemJoin.getInstance().getDataFolder(), String.valueOf(image))); }
			} catch (IOException e) { ServerHandler.sendDebugTrace(e); }
		} else if (image != null && image.equalsIgnoreCase("default.jpg") && ItemJoin.getInstance().getResource("files/generated/default.jpg") != null) {
			try { this.imgCache = ImageIO.read(ItemJoin.getInstance().getResource("files/generated/default.jpg"));
			} catch (IOException e) { ServerHandler.sendDebugTrace(e); }
		}
	}
    
    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
    	if (Rendered.isEmpty() || !Rendered.contains(this.id)) {
    		try {
    			Rendered.add(this.id);
				mapView.setScale(MapView.Scale.NORMAL);
				if (this.isGIF) { this.drawGIF(mapCanvas);} 
				else { mapCanvas.drawImage(0, 0, this.imgCache); }
				ServerHandler.logDebug("{ImageMap} Rendering custom-map-image; " + this.image + " with the id " + this.id);
			} catch (Exception e) {
				ServerHandler.logSevere("{ImageMap} There was a problem rending your map(s)!");
				ServerHandler.logWarn("{ImageMap} Please check and make sure your image size is no larger than 128x128 pixels.");
				ServerHandler.sendDebugTrace(e);
			}
    	}
    }
    
    private void drawGIF(final MapCanvas mapCanvas) {
    	final int maxFrames = this.imgCacheList.size();
    	int delay = 0; int frameSize = 1;
    	for (final BufferedImage frame: imgCacheList) {
    		final int frameNumber = frameSize;
    		new java.util.Timer().schedule(new java.util.TimerTask() {
    			@Override
				public void run() {
    				mapCanvas.drawImage(0, 0, frame);
    				if (frameNumber == maxFrames) { drawGIF(mapCanvas); }
    				this.cancel();
    			}
    		}, delay);
    		delay = delay + 135;
    		frameSize++;
    	}
    }

	public MapView FetchExistingView(int id) {
		MapView view = Legacy.getMapView(id);
		if (view == null) { view = Legacy.createLegacyMapView(); }
		return view;
	}
}