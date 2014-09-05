var g_attackTarget = null;

var g_attackRange = 0;
var g_attackSound = null;
var g_attackDamage = 0;
var g_attackPeriod = 0;

var g_dieSound = null;

var g_controlQueue = new Queue();

function constructTasks() {
	// Double check to assure we are constructing over a clean slate.
	me.cancelTasks();

	//If we're dead, there are no tasks that we can do...
	if (me.getHealth() <= 0)
		return;

	var controller = g_controlQueue.poll();

	if (controller != null) {
		controller.doTasks();
	} else if (g_attackTarget === null || g_attackTarget.getHealth() <= 0) {
		g_attackTarget = null;
		me.idle(500);
	} else {
		var deltaFromTarget = me.getLocation().difference(g_attackTarget.getLocation());
		var distance = deltaFromTarget.getLength();
		
		if (distance <= g_attackRange)
			me.attack(g_attackTarget, g_attackPeriod);
		else
		{
			var targetLocation = g_attackTarget.getLocation();
			me.moveTo(targetLocation.x, targetLocation.y, g_attackRange * 0.95);
			me.invokeTimeout(2000, constructTasks); //reconstruct tasks as it is likely that our target will move.
		}
	}

	me.invoke(constructTasks);
}

me.doAttack.assign(function(attackee) {
	var distance = me.getLocation().difference(attackee.getLocation()).getLength();
	
	if (distance >= g_attackRange)
		return false;

	attackee.invokeInterface("damage", g_attackDamage);

	if (g_attackSound !== null)
		me.playAudio(g_attackSound);

	return true;
});

me.getDefaultCommand.assign(function(subject) {
	if(subject.equals(g_attackTarget))
		return "Attack!";
	
	return null;
});

me.onCommand.add(function(subject, command) {
	if(command === "Attack!")
	{
		subject.invokeInterface("queryControl", function(controller) {
			controller.attack(me.getExternal());
		});
	}
});

me.onEnter.add(function() {

	//Upon entering a world, initialize with configuration parameters.
	var config = me.getConfiguration();

	if (config.childExists("attackDamage"))
		g_attackDamage = config.getChild("attackDamage").getValueInt();

	if (config.childExists("attackPeriod"))
		g_attackPeriod = config.getChild("attackPeriod").getValueInt();

	if (config.childExists("attackRange"))
		g_attackRange = config.getChild("attackRange").getValueDouble();

	if (config.childExists("attackSound"))
		g_attackSound = config.getChild("attackSound").getValueString();

	if (config.childExists("dieSound"))
		g_dieSound = config.getChild("dieSound").getValueString();

	constructTasks();
});

me.onLookFound.add(function(target) {
	if (me.isConflictingAllegiance(target) && g_attackTarget === null) {
		g_attackTarget = target;
		me.cancelTasks();
		constructTasks();
	}
});

me.onHealthChanged.add(function(delta) {
	if (me.getHealth() === 0) {
		if (g_dieSound !== null)
			me.playAudio(g_dieSound);
	}
});

me.mapInterface("target", function(target) {
	g_attackTarget = target;
});

me.mapInterface("damage", function(delta) {
	me.setHealth(me.getHealth() - delta);
});

me.mapInterface("queryControl", function(handler) {
	g_controlQueue.add(new CharacterController(handler));
});

function CharacterController(handler) {
	this.handler = handler;
}

CharacterController.prototype.doTasks = function() {
	this.handler(this);
	me.invoke(constructTasks);
};

CharacterController.prototype.moveTo = function(x, y) {
	me.moveTo(x, y);
};

CharacterController.prototype.speakTo = function(subject, dialogue) {
	me.speakTo(subject, dialogue);
};

CharacterController.prototype.setFlag = function(flag, value) {
	me.setFlag(flag, value);
};

CharacterController.prototype.idle = function(length) {
	me.idle(length);
}; 

CharacterController.prototype.attack = function(target) {
	me.attack(target, g_attackPeriod);
};