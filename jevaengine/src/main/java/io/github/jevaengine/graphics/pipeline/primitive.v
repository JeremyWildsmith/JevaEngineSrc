#version 150
#extension GL_ARB_explicit_attrib_location : enable

out vec2 textureCoordinate;

layout(location = 0) in ivec2 vertexPosition;
layout(location = 1) in vec2 texCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * ivec4(vertexPosition.x, vertexPosition.y, 0.0, 1.0);
	textureCoordinate = texCoord;
}