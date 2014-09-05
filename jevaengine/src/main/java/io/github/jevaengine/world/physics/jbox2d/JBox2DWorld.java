package io.github.jevaengine.world.physics.jbox2d;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsWorld;
import io.github.jevaengine.world.physics.PhysicsBodyDescription;
import io.github.jevaengine.world.physics.PhysicsBodyDescription.PhysicsBodyShape;
import io.github.jevaengine.world.physics.PhysicsBodyDescription.PhysicsBodyType;

import java.util.LinkedList;
import java.util.Queue;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.FrictionJointDef;

public final class JBox2DWorld implements IPhysicsWorld
{
	private World m_physicsWorld = new World(new Vec2());

	private static final float WORLD_STEP_INTERVAL = 1000.0F / 30.0F; //1/30th of a second
	
	private final float m_maxSurfaceFrictionForceNewtonMeters;
	
	private final Body m_surfaceBody;

	private int m_timeSinceStep = 0;
	
	private final PhysicsContactListener m_contactListener = new PhysicsContactListener();
	
	public JBox2DWorld(float maxSurfaceFrictionForceNewtonMeters, int worldWidthTiles, int worldHeightTiles)
	{
		m_maxSurfaceFrictionForceNewtonMeters = maxSurfaceFrictionForceNewtonMeters;
		
		BodyDef surfaceBodyDef = new BodyDef();
		surfaceBodyDef.type = BodyType.STATIC;
		m_surfaceBody = m_physicsWorld.createBody(surfaceBodyDef);
		
		FixtureDef surfaceFixtureDef = new FixtureDef();
		
		PolygonShape surfaceShape = new PolygonShape();
		surfaceShape.setAsBox(worldWidthTiles, worldHeightTiles);
		surfaceFixtureDef.shape = surfaceShape;
		surfaceFixtureDef.filter.categoryBits = 0;
		m_surfaceBody.createFixture(surfaceFixtureDef);
		
		m_physicsWorld.setContactListener(m_contactListener);
	}
	
	private Shape constructShape(PhysicsBodyShape shapeType, Rect3F aabb)
	{
		switch(shapeType)
		{
		default:
			assert false: "Unrecognized shape type.";
		case Box:
			PolygonShape polyShape = new PolygonShape();
			//polyShape.setAsBox(aabb.width / 2.0F, aabb.height / 2.0F, new Vec2(), 0);
			polyShape.setAsBox(aabb.width / 2, aabb.height / 2, new Vec2(aabb.x + aabb.width / 2.0F, aabb.y + aabb.height / 2.0F), 0);
			return polyShape;
		case Circle:
			CircleShape circShape = new CircleShape();
			circShape.setRadius(Math.min(aabb.width, aabb.height) / 2.0F);
			return circShape;
		}
	}
	
	World getWorld()
	{
		return m_physicsWorld;
	}
	
	@Override
	public float getMaxFrictionForce()
	{
		return m_maxSurfaceFrictionForceNewtonMeters;
	}
	
	@Override
	public void update(int deltaTime)
	{
		m_timeSinceStep += deltaTime;
		
		for(; m_timeSinceStep >= WORLD_STEP_INTERVAL; m_timeSinceStep -= WORLD_STEP_INTERVAL)
		{
			m_physicsWorld.step(WORLD_STEP_INTERVAL / 1000, 4, 1);
		}
		
		m_contactListener.relay();
	}
	
	@Override
	public void setGravity(Vector2F gravity)
	{
		m_physicsWorld.setGravity(JBox2DUtil.unwrap(gravity));
	}
	
	@Override
	public IPhysicsBody createBody(@Nullable IEntity owner, PhysicsBodyDescription bodyDescription)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = JBox2DUtil.unwrap(bodyDescription.type);
		bodyDef.fixedRotation = bodyDescription.isFixedRotation;
		bodyDef.angle = 0;
		Body body = m_physicsWorld.createBody(bodyDef);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.isSensor = bodyDescription.isSensor;
		fixtureDef.density = bodyDescription.density;
		fixtureDef.shape = constructShape(bodyDescription.shape, bodyDescription.aabb);
		fixtureDef.friction = bodyDescription.friction;
		Fixture fixture = body.createFixture(fixtureDef);

		if(bodyDescription.type == PhysicsBodyType.Dynamic)
		{
			FrictionJointDef frictionJointDef = new FrictionJointDef();
			frictionJointDef.maxForce = m_maxSurfaceFrictionForceNewtonMeters;
			frictionJointDef.maxTorque = m_maxSurfaceFrictionForceNewtonMeters;
			frictionJointDef.bodyA = body;
			frictionJointDef.bodyB = m_surfaceBody;		
			m_physicsWorld.createJoint(frictionJointDef);
		}
		
		return owner == null ? new JBox2DBody(this, body, fixture) : new JBox2DBody(this, body, fixture, owner);
	}
	
	@Override
	public IPhysicsBody createBody(PhysicsBodyDescription bodyDescription)
	{
		return createBody(null, bodyDescription);
	}
	
	/*
	 * The contact listener is notified during a world step routine. During a step routine, the world is locked,
	 * preventing bodies from being created in the physics world. If the contactListener notifies physics bodies of contacts
	 * during a step cycle, the body observers could react by mutating the world (which is not possible when it is locked in a step cycle.)
	 * 
	 * Thus, contact callbacks are queued and then executed after the world step routine has completed via the relay method.
	 */
	private class PhysicsContactListener implements ContactListener
	{
		public Queue<Runnable> m_contactProcesses = new LinkedList<>();
		
		public void relay()
		{
			for(Runnable r; (r = m_contactProcesses.poll()) != null;)
				r.run();
		}
		
		@Override
		public void beginContact(Contact contact)
		{
			final Object oAPhysicsBody = contact.getFixtureA().getBody().m_userData;
			final Object oBPhysicsBody = contact.getFixtureB().getBody().m_userData;
				
			if(!(oAPhysicsBody instanceof JBox2DBody && oBPhysicsBody instanceof JBox2DBody))
				return;

			m_contactProcesses.add(new Runnable() {
				
				@Override
				public void run() {
					((JBox2DBody)oAPhysicsBody).beginContact((JBox2DBody)oBPhysicsBody);
					((JBox2DBody)oBPhysicsBody).beginContact((JBox2DBody)oAPhysicsBody);					
				}
			});
		}

		@Override
		public void endContact(Contact contact)
		{
			final Object oAPhysicsBody = contact.getFixtureA().getBody().m_userData;
			final Object oBPhysicsBody = contact.getFixtureB().getBody().m_userData;
				
			if(!(oAPhysicsBody instanceof JBox2DBody && oBPhysicsBody instanceof JBox2DBody))
				return;

			m_contactProcesses.add(new Runnable() {
				
				@Override
				public void run() {
					((JBox2DBody)oAPhysicsBody).endContact((JBox2DBody)oBPhysicsBody);
					((JBox2DBody)oBPhysicsBody).endContact((JBox2DBody)oAPhysicsBody);					
				}
			});
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) { }

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) { }
	}
}
