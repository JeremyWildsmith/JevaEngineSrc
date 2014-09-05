var _hasTorso = true;
var _hasLowerBody = true;
var _hasFeet = true;
var _hasScene = true;

function hasTorso()
{
	return _hasTorso;
}

function hasLowerBody()
{
	return _hasLowerBody;
}

function hasFeet()
{
	return _hasFeet;
}

function hasScene()
{
	return _hasScene;
}

function examineTorso()
{
	_hasTorso = false;
}

function examineFeet()
{
	_hasFeet= false;
}

function examineLowerBody()
{
	_hasLowerBody = false;
}

function examineScene()
{
	_hasScene = false;
}