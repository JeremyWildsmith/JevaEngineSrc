#version 150

#define FTOL 0.009

//Normal
#define MODE_TEXTURE 0

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

out vec4 fragmentOut;

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
			abs(auxColor.z - map.z) < FTOL)
				return color;
			else
				return vec4(0.0, 0.0, 0.0, 0.0);
}

void main (void)  
{
	switch(mode)
	{
		case MODE_TEXTURE:
			fragmentOut = texture2D(srcTexture, textureCoordinate);
			break;
		case MODE_COLOUR:
			fragmentOut = srcColor;
			break;
		case MODE_COLOUR_MAP:
			fragmentOut = filterMap(texture2D(srcTexture, textureCoordinate));
			break;
		case MODE_COLOUR_REPLACE:
			fragmentOut = colorReplace(texture2D(srcTexture, textureCoordinate));
			break;
		default:
			fragmentOut = vec4(1.0, 1.0, 0.0, 1.0);
	}
}