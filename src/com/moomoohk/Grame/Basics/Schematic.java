package com.moomoohk.Grame.Basics;

import java.awt.Color;

import com.moomoohk.Grame.Essentials.Base;
import com.moomoohk.Grame.Essentials.Coordinates;
import com.moomoohk.Grame.Essentials.GrameUtils;
import com.moomoohk.Grame.Essentials.GrameUtils.MessageLevel;

public class Schematic
{
	private Color[][] map;
	private int height;
	private int width;
	private int type;

	public Schematic()
	{
		this((int) (Math.random() * 13.0D));
	}

	public Schematic(int type)
	{
		this.type = type;
		this.map = new Color[5][5];
		this.height = this.map.length;
		this.width = this.map[0].length;
		for (int i = 0; i < this.map.length; i++)
			for (int j = 0; j < this.map.length; j++)
				this.map[i][j] = null;
		switch (this.type)
		{
		case 0:
			def(2, 0);
			def(2, 1);
			def(1, 2);
			def(2, 2);
			def(3, 2);
			def(2, 3);
			def(2, 4);
			break;
		case 1:
			def(0, 2);
			def(1, 2);
			def(2, 1);
			def(2, 2);
			def(2, 3);
			def(3, 2);
			def(4, 2);
			break;
		case 2:
			def(0, 0);
			def(1, 0);
			def(2, 0);
			def(0, 1);
			def(0, 2);
			break;
		case 3:
			def(0, 0);
			def(0, 1);
			def(0, 2);
			def(0, 3);
			def(1, 0);
			def(2, 0);
			def(3, 0);
			break;
		case 4:
			def(0, 0);
			def(1, 0);
			def(2, 0);
			def(4, 0);
			def(0, 2);
			def(2, 2);
			def(3, 2);
			def(4, 2);
			def(0, 4);
			def(1, 4);
			def(2, 4);
			def(4, 4);
			break;
		case 5:
			def(0, 0);
			def(0, 1);
			def(0, 2);
			def(0, 4);
			def(2, 0);
			def(2, 2);
			def(2, 3);
			def(2, 4);
			def(4, 0);
			def(4, 1);
			def(4, 2);
			def(4, 4);
			break;
		case 6:
			def(0, 0);
			def(1, 0);
			def(2, 0);
			def(3, 0);
			def(4, 0);
			def(2, 1);
			def(2, 2);
			def(2, 3);
			def(0, 4);
			def(1, 4);
			def(2, 4);
			def(3, 4);
			def(4, 4);
			break;
		case 7:
			def(0, 0);
			def(0, 1);
			def(0, 2);
			def(0, 3);
			def(0, 4);
			def(1, 2);
			def(2, 2);
			def(3, 2);
			def(4, 0);
			def(4, 1);
			def(4, 2);
			def(4, 3);
			def(4, 4);
			break;
		case 8:
			def(0, 0);
			def(1, 0);
			def(3, 0);
			def(4, 0);
			def(1, 1);
			def(2, 1);
			def(3, 1);
			def(1, 3);
			def(2, 3);
			def(3, 3);
			def(0, 4);
			def(1, 4);
			def(3, 4);
			def(4, 4);
			break;
		case 9:
			def(0, 0);
			def(0, 1);
			def(0, 3);
			def(0, 4);
			def(1, 1);
			def(1, 2);
			def(1, 3);
			def(3, 1);
			def(3, 2);
			def(3, 3);
			def(4, 0);
			def(4, 1);
			def(4, 3);
			def(4, 4);
			break;
		case 10:
			def(1, 0);
			def(4, 0);
			def(0, 1);
			def(1, 1);
			def(3, 1);
			def(1, 3);
			def(3, 3);
			def(4, 3);
			def(0, 4);
			def(3, 4);
			break;
		case 11:
			def(0, 0);
			def(3, 0);
			def(1, 1);
			def(3, 1);
			def(4, 1);
			def(0, 3);
			def(1, 3);
			def(3, 3);
			def(1, 4);
			def(4, 4);
			break;
		case 12:
			def(0, 0);
			def(1, 0);
			def(3, 0);
			def(4, 0);
			def(0, 1);
			def(4, 1);
			def(2, 2);
			def(0, 3);
			def(4, 3);
			def(0, 4);
			def(1, 4);
			def(3, 4);
			def(4, 4);
			break;
		case 13:
			def(0, 0);
			def(1, 0);
			trig(2, 0);
			def(3, 0);
			def(4, 0);
			def(0, 1);
			def(4, 1);
			trig(0, 2);
			def(0, 3);
			def(4, 3);
			def(0, 4);
			def(1, 4);
			trig(2, 4);
			def(3, 4);
			def(4, 4);
			trig(4, 2);
			break;
		case 14:
			def(1, 1);
			def(2, 1);
			def(3, 1);
			def(1, 2);
			def(3, 2);
			def(1, 3);
			def(2, 3);
			def(3, 3);
			break;
		case 15:
			for (int i = 0; i < this.map.length; i++)
				for (int j = 0; j < this.map.length; j++)
					def(i, j);
		}
	}

