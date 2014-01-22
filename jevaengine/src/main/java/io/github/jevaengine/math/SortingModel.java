package io.github.jevaengine.math;

public enum SortingModel
{
	//In order of conflict resolution.
	//i.e, if distance conflicts with YOnly, YOnly > Distance thus 
	//the model is resolved to YOnly
	Distance,
	XOnly,
	YOnly,
}
