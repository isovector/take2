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
			var jsonObj = jQuery.parseJSON(attrs.popoverHtml);
			var name = jsonObj.name;
			var imageString = jsonObj.picture;
			var image = '<img src="' + imageString + '" style="float:left;margin-right:5px;margin-bottom:10px;">';
			var htmlContent = '<p style="">' + image + name + '</p>';
			$(el).popover({
				trigger: 'hover',
				html: true,
				delay: { show: 500, hide: 100 },
				content: htmlContent,
				placement: 'bottom',
				template: '<div style="width:200px" class="popover" role="tooltip"><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
			});
		}
	};
})
.controller('DirecCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
    $scope.filename = "";

    $scope.items = []
    
    $scope.setFilename = function(filename) {
		$scope.items = filename;
		$scope.pathArray = $scope.items[0].path.split("/");
		$scope.pathArray.pop();
		// TODO: put in real edited last info
		console.log($scope.items);
		for (var i = 0; i < $scope.items.length; i++) {
            //$scope.items[i].editedlast  = {"id":7, "time":new Date()};
			$scope.items[i].editedLast = {"id":7, "time":new Date($scope.items[i].lastUpdated)};
		}
		$scope.getUsers();
    }

	$scope.getUsers = function() {
		console.log("Getting users");
		for (var i = 0; i < $scope.items.length; i++) {
			//TODO: switch back
			if (i == 0) {
				$scope.items[0].users = [];
				$scope.items[0].users.push ({ "name":"Jeff Lee", "picture":"http://www.gravatar.com/avatar/12"});
			
			}
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
				});
			}(i));
		}

		var userTimeout = $timeout($scope.getUsers,10000);
	}
 
}])