	public Schematic(int width, int height)
	{
		this.type = 0;
		this.map = new Color[height][width];
		this.height = this.map.length;
		this.width = this.map[0].length;
		for (int i = 0; i < this.map.length; i++)
			for (int j = 0; j < this.map.length; j++)
				this.map[i][j] = Color.WHITE;
	}

	void def(int row, int col)
	{
		this.map[row][col] = Color.BLACK;
	}

	void trig(int row, int col)
	{
		this.map[row][col] = Color.cyan;
	}

	public Color getColor(Coordinates c)
	{
		return this.map[c.getY()][c.getX()];
	}

	int getType()
	{
		return this.type;
	}

	public void show()
	{
		Base b = new Base(5, 5, "Schematic " + type);
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++)
				b.setFloorColor(new Coordinates(j, i), this.map[i][j]);
	}

	public int width()
	{
		return this.width;
	}

	public int height()
	{
		return this.height;
	}

	public boolean isSolid(Coordinates c)
	{
		return (isInSchem(c)) && (this.map[c.getY()][c.getX()] == Color.black);
	}

	private boolean isInSchem(Coordinates c)
	{
		return (c.getY() >= 0) && (c.getX() >= 0) && (c.getX() < this.width) && (c.getY() < this.height);
	}

	public static Schematic rotator(Dir d, int amount, Schematic s)
	{
		amount %= 4;
		if (s.width != s.height)
		{
			GrameUtils.print("Sorry, I can't rotate non-square schematics just yet. Returning your schematic.", MessageLevel.ERROR);
			return s;
		}
		if (amount == 0)
			return s;
		GrameUtils.print("Rotating " + d.toString().toLowerCase() + amount + " times.", MessageLevel.DEBUG);
		if (!d.equals(Dir.LEFT))
			if (d.equals(Boolean.valueOf(!d.equals(Dir.RIGHT))))
			{
				GrameUtils.print("Invalid direction! Use only Dir.LEFT or Dir.RIGHT. Returning your schematic.", MessageLevel.ERROR);
				return s;
			}
		if (amount < 0)
		{
			GrameUtils.print("Invalid amount! Use only positive numbers. Returning your schematic.", MessageLevel.ERROR);
			return s;
		}
		Schematic sn = new Schematic(s.width(), s.height());
		for (int i = 0; i < sn.width; i++)
			for (int j = 0; j < sn.height; j++)
			{
				Coordinates c = new Coordinates(0, 0);
				if (d.equals(Dir.LEFT))
					c = new Coordinates(j, sn.width - i - 1);
				else
					c = new Coordinates(i, sn.width - j - 1);
				sn.setColor(c, s.getColor(new Coordinates(i, j)));
			}
		return rotator(d, amount - 1, sn);
	}

	public void setColor(Coordinates pos, Color c)
	{
		this.map[pos.getY()][pos.getX()] = c;
	}

	public void load(Base b, Coordinates loc)
	{
		//		if (b.getEdges())
		//		{
		//			if (loc.getX() < 3)
		//				loc = new Coordinates(3, loc.getY());
		//			if (loc.getX() > getColumns() - 4)
		//				loc = new Coordinates(getColumns() - 4, loc.getY());
		//			if (loc.getY() < 3)
		//				loc = new Coordinates(loc.getX(), 3);
		//			if (loc.getY() > getRows() - 4)
		//				loc = new Coordinates(loc.getX(), getRows() - 4);
		//		}
		int sx = 0;
		int sy = 0;
		int mapx = loc.getX() - 2;
		int mapy = loc.getY() - 2;
		for (int i = loc.getY() - 2; i < loc.getY() + 3; i++)
		{
			mapy = i;
			for (int j = loc.getX() - 2; j < loc.getX() + 3; j++)
			{
				mapx = j;
				if(this.map[sy][sx]==null)
					continue;
				if (b.isInMap(new Coordinates(mapx, mapy)) && !b.isOccupied(new Coordinates(mapx, mapy)))
					b.addGrameObject(new Wall(getColor(new Coordinates(sx, sy))), new Coordinates(mapx, mapy));
				sx++;
			}
			sx = 0;
			sy++;
		}
	}

}