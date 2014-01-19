#define FTOL 0.01

//Normal
#define MODE_NORMAL 0

//Render fragments of srcTexture where respective coordinate in auxTexture has colour auxColour
#define MODE_COLOUR 1

//Replace all auxColour with srcColor
#define MODE_COLOUR_REPLACE 2

#define MODE_COLOUR_MAP 3

varying vec2 textureCoordinate;

uniform sampler2D srcTexture;
uniform sampler2D auxTexture;

uniform vec4 srcColor;
uniform vec4 auxColor;

uniform int mode;

vec4 colorReplace(vec4 color)
{
	if(abs(color.x - auxColor.x) < FTOL &&
	   abs(color.y - auxColor.y) < FTOL &&
	   abs(color.z - auxColor.z) < FTOL &&
	   abs(color.w - auxColor.w) < FTOL)
	   	return srcColor;
	   else
	    return color;
}

vec4 filterMap(vec4 color)
{
	vec4 map = texture2D(auxTexture, textureCoordinate);
	
	if(abs(auxColor.x - map.x) < FTOL &&
	   abs(auxColor.y - map.y) < FTOL &&
	   abs(auxColor.z - map.z) < FTOL &&
	   abs(auxColor.w - map.w) < FTOL)
	   	return color;
	   else
	    return vec4(0.0, 0.0, 0.0, 0.0);
}

void main (void)  
{
	vec4 color;
	
	if(mode == MODE_COLOUR)
		color = srcColor;
	else
		color = texture2D(srcTexture, textureCoordinate);
	
	switch(mode)
	{
		case MODE_COLOUR_REPLACE:
				color = colorReplace(color);
			break;
		case MODE_COLOUR_MAP:
				color = filterMap(color);
			break;
		case MODE_NORMAL:
		case MODE_COLOUR:
		default:
	}
	
	gl_FragColor = color;
}