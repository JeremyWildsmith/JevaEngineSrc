##For converting PARPG character asset animations into a spritesheet and generating jsf meta-data files.

from wand.image import Image

workingDirectory = "C:/Users/Jeremy/Desktop/parpg - Copy/trunk/objects/scenery/woodstove/"
frameWidth = 180
frameHeight = 210

directions = {"n" : "045", 
              "ne": "000", 
              "nw": "090", 
              "w": "135", 
              "sw": "180", 
              "s": "225", 
              "se": "270", 
              "e": "315"}

def renderAnimation(animationName, numFrames):
    print("Rendering animation: " + animationName)
    dirToFramesMapping = dict()
    
    animationDirectory =  workingDirectory + animationName + "/"
 
    for key, value in directions.items():
        directory = animationDirectory + value + "/"
        frames = []
        
        for i in range(0, numFrames):
            frameBuffer = Image(filename=(directory + animationName + "_" + str(i + 1) + ".png"));
            
            if(frameBuffer.width > frameWidth):
                frameBuffer.crop(int((frameBuffer.width - frameWidth)/2), 0, width=frameWidth, height=frameBuffer.height)
        
            if(frameBuffer.height > frameHeight):
                frameBuffer.crop(0, int((frameBuffer.height - frameHeight)/2), frameBuffer.width, height=frameHeight)
        
            newBuffer = Image(width=frameWidth, height=frameHeight)
            newBuffer.composite(frameBuffer, int((newBuffer.width - frameBuffer.width) / 2), int((newBuffer.height - frameBuffer.height) / 2))
            frameBuffer = newBuffer
        
            frames.append(frameBuffer)
            
        dirToFramesMapping[key] = frames
    
    directionAnimations = dict()
    
    for key, value in dirToFramesMapping.items():
        directionAnimationBuffer = Image(width=frameWidth * numFrames, height=frameHeight)
        for i in range(0, len(value)):
            frameBuffer = value[i]
            directionAnimationBuffer.composite(frameBuffer, i * frameWidth, 0)
         
        directionAnimations[key] = directionAnimationBuffer
    
    animation = Image(width=frameWidth * numFrames, height=frameHeight * len(directions))
    animation.composite(directionAnimations["n"], 0, 0)
    animation.composite(directionAnimations["ne"], 0, frameHeight)
    animation.composite(directionAnimations["e"], 0, frameHeight * 2)
    animation.composite(directionAnimations["se"], 0, frameHeight * 3)
    animation.composite(directionAnimations["s"], 0, frameHeight * 4)
    animation.composite(directionAnimations["sw"], 0, frameHeight * 5)
    animation.composite(directionAnimations["w"], 0, frameHeight * 6)
    animation.composite(directionAnimations["nw"], 0, frameHeight * 7)
    
    animation.save(filename=animationDirectory + animationName + ".png")

renderAnimation("woodstove", 11)

print("DONE!")