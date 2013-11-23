package com.moomoohk.Grame.test;

import java.awt.Color;

import com.moomoohk.Grame.Basics.Entity;
import com.moomoohk.Grame.Essentials.Base;
import com.moomoohk.Grame.Essentials.GrameManager;
import com.moomoohk.Grame.Essentials.GrameUtils;
import com.moomoohk.Grame.Graphics.RenderManager;
import com.moomoohk.Grame.Interfaces.MainGrameClass;

public class SaveTest implements MainGrameClass
{
	public static void main(String[] args)
	{
		MenuConfiguration menuConfig = new MenuConfiguration();
		menuConfig.menuButtonStartColor = Color.cyan;
		menuConfig.menuButtonEndColor = Color.blue;
		menuConfig.otherButtonStartColor = Color.green;
		menuConfig.otherButtonEndColor = Color.red;
		GrameUtils.loadBasicCommands();
		GrameManager.initialize(new SaveTest(), menuConfig);
	}

	@Override
	public void newGame()
	{
		Base b = new Base(30, 30);
		Entity e = new Entity();
		e.makePlayer(1, true, b.ID);
		b.addGrameObject(e, GrameUtils.randomCoordinates(b));
		RenderManager.render(b.ID);
		RenderManager.setVisible(true);
	}

	@Override
	public String getGameName()
	{
		return "Save Test";
	}
}
