var moment = require('cloud/moment');

var Friendship = Parse.Object.extend("Friendship");
var SignalActivation = Parse.Object.extend("SignalActivation");

function getFriendInstallationsQuery(user) {
    var fromFriendQuery = new Parse.Query(Friendship)
        .equalTo("from", user)
        .equalTo("status", "accepted");
    var toFriendQuery = new Parse.Query(Friendship)
        .equalTo("to", user)
        .equalTo("status", "accepted");

    var installFromQuery = new Parse.Query(Parse.Installation)
        .matchesKeyInQuery("currentUser", "from", toFriendQuery);

    var installToQuery = new Parse.Query(Parse.Installation)
        .matchesKeyInQuery("currentUser", "to", fromFriendQuery);

    return Parse.Query.or(installFromQuery, installToQuery);
}

function getInstallationQueryForUser(user) {
    return new Parse.Query(Parse.Installation)
        .equalTo("currentUser", user)
}

function getSignalRecipientQuery(currentUser, recipients) {
    var recipientQuery;

    console.log("Recipients: " + recipients + ", " + !!recipients);
    if (!!recipients) {
        recipientQuery = new Parse.Query(Parse.User)
            .containedIn("objectId", recipients);

        recipientQuery.count({
            success: function (recipientUsers) {
                console.log("Recipient users: " + recipientUsers.length);
            },
            error: function (newFriendship, error) {
                console.error(error);
            }
        });
    }

    var fromFriendQuery = new Parse.Query(Friendship)
        .equalTo("from", currentUser);
    if (!!recipients) {
        fromFriendQuery.matchesQuery("to", recipientQuery);
    }
    fromFriendQuery.equalTo("status", "accepted");

    var toFriendQuery = new Parse.Query(Friendship)
        .equalTo("to", currentUser);
    if (!!recipients) {
        toFriendQuery.matchesQuery("from", recipientQuery);
    }
    toFriendQuery.equalTo("status", "accepted");

    var installFromQuery = new Parse.Query(Parse.Installation)
        .matchesKeyInQuery("currentUser", "from", toFriendQuery);

    var installToQuery = new Parse.Query(Parse.Installation)
        .matchesKeyInQuery("currentUser", "to", fromFriendQuery);

    return Parse.Query.or(installFromQuery, installToQuery);
}

Parse.Cloud.define("signal", function (request, response) {

    var now = moment();
    if (!!request.user.get('lastPush')) {
        var lastPushMoment = moment(request.user.get('lastPush'));
        // limit signals to 1 every 10 seconds
        if (lastPushMoment.add(10, 's').isAfter(now)) {
            response.error("signal_too_often");
            return;
        }
    }

    var signalParams = {
        alert: request.params.text || request.user.get("username") + " activated their beer signal",
        title: request.user.get("username")
    };

    var activation = new SignalActivation();

    activation.set("text", request.params.text);
    activation.set("sender", request.user);

    // add location and picture if they exist
    if (!!request.params.location) {
        activation.set("location", request.params.location);
        signalParams.location = request.params.location;
    }
    if (!!request.params.picture) {
        activation.set("picture", request.params.picture);
        signalParams.picture = request.params.picture;
    }
    if (!!request.params.recipients) {
        activation.set("recipients", request.params.recipients);
    }

    activation.save(null, {
        success: function (newActivation) {
            // send push
            Parse.Push.send({
                where: getSignalRecipientQuery(request.user, request.params.recipients), // Set our Installation query
                data: signalParams
            });

            request.user.set("lastPush", new Date());
            request.user.save();

            response.success("signal_sent");
        },
        error: function (newActivation, error) {
            // Execute any logic that should take place if the save fails.
            // error is a Parse.Error with an error code and message.
            response.error("failed_request_update");
            console.error(error);

        }
    });
});

Parse.Cloud.define("sendfriendrequest", function (request, response) {

    if (request.params.username == request.user.get("username")) {
        response.error("cannot_request_yourself");
        return;
    }

    var gotUser = function (user) {
        if (!user) {
            response.error("username_does_not_exist");
            return;
        }

        var existing1Query = new Parse.Query(Friendship)
            .equalTo("from", user)
            .equalTo("to", request.user);
        var existing2Query = new Parse.Query(Friendship)
            .equalTo("from", request.user)
            .equalTo("to", user);

        Parse.Query.or(existing1Query, existing2Query).first({
            success: function (existingFriendship) {
                if (!!existingFriendship) {
                    if (existingFriendship.get("status") == "accepted") {
                        response.error("friendship_already_exists");
                        return;
                    }
                    if (existingFriendship.get("status") == "pending") {
                        response.error("friendship_already_pending");
                        return;
                    }

                }

                var friendship = new Friendship();

                friendship.set("from", request.user);
                friendship.set("to", user);
                friendship.set("status", "pending");

                friendship.save(null, {
                    success: function (newFriendship) {
                        response.success("request_created");
                        Parse.Push.send({
                            where: getInstallationQueryForUser(user), // Set our Installation query
                            data: {
                                alert: request.user.get("username").toLowerCase().trim() + " has sent you a friend request"
                            }
                        });
                    },
                    error: function (newFriendship, error) {
                        // Execute any logic that should take place if the save fails.
                        // error is a Parse.Error with an error code and message.
                        response.error("failed_request_update");
                        console.error(error);

                    }
                });
            },
            error: function (error) {
                response.error("failed_existing_friendship_count");
                console.error(error);
            }
        });
    };

    var userQuery = new Parse.Query(Parse.User)
        .equalTo("username", request.params.username.toLowerCase().trim())
        .first({
            success: gotUser,
            error: function (user, error) {
                // Execute any logic that should take place if the save fails.
                // error is a Parse.Error with an error code and message.
                response.error("failed_user_get");
                console.error(error);
            }
        });
});

