package com.moomoohk.Grame.Core.Commands;

import java.awt.Color;

import com.moomoohk.Grame.Core.GrameManager;
import com.moomoohk.Grame.Core.Graphics.RenderManager;
import com.moomoohk.MooCommands.Command;

public class RenderGridCommand extends Command
{

	public RenderGridCommand()
	{
		super();
	}

	public boolean check(String[] params)
	{
		if (GrameManager.getRenders().size() == 0)
		{
			this.outputMessage = "No renders loaded!";
			this.outputColor = Color.red;
			return false;
		}
		if (GrameManager.getRenders().get(params[1]) == null)
		{
			this.outputMessage = "Valid renders: ";
			for (String name : GrameManager.getRenders().keySet())
				this.outputMessage += name + " ";
			return false;
		}
		if (GrameManager.findGrameObject(Integer.parseInt(params[0])) == null)
		{
			this.outputMessage = "Grid with ID:" + params[1] + " does not exist!";
			this.outputColor = Color.red;
			return false;
		}
		return true;
	}

	@Override
	public void execute(String[] params)
	{
		RenderManager.render(Integer.parseInt(params[0]), GrameManager.getRenders().get(params[1]));
	}

	@Override
	public String getCommand()
	{
		return "render";
	}

	@Override
	public String getHelpMessage()
	{
		return "Renders a grid using a render in the Render list";
	}

	@Override
	public String getUsage()
	{
		return "render <grid ID> <render name>";
	}

	@Override
	public int getMaxParams()
	{
		return 2;
	}

	@Override
	public int getMinParams()
	{
		return 2;
	}
}
