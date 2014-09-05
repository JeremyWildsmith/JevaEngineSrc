me.onUserAuthenticated.add(userAuthenticated);

function userAuthenticated(user)
{
	core.log("User authorized " + user.getUsername());

	/* Retrieve an instance to the world in which we want to spawn the character.
	 * The world may not be ready immediately (i.e, may require initialization) so we use
	 * getWorld with a call back that is invoked when the instance is prepared to prevent from
	 * blocking server logic cycle.*/
	
	game.getWorld("world/maze_a.jmp", function(world) {		
		
		var serverWorld = game.lookupNetworkComponent(world);
		
		//Construct a character entity which we will grant to the client
		world.createEntity("character", "artifact/entity/zombie/player.jec", function(entity) {
			//If by the time the entity is constructed, we'll terminate the entity corresponding to the player
			if(!user.isAuthenticated())
				entity.leave();
			else
			{
				core.log("Created entity.");
				
				//Grab network component corresponding to entity.
				var serverEntity = serverWorld.lookupEntity(entity);
				
				//Consturct a certificate required to control that entity.
				var playerCertificate = serverEntity.constructDefaultCertificate();
				playerCertificate.setAllowPlayerControl(true);
				
				user.grantCertificate(playerCertificate);
				
				//Set the location of the player to a reasonable spawn location for maze_a.jmp
				entity.setLocation(5.0, 20.0, 0.01);
				
				user.onDeauthorized.add(function() {
					user.revokeCertificate(playerCertificate);
					entity.leave();
				})
			}
		});		
	});
}