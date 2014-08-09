var ROOTURL = "http://localhost:9000/"

frostbite.filter('isDirectory', function() {
    return function(input) {
        if (input.isDir) {
	    return ROOTURL + "assets/images/directory.png"
	} else {
	    return ROOTURL + "assets/images/file.png"
	}	    
    }
})

.directive('customPopover', function () {
	return {
		link: function (scope, el, attrs) {
			console.log(attrs.popoverHtml);
			console.log("HELLO");
			var jsonObj = jQuery.parseJSON(attrs.popoverHtml);
			var name = jsonObj.name;
			var imageString = jsonObj.picture;
			var image = '<img src="' + imageString + '" width="60px" height="60px">';
			var htmlContent = '<p>' + name + image + '</p>';

			$(el).popover({
				trigger: 'hover',
				html: true,
				delay: { show: 500, hide: 100 },
				content: htmlContent,
				placement: 'auto',
			});
		}
	};
})
.controller('DirecCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.filename = "";

    $scope.items = []
    

    //$scope.popover({ title: 'LOOK a bird image', content:'<img src="https://si0.twimg.com/a/1339639284/images/three_circles/twitter-bird-white-on-blue.png" />'; html:true });

    $scope.setFilename = function(filename) {
	console.log("filename = " + filename);
	$scope.items = filename;
	$scope.pathArray = $scope.items[0].path.split("/");
	$scope.pathArray.pop();
	for (var i = 0; i < $scope.items.length; i++) {
            $scope.items[i].editedlast  = {"id":7, "time":new Date()};
        }
	console.log($scope.items);
    $scope.getUsers();
	//$scope.getDirectories();
    }

$scope.getUsers = function() {
	console.log("Getting users");
	for (var i = 0; i < $scope.items.length; i++) {
		//TODO: switch back
		if (i == 0) {
			$scope.items[0].users = [];
			$scope.items[0].users.push ({ "name":"Jeff Lee", "picture":"http://www.gravatar.com/avatar/12"});
			
		}/*
		(function(i) {
			$http.get("/api/currently/viewing/" + $scope.items[i].path).success(function(data) {
				$scope.items[i].users = data;
				var numUsers = $scope.items[i].users.length;
				for (var j = 0; j < numUsers; j++) {
					var emailLowerCase = $scope.items[i].users[j].email.toLowerCase();
					console.log(emailLowerCase);
					var hashEmail = CryptoJS.MD5( emailLowerCase );  
					console.log("EMAIL: " + hashEmail);
					$scope.items[i].users[j].picture = "http://www.gravatar.com/avatar/" + hashEmail;
				}
				if (numUsers >= 3) {
					$scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + (numUsers - 1) + " Others";
				} else if (numUsers == 2){
					$scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + $scope.items[i].users[1].name;
				} else if (numUsers == 1) {
					$scope.items[i].currentUsers = $scope.items[i].users[0].name;
				} else {
					$scope.items[i].currentUsers = "";
				}
				});
			}(i));*/
		}
	}


}])