Parse.Cloud.define("acceptfriendrequest", function (request, response) {

    var query = new Parse.Query(Friendship)
        .include("from")
        .get(request.params.friendshipId, {
            success: function (friendship) {
                if (!friendship) {
                    response.error("friendship_does_not_exist");
                    return;
                }

                if (friendship.get("to").id != request.user.id) {
                    response.error("user_not_friendship_requestee");
                    return;
                }

                friendship.set("status", "accepted");
                friendship.save(null, {
                    success: function (savedFriendship) {
                        // Execute any logic that should take place after the object is saved.
                        response.success("request_accepted");
                        var fromUser = friendship.get("from");
                        Parse.Push.send({
                            where: getInstallationQueryForUser(fromUser), // Set our Installation query
                            data: {
                                alert: request.user.get("username") + " has accepted your friend request"
                            }
                        });
                    },
                    error: function (gameScore, error) {
                        // Execute any logic that should take place if the save fails.
                        // error is a Parse.Error with an error code and message.
                        response.error("failed_friendship_update");
                        console.error(error);
                    }
                });
            },
            error: function (object, error) {
                // The object was not retrieved successfully.
                // error is a Parse.Error with an error code and message.
                response.error("failed_friendship_get");
                console.error(error);
            }
        });
});

Parse.Cloud.define("unfriend", function (request, response) {

    var query = new Parse.Query(Friendship);
	
	console.log("query made");
	query.get(request.params.friendshipId, {
            success: function (friendship) {
                if (!friendship) {
                    response.error("friendship_does_not_exist");
                    return;
                }

                if (friendship.get("to").id != request.user.id && friendship.get("from").id != request.user.id) {
                    response.error("user_not_friendship_member");
                    return;
                }

                friendship.destroy({
                    success: function () {
                        // Execute any logic that should take place after the object is saved.
                        response.success("unfriend_successful");
                    },
                    error: function (object, error) {
                        // Execute any logic that should take place if the save fails.
                        // error is a Parse.Error with an error code and message.
                        response.error("failed_friendship_update");
                        console.error(error);
                    }
                });
            },
            error: function (object, error) {
                // The object was not retrieved successfully.
                // error is a Parse.Error with an error code and message.
                response.error("failed_friendship_get");
                console.error(error);
            }
        });
});

Parse.Cloud.define("changeusername", function (request, response) {

    var query = new Parse.Query(Parse.User)
        .equalTo("username", request.params.newUsername.toLowerCase().trim());
    query.first({
            success: function (existingUser) {
                if (!!existingUser) {
                    response.error("username_taken");
                    return;
                }
				
                request.user.set("username", request.params.newUsername.toLowerCase().trim());
                request.user.save(null, {
                    success: function () {
                        // Execute any logic that should take place after the object is saved.
                        response.success("username_changed");
                    },
                    error: function (gameScore, error) {
                        // Execute any logic that should take place if the save fails.
                        // error is a Parse.Error with an error code and message.
                        console.error(error);
                        response.error("failed_user_update");
                    }
                });
            },
            error: function (object, error) {
                // The object was not retrieved successfully.
                // error is a Parse.Error with an error code and message.
                console.error(error);
                response.error("failed_user_count");
            }
        });
});
			
			
Parse.Cloud.afterSave(Parse.User, function(request) {
	var username = request.object.get("username");
	request.object.set("username", username.toLowerCase().trim());
	request.object.save();
});

// usernames were originally case-sensitive, this job makes all usernames lower-case only
Parse.Cloud.job("fixUsernames", function(request, status) {
  // Set up to modify user data
  Parse.Cloud.useMasterKey();
  var counter = 0;
  // Query for all users
  var query = new Parse.Query(Parse.User);
  query.each(function(user) {
	var username = user.get("username");
	user.set("username", username.toLowerCase().trim());
      if (counter % 10 === 0) {
        // Set the  job's progress status
        status.message(counter + " users processed.");
      }
      counter += 1;
      return user.save();
  }).then(function() {
    // Set the job's success status
    status.success("Migration completed successfully.");
  }, function(error) {
    // Set the job's error status
    status.error("Uh oh, something went wrong.");
  });
});
