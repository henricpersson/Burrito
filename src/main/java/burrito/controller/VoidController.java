package burrito.controller;

import taco.Controller;

public class VoidController implements Controller<Void> {

	@Override
	public Void execute() {
		return null;
	}

}
