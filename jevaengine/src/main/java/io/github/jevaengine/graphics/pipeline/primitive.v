varying vec2 textureCoordinate;

void main()
{
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
    textureCoordinate = vec2(gl_MultiTexCoord0);
}