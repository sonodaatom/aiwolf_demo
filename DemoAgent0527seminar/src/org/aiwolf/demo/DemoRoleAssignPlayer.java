package org.aiwolf.demo;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class DemoRoleAssignPlayer extends AbstractRoleAssignPlayer {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	public DemoRoleAssignPlayer(){
		setSeerPlayer(new DemoSeer());
	}
}
