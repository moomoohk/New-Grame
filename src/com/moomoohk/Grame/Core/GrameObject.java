package com.moomoohk.Grame.Core;

import java.awt.Color;
import java.io.Serializable;

import com.moomoohk.Grame.Core.GrameUtils.MessageLevel;

/**
 * Grame Objects are objects which are placed in {@link Grid}s.
 * 
 * @author Meshulam Silk (moomoohk@ymail.com)
 * @version 1.0
 * @since 2013-04-05
 */
public abstract class GrameObject implements Serializable
{
	private static final long serialVersionUID = 636001538082230753L;
	/**
	 * The ID of this Grame Object.
	 * <p>
	 * This ID is unique and cannot be changed.<br>
	 * It is this object's place in the {@link GrameManager}'s list of Grame Objects.
	 */
	public final int ID;
	protected String name;
	protected int speed;
	protected Color color;
	protected boolean paused;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Name of this object.
	 * @param speed
	 *            Speed of this object.
	 * @param color
	 *            Color of this object.
	 * @param paused
	 *            True to pause, else false.
	 */
	public GrameObject(String name, int speed, Color color, boolean paused)
	{
		this.name = name;
		this.speed = speed;
		this.color = color;
		this.paused = paused;
		ID = GrameManager.add(this);
	}

	/**
	 * Gets the name of this object.
	 * 
	 * @return The name of this object.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Sets the name of this object.
	 * 
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the speed of this object.
	 * 
	 * @return The speed of this object.
	 */
	public int getSpeed()
	{
		return this.speed;
	}

	/**
	 * Sets the speed of this object.
	 * 
	 * @param speed
	 *            The speed to set.
	 */
	public void setSpeed(int speed)
	{
		this.speed = speed;
	}

	/**
	 * Gets the color of this object.
	 * 
	 * @return The color of this object.
	 */
	public Color getColor()
	{
		return this.color;
	}

	/**
	 * Sets the color of this object.
	 * 
	 * @param c
	 *            The color to set.
	 */
	public void setColor(Color c)
	{
		this.color = c;
	}

	/**
	 * Pause or unpause this object.
	 * 
	 * @param pause
	 *            True to pause, else false.
	 */
	public void pause(boolean pause)
	{
		this.paused = pause;
	}

	/**
	 * Checks whether or not this object is paused or not.
	 * 
	 * @return True if pause, else false.
	 */
	public boolean isPaused()
	{
		return paused;
	}

	/**
	 * Gets the position of this object in a certain {@link Grid}.<br>
	 * If this object doesn't exist in the {@link Grid} null will be returned.
	 * 
	 * @param gID
	 *            {@link Grid#ID} of {@link Grid}.
	 * @return {@link Coordinates} of this object in the {@link Grid}. If this object isn't in the {@link Grid}, null will be returned.
	 */
	public Coordinates getPos(int gID)
	{
		if (GrameManager.findGrid(gID).containsGrameObject(ID))
			return GrameManager.findGrid(gID).getGrameObjectPos(ID);
		GrameUtils.print("Grid with ID:" + gID + " does not contain Entity with ID:" + ID + ". Returning null.", MessageLevel.ERROR);
		return null;
	}

	/**
	 * Sets the position of this object in a certain {@link Grid}.
	 * 
	 * @param gID
	 *            {@link Grid#ID} of {@link Grid}.
	 * @param pos
	 *            New position to set.
	 */
	public void setPos(int gID, Coordinates pos)
	{
		if (GrameManager.findGrid(gID).containsGrameObject(ID))
		{
			GrameManager.findGrid(gID).moveGrameObject(ID, pos);
			return;
		}
	}

	/**
	 * Checks whether this object is collidable.
	 * <p>
	 * If this object is collidable, other objects on the same {@link GrameObjectLayer} will not be able to occupy the same spot.<br>
	 * If this object is not collidable, it will be consumed by any collidable object that tries to occupy its spot.
	 * 
	 * @return True if collidable, else false.
	 * @see GrameObject#consume(GrameObject)
	 */
	public abstract boolean isCollidable();

	/**
	 * Ticks this object.
	 * <p>
	 * Users will never need to call this method (The {@link GrameManager} takes care of ticking everything).
	 * 
	 * @param gID
	 *            The {@link Grid#ID} of the {@link Grid} in which to tick.
	 */
	public abstract void tick(int gID);

	/**
	 * This method gets called if this object is collidable and it tries to occupy the position of a non collidable object.
	 * 
	 * @param go
	 *            A non collidable Grame Object.
	 */
	public abstract void consume(GrameObject go);
}
