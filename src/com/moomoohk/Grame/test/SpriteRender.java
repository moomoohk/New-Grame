package com.moomoohk.Grame.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.moomoohk.Grame.Core.Grid;
import com.moomoohk.Grame.Core.Coordinates;
import com.moomoohk.Grame.Core.GrameUtils;
import com.moomoohk.Grame.Core.Render;
import com.moomoohk.Grame.Core.GrameUtils.MessageLevel;

public class SpriteRender implements Render
{
	public static HashMap<String, BufferedImage> sprites = new HashMap<String, BufferedImage>();
	public static HashMap<Integer, String> objects = new HashMap<Integer, String>();

	static
	{
		String[] textures = new File(SpriteRender.class.getResource("/Textures/").toString().substring(5)).list();
		System.out.println(textures[0].substring(0, textures[0].indexOf(".")));
		for (String name : textures)
			sprites.put(name.substring(0, name.lastIndexOf(".")), loadImage("/Textures/" + name));
	}

	@Override
	public int[] getPixels(int[] pixels, Grid g, int width, int height)
	{
		for (int xSquare = 0; xSquare < g.getColumns(); xSquare++)
			for (int ySquare = 0; ySquare < g.getRows(); ySquare++)
			{
				try
				{
					drawSprite(xSquare, ySquare, width, height, pixels, g, sprites.get("grass"));
					for (int layer = 0; layer < g.getLayerCount(); layer++)
						if (g.getGrameObject(new Coordinates(xSquare, ySquare), layer) != null && objects.containsKey(g.getGrameObject(new Coordinates(xSquare, ySquare), layer).ID))
							drawSprite(xSquare, ySquare, width, height, pixels, g, sprites.get(objects.get(g.getGrameObject(new Coordinates(xSquare, ySquare), layer).ID)));
						else
							if (g.getGrameObject(new Coordinates(xSquare, ySquare), layer) != null)
								drawSquare(xSquare, ySquare, width, height, pixels, g, g.getGrameObject(new Coordinates(xSquare, ySquare), layer).getColor());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		return pixels;
	}

	private void drawSprite(int xSquare, int ySquare, int width, int height, int[] pixels, Grid g, BufferedImage sprite)
	{
		for (int y = 0; y < sprite.getHeight(); y++)
		{
			int yPix = y + (ySquare * sprite.getHeight());
			if ((yPix < 0) || (yPix >= height))
				continue;
			for (int x = 0; x < sprite.getHeight(); x++)
			{
				int xPix = x + (xSquare * sprite.getWidth());
				if ((xPix < 0) || (xPix >= width))
					continue;
				int alpha = sprite.getRGB(x, y);
				if ((alpha >> 24) != 0x00)
					pixels[xPix + yPix * width] = alpha;
			}
		}
	}

	private void drawSquare(int xSquare, int ySquare, int width, int height, int[] pixels, Grid g, Color c)
	{
		for (int y = 0; y < 30; y++)
		{
			int yPix = y + (ySquare * 30);
			if ((yPix < 0) || (yPix >= height))
				continue;
			for (int x = 0; x < 30; x++)
			{
				int xPix = x + (xSquare * 30);
				if ((xPix < 0) || (xPix >= width))
					continue;
				int alpha = c.getRGB();
				if ((alpha >> 24) != 0x00)
					pixels[xPix + yPix * width] = alpha;
			}
		}
	}

	public static BufferedImage loadImage(String path)
	{
		GrameUtils.print("Trying to load " + path, MessageLevel.DEBUG);
		try
		{
			return ImageIO.read(SpriteRender.class.getResource(path));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName()
	{
		return "Sprite render";
	}

}
