##For converting PARPG character asset animations into a spritesheet and generating jsf meta-data files.

from wand.image import Image

workingDirectory = "C:/Users/Jeremy/Desktop/parpg - Copy/trunk/objects/scenery/chairs/black_chair/"
frameWidth = 60
frameHeight = 80

directions = {"n" : "045", 
              "ne": "000", 
              "nw": "090", 
              "w": "135", 
              "sw": "180", 
              "s": "225", 
              "se": "270", 
              "e": "315"}

def renderAnimation(animationName):
    print("Rendering animation: " + animationName)
    dirToFramesMapping = dict()
    
    animationDirectory =  workingDirectory + animationName + "/"
 
    for key, value in directions.items():

        frameBuffer = Image(filename=(animationDirectory + animationName + "_" + value + ".png"));
        
        if(frameBuffer.width > frameWidth):
            frameBuffer.crop(int((frameBuffer.width - frameWidth)/2), 0, width=frameWidth, height=frameBuffer.height)
        
        if(frameBuffer.height > frameHeight):
            frameBuffer.crop(0, int((frameBuffer.height - frameHeight)/2), frameBuffer.width, height=frameHeight)
        
        newBuffer = Image(width=frameWidth, height=frameHeight)
        newBuffer.composite(frameBuffer, int((newBuffer.width - frameBuffer.width) / 2), int((newBuffer.height - frameBuffer.height) / 2))
        frameBuffer = newBuffer
            
        dirToFramesMapping[key] = frameBuffer;
    
    directionAnimations = dict()
    
    for key, value in dirToFramesMapping.items():
        directionAnimationBuffer = Image(width=frameWidth, height=frameHeight)
        directionAnimationBuffer.composite(value, 0, 0)
         
        directionAnimations[key] = directionAnimationBuffer
    
    animation = Image(width=frameWidth, height=frameHeight * len(directions))
    animation.composite(directionAnimations["n"], 0, 0)
    animation.composite(directionAnimations["ne"], 0, frameHeight)
    animation.composite(directionAnimations["e"], 0, frameHeight * 2)
    animation.composite(directionAnimations["se"], 0, frameHeight * 3)
    animation.composite(directionAnimations["s"], 0, frameHeight * 4)
    animation.composite(directionAnimations["sw"], 0, frameHeight * 5)
    animation.composite(directionAnimations["w"], 0, frameHeight * 6)
    animation.composite(directionAnimations["nw"], 0, frameHeight * 7)
    
    animation.save(filename=animationDirectory + animationName + ".png")

renderAnimation("black_chair")

print("DONE!")